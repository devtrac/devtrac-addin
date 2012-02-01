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

public class Kdb4ReadableList extends UnsortedReadableList {

	public void addObject(Kdb4Object ko) {
		doAdd(ko);
	}
	public void removeObject(Kdb4Object ko) {
		doRemove(ko);
	}
	public Kdb4Object objectAt(int i) {
		return (Kdb4Object) super.getAt(i);
	}
	public Object[] elements() {
		return getElements();
	}
	
	public Kdb4Object getFirstObject(String elementName) {
		int s = size();
		for(int i=0;i<s;i++) {
			Kdb4Object ko = objectAt(i);
			if (ko.elementName.equals(elementName)) return ko;
		}
		return null;
	}
	public Kdb4ReadableList getObjects(String elementName) {
		Kdb4ReadableList kl = new Kdb4ReadableList();
		int s = size();
		for(int i=0;i<s;i++) {
			Kdb4Object ko = objectAt(i);
			if (ko.elementName.equals(elementName)) kl.addObject(ko);
		}
		return kl;
	}

}
