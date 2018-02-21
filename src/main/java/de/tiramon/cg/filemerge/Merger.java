package de.tiramon.cg.filemerge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Merger {
	Set<String> importSet = new HashSet<>();

	public String merge(Collection<CodeFile> collection) {
		for (CodeFile file : collection) {
			importSet.addAll(file.imports);
		}

		importSet = cleanImports(importSet, new HashSet<>());

		List<String> cleanImports = new ArrayList<>(importSet);
		cleanImports.sort((a, b) -> a.compareTo(b));
		StringBuilder builder = new StringBuilder();

		for (String imp : cleanImports) {
			builder.append(imp);
			builder.append("\n");
		}

		for (CodeFile file : collection) {
			builder.append(file.content);
		}

		return builder.toString();
	}

	private Set<String> cleanImports(Set<String> importSet, Set<String> knownPackageSet) {
		Iterator<String> it = importSet.iterator();
		while (it.hasNext()) {
			String imp = it.next();
			if (knownPackageSet.contains(imp.substring("import ".length(), imp.lastIndexOf('.'))))
				it.remove();
		}
		return importSet;
	}

	public void writeFile(File outputFile, String content) throws FileNotFoundException {
		System.err.println("writing to " + outputFile.getAbsolutePath());

		try (PrintStream ps = new PrintStream(outputFile)) {
			ps.print(content);
		}
	}
}
