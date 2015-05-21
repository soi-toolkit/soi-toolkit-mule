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
package org.soitoolkit.commons.mule.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for the file-transport and file-handling in general.
 * 
 * @author Magnus Larsson
 *
 */
public class FileUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	/**
	 * Hidden constructor.
	 */
	private FileUtil() {
		throw new UnsupportedOperationException(
				"Not allowed to create an instance of this class");
	}

    public static void initFolder(File parent) throws IOException {

    	logger.debug("Init folder " + parent.getCanonicalPath());
    	
    	recursiveDelete(parent);
    	boolean ok = parent.mkdirs();
    	if (!ok) throw new IOException("Failed to recreate folder: " + parent);
	}


    /**
     *
     * @param parent
     * @throws IOException
     */
    public static List<File> listFilesInFolder(File parent) throws IOException {

        logger.debug("List files in folder " + parent.getCanonicalPath());

        List<File> files = new ArrayList<File>();

        // Bail out if the file/folder does not exist
        if(!parent.exists()) return files;

        // Bail out if the parent is no directory
        if (!parent.isDirectory()) return files;

        // Ok, let's list the files in the directory
        files = Arrays.asList(parent.listFiles());

        logger.debug("Found {} files in {}", files.size(), parent.getCanonicalPath());

        return files;
    }

    public static void recursiveDelete(File parent) throws IOException {

    	logger.debug("Recursive delete folder " + parent.getCanonicalPath());

    	// Bail out if the file/folder does not exist
        if(!parent.exists()) return;
    	
    	// If this file is a directory then first delete all its children
    	if (parent.isDirectory()) {
    		for (File child : parent.listFiles()) {
    			recursiveDelete(child);
    		}
    	}

    	// Now delete this file, but first check write permissions on its parent...
    	File parentParent = parent.getParentFile();
    	if (!parentParent.canWrite()) {
			if (!parentParent.setWritable(true)) throw new IOException("Failed to set readonly-folder: " + parentParent + " to writeable");
    	}
    	if (!parent.delete()) throw new IOException("Failed to delete folder: " + parent);
    }
}
