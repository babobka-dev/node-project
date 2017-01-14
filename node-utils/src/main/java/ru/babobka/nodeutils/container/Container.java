package ru.babobka.nodeutils.container;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Container {

	private final Map<Class<?>, Object> containerMap = new ConcurrentHashMap<>();

	private Container() {

	}

	private static class SingletonHolder {
		public static final Container HOLDER_INSTANCE = new Container();
	}

	public static Container getInstance() {
		return SingletonHolder.HOLDER_INSTANCE;
	}

	public void put(Object object) {
		containerMap.put(object.getClass(), object);

	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<?> clazz) {
		Object obj = null;
		for (Map.Entry<Class<?>, Object> entry : containerMap.entrySet()) {
			if (clazz.isAssignableFrom(entry.getKey())) {
				obj = entry.getValue();
			}
		}

		return (T) obj;

	}

	public void clear() {
		containerMap.clear();
	}

}
