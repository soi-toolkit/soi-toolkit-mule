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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class SwtUtil {

	/**
     * Hidden constructor.
     */
    private SwtUtil() {
        throw new UnsupportedOperationException("Not allowed to create an instance of this class");
    }

	public static Text createTextField(Composite container, String labelText, ModifyListener modifyListener) {
		Label label = new Label(container, SWT.NULL);
		label.setText(labelText);

		Text textComponent = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		textComponent.setLayoutData(gd);
		textComponent.addModifyListener(modifyListener);

		// TODO: ML FIX. Must learn layour mgm again :-)
		new Label(container, SWT.NULL);
		
		return textComponent;
	}
	
	public static Button createCheckboxButton(final Composite container, Listener listener, int i, String text) {
		Button button = new Button (container, SWT.CHECK);
		button.setData(i);
		button.setText (text);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		button.setLayoutData(gd);
		if (listener != null) {button.addListener (SWT.Selection, listener);}
		return button;
	}
	public static Button createRadioButton(final Composite container, Listener listener, int i, String text) {
		Button button = new Button (container, SWT.RADIO);
		button.setData(i);
		button.setText (text);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		button.setLayoutData(gd);
		button.addListener (SWT.Selection, listener);
		if (i == 0) button.setSelection (true);
		return button;
	}
	
	public static void addRadioButtons (String[] choices, String labelText, final ValueHolder<Integer> selection, Composite parent, final Listener selectionChangedListener) {

		final Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
//		layout.verticalSpacing = 9;

		Listener listener = new Listener () {
			public void handleEvent (Event e) {
				selection.value = (Integer)e.widget.getData();

				Control [] children = container.getChildren ();
				for (int i=0; i<children.length; i++) {
					Control child = children [i];
					if (e.widget != child && child instanceof Button && (child.getStyle () & SWT.RADIO) != 0) {
						((Button) child).setSelection (false);
					}
				}
				((Button) e.widget).setSelection (true);
				
				if (selectionChangedListener != null) {
					selectionChangedListener.handleEvent(e);
				}
			}
		};
		Label label = new Label(container, SWT.NULL);
		label.setText(labelText);

		for (int i = 0; i < choices.length; i++) {
			Button b = SwtUtil.createRadioButton(container, listener, i, choices[i]);
			b.setSelection(i == selection.value);
		}
	}

	public static Composite createGridContainer(Composite parent, int numColumns) {
		return createGridContainer(parent, numColumns, -1);
	}

	public static Composite createGridContainer(Composite parent, int numColumns, int verticalSpacing) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = numColumns;
		if (verticalSpacing > 0) layout.verticalSpacing = verticalSpacing;
		return container;
	}
	
	public static boolean isWindows() {
		String platform = SWT.getPlatform();
		return platform.equals("win32") || platform.equals("wpf");
	}
	
	
}
