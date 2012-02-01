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
import java.io.OutputStreamWriter;
import java.util.*;

import net.rim.device.api.crypto.MD5Digest;
import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.ui.component.TreeField;
import net.rim.device.api.util.Comparator;
import net.rim.device.api.util.SimpleSortingVector;

import com.fairview5.keepassbb2.Options;
import com.fairview5.keepassbb2.common.crypto.RandomStream;
import com.fairview5.keepassbb2.common.ui.ProgressDialog;
import com.fairview5.keepassbb2.common.util.CommonUtils;

public abstract class Kdb4Object {
	public Kdb4ReadableList childElements = null;
	public Kdb4Hashtable dataElements = null;
	public Hashtable elementAttributes = null;
	public StringBuffer elementContents = null;
	public String elementName = null;
	public Kdb4Object parent;
	public String[] keywordList;

	Kdb4Object(String name) {
		elementName = name;
		childElements = new Kdb4ReadableList();
		dataElements = new Kdb4Hashtable();
		elementAttributes = new Hashtable();
		elementContents = new StringBuffer();
	}

	Kdb4Object(String name, boolean generateUUID) {
		elementName = name;
		dataElements = new Kdb4Hashtable();
		childElements = new Kdb4ReadableList();
		elementAttributes = new Hashtable();

		if (generateUUID) {
			MD5Digest h = new MD5Digest();
			h.update(("com.fairview5.keepassbb2: " + name + " " + System
					.currentTimeMillis()).getBytes());
			String uuid = null;
			try {
				uuid = Base64OutputStream.encodeAsString(h.getDigest(), 0, h
						.getDigestLength(), false, false);
			} catch (IOException e) {
			}
			dataElements.put("UUID", uuid);
		}
	}

	protected Kdb4Object(String name, Hashtable attr) {
		elementName = name;
		elementAttributes = attr;
	}

	public void setDirty() {
		if (parent != null)
			parent.setDirty();
		return;
	}

	public void serialize(OutputStreamWriter out, int indentLevel,
			RandomStream rs) throws IOException {

		if (this instanceof Kdb4Group) {
			ProgressDialog.setProgress("Writing Group: "
					+ this.getStringDataElement("Name"));
		}

		boolean empty = (!hasKids() && !hasDataElements() && !hasContents());

		writeElementStart(out, indentLevel, elementName, empty);
		if (empty) {
			return;
		}

		if (elementContents == null || elementContents.length() == 0) {
			writeString(out, "\r\n");
		} else {
			writeContents(out, rs);
		}
		int kids = writeDataElements(out, indentLevel + 1);
		kids += serializeChildren(out, indentLevel + 1, rs);
		writeElementEnd(out, kids == 0 ? 0 : indentLevel, elementName);
	}

	public int traverse(TreeField tf, int parentNode, boolean sibling) {
		int newParent;
		if (sibling)
			newParent = tf.addSiblingNode(parentNode, this);
		else
			newParent = tf.addChildNode(parentNode, this);

		SimpleSortingVector entries = new SimpleSortingVector();
		entries.setSort(false);
		SimpleSortingVector groups = new SimpleSortingVector();
		groups.setSort(false);

		int size = childElements.size();
		for (int i = 0; i < size; i++) {
			Kdb4Object o = (Kdb4Object) childElements.getAt(i);
			if (o instanceof Kdb4Group)
				groups.addElement(o);
			else if (o instanceof Kdb4Entry)
				entries.addElement(o);
		}

		if (Options.getBooleanOption(Options.OPTION_SORT_TREE, false)) {
			groups.setSortComparator(new Comparator() {
				public int compare(Object obj1, Object obj2) {
					return ((Kdb4Group) obj1).compareTo(((Kdb4Group) obj2));
				}

				public boolean equals(Object obj1) {
					return false;
				}
			});
			groups.reSort();
		}

		int gsize = groups.size();
		int sib = -1;
		for (int i = 0; i < gsize; i++) {
			Kdb4Object o = (Kdb4Object) groups.elementAt(i);
			if (o instanceof Kdb4Group) {
				if (sib == -1)
					sib = ((Kdb4Group) o).traverse(tf, newParent, false);
				else
					sib = ((Kdb4Group) o).traverse(tf, sib, true);
			}
		}
		groups = null;

		if (Options.getBooleanOption(Options.OPTION_SORT_TREE, false)) {
			entries.setSortComparator(new Comparator() {
				public int compare(Object obj1, Object obj2) {
					return ((Kdb4Entry) obj1).compareTo(((Kdb4Entry) obj2));
				}

				public boolean equals(Object obj1) {
					return false;
				}
			});
			entries.reSort();
		}

		for (int i = 0; i < entries.size(); i++) {
			if (sib == -1)
				sib = tf.addChildNode(newParent, entries.elementAt(i));
			else
				sib = tf.addSiblingNode(sib, entries.elementAt(i));
		}
		entries = null;
		return newParent;
	}

	public String getUUID() {
		return this.getStringDataElement("UUID");
	}

	public String getParentUUID() {
		if (parent == null)
			return null;
		return parent.getStringDataElement("UUID");
	}

	public void getEntries(Kdb4EntryList list) {
		if (childElements == null)
			return;
		int kids = childElements.size();
		for (int i = 0; i < kids; i++) {
			Kdb4Object ko = (Kdb4Object) childElements.getAt(i);
			if (ko instanceof Kdb4Entry) {
				list.addElement((Kdb4Entry) ko);
			} else {
				ko.getEntries(list);
			}
		}
	}

	public void getEntries(Kdb4Hashtable list) {
		if (childElements == null)
			return;
		int kids = childElements.size();
		for (int i = 0; i < kids; i++) {
			Kdb4Object ko = (Kdb4Object) childElements.getAt(i);
			if (ko instanceof Kdb4Entry) {
				String uuid = ko.getStringDataElement("UUID");
				list.put(uuid, ko);
			} else {
				ko.getEntries(list);
			}
		}
	}

	public void getEntries(Kdb4ReadableList list) {
		if (childElements == null)
			return;
		int kids = childElements.size();
		for (int i = 0; i < kids; i++) {
			Kdb4Object ko = (Kdb4Object) childElements.getAt(i);
			if (ko instanceof Kdb4Entry) {
				list.addObject(ko);
			} else {
				ko.getEntries(list);
			}
		}
	}

	public void getGroups(Kdb4Hashtable list) {
		if (this instanceof Kdb4Group) {
			list.put(this.getUUID(), this);
		}
		if (childElements == null)
			return;
		int kids = childElements.size();
		for (int i = 0; i < kids; i++) {
			Kdb4Object ko = (Kdb4Object) childElements.getAt(i);
			if (ko instanceof Kdb4Group) {
				ko.getGroups(list);
			}
		}
	}

	public void getGroups(Kdb4ReadableList list) {
		if (this instanceof Kdb4Group) {
			list.addObject(this);
		}
		if (childElements == null)
			return;
		int kids = childElements.size();
		for (int i = 0; i < kids; i++) {
			Kdb4Object ko = (Kdb4Object) childElements.getAt(i);
			if (ko instanceof Kdb4Group) {
				ko.getGroups(list);
			}
		}
	}

	public Kdb4Object getFirstChild(String elementName) {
		return childElements.getFirstObject(elementName);
	}

	public Kdb4ReadableList getChildren(String elementName) {
		return childElements.getObjects(elementName);
	}

	public String getStringElementAttribute(String attributeName) {
		if (elementAttributes == null)
			return null;
		return (String) elementAttributes.get(attributeName);
	}

	public void setStringElementAttribute(String attributeName, String value) {
		if (elementAttributes == null)
			elementAttributes = new Hashtable();
		elementAttributes.put(attributeName, value);
	}

	public String getStringDataElement(String elementName) {
		return (String) dataElements.get(elementName);
	}

	public int getIntDataElement(String elementName) {
		return Integer.parseInt((String) dataElements.get(elementName));
	}

	public boolean getBooleanDataElement(String elementName) {
		return "true".equalsIgnoreCase(((String) dataElements.get(elementName)));
	}

	public Date getDateDataElement(String elementName) {
		String s = (String) dataElements.get(elementName);
		return CommonUtils.parseISOTime(s);
	}

	public void setDataElement(String elementName, Date v) {
		dataElements.put(elementName, CommonUtils.getISOTime(v));
	}

	public void setDataElement(String elementName, String v) {
		dataElements.put(elementName, v);
	}

	public void setDataElement(String elementName, int v) {
		dataElements.put(elementName, v + "");
	}

	public void setDataElement(String elementName, boolean v) {
		dataElements.put(elementName, v ? "True" : "False");
	}

	public boolean getBooleanElementAttribute(String attributeName) {
		String v = ((String) elementAttributes.get(attributeName));
		if (v == null)
			return false;
		return "true".equalsIgnoreCase(v);
	}

	public void setElementAttribute(String attributeName, boolean v) {
		if (elementAttributes == null)
			elementAttributes = new Hashtable();
		elementAttributes.put(attributeName, v ? "True" : "False");
	}

	protected boolean hasKids() {
		return (childElements != null && childElements.size() > 0);
	}

	protected boolean hasDataElements() {
		return (dataElements != null && dataElements.size() > 0);
	}

	protected boolean hasContents() {
		return (elementContents != null && elementContents.length() > 0);
	}

	protected void writeElementStart(OutputStreamWriter out, int level,
			String elementName, boolean empty) throws IOException {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < level; i++)
			sb.append('\t');
		sb.append("<");
		sb.append(elementName);
		if (elementAttributes != null) {
			Enumeration eee = elementAttributes.keys();
			while (eee.hasMoreElements()) {
				String key = (String) eee.nextElement();
				sb.append(" ");
				sb.append(key);
				sb.append("=\"");
				CommonUtils.XMLEncode(sb, (String) elementAttributes.get(key));
				sb.append("\"");
			}
		}
		if (empty) {
			sb.append("/>\r\n");
		} else {
			sb.append(">");
		}
		writeString(out, sb);
	}

	protected void writeElementEnd(OutputStreamWriter out, int level,
			String elementName) throws IOException {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < level; i++)
			sb.append('\t');
		sb.append("</");
		sb.append(elementName);
		sb.append(">\r\n");
		writeString(out, sb);
	}

	protected void writeDataElement(OutputStreamWriter out, int level,
			String elementName, String elementContents) throws IOException {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < level; i++)
			sb.append('\t');
		sb.append("<");
		sb.append(elementName);

		if (elementAttributes != null) {
			Enumeration eee = elementAttributes.keys();
			while (eee.hasMoreElements()) {
				String key = (String) eee.nextElement();
				sb.append(" ");
				sb.append(key);
				sb.append("=\"");
				CommonUtils.XMLEncode(sb, (String) elementAttributes.get(key));
				sb.append("\"");
			}
		}
		if (elementContents == null || elementContents.equals("")) {
			sb.append("/>\r\n");
		} else {
			sb.append(">");
			CommonUtils.XMLEncode(sb, elementContents);
			sb.append("</");
			sb.append(elementName);
			sb.append(">\r\n");
		}
		writeString(out, sb);
	}

	protected void writeContents(OutputStreamWriter out, RandomStream rs)
			throws IOException {
		StringBuffer sb = new StringBuffer();
		if (elementContents != null && elementContents.length() > 0) {
			String s = getStringElementAttribute("Protected");
			if (s != null && s.equalsIgnoreCase("true") && rs != null) {
				byte[] ba = elementContents.toString().getBytes("UTF-8");
				rs.xorBytes(ba, 0, ba.length);
				String b = Base64OutputStream.encodeAsString(ba, 0, ba.length,
						false, false);
				sb.append(b);
			} else {
				CommonUtils.XMLEncode(sb, elementContents);
			}
		}
		writeString(out, sb);
	}

	protected int writeDataElements(OutputStreamWriter out, int indentLevel)
			throws IOException {
		if (!hasDataElements())
			return (0);
		int kids = 0;
		Enumeration eee = dataElements.keys();
		while (eee.hasMoreElements()) {
			String key = (String) eee.nextElement();
			writeDataElement(out, indentLevel, key, (String) dataElements.get(key));
			kids++;
		}
		return kids;
	}

	protected void writeString(OutputStreamWriter out, StringBuffer s)
			throws IOException {
		out.write(s.toString());
	}

	protected void writeString(OutputStreamWriter out, String s)
			throws IOException {
		out.write(s);
	}

	protected int serializeChildren(OutputStreamWriter out, int indentLevel,
			RandomStream rs) throws IOException {
		if (childElements != null) {
			int size = childElements.size();
			for (int i = 0; i < size; i++) {
				childElements.objectAt(i).serialize(out, indentLevel, rs);
			}
			return (size);
		}
		return (0);
	}

	public void parsingFinished() {

	}

	static class Times extends Kdb4Object {
		public Times(Hashtable attr) {
			super("Times", attr);
		}

		public Times() {
			super("Times", false);
		}
	}
}
