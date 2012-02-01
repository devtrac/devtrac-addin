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
package com.fairview5.keepassbb2.common.ui;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.ui.component.Dialog;

import com.fairview5.keepassbb2.common.file.KFile;
import com.fairview5.keepassbb2.common.io.IOUtils;

public class HTMLReader {

	private HTMLReader() {
	}
	
	public static void showResource(String resource) {
		InputStream is = null;
		DataInputStream bis = null;
		try {
			Class c = new HTMLReader().getClass();
			if (c == null) throw new RuntimeException("didn't find class");
			is = c.getResourceAsStream(resource);
			if (is == null) throw new RuntimeException("didn't find resource");
			bis = new DataInputStream(is);
			if (bis == null) throw new RuntimeException("didn't find dis");
			byte[] ba = new byte[is.available()];
			bis.readFully(ba);
			HTMLReader.show(ba);
		} catch (Throwable e) {
			Dialog.alert("Unable to show help: "+e.getMessage());
		} finally {
			IOUtils.closeStream(bis);
			IOUtils.closeStream(is);
		}
		
	}
	
	public static void show(String filename) {
		KFile f = null;
		try {
			f = new KFile(filename);
			show(f.readContents());
		} catch (Exception e) {
			Dialog.alert(e.toString());
		} finally {
			IOUtils.closeStream(f);
		}
	}

	public static void show(byte[] input) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			Base64OutputStream boutput = new Base64OutputStream(output);
			// Write out the special sequence which indicates to the browser
			// that it should treat this as HTML data in base64 format.
			output.write("data:text/html;base64,".getBytes());
			boutput.write(input);
			boutput.flush();
			boutput.close();
			output.flush();
			output.close(); // Invoke the browser with the encoded HTML content.
			BrowserSession bSession = Browser.getDefaultSession();
			bSession.displayPage(output.toString());
		} catch (IOException e) {
			Dialog.alert("IOException: " + e);
		}
	}
	
	public static void showLocalizedHelp(String name) {
		String ln = Locale.getDefaultInputForSystem().getLanguage();
		String n1 = "/"+name+"_"+ln+".html";
		String n2 = "/"+name+"_en.html";
		InputStream is = null;
		DataInputStream bis = null;
		try {
			Class c = new HTMLReader().getClass();
//			Class c = Class.forName("com.fairview5.keepassbb.KeePassBB");
			if (c == null) throw new RuntimeException("didn't find class");
			
			is = c.getResourceAsStream(n1);
			if (is == null) {
				is = c.getResourceAsStream(n2);
			}
			if (is == null) throw new RuntimeException("didn't find resource");
			bis = new DataInputStream(is);
			if (bis == null) throw new RuntimeException("didn't find dis");
			byte[] ba = new byte[is.available()];
			bis.readFully(ba);
			HTMLReader.show(ba);
		} catch (Throwable e) {
			Dialog.alert("Unable to show help: "+e.getMessage()+":"+n1+":"+n2);
		} finally {
			IOUtils.closeStream(bis);
			IOUtils.closeStream(is);
		}
		
	}

}
