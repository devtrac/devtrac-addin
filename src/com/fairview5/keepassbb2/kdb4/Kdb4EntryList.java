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

import net.rim.device.api.collection.util.UnsortedReadableList;

public class Kdb4EntryList extends UnsortedReadableList {

	public Kdb4EntryList() {
	}

	public void addElement(Kdb4Entry ke) {
		doAdd(ke);
	}
	
	public Kdb4Entry getEntry(int index) {
		return (Kdb4Entry)getAt(index);
	}
	
	public String[] getKeywords(Object element) {
		Kdb4Entry ke = (Kdb4Entry)element;
		return ke.keywordList;
	}
}
