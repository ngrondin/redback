package io.redback.eclipse.editors.test;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

public class TestIPath implements IPath {

	protected String path;
	
	public TestIPath(String p) {
		path = p;
	}
	
	@Override
	public IPath addFileExtension(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath addTrailingSeparator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath append(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath append(IPath arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDevice() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFileExtension() {
		return "json";
	}

	@Override
	public boolean hasTrailingSeparator() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAbsolute() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPrefixOf(IPath arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRoot() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUNC() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValidPath(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValidSegment(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String lastSegment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath makeAbsolute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath makeRelative() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath makeRelativeTo(IPath arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath makeUNC(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int matchingFirstSegments(IPath arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IPath removeFileExtension() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath removeFirstSegments(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath removeLastSegments(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath removeTrailingSeparator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String segment(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int segmentCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] segments() {
		String[] s = path.split(File.separator);
		String[] s1 = new String[s.length - 1];
		System.arraycopy(s, 1, s1, 0, s.length - 1);
		return s1;
	}

	@Override
	public IPath setDevice(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File toFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toOSString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toPortableString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath uptoSegment(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object clone() {
		return null;
	}
}
