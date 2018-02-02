package de.tiramon.cg.filemerge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * I need to output everything in one file for the contest ...<br>
 * <br>
 * Source Folders are given as parameters seperated by spaces. Sub folders are
 * automatically searched for other folders and files
 * 
 * @author tiramon
 */
public class CreateOutput {
	public static void main(String[] args) throws IOException {

		List<String> folders = Arrays.asList(args);
		Set<String> importSet = new HashSet<>();
		Set<String> knownPackages = new HashSet<>();
		StringBuilder builder = new StringBuilder();
		for (String folder : folders) {
			handleFolder(folder, importSet, knownPackages, builder, folder);
		}

		cleanImports(knownPackages, importSet);
		File output = new File("Output.java");

		try (PrintStream ps = new PrintStream(output)) {
			for (String imp : importSet) {
				ps.println(imp);
			}
			ps.print(builder.toString());
		}
	}

	private static void cleanImports(Set<String> knownPackages, Set<String> importSet) {
		for (String pkg : knownPackages) {
			System.err.println(pkg);
		}

		Iterator<String> it = importSet.iterator();
		while (it.hasNext()) {
			String imp = it.next();
			if (knownPackages.contains(imp.substring("import ".length(), imp.lastIndexOf('.'))))
				it.remove();
		}
	}

	private static void handleFolder(String folder, Set<String> importSet, Set<String> knownPackages, StringBuilder builder, String baseFolder) throws IOException {
		File srcFolder = new File(folder);
		for (String fileString : srcFolder.list()) {
			String fileSubString = folder + "/" + fileString;
			File file = new File(fileSubString);
			if (!file.isDirectory()) {
				if (!folder.equals(baseFolder))
					knownPackages.add(folder.replaceAll("/", ".").replaceAll("\\\\", ".").replace(baseFolder + ".", ""));
				handleFile(fileSubString, importSet, builder);
			} else {
				handleFolder(fileSubString, importSet, knownPackages, builder, baseFolder);
			}
		}
	}

	private static void handleFile(String file, Set<String> importSet, StringBuilder builder) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("package ")) {

				} else if (line.startsWith("import ")) {
					importSet.add(line);
				} else {
					if (line.startsWith("public class")) {
						line = line.replace("public class", "class");
						System.err.println(line);
					} else if (line.startsWith("public interface")) {
						line = line.replace("public interface", "interface");
						System.err.println(line);
					} else if (line.startsWith("public enum")) {
						line = line.replace("public enum", "enum");
						System.err.println(line);
					}
					builder.append(line + "\n");
				}
			}
		}
	}
}
