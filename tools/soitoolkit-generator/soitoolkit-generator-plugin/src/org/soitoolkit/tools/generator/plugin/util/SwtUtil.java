package org.soitoolkit.tools.generator.plugin.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
