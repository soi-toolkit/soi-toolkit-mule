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
package org.soitoolkit.tools.generator.plugin.swt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class SimpleEditor {
	private Display display;
	private Shell shell;
	private HashMap<String, Image> hashImages;
	private StyledText textArea;
	private String currentFileName;
	private boolean modified = false;

	public SimpleEditor() {
		display = new Display();
		shell = new Shell(display);

		init();
		createMenu();
		createToolBar();
		createGUI();

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void createGUI() {
		shell.setLayout(new GridLayout(1, false));
		shell.setImage(getImageFor("open"));

		textArea = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
				| SWT.H_SCROLL);
		textArea.setLayoutData(new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		textArea.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				setModified(true);
			}
		});

		Link link = new Link(shell, SWT.NONE);
		// link.setText("Ein Link!");
		link.setText("Ein Link!");
		link.setToolTipText("Dies ist der Tooltip zum Link!");
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				System.err.println("link");
			}
		});
	}

	private void createToolBar() {
		CBanner banner = new CBanner(shell, SWT.NONE);
		banner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.GRAB_HORIZONTAL));
		banner.setRightWidth(200);

		ToolBar tb1 = new ToolBar(banner, SWT.NONE);
		tb1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		banner.setLeft(tb1);

		ToolItem item = new ToolItem(tb1, SWT.NONE);
		// item.setText("Open");
		item.setToolTipText("Open...");
		item.setImage(getImageFor("open"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				open();
			}
		});

		item = new ToolItem(tb1, SWT.NONE);
		// item.setText("Save");
		item.setToolTipText("Save");
		item.setImage(getImageFor("save"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				save();
			}
		});

		item = new ToolItem(tb1, SWT.NONE);
		// item.setText("Save");
		item.setToolTipText("Save as...");
		item.setImage(getImageFor("saveas"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				saveAs();
			}
		});

		item = new ToolItem(tb1, SWT.NONE);
		// item.setText("Close");
		item.setToolTipText("Close");
		item.setImage(getImageFor("close"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				close();
			}
		});

		// item = new ToolItem(tb1, SWT.SEPARATOR);

		tb1 = new ToolBar(banner, SWT.NONE);
		tb1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		banner.setRight(tb1);

		item = new ToolItem(tb1, SWT.NONE);
		// item.setText("Cut");
		item.setToolTipText("Cut");
		item.setImage(getImageFor("cut"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				cut();
			}
		});
		item = new ToolItem(tb1, SWT.NONE);
		// item.setText("Copy");
		item.setToolTipText("Copy");
		item.setImage(getImageFor("copy"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				copy();
			}
		});
		item = new ToolItem(tb1, SWT.NONE);
		// item.setText("Paste");
		item.setToolTipText("Paste");
		item.setImage(getImageFor("paste"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				paste();
			}
		});

		item = new ToolItem(tb1, SWT.SEPARATOR);

		item = new ToolItem(tb1, SWT.NONE);
		// item.setText("Undo");
		item.setToolTipText("Undo");
		item.setImage(getImageFor("undo"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				undo();
			}
		});
		item = new ToolItem(tb1, SWT.NONE);
		// item.setText("Redo");
		item.setToolTipText("Redo");
		item.setImage(getImageFor("redo"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				redo();
			}
		});

		item = new ToolItem(tb1, SWT.SEPARATOR);

		item = new ToolItem(tb1, SWT.NONE);
		// item.setText("Help");
		item.setToolTipText("Help");
		item.setImage(getImageFor("help"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				help();
			}
		});

		item = new ToolItem(tb1, SWT.SEPARATOR);

		item = new ToolItem(tb1, SWT.NONE);
		// item.setText("Exit");
		item.setToolTipText("Exit");
		item.setImage(getImageFor("exit"));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				exit();
			}
		});
	}

	private void createMenu() {
		Menu menubar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menubar);

		MenuItem menuFileHeader = new MenuItem(menubar, SWT.CASCADE);
		menuFileHeader.setText("&File");

		Menu menuFile = new Menu(shell, SWT.DROP_DOWN);
		menuFileHeader.setMenu(menuFile);

		MenuItem itemOpen = new MenuItem(menuFile, SWT.PUSH);
		itemOpen.setText("&Open...");
		itemOpen.setImage(getImageFor("open"));
		itemOpen.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				open();
			}
		});

		MenuItem itemClose = new MenuItem(menuFile, SWT.PUSH);
		itemClose.setText("&Close...");
		itemClose.setImage(getImageFor("close"));
		itemClose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				close();
			}
		});

		MenuItem itemSave = new MenuItem(menuFile, SWT.PUSH);
		itemSave.setText("&Save...");
		itemSave.setImage(getImageFor("save"));
		itemSave.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				save();
			}
		});

		MenuItem itemSaveAs = new MenuItem(menuFile, SWT.PUSH);
		itemSaveAs.setText("&Save as...");
		itemSaveAs.setImage(getImageFor("saveas"));
		itemSaveAs.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				saveAs();
			}
		});

		MenuItem sep = new MenuItem(menuFile, SWT.SEPARATOR);

		MenuItem itemExit = new MenuItem(menuFile, SWT.PUSH);
		itemExit.setText("E&xit");
		itemExit.setImage(getImageFor("exit"));
		itemExit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
				display.dispose();
			}
		});

		MenuItem menuEditHeader = new MenuItem(menubar, SWT.CASCADE);
		menuEditHeader.setText("&Edit");

		Menu menuEdit = new Menu(shell, SWT.DROP_DOWN);
		menuEditHeader.setMenu(menuEdit);

		MenuItem itemUndo = new MenuItem(menuEdit, SWT.PUSH);
		itemUndo.setText("&Undo");
		itemUndo.setImage(getImageFor("undo"));
		itemUndo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				undo();
			}
		});

		MenuItem itemRedo = new MenuItem(menuEdit, SWT.PUSH);
		itemRedo.setText("&Redo");
		itemRedo.setImage(getImageFor("redo"));
		itemRedo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				redo();
			}
		});

		sep = new MenuItem(menuEdit, SWT.SEPARATOR);

		MenuItem itemCut = new MenuItem(menuEdit, SWT.PUSH);
		itemCut.setText("&Cut");
		itemCut.setImage(getImageFor("cut"));
		itemCut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				cut();
			}
		});
		MenuItem itemCopy = new MenuItem(menuEdit, SWT.PUSH);
		itemCopy.setText("Co&py");
		itemCopy.setImage(getImageFor("copy"));
		itemCopy.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				copy();
			}
		});
		MenuItem itemPaste = new MenuItem(menuEdit, SWT.PUSH);
		itemPaste.setText("&Paste");
		itemPaste.setImage(getImageFor("paste"));
		itemPaste.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				paste();
			}
		});

		MenuItem menuHelpHeader = new MenuItem(menubar, SWT.CASCADE);
		menuHelpHeader.setText("&?");

		Menu menuHelp = new Menu(shell, SWT.DROP_DOWN);
		menuHelpHeader.setMenu(menuHelp);

		MenuItem itemHelp = new MenuItem(menuHelp, SWT.PUSH);
		itemHelp.setText("&Help index");
		itemHelp.setImage(getImageFor("help"));
		itemHelp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				help();
			}
		});

		sep = new MenuItem(menuHelp, SWT.SEPARATOR);

		MenuItem itemAbout = new MenuItem(menuHelp, SWT.PUSH);
		itemAbout.setText("&About");
		itemAbout.setImage(getImageFor("about"));
		itemAbout.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				about();
			}
		});
	}

	private void open() {
		// System.err.println("open");
		FileDialog dlg = new FileDialog(shell, SWT.OPEN);
		dlg.open();
		currentFileName = dlg.getFilterPath() + File.separator
				+ dlg.getFileName();
		File f = new File(currentFileName);
		if (f.isFile()) {
			// System.err.println("selected " + f);
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(f));
				StringBuffer buf = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					buf.append(line + '\n');
				}
				br.close();
				textArea.setText(buf.toString());
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void save() {
		if ("".equals(textArea.getText()))
			return;

		if (currentFileName == null) {
			FileDialog dlg = new FileDialog(shell, SWT.SAVE);
			String fn = dlg.open();
			if (fn == null) {
				// cancelled
				return;
			} else {
				currentFileName = fn;
			}
		}

		File f = new File(currentFileName);
		try {
			// TODO make this more robust, write out only portions
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write(textArea.getText());
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setModified(false);
	}

	private void saveAs() {
		currentFileName = null;
		save();
	}

	private void close() {
		if (!"".equals(textArea.getText()) && isModified()) {
			MessageBox alert = new MessageBox(shell, SWT.YES | SWT.NO);
			alert.setMessage("File is modified. Do you want to save?");
			int result = alert.open();
			if (result == SWT.YES) {
				save();
			}
		}

		currentFileName = null;
		textArea.setText("");
		setModified(false);
	}

	private void exit() {
		close();

		shell.dispose();
	}

	private void cut() {
		textArea.cut();
	}

	private void copy() {
		textArea.copy();
	}

	private void paste() {
		textArea.paste();
	}

	private void redo() {
		System.err.println("redo");
	}

	private void undo() {
		System.err.println("undo");
	}

	private void about() {
		System.err.println("about");
		AboutDialog dlg = new AboutDialog(shell);
		dlg.setMessage("Dies ist ein About Dialog.\nEr hat 2 Zeilen.");
		int result = dlg.open();
		System.err.println("result=" + result);
	}

	private void help() {
		System.err.println("help");
	}

	private void setModified(boolean modified) {
		this.modified = modified;

		if (modified && !shell.getText().endsWith("*")) {
			shell.setText(shell.getText() + "*");
		} else if (!modified && shell.getText().endsWith("*")) {
			shell.setText(shell.getText().substring(0,
					shell.getText().length() - 1));
		}
	}

	private boolean isModified() {
		return modified;
	}

	private Image getImageFor(String cmd) {
		return (Image) hashImages.get(cmd.toLowerCase());
	}

	private void init() {
		shell.setText("SimpleEditor");
		shell.setSize(500, 300);

		hashImages = new HashMap<String, Image>();
		// TODO remove absolute path
/*
		hashImages
				.put(
						"open",
						new Image(display,
								"C:\\Programme\\eclipse\\workspace\\TestSwt\\src\\icons\\fileopen.png"));
		hashImages
				.put(
						"close",
						new Image(display,
								"C:\\Programme\\eclipse\\workspace\\TestSwt\\src\\icons\\fileclose.png"));
		hashImages
				.put(
						"save",
						new Image(display,
								"C:\\Programme\\eclipse\\workspace\\TestSwt\\src\\icons\\filesave.png"));
		hashImages
				.put(
						"saveas",
						new Image(display,
								"C:\\Programme\\eclipse\\workspace\\TestSwt\\src\\icons\\filesaveas.png"));
		hashImages
				.put(
						"exit",
						new Image(display,
								"C:\\Programme\\eclipse\\workspace\\TestSwt\\src\\icons\\exit.png"));

		hashImages
				.put(
						"cut",
						new Image(display,
								"C:\\Programme\\eclipse\\workspace\\TestSwt\\src\\icons\\editcut.png"));
		hashImages
				.put(
						"copy",
						new Image(display,
								"C:\\Programme\\eclipse\\workspace\\TestSwt\\src\\icons\\editcopy.png"));
		hashImages
				.put(
						"paste",
						new Image(display,
								"C:\\Programme\\eclipse\\workspace\\TestSwt\\src\\icons\\editpaste.png"));

		hashImages
				.put(
						"undo",
						new Image(display,
								"C:\\Programme\\eclipse\\workspace\\TestSwt\\src\\icons\\undo.png"));
		hashImages
				.put(
						"redo",
						new Image(display,
								"C:\\Programme\\eclipse\\workspace\\TestSwt\\src\\icons\\redo.png"));

		hashImages
				.put(
						"help",
						new Image(display,
								"C:\\Programme\\eclipse\\workspace\\TestSwt\\src\\icons\\help.png"));
		hashImages
				.put(
						"about",
						new Image(display,
								"C:\\Programme\\eclipse\\workspace\\TestSwt\\src\\icons\\about_kde.png"));
*/
	}

	public static void main(String[] args) {
		new SimpleEditor();
	}
}

class AboutDialog {

	private MessageBox box;

	public AboutDialog(Shell shell) {
		box = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
		box.setText("About...");
		Rectangle bounds = shell.getBounds();
		int x = bounds.x + (bounds.width / 2);
		int y = bounds.y + (bounds.height / 2);
		System.err.println("x~=" + x + ",y~=" + y);
	}

	public void setMessage(String message) {
		box.setMessage(message);
	}

	public int open() {
		return box.open();
	}
}