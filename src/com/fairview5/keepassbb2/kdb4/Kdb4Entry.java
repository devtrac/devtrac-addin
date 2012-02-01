/*
KeePass for BlackBerry
Copyright 2007 Fairview 5 Engineering, LLC <george.joseph@fairview5.com>

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
package com.fairview5.keepassbb2.kdb4;

import java.io.IOException;
import java.util.*;

import net.rim.device.api.io.Base64InputStream;
import net.rim.device.api.io.Base64OutputStream;

public class Kdb4Entry extends Kdb4Object {

	public Kdb4Entry(Hashtable attributes) {
		super("Entry", attributes);
	}

	public Kdb4Entry() {
		super("Entry", true);
		this.setDataElement("IconID", 0);
		Kdb4Object.Times times = new Kdb4Object.Times();
		Date ct = new Date();
		times.setDataElement("LastModificationTime", ct);
		times.setDataElement("CreationTime", ct);
		times.setDataElement("LastAccessTime", ct);
		times.setDataElement("ExpiryTime", ct);
		times.setDataElement("UsageCount", 0);
		times.setDataElement("Expires", false);
		childElements.addObject(times);
		setDynamicAttribute("Title", "");
		setDynamicAttribute("UserName", "");
		setDynamicAttribute("Password", "", true);
		setDynamicAttribute("UserName", "");
		setDynamicAttribute("Notes", "");
	}

	public void parsingFinished() {
		setKeywords();
	}

	public int compareTo(Kdb4Entry obj2) {
		String n1 = getStringAttribute("Title").getValue()
				+ getStringAttribute("UserName").getValue();
		String n2 = obj2.getStringAttribute("Title").getValue()
				+ obj2.getStringAttribute("UserName").getValue();
		return n1.compareTo(n2);
	}

	private void setKeywords() {
		Vector v = new Vector();
		String t = getStringDynamicAttribute("Title");
		if (t != null) {
			v.addElement(t);
		}
		t = getStringDynamicAttribute("URL");
		if (t != null) {
			v.addElement(t);
		}
		t = getStringDynamicAttribute("UserName");
		if (t != null) {
			v.addElement(t);
		}
		keywordList = new String[v.size()];
		v.copyInto(keywordList);
	}

	public boolean isExpired() {
		Date ex = getDate("ExpiryTime");
		long et = ex.getTime();
		long ct = new Date().getTime();
		boolean expires = getExpires();
		return (expires && et <= ct);
	}

	public Date getDate(String key) {
		Times t = (Times) getFirstChild("Times");
		return t.getDateDataElement(key);
	}

	public boolean getExpires() {
		Times t = (Times) getFirstChild("Times");
		return t.getBooleanDataElement("Expires");
	}

	public void setDate(String key, Date d) {
		Times t = (Times) getFirstChild("Times");
		t.setDataElement(key, d);
	}

	public void setExpires(boolean expires) {
		Times t = (Times) getFirstChild("Times");
		t.setDataElement("Expires", expires);
	}

	public void setExpired(boolean expires, Date expdate) {
		Times t = (Times) getFirstChild("Times");
		t.setDataElement("Expires", expires);
		t.setDataElement("ExpiryTime", expdate);
	}

	public String toString() {
		return getStringAttribute("Title").getValue();
	}

	public StringAttribute getStringAttribute(String key) {
		int size = childElements.size();
		for (int i = 0; i < size; i++) {
			Object o = childElements.objectAt(i);
			if (!(o instanceof StringAttribute))
				continue;
			StringAttribute sa = (StringAttribute) o;
			if (!key.equals(sa.getKey()))
				continue;
			return sa;
		}
		return null;
	}

	public String getStringDynamicAttribute(String key) {
		StringAttribute sa = getStringAttribute(key);
		if (sa == null)
			return null;
		return sa.getValue();
	}

	public void addStringAttribute(String key, String value) {
		StringAttribute sa = new StringAttribute(key, value);
		childElements.addObject(sa);
	}

	public void deleteStringAttribute(String key) {
		int size = childElements.size();
		for (int i = 0; i < size; i++) {
			Object o = childElements.objectAt(i);
			if (!(o instanceof StringAttribute))
				continue;
			StringAttribute sa = (StringAttribute) o;
			if (key.equals(sa.getKey())) {
				childElements.removeObject(sa);
				return;
			}
		}
	}

	public BinaryAttribute getBinaryAttribute(String key) {
		int size = childElements.size();
		for (int i = 0; i < size; i++) {
			Object o = childElements.objectAt(i);
			if (!(o instanceof BinaryAttribute))
				continue;
			BinaryAttribute sa = (BinaryAttribute) o;
			if (!key.equals(sa.getKey()))
				continue;
			return sa;
		}
		return null;
	}

	public void setDynamicAttribute(String key, String value, boolean isProtected) {
		StringAttribute sa = getStringAttribute(key);
		if (sa == null) {
			sa = new StringAttribute(key, value, isProtected);
			childElements.addObject(sa);
			return;
		}
		sa.setValue(value, isProtected);
	}

	public void setDynamicAttribute(String key, String value) {
		StringAttribute sa = getStringAttribute(key);
		if (sa == null) {
			sa = new StringAttribute(key, value);
			childElements.addObject(sa);
			return;
		}
		sa.setValue(value);
	}

	public void setDynamicAttribute(String key, byte[] value) {
		BinaryAttribute sa = getBinaryAttribute(key);
		if (sa == null) {
			sa = new BinaryAttribute(key, value);
			childElements.addObject(sa);
			return;
		}
		sa.setValue(value);
	}

	static class AutoType extends Kdb4Object {
		public AutoType(Hashtable attributes) {
			super("AutoType", attributes);
		}
	}

	static class Association extends Kdb4Object {
		public Association(Hashtable attributes) {
			super("Association", attributes);
		}
	}

	static class History extends Kdb4Object {
		public History(Hashtable attributes) {
			super("History", attributes);
		}
	}

	public static class StringAttribute extends Kdb4Object {
		public StringAttribute(Hashtable attributes) {
			super("String", attributes);
		}

		public StringAttribute(String key, String value, boolean isProtected) {
			super("String", false);
			AttributeKey k = new AttributeKey(key);
			AttributeValue v = new AttributeValue(value);
			v.setProtected(isProtected);
			childElements.addObject(k);
			childElements.addObject(v);
		}

		public StringAttribute(String key, String value) {
			super("String", false);
			AttributeKey k = new AttributeKey(key);
			AttributeValue v = new AttributeValue(value);
			childElements.addObject(k);
			childElements.addObject(v);
		}

		public String getKey() {
			int size = childElements.size();
			for (int i = 0; i < size; i++) {
				Object o = childElements.objectAt(i);
				if (!(o instanceof AttributeKey))
					continue;
				AttributeKey k = (AttributeKey) o;
				return k.elementContents.toString();
			}
			return null;
		}

		public String getValue() {
			if (childElements == null)
				return null;
			int size = childElements.size();
			for (int i = 0; i < size; i++) {
				Object o = childElements.objectAt(i);
				if (!(o instanceof AttributeValue))
					continue;
				AttributeValue k = (AttributeValue) o;
				if (k.elementContents == null)
					return null;
				return k.elementContents.toString();
			}
			return null;
		}

		public void setValue(String value, boolean isProtected) {
			int size = childElements.size();
			for (int i = 0; i < size; i++) {
				Object o = childElements.objectAt(i);
				if (!(o instanceof AttributeValue))
					continue;
				AttributeValue k = (AttributeValue) o;
				k.setProtected(isProtected);
				k.elementContents = new StringBuffer(value);
			}

		}

		public void setValue(String value) {
			int size = childElements.size();
			for (int i = 0; i < size; i++) {
				Object o = childElements.objectAt(i);
				if (!(o instanceof AttributeValue))
					continue;
				AttributeValue k = (AttributeValue) o;
				k.elementContents = new StringBuffer(value);
			}

		}
	}

	public static class BinaryAttribute extends Kdb4Object {
		public BinaryAttribute(Hashtable attributes) {
			super("Binary", attributes);
		}

		public BinaryAttribute(String key, byte[] value) {
			super("Binary", false);
			AttributeKey k = new AttributeKey(key);
			String s = null;
			try {
				s = Base64OutputStream.encodeAsString(value, 0, value.length,
						false, false);
			} catch (IOException e) {
			}
			AttributeValue v = new AttributeValue(s);
			childElements.addObject(k);
			childElements.addObject(v);
		}

		public String getKey() {
			int size = childElements.size();
			for (int i = 0; i < size; i++) {
				Object o = childElements.objectAt(i);
				if (!(o instanceof AttributeKey))
					continue;
				AttributeKey k = (AttributeKey) o;
				return k.elementContents.toString();
			}
			return null;
		}

		public byte[] getValue() {
			if (childElements == null)
				return null;
			int size = childElements.size();
			for (int i = 0; i < size; i++) {
				Object o = childElements.objectAt(i);
				if (!(o instanceof AttributeValue))
					continue;
				AttributeValue k = (AttributeValue) o;
				if (k.elementContents == null)
					return null;
				try {
					return Base64InputStream.decode(k.elementContents.toString());
				} catch (IOException e) {
				}
			}
			return null;
		}

		public void setValue(byte[] value) {
			int size = childElements.size();
			for (int i = 0; i < size; i++) {
				Object o = childElements.objectAt(i);
				if (!(o instanceof AttributeValue))
					continue;
				AttributeValue k = (AttributeValue) o;
				String s = null;
				try {
					s = Base64OutputStream.encodeAsString(value, 0, value.length,
							false, false);
				} catch (IOException e) {
				}
				k.elementContents = new StringBuffer(s);
			}
		}
	}

	static class AttributeKey extends Kdb4Object {
		public AttributeKey(Hashtable attributes) {
			super("Key", attributes);
		}

		public AttributeKey(String key) {
			super("Key", false);
			elementContents = new StringBuffer(key);
		}
	}

	static class AttributeValue extends Kdb4Object {
		public AttributeValue(Hashtable attributes) {
			super("Value", attributes);
		}

		public AttributeValue(String value) {
			super("Value", false);
			elementContents = new StringBuffer(value);
		}

		public boolean isProtected() {
			return getBooleanElementAttribute("Protected");
		}

		public void setProtected(boolean v) {
			setElementAttribute("Protected", v);
		}
	}
}
