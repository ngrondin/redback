package io.redback.eclipse.editors.test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.FileEditorInput;

import io.redback.eclipse.editors.RedbackConfigEditor;

public class RedbackEditorTest {
	public static void main(String[] args) {
		try {
			IFileEditorInput fei = new TestFileEditorInput("C:\\src\\pulse\\Office\\office\\src\\main\\resources\\io\\redback\\config\\rbui\\view\\asset.json");
			IEditorSite es = new TestEditorSite();
			RedbackConfigEditor rce = new RedbackConfigEditor();
			rce.init(es, fei);
			
			Display display = new Display();
			Shell shell = new Shell(display);
			shell.setMinimumSize(1000, 700);
			shell.setLayout(new FillLayout());
			Composite composite = new Composite(shell, SWT.HORIZONTAL);
			composite.getHorizontalBar().setVisible(false);
			rce.createPartControl(composite);
			
			shell.pack();
			shell.open ();
			while (!shell.isDisposed ()) {
				if (!display.readAndDispatch ()) 
					display.sleep ();
			}
			display.dispose ();
		} catch(Exception e) {
			e.printStackTrace();			
		}
		
	}

}
