package fr.rthd.checker.pe;

import fr.rthd.checker.LittleEndianReader;
import fr.rthd.checker.Loader;
import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;
import fr.rthd.common.Logger;
import fr.rthd.types.pe.CoffCharacteristicsFlags;
import fr.rthd.types.pe.CoffExtendedHeader;
import fr.rthd.types.pe.CoffHeader;
import fr.rthd.types.pe.DataDirectory;
import fr.rthd.types.pe.DataDirectoryFieldName;
import fr.rthd.types.pe.MachineType;
import fr.rthd.types.pe.PeHeader;
import fr.rthd.types.pe.Section;
import fr.rthd.types.pe.SectionCharacteristicsFlags;
import fr.rthd.types.pe.WindowsSubsystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Portable Executable format
 * FIXME: Currently only supports x86
 */
public class PeLoader implements Loader {
	private static final Logger logger = new Logger(PeLoader.class);

	private final LittleEndianReader reader;

	public PeLoader(List<Byte> bytes) {
		this.reader = new LittleEndianReader(bytes);
	}

	@Override
	public void load() {
		checkDosHeader();
		checkDosStub();
		var coffHeader = loadCoffHeader();
		var coffExtendedHeader = loadOptCoffHeader(coffHeader);
		var peHeader = loadDataDirectories(coffExtendedHeader);
		var sections = loadSectionTable(peHeader);
	}

	private void checkDosHeader() {
		if (nextU16() != 0x5a4d) {
			throw FailureManager.fail(
				PeLoader.class,
				ExitCode.InvalidFile,
				"Not a DOS executable: magic number not found"
			);
		}

		reader.skipAt(0x3c);
		var peHeaderStart = nextU32();
		reader.skipAt(peHeaderStart);
	}

	private void checkDosStub() {
		// Never checked, @0x3c will skip directly to NT headers
	}

	private CoffHeader loadCoffHeader() {
		if (nextU32() != 0x4550) {
			throw FailureManager.fail(PeLoader.class, ExitCode.InvalidFile, "Not a PE image: magic number not found");
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
			throw FailureManager.fail(PeLoader.class, ExitCode.InvalidFile, "File is not executable");
		}

		if (!coffHeader.getCharacteristics().contains(CoffCharacteristicsFlags.Machine32Bit)) {
			// FIXME: is it really 16 bits exe?
			throw FailureManager.fail(
				PeLoader.class,
				ExitCode.Unsupported,
				"16 bits executables are not supported"
			);
		}

		if (coffHeader.getCharacteristics().contains(CoffCharacteristicsFlags.DLL)) {
			throw FailureManager.fail(PeLoader.class, ExitCode.Unsupported, "DLLs are not supported yet");
		}

		return coffHeader;
	}

	private CoffExtendedHeader loadOptCoffHeader(CoffHeader coffHeader) {
		// TODO: Make sure the header size is correct and doesn't overwrite data

		if (coffHeader.getOptHeaderSize() == 0) {
			throw FailureManager.fail(
				PeLoader.class,
				ExitCode.Unsupported,
				"Optional COFF header is currently required"
			);
		}

		var format = nextU16();
		if (format == 0x10b) {
			// OK, PE32
		} else if (format == 0x20b) {
			throw FailureManager.fail(
				PeLoader.class,
				ExitCode.Unsupported,
				"64 bit address space is not supported yet"
			);
		} else {
			throw FailureManager.fail(
				PeLoader.class,
				ExitCode.InvalidFile,
				"Incorrect optional COFF header magic number"
			);
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

		// TODO: do all kind of checks

		return extHeader;
	}

	private PeHeader loadDataDirectories(CoffExtendedHeader coffHeader) {
		if (coffHeader.getRvaCount() > DataDirectoryFieldName.values().length) {
			throw FailureManager.fail(PeLoader.class, ExitCode.InvalidFile, "Too many data directories");
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

	private List<Section> loadSectionTable(PeHeader peHeader) {
		var sections = new ArrayList<Section>();

		for (var idx = 0; idx < peHeader.getCoffExtendedHeader().getCoffHeader().getNumberOfSections(); ++idx) {
			var nameBuilder = new StringBuilder();
			for (int i = 0; i < 8; ++i) {
				var c = nextU8();
				if (c != 0) {
					if (Character.isISOControl(c)) {
						throw FailureManager.fail(PeLoader.class, ExitCode.InvalidFile, "Section name is invalid");
					}
					nameBuilder.append(Character.toString(c));
				}
			}

			sections.add(
				Section
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

	private int nextU8() {
		var v = reader.nextU8();
		logger.debug(String.format("Reading 0x%1$02X", v));
		return v;
	}

	private int nextU16() {
		var v = reader.nextU16();
		logger.debug(String.format("Reading 0x%1$04X", v));
		return v;
	}

	private long nextU32() {
		var v = reader.nextU32();
		logger.debug(String.format("Reading 0x%1$08X", v));
		return v;
	}
}
