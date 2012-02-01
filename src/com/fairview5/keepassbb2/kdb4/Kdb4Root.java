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

import java.util.Hashtable;

public class Kdb4Root extends Kdb4Object {
	public Kdb4Root(Hashtable attributes) {
		super("Root", attributes);
	}
	public Kdb4Root() {
		super("Root");

		Kdb4Group pg = addGroup("NewDatabase", 49);
		pg.addNewGroup("General", 48);
		pg.addNewGroup("Windows", 38);
		pg.addNewGroup("Network", 3);
		pg.addNewGroup("Internet", 1);
		pg.addNewGroup("eMail", 19);
		pg.addNewGroup("Homebanking", 37);
	}
	
	public Kdb4Group addGroup(String name, int iconid) {
		Kdb4Group g = new Kdb4Group(name, iconid); 		
		g.parent = this;
		childElements.addObject(g);
		setDirty();
		return g;
	}
	public Kdb4Group getTopGroup() {
		for(int i=0;i<childElements.size();i++) {
			Kdb4Object o = childElements.objectAt(i);
			if (o instanceof Kdb4Group) return (Kdb4Group)o;
		}
		return null;
	}
	public Kdb4DeletedObjects getDeletedObjects() {
		for(int i=0;i<childElements.size();i++) {
			Kdb4Object o = childElements.objectAt(i);
			if (o instanceof Kdb4DeletedObjects) return (Kdb4DeletedObjects)o;
		}
		return null;
	}
	
}
