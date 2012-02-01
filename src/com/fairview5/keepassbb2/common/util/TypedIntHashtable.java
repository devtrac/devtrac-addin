package com.fairview5.keepassbb2.common.util;

import net.rim.device.api.util.IntHashtable;
import net.rim.device.api.util.Persistable;

public class TypedIntHashtable extends IntHashtable implements Persistable {
	
	public TypedIntHashtable() {
		super();
	}
	
	public void put(int key, int value) {
		super.put(key, Integer.toString(value));
	}
	public void put(int key, boolean value) {
		super.put(key, value ? "true" : "false");
	}
	public void put(int key, String value) {
		super.put(key, value);
	}
	public void put(int key, double value) {
		super.put(key, Double.toString(value));
	}
	
	public int get(int key, int dflt) {
		Object o = get(key);
		if (o == null) return dflt;
		if (!(o instanceof String)) return dflt;
		try {
			return Integer.parseInt((String)o);
		} catch (Exception e) {
			
		}
		return dflt;
	}
	public boolean get(int key, boolean dflt) {
		Object o = get(key);
		if (o == null) return dflt;
		if (!(o instanceof String)) return dflt;
		if ("true".equals((String)o)) return true;
		if ("false".equals((String)o)) return false;
		return dflt;
	}
	public double get(int key, double dflt) {
		Object o = get(key);
		if (o == null) return dflt;
		if (!(o instanceof String)) return dflt;
		try {
			return Double.parseDouble((String)o);
		} catch (Exception e) {
		}
		return dflt;
	}
	public String get(int key, String dflt) {
		Object o = get(key);
		if (o == null) return dflt;
		if (!(o instanceof String)) return dflt;
		return (String)o;
	}

}
