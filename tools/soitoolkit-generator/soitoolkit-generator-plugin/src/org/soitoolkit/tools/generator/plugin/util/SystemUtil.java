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
package org.soitoolkit.tools.generator.plugin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class SystemUtil {
	
//	public static final String TEST_OUT_FOLDER = System.getProperty("user.home") + "/Documents/temp/_test";
	// Build command used by generator tests
	public static final String BUILD_COMMAND = "mvn" + (SwtUtil.isWindows() ? ".bat" : "") + " install eclipse:m2eclipse";
//	public static final String BUILD_COMMAND = "/Users/magnuslarsson/Applications/apache-maven-2.2.1/bin/mvn install eclipse:m2eclipse";
//	public static final String BUILD_COMMAND = "mvn -version";
//	public static final String BUILD_COMMAND = "mvn install eclipse:m2eclipse";
//	public static final String BUILD_COMMAND = "mvn -o eclipse:m2eclipse ";

	static class ThreadedStreamReader extends Thread {
	    InputStream in;
	    PrintStream out;
	    String type;
	    
	    ThreadedStreamReader(InputStream in, PrintStream out, String type) {
	        this.in = in;
	        this.out = out;
	        this.type = type;
	    }
	    
	    public void run() {
	        try {
	            InputStreamReader isr = new InputStreamReader(in);
	            BufferedReader br = new BufferedReader(isr);
	            String line = null;
	            while ((line = br.readLine()) != null) {
	                out.println(type + line);    
	            }
	            System.err.println("### ThreadedStreamReader terminates for type: " + type);
            } catch (IOException ioe) {
                ioe.printStackTrace();  
            }
	    }
	}

	/**
     * Hidden constructor.
     */
    private SystemUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

    public static void delDirs(String rootfolder) throws IOException {
		doDelete(new File(rootfolder));
	}

	public static void doDelete(File path) throws IOException {
		if (!path.exists()) return;
		
		if (path.isDirectory()) {
			for (File child : path.listFiles()) {
				doDelete(child);
			}
		}
		if (!path.delete()) {
			throw new IOException("Could not delete " + path);
		}
	}

	public static void executeCommand(String command, String workingDirectory) throws IOException {
		executeCommand(command, workingDirectory, System.out, System.err);
	}

	public static void executeCommand(String command, String workingDirectory, PrintStream out, PrintStream err) throws IOException {
		Process p = Runtime.getRuntime().exec(command, null, new File(workingDirectory));

		ThreadedStreamReader outputReader = new ThreadedStreamReader(p.getErrorStream(), err, "");            
		ThreadedStreamReader errorReader  = new ThreadedStreamReader(p.getInputStream(), out, "");            
		    
		// Start reader threads
		// TODO: How do we stop them? Will they die when 
		outputReader.start();
		errorReader.start();
		   
		try {p.waitFor();} catch (InterruptedException e) {}
		
		int retStatus = p.exitValue();
		if (retStatus != 0) {
			throw new IOException("Failed to execute command: [" + command + "]: ret-status = " + retStatus);
		}
	}

	public static int countFiles(String path) throws IOException {
		return countFiles(new File(path));
	}

	public static int countFiles(File path) throws IOException {
		int count = 0;

		if (!path.exists()) return count;

		if (path.isDirectory()) {
			for (File child : path.listFiles()) {
				count += countFiles(child);
			}
		}
		
		return ++count;
	}

}
