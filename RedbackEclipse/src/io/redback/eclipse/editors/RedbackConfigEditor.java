package io.redback.eclipse.editors;

import java.io.IOException;

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
import org.eclipse.ui.part.EditorPart;

import com.nic.firebus.utils.DataException;
import com.nic.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.impl.ObjectManager;
import io.redback.eclipse.editors.components.impl.ObjectTree;

public class RedbackConfigEditor extends EditorPart implements IResourceChangeListener 
{
	protected String rbService;
	protected String rbConfigType;
	protected DataMap config;

	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		setSite(site);
		IFileEditorInput fei = (IFileEditorInput)editorInput;
		String[] segments = fei.getFile().getFullPath().segments();
		rbService = null;
		rbConfigType = null;
		if(segments.length>=3) {
			rbService = segments[segments.length - 3];
			rbConfigType = segments[segments.length - 2];
			try {
				config = new DataMap(fei.getFile().getContents());
			} catch (IOException | DataException | CoreException e) {
				throw new PartInitException("Error initialising the editor", e);
			}
		}
	}

	public void resourceChanged(IResourceChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void doSave(IProgressMonitor arg0) {
		// TODO Auto-generated method stub
		
	}

	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return true;
	}

	public void createPartControl(Composite composite) {
		composite.setLayout(new FillLayout());
		if(config != null) {
			if(rbService != null) {
				if(rbService.equals("rbo")) {
					if(rbConfigType.equals("object")) {
						new ObjectManager(config, composite, SWT.HORIZONTAL);
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
