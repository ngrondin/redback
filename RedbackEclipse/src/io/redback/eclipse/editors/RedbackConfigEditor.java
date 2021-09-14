package io.redback.eclipse.editors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.EditorPart;

import io.firebus.data.DataException;
import io.firebus.data.DataMap;

import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.impl.AppManager;
import io.redback.eclipse.editors.components.impl.IncludeManager;
import io.redback.eclipse.editors.components.impl.KeyManager;
import io.redback.eclipse.editors.components.impl.MenuManager;
import io.redback.eclipse.editors.components.impl.ObjectManager;
import io.redback.eclipse.editors.components.impl.ObjectTree;
import io.redback.eclipse.editors.components.impl.ProcessManager;
import io.redback.eclipse.editors.components.impl.ResourceManager;
import io.redback.eclipse.editors.components.impl.RoleManager;
import io.redback.eclipse.editors.components.impl.ScriptManager;
import io.redback.eclipse.editors.components.impl.ViewManager;

public class RedbackConfigEditor extends EditorPart implements IResourceChangeListener 
{
	protected String rbService;
	protected String rbConfigType;
	protected String name;
	protected DataMap data;
	protected Manager manager;

	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		setSite(site);
		IFileEditorInput fei = (IFileEditorInput)editorInput;
		setInput(fei);
		
		String[] segments = fei.getFile().getFullPath().segments();
		rbService = null;
		rbConfigType = null;
		if(segments.length>=3) {
			rbService = segments[segments.length - 3];
			rbConfigType = segments[segments.length - 2];
			String filename = segments[segments.length - 1];
			String extension = fei.getFile().getFileExtension();
			name = filename.substring(0, filename.length() - extension.length() - 1);
			setTitle(name);
			try {
				InputStream is = fei.getFile().getContents();
				if(is.available() == 0) {
					data = new DataMap("name", name);
				} else {
					data = new DataMap(is);
				}
			} catch (IOException | DataException | CoreException e) {
				throw new PartInitException("Error initialising the editor", e);
			}
		}
	}

	public void resourceChanged(IResourceChangeEvent event) {
	}

	public void doSave(IProgressMonitor arg0) {
		IFileEditorInput fei = ((IFileEditorInput)getEditorInput());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		data.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		try {
			fei.getFile().setContents(bais, bais.available(), arg0);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		manager.setDataChanged(false);
	}

	public void doSaveAs() {
		// TODO Auto-generated method stub
	}
	
	public void setDirty() {
		firePropertyChange(PROP_DIRTY);
	}

	public boolean isDirty() {
		return manager.isDataChanged();
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	public void createPartControl(Composite composite) {
		composite.setLayout(new FillLayout());
		if(data != null) {
			if(rbService != null) {
				if(rbService.equals("rbo")) {
					if(rbConfigType.equals("object")) {
						manager = new ObjectManager(data, this, composite, SWT.HORIZONTAL);
					} else if(rbConfigType.equals("include")) {
						manager = new IncludeManager(data, this, composite, SWT.HORIZONTAL);
					} else if(rbConfigType.equals("script")) {
						manager = new ScriptManager(data, this, composite, SWT.HORIZONTAL);
					}
				} else if(rbService.equals("rbui")) {
					if(rbConfigType.equals("view")) {
						manager = new ViewManager(data, this, composite, SWT.HORIZONTAL);
					} else if(rbConfigType.equals("resource")) {
						manager = new ResourceManager(data, this, composite, SWT.HORIZONTAL);
					} else if(rbConfigType.equals("menu")) {
						manager = new MenuManager(data, this, composite, SWT.HORIZONTAL);
					} else if(rbConfigType.equals("app")) {
						manager = new AppManager(data, this, composite, SWT.HORIZONTAL);
					}
				} else if(rbService.equals("rbid")) {
					if(rbConfigType.equals("key")) {
						manager = new KeyManager(data, this, composite, SWT.HORIZONTAL);
					} 
				} else if(rbService.equals("rbam")) {
					if(rbConfigType.equals("role")) {
						manager = new RoleManager(data, this, composite, SWT.HORIZONTAL);
					} 
				} else if(rbService.equals("rbpm")) {
					if(rbConfigType.equals("process")) {
						manager = new ProcessManager(data, this, composite, SWT.HORIZONTAL);
					} 
				} else if(rbService.equals("rbdm")) {
					if(rbConfigType.equals("include")) {
						manager = new IncludeManager(data, this, composite, SWT.HORIZONTAL);
					} 
				}
			}
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
