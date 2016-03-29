package com.ibra.services.trakingfile.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KeyValueStore
{
	private Map<String, Object> store = new HashMap<String, Object>();

	public KeyValueStore()
	{

	}

	public KeyValueStore(KeyValueStore toBeCopied)
	{
		store.putAll(toBeCopied.store);
	}

	public void put(String key, Object value)
	{
		store.put(key, value);
	}

	public Integer getInt(String key)
	{
		return (Integer) store.get(key);
	}

	public Integer getInt(String key, Integer fallback)
	{
		Integer value = (Integer) store.get(key);
		if (value == null)
		{
			return fallback;
		}
		return value;
	}

	public Double getDouble(String key, Double fallback)
	{
		Double value = (Double) store.get(key);
		if (value == null)
		{
			return fallback;
		}
		return value;
	}

	public Float getFloat(String key, Float fallback)
	{
		Float value = (Float) store.get(key);
		if (value == null)
		{
			return fallback;
		}
		return value;
	}

	public Float parseFloat(String key, Float fallback)
	{
		String value = (String) store.get(key);
		try
		{
			return Float.parseFloat(value);
		}
		catch (Exception e)
		{
			return fallback;
		}
	}

	public Integer parseInteger(String key, Integer fallback)
	{
		String value = (String) store.get(key);
		try
		{
			return Integer.parseInt(value);
		}
		catch (Exception e)
		{
			return fallback;
		}
	}

	public String getString(String key, String fallback)
	{
		String value = (String) store.get(key);
		if (value == null)
		{
			return fallback;
		}
		return value;
	}

	public Object get(String key, Object fallback)
	{
		Object value = (Object) store.get(key);
		if (value == null)
		{
			return fallback;
		}
		return value;
	}

	public Boolean getBoolean(String key, Boolean fallback)
	{
		Boolean value = (Boolean) store.get(key);
		if (value == null)
		{
			return fallback;
		}
		return value;
	}

	public void addInt(String key, int toBeAdded)
	{
		store.put(key, getInt(key) + toBeAdded);
	}

	public void addLong(String key, long toBeAdded)
	{
		store.put(key, getLong(key) + toBeAdded);
	}

	public String getString(String key)
	{
		Object obj = store.get(key);
		if(obj != null) {
			return obj.toString() ;
		}
		return null;
	}

	public Boolean getBoolean(String key)
	{
		return (Boolean) store.get(key);
	}

	public Date getDate(String key)
	{
		return (Date) store.get(key);
	}

	public Long getLong(String key)
	{
		return (Long) store.get(key);
	}

	public Double getDouble(String key)
	{
		return (Double) store.get(key);
	}

	public Float getFloat(String key)
	{
		return (Float) store.get(key);
	}

	public Object get(String key)
	{
		return store.get(key);
	}

	public boolean containsKey(String key)
	{
		return store.containsKey(key);
	}

	public boolean isEmpty()
	{
		return store.isEmpty();
	}

	public Set<String> getAllKeys()
	{
		return store.keySet();
	}

	public Set<Map.Entry<String,Object>> getEntries()
	{
		return store.entrySet();
	}

	public String toString()
	{
		return store.toString();
	}

	public int getSize()
	{
		return store.size();
	}

	public boolean remove(String key)
	{
		return store.remove(key) != null;
	}
}
