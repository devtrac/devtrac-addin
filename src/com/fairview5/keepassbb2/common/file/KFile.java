/*
KeePass for BlackBerry
Copyright 2007,2008 Fairview 5 Engineering, LLC <george.joseph@fairview5.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.fairview5.keepassbb2.common.file;

import java.io.*;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileSystemRegistry;

import net.rim.device.api.io.file.ExtendedFileConnection;
import net.rim.device.api.system.CodeSigningKey;

public class KFile implements DataInput, DataOutput, ExtendedFileConnection {

	KFile fcParent = null;
	ExtendedFileConnection fc = null;
	String path;
	DataOutputStream out;
	DataInputStream dis;
	Object cookie;
	boolean isRoot = false;
	
	public KFile(String path) throws IOException {
		if (path.equals("/")) {
			isRoot = true;
			return;
		}
		fc = (ExtendedFileConnection)Connector.open("file://" + path);
	}
	public KFile(ExtendedFileConnection fileConnection) throws IOException {
      fc = fileConnection;
	}
	public KFile(KFile parentFileConnection, String name) throws IOException {
      fc = new KFile(parentFileConnection.getFullName()+name);
      fcParent = parentFileConnection;
	}

	public KFile getParent() throws IOException {
		if (fcParent != null) return fcParent;
		if (isRoot) return null;
		return new KFile(getPath());
	}
	
	public void close() {
      try {if (out != null) out.close();} catch(Exception e){}
      try {if (dis != null) dis.close();} catch(Exception e){}
      try {if (fc != null) fc.close();} catch(Exception e){}
	}
	
	public boolean exists() {
		if (isRoot) return true;
		return fc.exists();
	}
	public void create() throws IOException {
		fc.create();
	}
	public void create(boolean createParents) throws IOException {
		if (createParents) {
			KFile kfdir = new KFile(getPath());
			if (!kfdir.exists()) kfdir.mkdir(createParents);
			kfdir.close();
		}
		fc.create();
	}
	

	public void setCookie(int cookie) {
		this.cookie = new Integer(cookie);
	}
	public void setCookie(Object cookie) {
		this.cookie = cookie;
	}

	public int getIntCookie() {
		return (cookie != null && cookie instanceof Integer) ? ((Integer)cookie).intValue() : 0;
	}
	public Object getCookie() {
		return this.cookie;
	}
	
	public String getURL() {
		if (isRoot) return("file:///");
		return fc.getURL();
	}
	
	public String getName() {
		if (isRoot) return("/");
		if (fcParent != null && fcParent.isRoot) {
			return fc.getPath().substring(1);
		}
		return fc.getName();
	}
	
	public String getFullName() {
		return getPath() + getName();
	}
	
	public String getPath() {
		if (isRoot) return("");
		if (fcParent != null && fcParent.isRoot) {
			return "/";
		}
		return fc.getPath();
	}
	
	public boolean isDirectory() {
		if (isRoot) return(true);
		return fc.isDirectory();
	}
	
	public boolean isRoot() {
		return isRoot;
	}
	
	public boolean isOpen() {
		return fc.isOpen();
	}
	
	public void truncate() throws IOException {
		fc.truncate(0);
	}
	public byte[] readContents() throws IOException {
		byte[] ba = new byte[(int)fileSize()];
		readFully(ba);
		return ba;
	}
	public void flush() throws IOException {
      if (out == null) return;
		out.flush();
	}
	public boolean readBoolean() throws IOException {
      if (dis == null) dis = fc.openDataInputStream();
		return dis.readBoolean();
	}
	public byte readByte() throws IOException {
      if (dis == null) dis = fc.openDataInputStream();
		return dis.readByte();
	}
	public char readChar() throws IOException {
      if (dis == null) dis = fc.openDataInputStream();
		return dis.readChar();
	}
	public double readDouble() throws IOException {
      if (dis == null) dis = fc.openDataInputStream();
		return dis.readDouble();
	}
	public float readFloat() throws IOException {
      if (dis == null) dis = fc.openDataInputStream();
		return dis.readFloat();
	}
	public void readFully(byte[] b) throws IOException {
      if (dis == null) dis = fc.openDataInputStream();
		dis.readFully(b);
	}
	public void readFully(byte[] b, int off, int len) throws IOException {
      if (dis == null) dis = fc.openDataInputStream();
		dis.readFully(b, off, len);
	}
	public int readInt() throws IOException {
      if (dis == null) dis = fc.openDataInputStream();
		return dis.readInt();
	}
	public long readLong() throws IOException {
      if (dis == null) dis = fc.openDataInputStream();
		return dis.readLong();
	}
	public short readShort() throws IOException {
      if (dis == null) dis = fc.openDataInputStream();
		return dis.readShort();
	}
	public String readUTF() throws IOException {
      if (dis == null) dis = fc.openDataInputStream();
		return dis.readUTF();
	}
	public int readUnsignedByte() throws IOException {
      if (dis == null) dis = fc.openDataInputStream();
		return dis.readUnsignedByte();
	}
	public int readUnsignedShort() throws IOException {
      if (dis == null) dis = fc.openDataInputStream();
		return dis.readUnsignedShort();
	}
	public int skipBytes(int n) throws IOException {
      if (dis == null) dis = fc.openDataInputStream();
		return dis.skipBytes(n);
	}
	
	
	public void write(int b) throws IOException {
      if (out == null) out = fc.openDataOutputStream();
      out.write(b);
	}
	public void write(byte[] b) throws IOException {
      if (out == null) out = fc.openDataOutputStream();
      out.write(b);
	}
	public void write(byte[] ba, int start, int len) throws IOException {
      if (out == null) out = fc.openDataOutputStream();
      out.write(ba, start, len);
	}
	public void writeBoolean(boolean v) throws IOException {
      if (out == null) out = fc.openDataOutputStream();
      out.writeBoolean(v);
	}
	public void writeByte(int v) throws IOException {
      if (out == null) out = fc.openDataOutputStream();
      out.writeByte(v);
	}
	public void writeChar(int v) throws IOException {
      if (out == null) out = fc.openDataOutputStream();
      out.writeChar(v);
	}
	public void writeChars(String s) throws IOException {
      if (out == null) out = fc.openDataOutputStream();
      out.writeChars(s);
	}
	public void writeDouble(double v) throws IOException {
      if (out == null) out = fc.openDataOutputStream();
      out.writeDouble(v);
	}
	public void writeFloat(float v) throws IOException {
      if (out == null) out = fc.openDataOutputStream();
      out.writeFloat(v);
	}
	public void writeInt(int v) throws IOException {
      if (out == null) out = fc.openDataOutputStream();
      out.writeInt(v);
	}
	public void writeLong(long v) throws IOException {
      if (out == null) out = fc.openDataOutputStream();
      out.writeLong(v);
	}
	public void writeShort(int v) throws IOException {
      if (out == null) out = fc.openDataOutputStream();
      out.writeShort(v);
	}
	public void writeUTF(String str) throws IOException {
      if (out == null) out = fc.openDataOutputStream();
      out.writeUTF(str);
	}
		
	/* ExtendedFileConnection methods */
	
	public void setControlledAccess() throws IOException {
      CodeSigningKey csk = CodeSigningKey.get(this);
		fc.setControlledAccess(csk);
	}
	public CodeSigningKey getControlledAccess() throws IOException {
		return fc.getControlledAccess();
	}
	public boolean isContentDRMForwardLocked() throws IOException {
		return fc.isContentDRMForwardLocked();
	}
	public boolean isFileEncrypted() throws IOException {
		return fc.isFileEncrypted();
	}
	public void enableDRMForwardLock() throws IOException {
		fc.enableDRMForwardLock();
	}
	public boolean isContentBuiltIn() throws IOException {
		return fc.isContentBuiltIn();
	}
	public Enumeration listWithDetails(String filter, boolean includeHidden) throws IOException {
		return fc.listWithDetails(filter, includeHidden);
	}
	public InputStream openRawInputStream() throws IOException {
		return fc.openRawInputStream();
	}
	public long rawFileSize() throws IOException {
		return fc.rawFileSize();
	}
	public void setAutoEncryptionResolveMode(boolean mode) throws IOException {
		fc.setAutoEncryptionResolveMode(mode);
	}
	public boolean setControlledAccess(CodeSigningKey csk) throws IOException {
		return fc.setControlledAccess(csk);
	}
	public long availableSize() {
		return fc.availableSize();
	}
	public boolean canRead() {
		if (isRoot) return true;
		return fc.canRead();
	}
	public boolean canWrite() {
		if (isRoot) return false;
		return fc.canWrite();
	}
	public void delete() throws IOException {
		fc.delete();
	}
	public long directorySize(boolean includeSubDirs) throws IOException {
		return fc.directorySize(includeSubDirs);
	}
	public boolean isHidden() {
		if (isRoot) return false;
		return fc.isHidden();
	}
	public long lastModified() {
		if (isRoot) return 0;
		return fc.lastModified();
	}
	public Enumeration list() throws IOException {
		if (isRoot) return FileSystemRegistry.listRoots();
		return fc.list();
	}
	public Enumeration list(String filter, boolean includeHidden) throws IOException {
		if (isRoot) return FileSystemRegistry.listRoots();
		return fc.list(filter, includeHidden);
	}
	public void mkdir() throws IOException {
		fc.mkdir();
	}
	public void mkdir(boolean createParents) throws IOException {
		if (createParents) {
			KFile kfdir = new KFile(getPath());
			if (!kfdir.exists()) kfdir.mkdir(createParents);
			kfdir.close();
		}
		fc.mkdir();
	}	
	public DataInputStream openDataInputStream() throws IOException {
		return fc.openDataInputStream();
	}
	public DataOutputStream openDataOutputStream() throws IOException {
		return fc.openDataOutputStream();
	}
	public InputStream openInputStream() throws IOException {
		return fc.openInputStream();
	}
	public OutputStream openOutputStream() throws IOException {
		return fc.openOutputStream();
	}
	public OutputStream openOutputStream(long byteOffset) throws IOException {
		return fc.openOutputStream(byteOffset);
	}
	public void rename(String newName) throws IOException {
		fc.rename(newName);
	}
	public void setFileConnection(String fileName) throws IOException {
		fc.setFileConnection(fileName);
	}
	public void setHidden(boolean hidden) throws IOException {
		fc.setHidden(hidden);
	}
	public void setReadable(boolean readable) throws IOException {
		fc.setReadable(readable);
	}
	public void setWritable(boolean writable) throws IOException {
		fc.setWritable(writable);
	}
	public long totalSize() {
		return fc.totalSize();
	}
	public void truncate(long byteOffset) throws IOException {
		fc.truncate(byteOffset);
	}
	public long usedSize() {
		return fc.usedSize();
	}
	public long fileSize() throws IOException {
		return fc.fileSize();
	}
	public void writeBytes(String arg0) throws IOException {
	      if (out == null) out = fc.openDataOutputStream();
	      out.write(arg0.getBytes());
	}
}
