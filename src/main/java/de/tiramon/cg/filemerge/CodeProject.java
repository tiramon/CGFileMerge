package de.tiramon.cg.filemerge;

public interface CodeProject {

	String getFileExtension();

	boolean isImportLine(String line);

	String readImportFromLine(String line);

}
