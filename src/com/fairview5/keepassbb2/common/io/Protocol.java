package com.fairview5.keepassbb2.common.io;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.i18n.Locale;
import net.rim.device.api.io.FilterBaseInterface;
import net.rim.device.api.io.MIMETypeAssociations;

import com.fairview5.keepassbb2.common.util.CommonUtils;

public class Protocol implements HttpConnection, FilterBaseInterface {

	FileConnection _conn;
	DataInputStream _is;
   private String _url;
   private String _scheme;
   private String _host;
   private String _path;
   private String _ref;
	
	public Protocol(String name) throws IOException {
		_url = name;
		_scheme = CommonUtils.getScheme(_url);
		_host = CommonUtils.getHost(_url);
		_path = CommonUtils.getPath(_url);
		_ref = CommonUtils.getRef(_url);
		
		if (_scheme.equals("resource")) {
			try {
				_is = new DataInputStream(getResourceAsStream(this.getClass(), _path));
			} catch (ClassNotFoundException e) {
				throw new IOException(e.getMessage());
			}
		} else if (_scheme.equals("resource-help")) {
			try {
				_is = new DataInputStream(getHelpResourceAsStream(this.getClass(), _path));
			} catch (ClassNotFoundException e) {
				throw new IOException(e.getMessage());
			}
			
		} else if (_scheme.equals("file")) {
			_conn = (FileConnection)Connector.open("file://"+name.substring(11));
		}
		if (_is == null && _conn == null) throw new IOException("Unable tto open connection");
	}
	   
	
	public Connection openFilter(String name, int mode, boolean timeouts) throws IOException {
		_url = "http:" + name;
		_scheme = "http";
		_host = CommonUtils.getHost(_url);
		_path = CommonUtils.getPath(_url);
		_ref = CommonUtils.getRef(_url);
		
		if (_host.startsWith("resource.")) {
			try {
				_is = new DataInputStream(getResourceAsStream(this.getClass(), _path));
			} catch (ClassNotFoundException e) {
				throw new IOException(e.getMessage());
			}
		} else if (_host.startsWith("help.")) {
			try {
				_is = new DataInputStream(getHelpResourceAsStream(this.getClass(), _path));
			} catch (ClassNotFoundException e) {
				throw new IOException(e.getMessage());
			}
			
		} else if (_host.startsWith("localhost.")) {
			_conn = (FileConnection)Connector.open("file://"+name.substring(11), mode, timeouts);
		}
		if (_is != null || _conn != null) return this;
		return null;
	}
	
 	private InputStream getResourceAsStream(Class clazz, String name) throws ClassNotFoundException {
		InputStream is = null;
			is = clazz.getResourceAsStream(name);
			return is;
	}
 	
	private InputStream getHelpResourceAsStream(Class clazz, String name) throws ClassNotFoundException {
		String ln = Locale.getDefaultInputForSystem().getLanguage();
		int ix = name.lastIndexOf('.');
		String suffix = "";
		String basename = name;
		if (ix > 0) {
			basename = name.substring(0,ix);
			suffix = name.substring(ix);
		}
		String n1 = basename + "." + ln + suffix;
		String n2 = basename + ".en" + suffix;
		InputStream is = null;
			is = getResourceAsStream(clazz, n1);
			if (is == null)
				is = getResourceAsStream(clazz, n2);
			if (is == null)
				is = getResourceAsStream(clazz, name);
		return is;
	}
	
	
	public long getDate() throws IOException {
		return System.currentTimeMillis();
	}

	public long getExpiration() throws IOException {
		return 0;
	}

	public String getFile() {
		return _path;
	}

	public String getHeaderField(String name) throws IOException {
		return null;
	}

	public String getHeaderField(int n) throws IOException {
		return null;
	}

	public long getHeaderFieldDate(String name, long def) throws IOException {
		return 0;
	}

	public int getHeaderFieldInt(String name, int def) throws IOException {
		return 0;
	}

	public String getHeaderFieldKey(int n) throws IOException {
		return null;
	}

	public String getHost() {
		return _host;
	}

	public long getLastModified() throws IOException {
		if (_conn != null) return _conn.lastModified();
		return System.currentTimeMillis();
	}

	public int getPort() {
		return 80;
	}

	public String getProtocol() {
		return "http";
	}

	public String getQuery() {
		return null;
	}

	public String getRef() {
		return _ref;
	}

	public String getRequestMethod() {
		return "GET";
	}

	public String getRequestProperty(String key) {
		return null;
	}

	public int getResponseCode() throws IOException {
		return 200;
	}

	public String getResponseMessage() throws IOException {
		return "OK";
	}

	public String getURL() {
		return _url;
	}

	public void setRequestMethod(String method) throws IOException {
	}

	public void setRequestProperty(String key, String value) throws IOException {
	}

	public String getEncoding() {
		return null;
	}

	public long getLength() {
		if (_conn != null) return _conn.availableSize();
		try {
			return _is.available();
		} catch (IOException e) {
		}
		return 0;
	}

	public String getType() {
      return MIMETypeAssociations.getMIMEType(_path);
	}

	public DataInputStream openDataInputStream() throws IOException {
		if (_conn!= null) return _conn.openDataInputStream();
		return _is;
	}

	public InputStream openInputStream() throws IOException {
		if (_conn!= null) return _conn.openInputStream();
		return _is;
	}

	public void close() throws IOException {
		if (_conn!= null) _conn.close();
		_is.close();
	}

	public DataOutputStream openDataOutputStream() throws IOException {
		return null;
	}

	public OutputStream openOutputStream() throws IOException {
		return null;
	}

}
