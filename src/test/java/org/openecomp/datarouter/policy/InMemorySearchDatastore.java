package org.openecomp.datarouter.policy;

import java.util.concurrent.ConcurrentHashMap;

public final class InMemorySearchDatastore {

	private final static ConcurrentHashMap<String, String> documents = new ConcurrentHashMap<String, String>();

	public static ConcurrentHashMap<String, String> getAll() {
		return documents;
	}

	public static void put(String key, String value) {
		documents.put(key, value);
	}

	public static String get(String key) {
		return documents.get(key);
	}

	public static void remove(String key) {
		documents.remove(key);
	}
}
