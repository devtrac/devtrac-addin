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

public class Kdb4Meta extends Kdb4Object {
	public Kdb4Meta(Hashtable attributes) {
		super("Meta", attributes);
	}
	public Kdb4Meta() {
		super("Meta");
		this.setDataElement("LastSelectedGroup", "AAAAAAAAAAAAAAAAAAAAAA==");
		this.setDataElement("Generator", "KeePassBB2");
		this.setDataElement("DefaultUserName", "");
		this.setDataElement("MaintenanceHistoryDays", 0);
		this.setDataElement("DatabaseName", "");
		this.setDataElement("DatabaseDescription", "");
		this.setDataElement("LastTopVisibleGroup", "AAAAAAAAAAAAAAAAAAAAAA==");
		this.setDataElement("LastModified", new Date());
		MemoryProtection mp = new MemoryProtection();
		mp.setDataElement("AutoEnableVisualHiding", false);
		mp.setDataElement("ProtectUserName", false);
		mp.setDataElement("ProtectTitle", false);
		mp.setDataElement("ProtectPassword", true);
		mp.setDataElement("ProtectURL", false);
		mp.setDataElement("ProtectNotes", false);
		childElements.addObject(mp);
	}
	
	static class MemoryProtection extends Kdb4Object {
		public MemoryProtection(Hashtable attributes) {
			super("MemoryProtection", attributes);
		}
		public MemoryProtection() {
			super("MemoryProtection");
		}
	}
	static class CustomIcons extends Kdb4Object {
		public CustomIcons(Hashtable attributes) {
			super("CustomIcons", attributes);
		}
	}
	static class Icon extends Kdb4Object {
		public Icon(Hashtable attributes) {
			super("Icon", attributes);
		}
	}
	
	static class Binaries extends Kdb4Object {
		public Binaries(Hashtable attributes) {
			super("Binaries", attributes);
		}
	}
	static class Binary extends Kdb4Object {
		public Binary(Hashtable attributes) {
			super("Binary", attributes);
		}
	}
	
	
	
	
}
