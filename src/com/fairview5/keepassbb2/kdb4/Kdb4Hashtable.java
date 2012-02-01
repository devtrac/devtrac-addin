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

public class Kdb4Hashtable extends Hashtable {
	
	public void put(String key, String o) {
		super.put(key,o);
	}
	public Kdb4Object getKdb4Object(String key) {
		return (Kdb4Object) super.get(key);
	}
	public String getString(String key) {
		return (String)super.get(key);
	}

}
