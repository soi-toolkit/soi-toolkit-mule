package org.soitoolkit.tools.generator.plugin.generator;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.text.GStringTemplateEngine;
import groovy.text.Template;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.soitoolkit.tools.generator.plugin.model.DefaultModelImpl;
import org.soitoolkit.tools.generator.plugin.model.IModel;
import org.soitoolkit.tools.generator.plugin.model.ModelFactory;
import org.soitoolkit.tools.generator.plugin.model.ModelReadOnlyMap;
import org.soitoolkit.tools.generator.plugin.model.enums.TransportEnum;

public class GeneratorUtil {

	private static final int LOG_WARN  = 0;
	private static final int LOG_INFO  = 1;
	private static final int LOG_DEBUG = 2;
	private int logLevel = LOG_INFO;
	
	private PrintStream ps;
	private String templateFolder; // Without trailing "/" or "\"
	private String outputFolder; // Without trailing "/" or "\"
	private String outputRootFolder;

	private IModel model;
	private Map<String, Object> modelMap;
	
	public GeneratorUtil(PrintStream ps, String groupId, String artifactId, String version, String service, List<TransportEnum> transports, String templateFolder, String outputFolder, String outputRootFolderModelExpression) {

		model = ModelFactory.newModel(groupId, artifactId, version, service, transports);

		init(ps, templateFolder, outputFolder, outputRootFolderModelExpression);			
		
		logInfo("Generate files from templateFolder: " + templateFolder);

	}

	public GeneratorUtil(PrintStream ps, String groupId, String artifactId, String version, String service, List<TransportEnum> transports, String schemaName, List<String> operations, String templateFolder, String outputFolder, String outputRootFolderModelExpression) {
		model = ModelFactory.newModel(groupId, artifactId, version, service, transports, schemaName, operations);

		init(ps, templateFolder, outputFolder, outputRootFolderModelExpression);			
	}
	
	private void init(PrintStream ps, String templateFolder, String outputFolder, String outputRootFolderModelExpression) {
		this.ps               = ps;
		this.templateFolder   = removeTrailingFolderSeparator(templateFolder); 
		this.outputFolder     = removeTrailingFolderSeparator(outputFolder);
		this.outputRootFolder = (String)model.resolveParameter(outputRootFolderModelExpression);

		modelMap = new ModelReadOnlyMap(model);
	}
	
	public void generateFolder(String folder) {
		// First resolve any variables in the foldername
		folder = resolveVariables(folder);
		getFolderAndCreateIfMissing(folder);
	}
		
	public void generateContentAndCreateFile(String templateFile) {
		String content = generateContent(templateFile);
		if (content == null) {
			logWarn("Couldn't generate code for template " + templateFile + ", skipping it");
			return;
		}
		String filename = getFilename(templateFile);
		logDebug("Create file: " + filename + " size: " + content.length() + " characters");
		createFile(filename, content);
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public String getOutputRootFolder() {
		return outputRootFolder;
	}

	public IModel getModel() {
		return model;
	}


	/*
	 * Internal methods
	 */
	
	private String generateContent(String templateFile) {
		try {
			GStringTemplateEngine gStringEngine = new GStringTemplateEngine();
			URL url = loadTemplate(templateFolder + "/" + templateFile);
			if (url == null) {
				logWarn("NO TEMPLATE FOUND FOR " + templateFile);
			} else {
				logDebug("Loaded template: " + url.getPath());
			}
			if (url == null) return null;
			
			Template template = gStringEngine.createTemplate(url);
			return template.make(modelMap).toString();
		} catch (Exception e) {
			ps.println("### ERROR: Failed generate templatefile: " + templateFile);
//			Set<String> keys = modelMap.keySet();
//			for (String key : keys) {
//				System.err.println("- " + key + " = " + modelMap.get(key));
//			}
			e.printStackTrace(ps);
			return null;
		}
	}

	/* File handling methods... */
	
	private String removeTrailingFolderSeparator(String folder) {
		if (folder.endsWith("/") || folder.endsWith("\\")) { 
			folder = folder.substring(0, folder.length() - 1);
		}
		return folder;
	}
	
	private URL loadTemplate(String classpathName) {
		return getClass().getResource(classpathName);
	}

	
	private String getFilename(String templateFile) {

		// First resolve any variables in the filenam of the template
		templateFile = resolveVariables(templateFile);
		
		// Separate the folder part of the templateFilename, if any 
		String templateFolder = null;
		int lastSepPos = templateFile.lastIndexOf('/');		
		if (lastSepPos != -1) {
			templateFolder = templateFile.substring(0, lastSepPos);
			templateFile = templateFile.substring(lastSepPos + 1);
		}

		// Now compute the target folder and filename, also ensure that all directories exists in the foldername
		String folder = getFolderAndCreateIfMissing(templateFolder);

		int suffixSize = ".gt".length();
		int endPos = templateFile.length() - suffixSize;
		String filename = templateFile.substring(0, endPos);
			    
		filename = folder + "/" + filename;
		logDebug("Template file [" + templateFile + "] resulted in target filename [" + filename + "]");
		return filename;
	}

	/**
	 * Replaces any __variable__ in the supplied name with the corresponding value in the model
	 * 
	 * @param name
	 * @return
	 */
	private String resolveVariables(String name) {
		// Replace __variable__ with ${variable} and then evaluate the name as a groovy string
		String outName = "";
		int startPos = 0;
		int pos = name.indexOf("__", startPos);
		while (pos != -1) {
			outName += name.substring(startPos, pos);
			int endPos = name.indexOf("__", pos + 2);
			String variableName = name.substring(pos + 2, endPos);
			Object variableValue = model.resolveParameter(variableName, null);
			if (variableValue == null) {
				logWarn("Found no filepath variable __" + variableName + "__! Will use default value: " + variableName);
				variableValue = variableName;
			}
			logDebug("Found filepath variable: " + variableName + " = " + variableValue);
			outName += variableValue;
			startPos = endPos + 2;

			// Look for the next variable
			pos = name.indexOf("__", startPos);
		}
		outName += name.substring(startPos);
		logDebug("Resolved filepath [" + name + "] to [" + outName + "]");
		return outName;
	}

	/**
	 * Computes foldername and creates the folders that does not already exist
	 * 
	 * @param inFolder, left out if null 
	 * @return
	 */
	private String getFolderAndCreateIfMissing(String inFolder) {
		String folder = outputFolder + "/" + outputRootFolder;
		if (inFolder != null) {
			folder += "/" + inFolder;
		}
		mkdirs(folder);
		return folder;
	}

	private void mkdirs(String folder) {
		boolean success = (new File(folder)).mkdirs();
	    if (success) {
	      logInfo("Created directory: " + folder);
//	    } else {
//		      .err.println("Directory: " + folder + " NOT created, already exists?");
	    }
	}

	private void createFile(String filename, String content) {
		Writer out = null;
		try {
			out = new BufferedWriter(new FileWriter(filename));
			out.write(content);
			logInfo("Created file: " + filename + ", size: " + content.length());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null) try {out.close();} catch (IOException e) {}
		}
	}

	private void logWarn(String msg) {
		if (logLevel >= LOG_WARN) ps.println("WARN: " + msg);
	}
	private void logInfo(String msg) {
		if (logLevel >= LOG_INFO) ps.println("INFO: " + msg);
	}
	private void logDebug(String msg) {
		if (logLevel >= LOG_DEBUG) ps.println("DEBUG: " + msg);
	}

	/*
	 * LAB CODE
	 * Inactive but maybe good to have in the future...
	 */
	
    @SuppressWarnings("unused")
	private void groovyLabCode(Map<String, Object> model) {
        try {

        	groovy.lang.Binding binding = new Binding();
			binding.setVariable("first", "HELLO");
        	binding.setVariable("second", "world");

        	groovy.lang.GroovyShell groovyShell = new GroovyShell(getClass().getClassLoader(), binding);
			System.out.println(groovyShell.evaluate("(1..20).sum()"));

			System.out.println(groovyShell.evaluate("first.toLowerCase() + second.toUpperCase()"));
        	
        	ScriptEngineManager factory = new ScriptEngineManager();
			ScriptEngine engine = factory.getEngineByName("groovy");

			// basic example
			System.out.println(engine.eval("(1..10).sum()"));

			// example showing scripting variables
			engine.put("first", "HELLO");
			engine.put("second", "world");
			for (String name : model.keySet()) {
				engine.put(name, model.get(name));
			}
			System.out.println(engine.eval("first.toLowerCase() + second.toUpperCase()"));
			System.out.println(engine.eval("componentType + ':' + componentName + ':' + folderName"));
			
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
