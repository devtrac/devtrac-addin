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

import java.io.*;
import java.util.Date;
import java.util.Hashtable;

import com.fairview5.keepassbb2.common.crypto.RandomStream;
import com.fairview5.keepassbb2.common.io.IOUtils;

public class Kdb4Database extends Kdb4Object {
	public static Kdb4Database current;
	public boolean dirty;
	
	public Kdb4Database(Hashtable attr) {
		super("KeePassFile", attr);
		current = this;
	}
	
	public void serialize(OutputStream out, int level, RandomStream rs) throws IOException {
		OutputStreamWriter osw = null;
		try {
			osw = new OutputStreamWriter(out, "UTF-8");
			osw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\r\n");
			serialize(osw, level, rs);
		} finally {
			IOUtils.closeWriter(osw);
		}
	}
	
	public Kdb4Database() {
		super("KeePassFile");
		childElements.addObject(new Kdb4Meta());
		childElements.addObject(new Kdb4Root());
	}

	public boolean isDirty() {
		return dirty;
	}
	public void setDirty() {
		dirty = true;
	}
	public void clearDirty() {
		dirty = false;
	}
	
	public Kdb4Root getRoot() {
		for(int i=0;i<childElements.size();i++) {
			Kdb4Object o = (Kdb4Object)childElements.getAt(i);
			if (o instanceof Kdb4Root) return (Kdb4Root)o;
		}
		return null;
	}
	
	public boolean mergeInto(Kdb4Database to) {
		return merge(this,to);
	}
	
	public boolean mergeFrom(Kdb4Database from) {
		return merge(from, this);
	}
	
	public static boolean merge(Kdb4Database from, Kdb4Database to) {
		boolean d1 = mergeGroups(from, to);
		boolean d2 = mergeEntries(from, to);
		return d1 || d2;
	}
	private static boolean mergeEntries(Kdb4Database fromDatabase, Kdb4Database toDatabase) {
		boolean dirty = false;
		Kdb4ReadableList efromlist = new Kdb4ReadableList();
		Kdb4Root r = fromDatabase.getRoot();
		Kdb4Group tg = r.getTopGroup();
		tg.getEntries(efromlist);
		
		Kdb4Hashtable etohash = new Kdb4Hashtable();
		r = toDatabase.getRoot();
		tg = r.getTopGroup();
		tg.getEntries(etohash);
		
		Kdb4Hashtable gto = new Kdb4Hashtable();
		r = toDatabase.getRoot();
		tg = r.getTopGroup();
		tg.getGroups(gto);
		
		
		int size1 = efromlist.size();
		for(int i=0;i<size1;i++) {
			Kdb4Entry efrom = (Kdb4Entry)efromlist.objectAt(i);
			String uuid1 = efrom.getUUID();
			if (etohash.containsKey(uuid1)) {
				Kdb4Entry eto = (Kdb4Entry)etohash.get(uuid1);
				Date fromdate = efrom.getDate("LastModificationTime");
				Date todate = eto.getDate("LastModificationTime");
				long fromt = fromdate.getTime() / 10000;
				long tot = todate.getTime() / 10000;
				if (fromt <= tot) continue;
				eto.parent.childElements.removeObject(eto);
				((Kdb4Group)eto.parent).addEntry(efrom);
				dirty = true;
				continue;
			}
			String uuidparent = efrom.getParentUUID();
			Kdb4Group toparent = (Kdb4Group)gto.get(uuidparent);
			if (toparent == null) continue;
			toparent.addEntry(efrom);
			dirty = true;
		}
		
		return dirty;
	}
	private static boolean mergeGroups(Kdb4Database fromDatabase, Kdb4Database toDatabase) {
		boolean dirty = false;
		
		Kdb4ReadableList gfrom = new Kdb4ReadableList();
		Kdb4Root r = fromDatabase.getRoot();
		Kdb4Group tg = r.getTopGroup();
		tg.getGroups(gfrom);
		
		Kdb4Hashtable gto = new Kdb4Hashtable();
		r = toDatabase.getRoot();
		tg = r.getTopGroup();
		tg.getGroups(gto);
		
		int size1 = gfrom.size();
		for(int i=0;i<size1;i++) {
			Kdb4Group g = (Kdb4Group)gfrom.objectAt(i);
			String uuid1 = g.getUUID();
			if (gto.containsKey(uuid1)) continue;
			String uuidparent = g.getParentUUID();
			Kdb4Group toparent = (Kdb4Group)gto.get(uuidparent);
			if (toparent == null) continue;
			toparent.addGroup(g);
			dirty = true;
			gto.clear();
			toDatabase.getGroups(gto);
		}
		return dirty;
	}
	
}
