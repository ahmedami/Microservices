package com.ibra.services.trakingfile.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CharSetConverter
{
	public static final Charset UTF8 = StandardCharsets.UTF_8;
	public static final String CHARSET_UTF8 = UTF8.name();

	public static String convertToCharset(String orig, String charSet)
	{
		if (orig == null || charSet == null)
		{
			return orig;
		}
		try
		{
			return new String(orig.getBytes(charSet), charSet);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return orig;
	}

	public static String convertToUTF8(String orig)
	{
		return convertToCharset(orig, CHARSET_UTF8);
	}
}