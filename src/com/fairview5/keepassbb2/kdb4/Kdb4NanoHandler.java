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
import java.util.Hashtable;
import java.util.Stack;

import net.rim.device.api.io.Base64InputStream;

import com.fairview5.keepassbb2.common.crypto.*;
import com.fairview5.keepassbb2.common.ui.ProgressDialog;
import com.fairview5.keepassbb2.common.util.CommonUtils;
import com.fairview5.keepassbb2.common.xml.XMLNanoHandler;

public class Kdb4NanoHandler implements XMLNanoHandler {

	public Kdb4Database kdb4;
	Stack objects = new Stack();
	StringBuffer elementContents;
	RandomStream rs = null;
	long startTime = 0;

	static final int HASH_ROOT = 0;
	static final int HASH_GROUP = 1;
	static final int HASH_ENTRY = 2;
	static final int HASH_META = 3;
	static final int HASH_HISTORY = 4;
	static final int HASH_AUTOTYPE = 5;
	static final int HASH_STRING = 6;
	static final int HASH_BINARY = 7;
	static final int HASH_KEY = 8;
	static final int HASH_VALUE = 9;
	static final int HASH_DELETEDOBJECTS = 10;
	static final int HASH_DELETEDOBJECT = 11;
	static final int HASH_TIMES = 12;
	static final int HASH_MEMORYPROTECTION = 13;
	static final int HASH_ASSOCIATION = 14;
	static final int HASH_CUSTOMICONS = 15;
	static final int HASH_ICON = 16;
	static final int HASH_KEEPASSFILE = 17;
	static final int HASH_BINARIES = 18;

	static final int[] hashes = new int[] { "Root".hashCode(),
			"Group".hashCode(), "Entry".hashCode(), "Meta".hashCode(),
			"History".hashCode(), "AutoType".hashCode(), "String".hashCode(),
			"Binary".hashCode(), "Key".hashCode(), "Value".hashCode(),
			"DeletedObjects".hashCode(), "DeletedObject".hashCode(),
			"Times".hashCode(), "MemoryProtection".hashCode(),
			"Association".hashCode(), "CustomIcons".hashCode(), "Icon".hashCode(),
			"KeePassFile".hashCode(), "Binaries".hashCode(), };

	public Kdb4NanoHandler(int randomStreamID, byte[] protectedStreamKey) {
		if (protectedStreamKey == null)
			return;
		switch (randomStreamID) {
		case RandomStream.CRYPTO_RANDOM_NONE:
			return;
		case RandomStream.CRYPTO_RANDOM_ARC4:
			rs = new Arc4RandomStream(protectedStreamKey);
			return;
		case RandomStream.CRYPTO_RANDOM_SALSA20:
			rs = new Salsa20RandomStream(protectedStreamKey);
			return;
		}
		ProgressDialog.setLegend("Decrypting and Parsing...");
	}

	public Kdb4Database getDatabase() {
		return kdb4;
	}

	public void startDocument() {
		ProgressDialog.setLegend("Decrypting and parsing...");
		ProgressDialog.setProgress("Loading first group");
		startTime = System.currentTimeMillis();
	}

	public void startElement(String localName, Hashtable attributes) {

		int hash = localName.hashCode();

		if (hash == hashes[HASH_KEEPASSFILE]) {
			objects.push(new Kdb4Database(attributes));
			kdb4 = (Kdb4Database) objects.peek();
			return;
		}
		if (hash == hashes[HASH_ROOT]) {
			objects.push(new Kdb4Root(attributes));
			return;
		}
		if (hash == hashes[HASH_GROUP]) {
			objects.push(new Kdb4Group(attributes));
			return;
		}
		if (hash == hashes[HASH_ENTRY]) {
			objects.push(new Kdb4Entry(attributes));
			return;
		}
		if (hash == hashes[HASH_META]) {
			objects.push(new Kdb4Meta(attributes));
			return;
		}
		if (hash == hashes[HASH_HISTORY]) {
			objects.push(new Kdb4Entry.History(attributes));
			return;
		}
		if (hash == hashes[HASH_AUTOTYPE]) {
			objects.push(new Kdb4Entry.AutoType(attributes));
			return;
		}
		if (hash == hashes[HASH_DELETEDOBJECTS]) {
			objects.push(new Kdb4DeletedObjects(attributes));
			return;
		}
		if (hash == hashes[HASH_DELETEDOBJECT]) {
			objects.push(new Kdb4DeletedObjects.DeletedObject(attributes));
			return;
		}
		if (hash == hashes[HASH_STRING]) {
			objects.push(new Kdb4Entry.StringAttribute(attributes));
			return;
		}
		if (hash == hashes[HASH_KEY]) {
			objects.push(new Kdb4Entry.AttributeKey(attributes));
			return;
		}
		if (hash == hashes[HASH_VALUE]) {
			objects.push(new Kdb4Entry.AttributeValue(attributes));
			return;
		}
		if (hash == hashes[HASH_BINARY]) {
			if (!objects.empty() && objects.peek() instanceof Kdb4Entry) {
				objects.push(new Kdb4Entry.BinaryAttribute(attributes));
			}	else {
				objects.push(new Kdb4Meta.Binary(attributes));
			}
			return;
		}
		if (hash == hashes[HASH_MEMORYPROTECTION]) {
			objects.push(new Kdb4Meta.MemoryProtection(attributes));
			return;
		}
		if (hash == hashes[HASH_TIMES]) {
			objects.push(new Kdb4Object.Times(attributes));
			return;
		}
		if (hash == hashes[HASH_ASSOCIATION]) {
			objects.push(new Kdb4Entry.Association(attributes));
			return;
		}
		if (hash == hashes[HASH_CUSTOMICONS]) {
			objects.push(new Kdb4Meta.CustomIcons(attributes));
			return;
		}
		if (hash == hashes[HASH_ICON]) {
			objects.push(new Kdb4Meta.Icon(attributes));
			return;
		}
		if (hash == hashes[HASH_BINARIES]) {
			objects.push(new Kdb4Meta.Binaries(attributes));
			return;
		}
	}

	public void characters(char[] arg0, int arg1, int arg2) {
		if (elementContents == null)
			elementContents = new StringBuffer();
		elementContents.append(arg0, arg1, arg2);
	}

	public void endDocument() {
		long endTime = System.currentTimeMillis();
		double diff = (endTime - startTime) / 1000.0;
		CommonUtils.logger("Parse time: " + diff);
	}

	public void endElement(String localName) {

		int hash = localName.hashCode();
		boolean isKnownGroup = false;
		for (int i = 0; i < hashes.length; i++) {
			if (hash == hashes[i]) {
				isKnownGroup = true;
				break;
			}
		}

		if (isKnownGroup) {
			Kdb4Object ko = (Kdb4Object) objects.pop();
			ko.elementContents = elementContents;

			if (ko instanceof Kdb4Group) {
				ProgressDialog.setProgress("Loaded Group: "
						+ ko.getStringDataElement("Name"));
			}

			if (rs != null && ko.elementName.equals("Value")
					&& ko.elementAttributes != null) {
				String s = (String) ko.elementAttributes.get("Protected");
				if (s != null && s.equalsIgnoreCase("True")
						&& ko.elementContents != null) {
					try {
						byte[] ba = Base64InputStream.decode(elementContents
								.toString());
						rs.xorBytes(ba, 0, ba.length);
						String s2 = new String(ba, "UTF-8");
						ko.elementContents = new StringBuffer();
						ko.elementContents.append(s2);

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (!objects.empty()) {
				Kdb4Object parent = (Kdb4Object) objects.peek();
				if (parent.childElements == null)
					parent.childElements = new Kdb4ReadableList();
				parent.childElements.addObject(ko);
				ko.parent = parent;
			}
			ko.parsingFinished();
			elementContents = null;
			return;
		}
		Kdb4Object ko = (Kdb4Object) objects.peek();
		if (ko.dataElements == null)
			ko.dataElements = new Kdb4Hashtable();
		if (elementContents != null) {
			ko.dataElements.put(localName, elementContents.toString());
			elementContents = null;
		} else {
			ko.dataElements.put(localName, "");
		}
	}
}
