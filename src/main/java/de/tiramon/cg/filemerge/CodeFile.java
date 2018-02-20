package de.tiramon.cg.filemerge;

import java.util.HashSet;
import java.util.Set;

public class CodeFile {
	String path;
	Set<String> imports = new HashSet<>();
	String filePackage;
	String content;
}
