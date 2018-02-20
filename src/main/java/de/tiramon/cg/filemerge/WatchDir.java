package de.tiramon.cg.filemerge;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class WatchDir {
	private static CodeProject project = new JavaProject();
	private static Map<Path, File> outputfiles = new HashMap<>();

	private final WatchService watcher;
	private final Map<WatchKey, Path> keys;
	private final boolean recursive;
	private boolean trace = true;
	private final String outputFile;

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		if (trace) {
			Path prev = keys.get(key);
			if (prev == null) {
				System.out.format("register: %s\n", dir);
			} else {
				if (!dir.equals(prev)) {
					System.out.format("update: %s -> %s\n", prev, dir);
				}
			}
		}
		keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	private void registerAll(final Path start) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Creates a WatchService and registers the given directory
	 * 
	 * @param project
	 */
	WatchDir(Path dir, boolean recursive, String outputFile) throws IOException {
		watcher = FileSystems.getDefault().newWatchService();
		keys = new HashMap<>();
		this.outputFile = outputFile;
		this.recursive = recursive;

		if (recursive) {
			System.out.format("Scanning %s ...\n", dir);
			registerAll(dir);
			System.out.println("Done.");
		} else {
			register(dir);
		}

		// enable trace after initial registration
		trace = true;
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	void processEvents() {
		for (;;) {

			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				System.err.println("WatchKey not recognized!!");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if (kind == OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);

				// print out event
				// System.out.format("%s: %s\n", event.kind().name(), child);

				// if directory is created, and watching recursively, then
				// register it and its sub-directories
				if (recursive && (kind == ENTRY_CREATE)) {
					try {
						if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
							registerAll(child);
						}
					} catch (IOException x) {
						// ignore to keep sample readbale
					}
				}

				if (!Files.isDirectory(child) && child.toString().endsWith(project.getFileExtension())) {
					System.out.format("%s: %s\n", event.kind().name(), child);
					if (kind == ENTRY_DELETE) {
						files.remove(child.toString());
					} else if (kind == ENTRY_CREATE) {
						files.put(child.toString(), readCodeFile(child.toString(), null));
					} else if (kind == ENTRY_MODIFY) {
						CodeFile file = files.get(child.toString());
						readCodeFile(child.toString(), file);
					}
					Merger merger = new Merger();
					String content = merger.merge(files.values());
					try {
						merger.writeFile(outputFile, content);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}

	static void usage() {
		System.err.println("usage: java WatchDir [-r] dir");
		System.exit(-1);
	}

	Map<String, CodeFile> files = new HashMap<>();

	public void gatherFiles() {
		FilenameFilter filter = (dir, name) -> {
			return name.endsWith(project.getFileExtension());
		};
		for (Path key : keys.values()) {
			String[] files = key.toFile().list(filter);
			for (String f : files) {
				String resolved = key.resolve(f).toString();
				System.out.println(resolved);
				this.files.put(resolved, readCodeFile(resolved, null));
			}
		}
	}

	public void print() {

		for (WatchKey key : keys.keySet()) {
			System.out.println(key);
		}
	}

	public static void main(String[] args) throws IOException {
		// parse arguments
		if (args.length == 0 || args.length > 2)
			usage();
		boolean recursive = false;
		int dirArg = 0;
		if (args[0].equals("-r")) {
			if (args.length < 2)
				usage();
			recursive = true;
			dirArg++;
		}

		// register directory and process its events
		Path dir = Paths.get(args[dirArg]);
		/*
		 * Set<String> files = new WatchDir(dir, recursive).gatherFiles();
		 * List<CodeFile> codeFiles = readCodeFiles(files);
		 * System.out.println(new Merger().merge(codeFiles));
		 */
		WatchDir d = new WatchDir(dir, recursive, "C:\\Privat\\CGFileMerge\\Output.java");
		d.gatherFiles();
		d.processEvents();
	}

	private static List<CodeFile> readCodeFiles(Set<String> files) throws FileNotFoundException, IOException {
		List<CodeFile> list = new ArrayList<>();
		for (String file : files) {
			list.add(readCodeFile(file, null));
		}
		return list;
	}

	private static CodeFile readCodeFile(String path, CodeFile file) {
		if (file == null) {
			file = new CodeFile();
		}
		file.path = path;
		System.out.println("reading " + path);
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = br.readLine()) != null) {
				if (line.startsWith("package ")) {
					file.filePackage = line.substring(8, line.length() - 1);
				} else if (project.isImportLine(line)) {
					file.imports.add(line);
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
			file.content = builder.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
}