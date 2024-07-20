package fr.rthd.io;

import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BinaryReader {
	public List<Byte> readFileFromPath(String path) {
		var res = new ArrayList<Byte>();

		try (
			var inputStream = new FileInputStream(path);
		) {
			int byteRead;
			while ((byteRead = inputStream.read()) != -1) {
				// TODO: Support endianness?
				res.add((byte) byteRead);
			}

			return res;
		} catch (FileNotFoundException e) {
			throw FailureManager.fail(ExitCode.FileNotFound, e);
		} catch (IOException e) {
			throw FailureManager.fail(ExitCode.UnknownException, e);
		}
	}
}
