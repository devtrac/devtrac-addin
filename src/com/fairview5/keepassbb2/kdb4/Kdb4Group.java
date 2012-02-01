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

import java.util.Date;
import java.util.Hashtable;

public class Kdb4Group extends Kdb4Object {
	public Kdb4Group(Hashtable attributes) {
		super("Group", attributes);
	}

	public Kdb4Group() {
		super("Group", true);
		Kdb4Object.Times times = new Kdb4Object.Times();
		this.setDataElement("IconID", 48);
		Date ct = new Date();
		times.setDataElement("LastModificationTime", ct);
		times.setDataElement("CreationTime", ct);
		times.setDataElement("LastAccessTime", ct);
		times.setDataElement("ExpiryTime", ct);
		times.setDataElement("UsageCount", 0);
		times.setDataElement("Expires", false);
		childElements.addObject(times);
	}

	public Kdb4Group(String name, int iconid) {
		this();
		setDataElement("Name", name);
		setDataElement("IconID", iconid);
	}

	public Kdb4Group addNewGroup(String name, int iconid) {
		Kdb4Group g = new Kdb4Group(name, iconid);
		g.parent = this;
		childElements.addObject(g);
		setDirty();
		return g;
	}

	public void addEntry(Kdb4Entry e) {
		e.parent = this;
		childElements.addObject(e);
		setDirty();
	}

	public void addGroup(Kdb4Group g) {
		g.parent = this;
		childElements.addObject(g);
		setDirty();
	}

	public String toString() {
		return getStringDataElement("Name");
	}

	public Date getDate(String key) {
		Times t = (Times) getFirstChild("Times");
		return t.getDateDataElement(key);
	}

	public void setDate(String key, Date d) {
		Times t = (Times) getFirstChild("Times");
		t.setDataElement(key, d);
	}

	public int compareTo(Kdb4Group obj2) {
		String n1 = (String) dataElements.get("Name");

		String n2 = (String) obj2.dataElements.get("Name");

		return n1.compareTo(n2);
	}

}
