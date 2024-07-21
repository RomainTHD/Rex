package fr.rthd.types.pe;

import lombok.Getter;

@Getter
public enum DataDirectoryFieldName {
	/**
	 * Symbols that other images can access through dynamic linking
	 * .edata
	 */
	ExportTable,
	/**
	 * .idata
	 */
	ImportTable,
	/**
	 * .rsrc
	 */
	ResourceTable,
	/**
	 * Array of function table entries that are used for exception handling
	 * .pdata
	 */
	ExceptionTable,
	/**
	 * Used for signed PE
	 */
	CertificateTable,
	/**
	 * Entries for all base relocations in the image
	 * .reloc
	 */
	BaseRelocationTable,
	/**
	 * Compiler-generated debug information
	 * .debug
	 */
	Debug,
	/**
	 * Reserved
	 */
	ArchitectureData,
	/**
	 * RVA of the value to be stored in the global pointer register
	 */
	GlobalPtr,
	/**
	 * Thread Local Storage (TLS) table
	 * .tls
	 */
	TLSTable,
	LoadConfigTable,
	BoundImport,
	/**
	 * Identical content to the import lookup table, until the file is bound.
	 * During binding, the entries in the import address table are overwritten with the 32-bit addresses of the symbols
	 * that are being imported
	 */
	ImportAddressTable,
	/**
	 * Delay the loading of a DLL until the first call into that DLL
	 */
	DelayImportDescriptor,
	/**
	 * Object file contains managed code
	 * .cormeta
	 */
	CLRRuntimeHeader,
	/**
	 * Reserved
	 */
	Reserved,
}
