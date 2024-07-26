package fr.rthd.loader;

import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.Logger;
import fr.rthd.common.Utils;
import fr.rthd.io.LittleEndianDataManager;
import fr.rthd.types.CoffCharacteristicsFlags;
import fr.rthd.types.CoffExtendedHeader;
import fr.rthd.types.CoffHeader;
import fr.rthd.types.DataDirectory;
import fr.rthd.types.DataDirectoryFieldName;
import fr.rthd.types.MachineType;
import fr.rthd.types.PeFile;
import fr.rthd.types.PeHeader;
import fr.rthd.types.PeSection;
import fr.rthd.types.PeSectionHeader;
import fr.rthd.types.SectionCharacteristicsFlags;
import fr.rthd.types.WindowsSubsystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Portable Executable format
 * FIXME: Currently only supports x86
 */
public class Loader {
	private static final Logger logger = new Logger(Loader.class);
	private static final int MAX_SAFE_VIRTUAL_SECTION_SIZE = 100_000;

	private final LittleEndianDataManager reader;

	public Loader(int[] bytes) {
		this.reader = new LittleEndianDataManager(bytes);
	}

	public PeFile load() {
		checkDosHeader();
		checkDosStub();
		var coffHeader = loadCoffHeader();
		var coffExtendedHeader = loadOptCoffHeader(coffHeader);
		var peHeader = loadDataDirectories(coffExtendedHeader);
		var sections = loadSectionTable(peHeader);
		return loadSectionContent(peHeader, sections);
	}

	private void checkDosHeader() {
		if (nextU16() != 0x5a4d) {
			throw fail(ExitCode.InvalidFile, "Not a DOS executable: magic number not found");
		}

		reader.jumpAt(0x3c);
		var peHeaderStart = nextU32();
		reader.jumpAt((int) peHeaderStart);
	}

	private void checkDosStub() {
		// Never checked, @0x3c will skip directly to NT headers
	}

	private CoffHeader loadCoffHeader() {
		if (nextU32() != 0x4550) {
			throw fail(ExitCode.InvalidFile, "Not a PE image: magic number not found");
		}

		var coffHeader = CoffHeader
			.builder()
			.machine(MachineType.fromValue(nextU16()))
			.numberOfSections(nextU16())
			.timeDateStamp(nextU32())
			.symbolTablePtr(nextU32())
			.symbolsCount(nextU32())
			.optHeaderSize(nextU16())
			.characteristics(CoffCharacteristicsFlags.toFlagSet(nextU16()))
			.build();
		logger.debug(coffHeader.toString());

		if (!coffHeader.getCharacteristics().contains(CoffCharacteristicsFlags.ExecutableImage)) {
			throw fail(ExitCode.InvalidFile, "File is not executable");
		}

		if (!coffHeader.getCharacteristics().contains(CoffCharacteristicsFlags.Machine32Bit)) {
			// FIXME: is it really 16 bits exe?
			throw fail(ExitCode.Unsupported, "16 bits executables are not supported");
		}

		if (coffHeader.getCharacteristics().contains(CoffCharacteristicsFlags.DLL)) {
			throw fail(ExitCode.Unsupported, "DLLs are not supported yet");
		}

		return coffHeader;
	}

	private CoffExtendedHeader loadOptCoffHeader(CoffHeader coffHeader) {
		// TODO: Make sure the header size is correct and doesn't overwrite data

		if (coffHeader.getOptHeaderSize() == 0) {
			throw fail(ExitCode.Unsupported, "Optional COFF header is currently required");
		}

		var format = nextU16();
		if (format == 0x10b) {
			// OK, PE32
		} else if (format == 0x20b) {
			throw fail(ExitCode.Unsupported, "64 bit address space is not supported yet");
		} else {
			throw fail(ExitCode.InvalidFile, "Incorrect optional COFF header magic number");
		}

		var extHeader = CoffExtendedHeader
			.builder()
			// COFF fields
			.coffHeader(coffHeader)
			.is64bitAddressSpace(false)
			.linkerVersion(nextU16())
			.textSize(nextU32())
			.dataSize(nextU32())
			.bssSize(nextU32())
			.entryPointAddr(nextU32())
			.textBase(nextU32())
			.dataBase(nextU32())
			// Windows specific fields
			.imageBase(nextU32())
			.sectionAlignment(nextU32())
			.fileAlignment(nextU32())
			.osVersion(nextU32())
			.imageVersion(nextU32())
			.subsystemVersion(nextU32())
			.win32VersionValue(nextU32())
			.imageSize(nextU32())
			.headersSize(nextU32())
			.checkSum(nextU32())
			.subsystem(WindowsSubsystem.fromValue(nextU16()))
			.dllCharacteristics(nextU16())
			.stackSizeToReserve(nextU32())
			.stackSizeToCommit(nextU32())
			.heapSizeToReserve(nextU32())
			.heapSizeToCommit(nextU32())
			.loaderFlags(nextU32())
			.rvaCount(nextU32())
			.build();
		logger.debug(extHeader.toString());

		if (extHeader.getFileAlignment() == 0) {
			throw fail(ExitCode.InvalidFile, "File alignment cannot be zero");
		}

		// TODO: do all kind of checks

		return extHeader;
	}

	private PeHeader loadDataDirectories(CoffExtendedHeader coffHeader) {
		if (coffHeader.getRvaCount() > DataDirectoryFieldName.values().length) {
			throw fail(ExitCode.InvalidFile, "Too many data directories");
		}

		LinkedHashMap<DataDirectoryFieldName, DataDirectory> dataDirs = new LinkedHashMap<>();

		for (int i = 0; i < coffHeader.getRvaCount(); ++i) {
			var addr = nextU32();
			var size = nextU32();
			if (addr != 0 || size != 0) {
				var fieldName = DataDirectoryFieldName.values()[i];
				dataDirs.put(
					fieldName,
					DataDirectory
						.builder()
						.fieldName(fieldName)
						.addr(addr)
						.size(size)
						.build()
				);
			}
		}

		var header = PeHeader
			.builder()
			.coffExtendedHeader(coffHeader)
			.dataDirectories(dataDirs)
			.build();
		logger.debug(header.toString());
		return header;
	}

	private List<PeSectionHeader> loadSectionTable(PeHeader peHeader) {
		var sections = new ArrayList<PeSectionHeader>();

		for (var idx = 0; idx < peHeader.getCoffExtendedHeader().getCoffHeader().getNumberOfSections(); ++idx) {
			var nameBuilder = new StringBuilder();
			for (int i = 0; i < 8; ++i) {
				var c = nextU8();
				if (c != 0) {
					if (Character.isISOControl(c)) {
						throw fail(ExitCode.InvalidFile, "Section name is invalid");
					}
					nameBuilder.append(Character.toString(c));
				}
			}

			sections.add(
				PeSectionHeader
					.builder()
					.name(nameBuilder.toString())
					.virtualSize(nextU32())
					.virtualAddress(nextU32())
					.rawDataSize(nextU32())
					.rawDataPtr(nextU32())
					.relocationsPtr(nextU32())
					.lineNumberPtr(nextU32())
					.relocationsCount(nextU16())
					.lineNumberCount(nextU16())
					.characteristics(SectionCharacteristicsFlags.toFlagSet(nextU32()))
					.build()
			);
		}

		logger.debug(sections.toString());
		return sections;
	}

	private PeFile loadSectionContent(PeHeader peHeader, List<PeSectionHeader> sectionHeaders) {
		var sections = sectionHeaders
			.stream()
			.map((sectionHeader) -> {
				if (sectionHeader.getRawDataSize() % peHeader.getCoffExtendedHeader().getFileAlignment() != 0) {
					throw fail("Section raw data is not aligned");
				}

				if (sectionHeader.getVirtualSize() >= MAX_SAFE_VIRTUAL_SECTION_SIZE) {
					throw fail(ExitCode.Unsupported, "Section too big ; is it a mistake?");
				}

				var content = new int[(int) sectionHeader.getVirtualSize()];

				reader.jumpAt((int) sectionHeader.getRawDataPtr());
				for (int i = 0; i < sectionHeader.getVirtualSize(); ++i) {
					content[i] = nextU8();
				}

				return PeSection
					.builder()
					.header(sectionHeader)
					.content(content)
					.build();
			})
			.toList();

		var peFile = PeFile
			.builder()
			.header(peHeader)
			.sections(sections)
			.build();
		logger.debug(peFile.toString());
		return peFile;
	}

	private int nextU8() {
		var v = reader.readU8();
		logger.debug(String.format("Reading %s", Utils.u8ToString(v)));
		return v;
	}

	private int nextU16() {
		var v = reader.readU16();
		logger.debug(String.format("Reading %s", Utils.u16ToString(v)));
		return v;
	}

	private long nextU32() {
		var v = reader.readU32();
		logger.debug(String.format("Reading %s", Utils.u32ToString(v)));
		return v;
	}

	private RuntimeException fail(String reason) {
		return fail(ExitCode.InvalidFile, reason);
	}

	private RuntimeException fail(ExitCode exitCode, String reason) {
		return FailureManager.fail(Loader.class, exitCode, reason);
	}
}
