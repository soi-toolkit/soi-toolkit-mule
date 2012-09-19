/* 
 * Licensed to the soi-toolkit project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The soi-toolkit project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.soitoolkit.tools.generator.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtil {

	/**
	 * Hidden constructor.
	 */
	private FileUtil() {
		throw new UnsupportedOperationException(
				"Not allowed to create an instance of this class");
	}

	public static PrintWriter openFileForAppend(String filename)
			throws IOException {
		return new PrintWriter(new BufferedWriter(
				new FileWriter(filename, true)));
	}

	public static PrintWriter openFileForOverwrite(String filename)
			throws IOException {
		return new PrintWriter(new BufferedWriter(new FileWriter(filename,
				false)));
	}

	/**
	 * Returns the relative path.
	 * 
	 * @param file
	 * @param srcDir
	 * @return the relative path
	 */
	public static String getRelativePath(File file, File srcDir) {

		String base = srcDir.getPath();
		String filePath = file.getPath();

		String relativePath = new File(base).toURI()
				.relativize(new File(filePath).toURI()).getPath();
		return relativePath;
	}

	/**
	 * Copy a file to new destination.
	 * 
	 * @param srcFile
	 * @param destFile
	 */
	public static void copyFile(File srcFile, File destFile) {
		OutputStream out = null;
		InputStream in = null;
		try {
			if (!destFile.getParentFile().exists())
				destFile.getParentFile().mkdirs();

			in = new FileInputStream(srcFile);
			out = new FileOutputStream(destFile);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Copies an existing structure to a new destination.
	 * 
	 * @param sourceFile
	 * @param targetDir
	 */
	public static void copyFiles(File sourceFile, File targetDir) {
		copyFiles(sourceFile, sourceFile, targetDir);
	}

	public static void copyFiles(File rootDir, File sourceFile, File targetDir) {

		File[] files = sourceFile.listFiles();

		if (files != null) {
			for (File file : files) {
				String relativePath = getRelativePath(file, rootDir);
				File target = new File(targetDir + "/" + relativePath);

				if (!target.exists()) {
					if (file.isDirectory()) {
						target.mkdir();
						copyFiles(rootDir, file, targetDir);
					} else {
						copyFile(file, target);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param srcDir
	 * @param regex
	 * @return
	 */
	public static List<File> getAllFilesMatching(File srcDir, final String regex) {

		List<File> foundFiles = new ArrayList<File>();

		if (!srcDir.isDirectory()) {
			if (srcDir.getPath().matches(regex)) {
				foundFiles.add(srcDir);
				return foundFiles;
			}
			return foundFiles;
		}

		FilenameFilter filenameFiler = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.matches(regex);
			};
		};

		foundFiles.addAll(Arrays.asList(srcDir.listFiles(filenameFiler)));

		FileFilter fileFiler = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			};
		};

		File[] directories = srcDir.listFiles(fileFiler);

		for (File file : directories) {
			foundFiles.addAll(getAllFilesMatching(file, regex));
		}

		return foundFiles;
	}
}