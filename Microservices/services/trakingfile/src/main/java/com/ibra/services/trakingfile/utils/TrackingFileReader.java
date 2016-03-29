package com.ibra.services.trakingfile.utils;

import java.io.*;
import java.util.Calendar;

public class TrackingFileReader
{
	private BufferedReader br = null;

	public TrackingFileReader(String path, Calendar day, String language)
	{
		try
		{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(path + TrackingFileHelper.getFilename(day.getTime(), language)
					+ ".clean"), CharSetConverter.CHARSET_UTF8));
		}
		catch (FileNotFoundException fnfe)
		{
			throw new RuntimeException(fnfe);
		}
		catch (UnsupportedEncodingException uee)
		{
			throw new RuntimeException(uee);
		}
	}

	public KeyValueStore read() throws IOException
	{
		String line = br.readLine();

		if (line == null)
		{
			return null;
		}

		return processRow(line);
	}

	public static long getTimestamp(String line)
	{
		int index = line.indexOf('|');
		if (index > -1 && index < 20)
		{
			try
			{
				return Long.parseLong(line.substring(0, index));
			}
			catch (NumberFormatException ex)
			{
				ex.printStackTrace();
			}
		}
		return -1;
	}

	public static KeyValueStore processRow(String line)
	{
		String[] split = line.split("\\|");

		KeyValueStore map = new KeyValueStore();

		map.put(DefaultKeys.TIMESTAMP, Long.valueOf(split[0]));
		map.put(DefaultKeys.SESSION, split[1]);
		map.put(DefaultKeys.URL, split[2]);
		map.put(DefaultKeys.NEW_VISIT, split[4].contains("true"));

		for (int i = 3; i < split.length; i++)
		{
			if (i == 4)
			{
				//'new_visit' is in position 4, already read. We start with 3 so that we don't skip 'elapsed', in position 3
				continue;
			}
			String keyValue = split[i];
			String[] keyValueSplit = keyValue.split("=");
			String key = keyValueSplit[0];
			String value = keyValueSplit.length > 1 ? keyValueSplit[1] : "";
			value = value.replace('~', '=').replace('#', '|');
			if (value.contains("href"))
			{
				StringBuilder sb = new StringBuilder();
				String[] links = value.split("</a>");
				for (String link : links)
				{
					if (link.contains("href"))
					{
						link = link.replaceAll("\"?>", "\">");
						sb.append(link + "</a>");
					}
					else
					{
						sb.append(link);
					}
				}
				value = sb.toString();
			}
			// ignore reserved parameters as they should not be overwritten
			if (isReservedParameter(key))
			{
				continue;
			}

			map.put(key, value);
		}

		return map;
	}

	private static boolean isReservedParameter(String key)
	{
		if (StringTools.isNullOrEmpty(key))
		{
			return false;
		}
		return key.equals(DefaultKeys.TIMESTAMP) || key.equals(DefaultKeys.SESSION) || key.equals(DefaultKeys.URL)
				|| key.equals(DefaultKeys.NEW_VISIT);
	}
}