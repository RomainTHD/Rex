package fr.rthd.io;

import fr.rthd.common.ExitCode;
import fr.rthd.common.FailureManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BinaryReader {
	public int[] readFileFromPath(String path) {
		var res = new ArrayList<Integer>();

		try (
			var inputStream = new FileInputStream(path);
		) {
			int byteRead;
			while ((byteRead = inputStream.read()) != -1) {
				// TODO: Support endianness?
				res.add(byteRead);
			}

			return res.stream().mapToInt((i) -> i).toArray();
		} catch (FileNotFoundException e) {
			throw FailureManager.fail(BinaryReader.class, ExitCode.FileNotFound, e);
		} catch (IOException e) {
			throw FailureManager.fail(BinaryReader.class, ExitCode.UnknownException, e);
		}
	}
}
