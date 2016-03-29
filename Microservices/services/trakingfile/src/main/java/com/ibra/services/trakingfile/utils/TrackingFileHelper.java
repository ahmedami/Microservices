package com.ibra.services.trakingfile.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TrackingFileHelper
{
	private TrackingFileHelper()
	{
		// No instances shall be created.
	}

	public static String getFilename(Date date, String language)
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(date) + "-new-tracking" + "." + language + ".txt";
	}
	
	public static String getFilename(LocalDate localDate, String language)
	{
		String formattedDate = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		return formattedDate + "-new-tracking" + "." + language + ".txt";
	}
	
	public static String getSessionFilename(Date date, String language)
	{
		return getFilename(date, language) + ".session";
	}

	public static String getArchiveFilename(Date date, String language)
	{
		return getFilename(date, language) + ".gz";
	}

	public static String getTodaysTrackingLogFilename(String basePath, String language)
	{
		return basePath + "/" + getFilename(new Date(), language);
	}
}
