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
package com.fairview5.keepassbb2.common.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

import javax.microedition.io.*;
import javax.microedition.io.file.FileConnection;

import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.io.FileNotFoundException;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.util.DateTimeUtilities;
import net.rim.device.api.util.NumberUtilities;

import com.fairview5.keepassbb2.common.ui.StatusDialog;

public class CommonUtils {

	static long GUID_BASE = 0;
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss ");
	static SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ");
	static SimpleDateFormat isoLocal = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss");
	static boolean debug = false;
	
	static ObjectListField olfDebug;
	
	public static Date parseISOTime(String t) {
		int[] times = new int[7];
		times[0] = Integer.parseInt(t.substring(0,4));
		times[1] = Integer.parseInt(t.substring(5,7)) - 1;
		times[2] = Integer.parseInt(t.substring(8,10));
		times[3] = Integer.parseInt(t.substring(11,13));
		times[4] = Integer.parseInt(t.substring(14,16));
		times[5] = Integer.parseInt(t.substring(17,19));
		times[6] = 0;
		Calendar cal;
		if (t.endsWith("Z")) {
			cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		} else {
			cal = Calendar.getInstance();
		}
		DateTimeUtilities.setCalendarFields(cal, times);
		return cal.getTime();
	}
	
	public static String getISOTime(Date d) {
		StringBuffer sb = new StringBuffer();
		getISOTime(d, sb);
		return sb.toString();
	}
	public static String getISOLocalTime(Date d) {
		return isoLocal.formatLocal(d.getTime());
	}
		
	public static StringBuffer getISOTime(Date d, StringBuffer sb) {
		TimeZone tz = TimeZone.getTimeZone("GMT");
		Calendar c = Calendar.getInstance(tz);
		c.setTime(d);
		return iso.format(c, sb, null);
	}
	
	public static StringBuffer getSQLLocalTime(Date d, StringBuffer sb) {
		return iso.formatLocal(sb, d.getTime());
	}
	
	public static String getSQLLocalTime(Date d) {
		return iso.formatLocal(d.getTime());
	}
	
	public static String getSQLLocalTime() {
		return iso.formatLocal(new Date().getTime());
	}

	public static String printBA(String msg, byte[] ba) {
		return CommonUtils.printBA(msg, ba, 0, ba.length);
	}

	public static String printBA(byte[] ba) {
		return CommonUtils.printBA(null, ba, 0, ba.length);
	}

	public static String printBA(String msg, byte[] ba, int offset, int len) {
		StringBuffer sb = new StringBuffer();
		if (msg != null)
			sb.append(msg);
		for (int i = offset; i < offset + len; i++) {
			NumberUtilities.appendNumber(sb, ba[i], 16, 2);
		}
		return sb.toString();
	}

	public static byte[] getHttpFile(final String url) throws IOException {
		if (url == null)
			throw new IllegalArgumentException("URL is null");
		final Throwable[] ex = new Throwable[1];
		final byte[][] ba = new byte[1][];

		final StatusDialog sd = new StatusDialog("Retrieving " + url);
		Thread t = new Thread(new Runnable() {
			public void run() {
				Connection hc = null;
				DataInputStream dis = null;
				try {
					hc = Connector.open(url);
					int size = 0;

					if (hc instanceof FileConnection) {
						if (!((FileConnection) hc).exists())
							throw new FileNotFoundException("Unable to find file " + url);
						dis = ((FileConnection) hc).openDataInputStream();
						size = (int) ((FileConnection) hc).fileSize();
					} else {
						if (((HttpConnection) hc).getResponseCode() != HttpConnection.HTTP_OK)
							throw new FileNotFoundException("Unable to open file " + url+". Respose code: "+ ((HttpConnection) hc).getResponseCode());

						dis = ((HttpConnection) hc).openDataInputStream();
						size = (int) ((HttpConnection) hc).getLength();
					}
					if (size == 0)
						throw new IOException("File size was 0");

					ba[0] = new byte[size];
					dis.readFully(ba[0]);
				} catch (Throwable e) {
					if (e instanceof FileNotFoundException) {
						ex[0] = e;
					} else {
						ex[0] = new RuntimeException(e.toString());
					}
				} finally {
					try {
						if (dis != null)
							dis.close();
						if (hc != null)
							hc.close();
					} catch (IOException e) {
					}
				}
				sd.close();
				return;
			}
		});

		t.start();
		UiApplication.getUiApplication().pushModalScreen(sd);
		if (ex[0] != null) {
			if (ex[0] instanceof FileNotFoundException) throw (IOException) ex[0];
			else throw new RuntimeException(ex[0].getMessage());
			
		}
		return (ba[0]);
	}
	
	public static long initializeGUID(String base) {
		GUID_BASE = ((long)base.hashCode()) << 32;
		return GUID_BASE;
	}

	public static long createGUID(String s) {
		if (GUID_BASE == 0) throw new IllegalStateException("GUID_BASE wasn't set.");
		long h1 = Math.abs(s.hashCode());
		long h = GUID_BASE | h1;
		return (h);
	}

	public static long createGUID(String base, String s) {
		long GUID_BASE = ((long)base.hashCode()) << 32; 
		long h1 = Math.abs(s.hashCode());
		long h = GUID_BASE | h1;
		return (h);
	}
	
	public static void registerLogger(String name) {
		long GUID_LOG = CommonUtils.createGUID("GUID_LOG");
		EventLogger.register(GUID_LOG, name, EventLogger.VIEWER_STRING);
	}
	
	public static void logger(String msg, int level) {
		long GUID_LOG = CommonUtils.createGUID("GUID_LOG");
		EventLogger.logEvent(GUID_LOG, msg.getBytes(), level);
	}

	public static void logger(String msg) {
		logger(msg, EventLogger.ALWAYS_LOG);
	}

	
	public static boolean isHexDigit(char c) {
		return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
	}

	public static boolean isHexString(String s) {
		int l = s.length();
		for (int i = 0; i < l; i++) {
			if (!isHexDigit(s.charAt(i)))
				return false;
		}
		return true;
	}

	public static String getScheme(String url) {
		int ix = url.indexOf(':');
		if (ix <= 0)
			return null;
		return (url.substring(0, ix));
	}

	public static String getHost(String url) {
		int ix1 = url.indexOf("://");
		if (ix1 <= 0)
			return null;
		ix1 += 3;
		int ix2 = url.indexOf('/', ix1);
		if (ix2 <= 0)
			return url.substring(ix1);
		return (url.substring(ix1, ix2));
	}

	public static String getPath(String url) {
		int ix1 = url.indexOf("://");
		if (ix1 <= 0)
			return null;
		ix1 += 3;
		int ix2 = url.indexOf('/', ix1);
		if (ix2 <= 0)
			return null;
		int ix3 = url.indexOf('#');
		if (ix3 > 0)
			return (url.substring(ix2, ix3));
		int ix4 = url.indexOf('?');
		if (ix4 > 0)
			return (url.substring(ix2, ix4));
		return (url.substring(ix2));
	}

	public static String getRef(String url) {
		int ix3 = url.indexOf('#');
		if (ix3 <= 0)
			return null;
		ix3++;
		int ix4 = url.indexOf('?');
		if (ix4 > 0)
			return (url.substring(ix3, ix4));
		return (url.substring(ix3));
	}

	public static String getQueryString(String url) {
		int ix4 = url.indexOf('?');
		if (ix4 <= 0)
			return null;
		return (url.substring(ix4 + 1));
	}
	
	public static void showWebPage(String path) {
		String host = "f5bbutils.fairview5.com";
//		if (DeviceInfo.isSimulator()) {
//			host = "localbb:8090";
//		}
		Browser.getDefaultSession().displayPage("http://"+host+path);
		
	}
	
	public static String getOSSoftwareVersionString() {
		int mh = CodeModuleManager.getModuleHandle("net_rim_bb_phone_api");
		return CodeModuleManager.getModuleVersion(mh);
	}
	public static double getOSSoftwareVersion() {
		String s = getOSSoftwareVersionString();
		int ix1 = s.indexOf('.');
		if (ix1 <= 0) return(0);
		int ix2 = s.indexOf('.',ix1+1);
		return Double.parseDouble(s.substring(0,ix2));
	}
	
	public static String formatDouble(double d, int dp) {
		String s = Double.toString(d);	
		int ix = s.indexOf('.');
		if (ix < 0)
			return (s);
		if (dp == 0)
			return (s.substring(0, ix));
		return (s.substring(0, ix + dp + 1));
	}
	
	public static void setDebug(boolean d) {
		debug = d;
	}

	public static void debug(String msg) {
		if (debug) return;
		String n = Thread.currentThread().getName();
		try {
			int t = Integer.parseInt(n.substring(7));
			n = "0x"+Integer.toHexString(t);
		} catch (Exception ex) {}
		logger("["+n+"]"+ msg, EventLogger.ALWAYS_LOG);
	}
	
	public static void debug(String msg, Throwable e) {
		if (debug) return;
		String n = Thread.currentThread().getName();
		try {
			int t = Integer.parseInt(n.substring(7));
			n = "0x"+Integer.toHexString(t);
		} catch (Exception ex) {}
		logger("["+n+"]"+ msg, EventLogger.ALWAYS_LOG);
		e.printStackTrace();
	}

	
	public static String XMLEncode(String s) {
		int len = s.length();
		StringBuffer sb = new StringBuffer(len * 2);
		for (int i = 0; i < len; i++) {
			switch (s.charAt(i)) {
			case '\'':
				sb.append("&apos;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			default:
				sb.append(s.charAt(i));
			}
		}
		return sb.toString();
	}

	public static String XMLEncode(StringBuffer s) {
		int len = s.length();
		StringBuffer sb = new StringBuffer(len * 2);
		for (int i = 0; i < len; i++) {
			switch (s.charAt(i)) {
			case '\'':
				sb.append("&apos;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			default:
				sb.append(s.charAt(i));
			}
		}
		return sb.toString();
	}

	public static void XMLEncode(StringBuffer sb, String s) {
		int len = s.length();
		for (int i = 0; i < len; i++) {
			switch (s.charAt(i)) {
			case '\'':
				sb.append("&apos;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			default:
				sb.append(s.charAt(i));
			}
		}
	}

	public static void XMLEncode(StringBuffer sb, StringBuffer s) {
		int len = s.length();
		for (int i = 0; i < len; i++) {
			switch (s.charAt(i)) {
			case '\'':
				sb.append("&apos;");
				break;
			case '&':
				sb.append("&amp;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			default:
				sb.append(s.charAt(i));
			}
		}
	}
	
	public static void setDebugObjectListField(ObjectListField olf) {
		olfDebug = olf;
	}
	
	public static void olfDebug(String string) {
		if (olfDebug == null) return;
		int s = olfDebug.getSize();
		olfDebug.insert(s, string);
		olfDebug.setSelectedIndex(olfDebug.getSize());
	}

	
	
		
}
