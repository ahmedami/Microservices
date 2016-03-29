package com.ibra.services.trakingfile;

import com.ibra.services.trakingfile.utils.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

@Service
public class TrackingFileService
{
	private static final Log LOGGER = LogFactory.getLog(TrackingFileController.class);
	private static final int MAX_ITEMS_PER_PAGE = 5000;
	private static final int DEFAULT_ITEMS_PER_PAGE = 200;
	private static final String MAX_ITEMS_PER_PAGE_STR = Integer.toString(MAX_ITEMS_PER_PAGE);
	private static final String OUTPUT_CSV = "csv";
	private static final OutputWritter htmlWriter = new HtmlOutputWritter();
	private static final OutputWritter csvWriter = new CsvOutputWritter();
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Autowired
	private VirtualFileSystem virtualFileSystem;

	public String buildHTML(HttpServletRequest request, String country) throws FileNotFoundException
	{
		StringBuilder out = new StringBuilder();//new PrintWriter("C:\\Users\\atewelde\\Desktop\\notes.txt\\");
		OutputWritter writer;
		int dateFlag = 0;
		LocalDateTime now = LocalDateTime.now();

		LocalDate date = null;
		if (request.getParameter("year") != null && request.getParameter("month") != null && request.getParameter("day") != null)
		{
			if (!isDateValid(Integer.parseInt(request.getParameter("year")), Integer.parseInt(request.getParameter("month")),
				Integer.parseInt(request.getParameter("day"))))
			{
				dateFlag = 1;
				date = LocalDate.now();
			}
			else
			{
				date = LocalDate.of(toInt(request.getParameter("year"), now.getYear()), toInt(request.getParameter("month"), now.getMonthValue()),
					toInt(request.getParameter("day"), now.getDayOfMonth()));
			}

		}
		else
		{
			date = LocalDate.of(toInt(request.getParameter("year"), now.getYear()), toInt(request.getParameter("month"), now.getMonthValue()),
				toInt(request.getParameter("day"), now.getDayOfMonth()));
		}
		LocalTime startTime = LocalTime.of(toInt(request.getParameter("start"), 0), toInt(request.getParameter("startminute"), 0));
		LocalTime endTime = LocalTime.of(toInt(request.getParameter("end"), 23), toInt(request.getParameter("endminute"), 59));

		LocalDateTime start = LocalDateTime.of(date, startTime);
		LocalDateTime end = LocalDateTime.of(date, endTime);

		String columnKeys = request.getParameter("columns");
		String offsetStr = request.getParameter("offset");
		String search = "";//request.getParameter("search");

		boolean simpleSearch = "true".equals(request.getParameter("simpleSearch"));
		boolean regexSearch = "true".equals(request.getParameter("allowRegex"));
		boolean exactSearch = "true".equals(request.getParameter("exactSearch"));

		String itemsPerPageStr = request.getParameter("itemsPerPage");
		writer = getOutputWritter(request.getParameter("output"));
		String trackingFilePath = "C:\\visualMeta\\tracking\\2015-03-01-new-tracking.de.txt";
		BufferedReader reader = null;
		InputStream is = null;
		int offset = 0;
		if (offsetStr == null)
		{
			offset = 0;
		}
		else
		{
			try
			{
				offset = Integer.parseInt(offsetStr);
			}
			catch (NumberFormatException e)
			{
				offset = 0;
			}
		}
		int itemsPerPage = toInt(itemsPerPageStr, DEFAULT_ITEMS_PER_PAGE);
		if (new File(trackingFilePath).exists())
		{
			is = new FileInputStream(trackingFilePath);
		}
		try
		{
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			writer.write(out, reader, date, start, end, offset, columnKeys, itemsPerPage, search, simpleSearch, regexSearch, exactSearch);
		}
		catch (IOException ex)
		{
			LOGGER.error(ex.getMessage(), ex);
			writer.writeError(out, date, start, end, columnKeys, itemsPerPage, search, simpleSearch, regexSearch, exactSearch,
				"Unable to process the tracking file.");
		}
		finally
		{
			IOHelper.close(reader);
		}
		if (request.getParameter("day") != null && dateFlag == 0)
		{
			//int itemsPerPage = toInt(itemsPerPageStr, DEFAULT_ITEMS_PER_PAGE);
			if (itemsPerPage < 1)
			{
				itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
			}
			else if (itemsPerPage > MAX_ITEMS_PER_PAGE)
			{
				itemsPerPage = MAX_ITEMS_PER_PAGE;
			}

			//int offset;
			if (offsetStr == null)
			{
				offset = 0;
			}
			else
			{
				try
				{
					offset = Integer.parseInt(offsetStr);
				}
				catch (NumberFormatException e)
				{
					offset = 0;
				}
			}

			if ((end.getHour() == 0 && end.getMinute() == 0) || end.isBefore(start))
			{
				end = start.plus(1, ChronoUnit.DAYS);
			}

			//			BufferedReader reader = null;
			//			InputStream is = null;

			//String trackingFilePath = properties.getFrontendRequestTrackingDir() + '/' + TrackingFileHelper.getFilename(date, country);
			//String trackingFilePath = "C:\\visualMeta\\tracking\\2015-03-01-new-tracking.de.txt";// + TrackingFileHelper.getFilename(date, country);
			try
			{
				if (new File(trackingFilePath).exists())
				{
					is = new FileInputStream(trackingFilePath);
				}
				else if (new File(trackingFilePath + ".gz").exists())
				{
					is = new GZIPInputStream(new FileInputStream(trackingFilePath + ".gz"));
				}
				else if (virtualFileSystem.isHdfsEnabled())
				{
					trackingFilePath = trackingFilePath.replaceAll(":", "");

					if (virtualFileSystem.exists(trackingFilePath))
					{
						is = virtualFileSystem.openInputStream(trackingFilePath);
					}
					else if (virtualFileSystem.exists(trackingFilePath + ".gz"))
					{
						is = new GZIPInputStream(virtualFileSystem.openInputStream(trackingFilePath + ".gz"));
					}
				}
			}
			catch (FileNotFoundException ex)
			{
				LOGGER.error(ex.getMessage(), ex);
				writer.writeError(out, date, start, end, columnKeys, itemsPerPage, search, simpleSearch, regexSearch, exactSearch,
					"Unable to process the tracking file.");
				return null;
			}
			catch (IOException ex)
			{
				LOGGER.error(ex.getMessage(), ex);
				writer.writeError(out, date, start, end, columnKeys, itemsPerPage, search, simpleSearch, regexSearch, exactSearch,
					"Unable to process the tracking file.");
				return null;
			}

			if (is == null)
			{
				writer.writeError(out, date, start, end, columnKeys, itemsPerPage, search, simpleSearch, regexSearch, exactSearch,
					"Unable to find a tracking file with the name '" + trackingFilePath + "'.");
			}
			else
			{
				try
				{
					reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
					writer.write(out, reader, date, start, end, offset, columnKeys, itemsPerPage, search, simpleSearch, regexSearch, exactSearch);
				}
				catch (IOException ex)
				{
					LOGGER.error(ex.getMessage(), ex);
					writer.writeError(out, date, start, end, columnKeys, itemsPerPage, search, simpleSearch, regexSearch, exactSearch,
						"Unable to process the tracking file.");
				}
				finally
				{
					IOHelper.close(reader);
				}
			}
		}
		else if (dateFlag == 1)
		{
			dateFlag = 0;
			writer.writeError(out, date, start, end, columnKeys, DEFAULT_ITEMS_PER_PAGE, search, simpleSearch, regexSearch, exactSearch,
				"The selected date '" + Integer.parseInt(request.getParameter("day")) + "-" + Integer.parseInt(request.getParameter("month")) + "-"
						+ Integer.parseInt(request.getParameter("year")) + "' is not valid.");
		}
		//		else
		//		{
		//			writer.writeError(out, date, start, end, columnKeys, DEFAULT_ITEMS_PER_PAGE, search, simpleSearch, regexSearch, exactSearch, null);
		//		}
		//out.flush();
		return out.toString();
	}
	/*
	* Checking Validity of the Date
	* @param int year , int month , int day
	* @return boolean ,, is date is valid or not
	*/
	public static boolean isDateValid(int year, int month, int day)
	{
		boolean dateIsValid = true;
		try
		{
			LocalDate.of(year, month, day);
		}
		catch (DateTimeException e)
		{
			dateIsValid = false;
		}
		return dateIsValid;
	}

	private static int toInt(String value, int fallback)
	{
		try
		{
			return (value == null) ? fallback : Integer.parseInt(value);
		}
		catch (NumberFormatException e)
		{
			return fallback;
		}
	}

	private OutputWritter getOutputWritter(String output)
	{
		if (OUTPUT_CSV.equals(output))
		{
			return csvWriter;
		}
		return htmlWriter;
	}

	private static int getRowsToDisplay(BufferedReader reader, StringBuilder out, List<String> order, RowWritter writter, String needle,
			int rowsToSkip, long start, long end, int itemsPerPage, Map<String, Integer> keyDistribution, List<KeyValueStore> rowsToDisplay, int limit)
			throws IOException
	{
		LinkedList<Pattern> patterns = new LinkedList<Pattern>();
		String[] needles = needle.split("\\|");
		String[] tmpStrArr;
		for (int i = 0; i < needles.length; i++)
		{
			if (!(needles[i] = needles[i].trim()).isEmpty())
			{
				tmpStrArr = needles[i].split("=", 2);
				if (tmpStrArr.length > 1)
				{
					//(\|new_visit=[^|]*false[^|]*)
					patterns.add(Pattern.compile("(\\|" + Pattern.quote(tmpStrArr[0].trim()) + "=[^|]*" + Pattern.quote(tmpStrArr[1].trim())
							+ "[^|]*\\|)", Pattern.CASE_INSENSITIVE));
				}
				else
				{
					//(=[^|=]*page[^|=]*\|)|(\|[^|=]*14[^|=]*\|)
					patterns.add(Pattern.compile("([=|][^|=]*" + Pattern.quote(needles[i]) + "[^|=]*\\|)", Pattern.CASE_INSENSITIVE));
				}
			}
		}
		String currentRow;
		int foundRows = 0;
		boolean found;
		long timeStamp;
		while ((currentRow = reader.readLine()) != null && foundRows < limit)
		{
			timeStamp = TrackingFileReader.getTimestamp(currentRow);
			if (timeStamp >= end)
			{
				break;
			}
			else if (timeStamp > -1)
			{
				found = true;
				for (Pattern pattern : patterns)
				{
					if (!pattern.matcher(currentRow).find())
					{
						found = false;
						break;
					}
				}
				if (found)
				{
					addRow(out, order, writter, rowsToDisplay, currentRow, rowsToSkip, foundRows, itemsPerPage, keyDistribution);
					foundRows++;
				}
			}
		}
		return foundRows;

	}

	private static int getRowsToDisplayRegex(BufferedReader reader, StringBuilder out, List<String> order, RowWritter writter, String needle,
			int rowsToSkip, long start, long end, int itemsPerPage, Map<String, Integer> keyDistribution, List<KeyValueStore> rowsToDisplay, int limit)
			throws IOException
	{
		ArrayList<NumPattern> fieldPatternsList = new ArrayList<NumPattern>();
		ArrayList<KeyPattern> keyFieldPatternsList = new ArrayList<KeyPattern>();
		String[] needles = needle.split("\\|");
		String[] tmpStrArr;
		for (int i = 0; i < needles.length; i++)
		{
			if (!(needles[i] = needles[i].trim()).isEmpty())
			{
				tmpStrArr = needles[i].split("=", 2);
				if (tmpStrArr.length > 1)
				{
					keyFieldPatternsList.add(new KeyPattern(tmpStrArr[0].trim(), Pattern.compile(tmpStrArr[1].trim(), Pattern.CASE_INSENSITIVE)));
				}
				else
				{
					fieldPatternsList.add(new NumPattern(Pattern.compile(needles[i].trim(), Pattern.CASE_INSENSITIVE)));
				}
			}
		}
		NumPatternList fieldPatterns = (fieldPatternsList.isEmpty()) ? new NumPatternList() : new NumPatternList(
			fieldPatternsList.toArray(new NumPattern[fieldPatternsList.size()]));
		KeyPattern[] keyFieldPatterns = (keyFieldPatternsList.isEmpty()) ? new KeyPattern[0] : keyFieldPatternsList
			.toArray(new KeyPattern[keyFieldPatternsList.size()]);
		String currentRow;
		int foundRows = 0;
		long timeStamp;
		while ((currentRow = reader.readLine()) != null && foundRows < limit)
		{
			timeStamp = TrackingFileReader.getTimestamp(currentRow);
			if (timeStamp > end)
			{
				break;
			}
			else if (timeStamp >= start)
			{
				if (addRow(out, order, writter, rowsToDisplay, currentRow, rowsToSkip, foundRows, itemsPerPage, keyDistribution, fieldPatterns,
					keyFieldPatterns))
				{
					foundRows++;
				}
			}
		}
		return foundRows;
	}

	private static int getRowsToDisplaySimple(BufferedReader reader, StringBuilder out, List<String> order, RowWritter writter, String needle,
			int rowsToSkip, long start, long end, int itemsPerPage, Map<String, Integer> keyDistribution, List<KeyValueStore> rowsToDisplay, int limit)
			throws IOException
	{
		String[] needles = needle.split("\\|");
		for (int i = 0; i < needles.length; i++)
		{
			needles[i] = needles[i].trim();
		}
		String currentRow;
		int foundRows = 0;
		boolean found;
		long timeStamp;
		while ((currentRow = reader.readLine()) != null && foundRows < limit)
		{
			timeStamp = TrackingFileReader.getTimestamp(currentRow);
			if (timeStamp > end)
			{
				break;
			}
			else if (timeStamp >= start)
			{
				found = true;
				for (String str : needles)
				{
					if (!currentRow.contains(str))
					{
						found = false;
						break;
					}
				}
				if (found)
				{
					addRow(out, order, writter, rowsToDisplay, currentRow, rowsToSkip, foundRows, itemsPerPage, keyDistribution);
					foundRows++;
				}
			}
		}
		return foundRows;

	}

	private static int getRowsToDisplayExact(BufferedReader reader, StringBuilder out, List<String> order, RowWritter writter, String needle,
			int rowsToSkip, long start, long end, int itemsPerPage, Map<String, Integer> keyDistribution, List<KeyValueStore> rowsToDisplay, int limit)
			throws IOException
	{
		String[] needles = needle.split("\\|");
		for (int i = 0; i < needles.length; i++)
		{
			needles[i] = needles[i].trim();
			needles[i] = (needles[i].contains("=") ? "|" : "=") + trimAround(needles[i], "=") + "|";
		}

		String currentRow;
		int foundRows = 0;
		boolean found;
		long timeStamp;
		while ((currentRow = reader.readLine()) != null && foundRows < limit)
		{
			timeStamp = TrackingFileReader.getTimestamp(currentRow);
			if (timeStamp > end)
			{
				break;
			}
			else if (timeStamp >= start)
			{
				found = true;
				for (String searchTerm : needles)
				{
					if (!currentRow.contains(searchTerm))
					{
						found = false;
						break;
					}
				}
				if (found)
				{
					addRow(out, order, writter, rowsToDisplay, currentRow, rowsToSkip, foundRows, itemsPerPage, keyDistribution);
					foundRows++;
				}
			}
		}
		return foundRows;
	}

	private static int writeGetRows(BufferedReader reader, StringBuilder out, List<String> order, RowWritter writter,
			Map<String, Integer> keyDistribution, List<KeyValueStore> rowsToDisplay, String searchQuery, int offset, Instant startInstant,
			Instant endInstant, int itemsPerPage, final boolean simpleSearch, final boolean regexSearch, boolean exactSearch) throws IOException
	{
		return writeGetRows(reader, out, order, writter, keyDistribution, rowsToDisplay, searchQuery, offset, startInstant, endInstant, itemsPerPage,
			simpleSearch, regexSearch, exactSearch, Integer.MAX_VALUE);
	}

	private static int writeGetRows(BufferedReader reader, StringBuilder out, List<String> order, RowWritter writter,
			Map<String, Integer> keyDistribution, List<KeyValueStore> rowsToDisplay, String searchQuery, int offset, Instant startInstant,
			Instant endInstant, int itemsPerPage, final boolean simpleSearch, final boolean regexSearch, boolean exactSearch, int limit)
			throws IOException
	{
		long startInstantEpoch = startInstant.toEpochMilli();
		long endInstantEpoch = endInstant.toEpochMilli();

		if (endInstantEpoch == 0)
		{
			endInstantEpoch = Long.MAX_VALUE;
		}
		try
		{
			searchQuery = URLDecoder.decode(searchQuery, "UTF-8");
		}
		catch (UnsupportedEncodingException ex)
		{
			LOGGER.error(ex.getMessage(), ex);
		}
		int rowsToSkip = itemsPerPage * offset;
		int foundRows;
		if (simpleSearch)
		{
			foundRows = getRowsToDisplaySimple(reader, out, order, writter, searchQuery, rowsToSkip, startInstantEpoch, endInstantEpoch,
				itemsPerPage, keyDistribution, rowsToDisplay, limit);
		}
		else if (exactSearch)
		{
			foundRows = getRowsToDisplayExact(reader, out, order, writter, searchQuery, rowsToSkip, startInstantEpoch, endInstantEpoch, itemsPerPage,
				keyDistribution, rowsToDisplay, limit);
		}
		else if (regexSearch)
		{
			foundRows = getRowsToDisplayRegex(reader, out, order, writter, searchQuery, rowsToSkip, startInstantEpoch, endInstantEpoch, itemsPerPage,
				keyDistribution, rowsToDisplay, limit);
		}
		else
		{
			foundRows = getRowsToDisplay(reader, out, order, writter, searchQuery, rowsToSkip, startInstantEpoch, endInstantEpoch, itemsPerPage,
				keyDistribution, rowsToDisplay, limit);
		}
		return foundRows;
	}

	private static boolean addRow(StringBuilder out, List<String> order, RowWritter writter, List<KeyValueStore> rowsToDisplay, String currentRow,
			int rowsToSkip, int foundRows, int itemsPerPage, Map<String, Integer> keyDistribution) throws IOException
	{
		KeyValueStore current = TrackingFileReader.processRow(currentRow);
		if (foundRows >= rowsToSkip && (foundRows - rowsToSkip) < itemsPerPage)
		{
			if (keyDistribution != null)
			{
				Integer count;
				for (String key : current.getAllKeys())
				{
					keyDistribution.put(key, (count = keyDistribution.get(key)) == null ? 0 : (count + 1));
				}
			}
			if (writter != null)
			{
				writter.writeRow(out, order, current, foundRows - rowsToSkip);
			}
			if (rowsToDisplay != null)
			{
				rowsToDisplay.add(current);
			}
		}
		return true;
	}

	private static boolean addRow(StringBuilder out, List<String> order, RowWritter writter, List<KeyValueStore> rowsToDisplay, String currentRow,
			int rowsToSkip, int foundRows, int itemsPerPage, Map<String, Integer> keyDistribution, NumPatternList fieldPatterns,
			KeyPattern[] keyFieldPatterns) throws IOException
	{
		KeyValueStore current = TrackingFileReader.processRow(currentRow);
		int foundPattern = 0;
		boolean hasPatterns = (fieldPatterns.patterns.length > 0 || keyFieldPatterns.length > 0);
		if (foundRows < rowsToSkip || (foundRows - rowsToSkip) > itemsPerPage)
		{
			for (Map.Entry<String, Object> entry : current.getEntries())
			{
				if (hasPatterns)
				{
					for (NumPattern np : fieldPatterns.patterns)
					{
						if (np.num != fieldPatterns.currentNum && np.pattern.matcher(entry.getValue().toString()).find())
						{
							foundPattern++;
							np.num = fieldPatterns.currentNum;
						}
					}
					for (KeyPattern kp : keyFieldPatterns)
					{
						if (kp.key.equals(entry.getKey()) && kp.pattern.matcher(entry.getValue().toString()).find())
						{
							foundPattern++;
						}
					}
				}
			}
			return (foundPattern == (keyFieldPatterns.length + fieldPatterns.patterns.length));
		}
		else
		{
			Integer count;
			for (Map.Entry<String, Object> entry : current.getEntries())
			{
				if (keyDistribution != null)
				{
					keyDistribution.put(entry.getKey(), (count = keyDistribution.get(entry.getKey())) == null ? 0 : (count + 1));
				}
				if (hasPatterns)
				{
					for (NumPattern np : fieldPatterns.patterns)
					{
						if (np.num != fieldPatterns.currentNum && np.pattern.matcher(entry.getValue().toString()).find())
						{
							foundPattern++;
							np.num = fieldPatterns.currentNum;
						}
					}
					for (KeyPattern kp : keyFieldPatterns)
					{
						if (kp.key.equals(entry.getKey()) && kp.pattern.matcher(entry.getValue().toString()).find())
						{
							foundPattern++;
						}
					}
				}
			}
			if (foundPattern == (keyFieldPatterns.length + fieldPatterns.patterns.length))
			{
				if (writter != null)
				{
					writter.writeRow(out, order, current, foundRows - rowsToSkip);
				}
				if (rowsToDisplay != null)
				{
					rowsToDisplay.add(current);
				}
				return true;
			}
		}
		return false;
	}

	private static String trimAround(String term, String sep)
	{
		String[] terms = term.split(sep);
		if (terms.length < 2)
		{
			return term.trim();
		}
		StringBuilder sb = new StringBuilder();
		for (String term1 : terms)
		{
			sb.append(term1.trim()).append(sep);
		}
		if (sb.length() > 0)
		{
			sb.setLength(sb.length() - sep.length());
		}
		return sb.toString();
	}

	private static class NumPattern
	{
		long num;
		Pattern pattern;

		public NumPattern(Pattern pattern)
		{
			this.num = 0;
			this.pattern = pattern;
		}

	}

	private static class NumPatternList
	{
		long currentNum;
		NumPattern[] patterns;

		public NumPatternList()
		{
			this.currentNum = -1;
			this.patterns = new NumPattern[0];
		}

		public NumPatternList(NumPattern[] patterns)
		{
			this.currentNum = -1;
			this.patterns = patterns;
		}
	}

	private static class KeyPattern
	{
		String key;
		Pattern pattern;

		public KeyPattern(String key, Pattern pattern)
		{
			this.key = key;
			this.pattern = pattern;
		}

	}

	private static interface RowWritter
	{
		void writeRow(StringBuilder out, List<String> order, KeyValueStore row, int rownum) throws IOException;
	}

	private static interface OutputWritter
	{
		void write(StringBuilder out, BufferedReader reader, LocalDate date, LocalDateTime start, LocalDateTime end, int offset, String columns,
				   int itemsPerPage, String search, boolean simpleSearch, boolean regexSearch, boolean exactSearch) throws IOException;

		void writeError(StringBuilder out, LocalDate date, LocalDateTime start, LocalDateTime end, String columns, int itemsPerPage, String search,
						boolean simpleSearch, boolean regexSearch, boolean exactSearch, String message);

	}

	private static class HtmlOutputWritter implements OutputWritter, RowWritter
	{
		@Override
		public void writeError(StringBuilder out, LocalDate date, LocalDateTime start, LocalDateTime end, String columns, int itemsPerPage,
				String search, boolean simpleSearch, boolean regexSearch, boolean exactSearch, String message)
		{
			addHtmlHead(out);
			out.append("<body>");
			buildDatePickers(out, date, start, end, columns, Integer.toString(itemsPerPage), search, simpleSearch, regexSearch, exactSearch);
			if (message != null)
			{
				out.append(message);
			}
			out.append("</body>");
		}

		@Override
		public void write(StringBuilder out, BufferedReader reader, LocalDate date, LocalDateTime start, LocalDateTime end, int offset,
				String columns, int itemsPerPage, String search, boolean simpleSearch, boolean regexSearch, boolean exactSearch) throws IOException
		{
			addHtmlHead(out);
			//build filename from date
			out.append("<body>");
			buildDatePickers(out, date, start, end, columns, Integer.toString(itemsPerPage), search, simpleSearch, regexSearch, exactSearch);

			List<String> cols = (null == columns || columns.isEmpty()) ? null : Arrays.asList(columns.split(","));
			String postValues = getPostValues(date, start, end, columns, Integer.toString(itemsPerPage), search, simpleSearch, regexSearch,
				exactSearch);

			Instant startInstant = start.atZone(ZoneId.systemDefault()).toInstant();
			Instant endInstant = end.atZone(ZoneId.systemDefault()).toInstant();

			buildTable(reader, out, cols, postValues, search, offset, startInstant, endInstant, itemsPerPage, simpleSearch, regexSearch, exactSearch);
		}

		@Override
		public void writeRow(StringBuilder out, List<String> order, KeyValueStore row, int rownum) throws IOException
		{
			int r = row.getString(DefaultKeys.SESSION).hashCode();
			int g = r * r;
			int b = g * r;

			final int off = 80;
			final int currMod = 255 - off;

			r = (r % currMod);
			b = (b % currMod);
			g = (g % currMod);
			if (r < 0)
			{
				r += 155;
			}
			if (g < 0)
			{
				g += 155;
			}
			if (b < 0)
			{
				b += 155;
			}
			r += off;
			b += off;
			g += off;
			out.append("<tr style='background-color:rgb(").append(Integer.toString(r)).append(",").append(Integer.toString(g)).append(",")
				.append(Integer.toString(b)).append(");'>");

			String date;
			Object fieldValue;
			String fieldValueStr;
			for (String anOrder : order)
			{
				out.append("<td style='border: 1px solid black;' title='").append(anOrder).append("'>");
				fieldValue = row.get(anOrder);
				if (fieldValue != null)
				{
					if (anOrder.equals("last_visit") || anOrder.equals(DefaultKeys.TIMESTAMP))
					{
						try
						{
							date = dateFormat.format(new Date(row.getLong(DefaultKeys.TIMESTAMP)));
						}
						catch (Exception e)
						{
							date = "";
						}
						out.append(date);
					}
					else
					{
						fieldValueStr = fieldValue.toString();
						if (fieldValueStr.startsWith("http://"))
						{
							out.append("<a href='").append(fieldValueStr).append("' target='_blank'>").append(fieldValueStr).append("</a>");
						}
						else
						{
							out.append(fieldValueStr);
						}
					}
				}
				out.append("</td>");
			}
			out.append("</tr>\n");
		}

		private void addHtmlHead(StringBuilder out)
		{
			out.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n \n<html>\n<head>\n  "
					+ "<script type=\"text/javascript\"> "
					+ "function submitHtmlSearch(){ submitSearch('html',''); }"
					+ "function submitCsvSearch(){ submitSearch('csv','_blank'); }"
					+ "function submitSearch(type, target){ var f=document.getElementById('searchForm'); f.elements['output'].value=type; f.setAttribute('target',target); f.submit(); }"
					+ "function submitHtmlOnEnter(e){ if(e.keyCode == 13){submitHtmlSearch();}}" + " </script>" + "</head>\n");
		}

		private String getPostValues(LocalDate date, LocalDateTime start, LocalDateTime end, String columns, String itemsPerPage, String search,
				boolean simpleSearch, boolean regexSearch, boolean exactSearch)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("<input type ='hidden' name='end' value='").append(Integer.toString(end.getHour())).append("' >");
			sb.append("<input type ='hidden' name='start' value='").append(Integer.toString(start.getHour())).append("' >");
			sb.append("<input type ='hidden' name='endminute' value='").append(Integer.toString(end.getMinute())).append("' >");
			sb.append("<input type ='hidden' name='startminute' value='").append(Integer.toString(start.getMinute())).append("' >");
			sb.append("<input type ='hidden' name='day' value='").append(Integer.toString(date.getDayOfMonth())).append("' >");
			sb.append("<input type ='hidden' name='month' value='").append(Integer.toString(date.getMonthValue())).append("' >");
			sb.append("<input type ='hidden' name='year' value='").append(Integer.toString(date.getYear())).append("' >");
			sb.append("<input type ='hidden' name='search' value='").append(search).append("' >");
			sb.append("<input type ='hidden' name='simpleSearch' value='").append(simpleSearch ? "true" : "false").append("' >");
			sb.append("<input type ='hidden' name='allowRegex' value='").append(regexSearch ? "true" : "false").append("' >");
			sb.append("<input type ='hidden' name='exactSearch' value='").append(exactSearch ? "true" : "false").append("' >");
			sb.append("<input type ='hidden' name='columns' value='").append(columns).append("' >");
			sb.append("<input type ='hidden' name='itemsPerPage' value='").append(itemsPerPage).append("' >");
			sb.append("<input type ='hidden' name='output' value='' >");
			return sb.toString();
		}

		private void buildDatePickers(StringBuilder out, LocalDate date, LocalDateTime start, LocalDateTime end, String columns, String itemsPerPage,
				String search, boolean simpleSearch, boolean regexSearch, boolean exactSearch)
		{
			out.append("<form id=\"searchForm\" method='get'><input type ='hidden' name='offset' value='0'>"
					+ "<input type ='hidden' name='output' value='' ><select name = 'day'><p>Date: ");

			String tmpNum;
			for (int i = 1; i <= 31; i++)
			{
				tmpNum = Integer.toString(i);
				out.append("<option value='").append(tmpNum).append("' ");
				if (i == date.getDayOfMonth())
				{
					out.append("selected='selected' ");
				}
				out.append(">").append(tmpNum).append("</option>");
			}

			out.append("</select><select name = 'month'>");

			for (int i = 1; i <= 12; i++)
			{
				tmpNum = Integer.toString(i);
				out.append("<option value='").append(tmpNum).append("' ");
				if (i == date.getMonthValue())
				{
					out.append("selected='selected' ");
				}
				out.append(">").append(tmpNum).append("</option>");
			}

			out.append("</select><select name = 'year'>");

			for (int i = date.getYear() - 1; i <= date.getYear() + 1; i++)
			{
				tmpNum = Integer.toString(i);
				out.append("<option value='").append(tmpNum).append("' ");
				if (i == date.getYear())
				{
					out.append("selected='selected' ");
				}
				out.append(">").append(tmpNum).append("</option>");
			}

			out.append("</select></p><p>From <select name = 'start'>");

			for (int i = 0; i <= 23; i++)
			{
				tmpNum = Integer.toString(i);
				out.append("<option value='").append(tmpNum).append("' ");
				if (i == start.getHour())
				{
					out.append("selected='selected' ");
				}
				out.append(">").append(tmpNum).append("</option>");
			}
			out.append("</select>:<input name='startminute' type = 'text' size='2' maxlength = '2' value='");

			if (start.getMinute() < 10)
			{
				out.append("0");
			}

			out.append(Integer.toString(start.getMinute()));
			out.append("'>h to <select name = 'end'>");

			for (int i = 0; i <= 23; i++)
			{
				out.append("<option value='").append(Integer.toString(i)).append("' ");
				if (i == end.getHour())
				{
					out.append("selected='selected' ");
				}
				out.append(">").append(Integer.toString(i)).append("</option>");
			}

			out.append("</select>:<input name='endminute' type = 'text' size='2' maxlength = '2' value='");

			if (end.getMinute() < 10)
			{
				out.append("0");
			}

			out.append(Integer.toString(end.getMinute()));
			out.append("'>h</p><p>Searchterm (search in specific columns using key=value and use | to search for multiple columns (logical and): <br>"
					+ "<input name='search' type='text' style='width:400px;' value='");

			if (search != null)
			{
				out.append(search);
			}

			out.append("' onkeydown='submitHtmlOnEnter(event)'><input style=\"padding-left:10px;\" name='simpleSearch' type='checkbox' value='true'");

			if (simpleSearch)
			{
				out.append(" checked=\"checked\"");
			}

			out.append("><label style=\"padding-right:10px;\" for=\"allowRegex\">Simple search</label><input name=\"allowRegex\" type=\"checkbox\" value=\"true\"");

			if (regexSearch)
			{
				out.append(" checked=\"checked\"");
			}

			out.append("><label for=\"allowRegex\" style=\"padding-right:10px\">Regular expression</label>"
					+ "<input style=\"padding-left:10px;\" name='exactSearch' type='checkbox' value='true'");

			if (exactSearch)
			{
				out.append(" checked=\"checked\"");
			}

			out.append("><label for=\"exactSearch\">Exact Search</label></p></p>"
					+ "<p>Column filter(leave empty to display all columns; add columns seperated by ','): <br>"
					+ "<input name='columns' type = 'text' style='width:400px;' value = '");

			if (columns != null)
			{
				out.append(columns);
			}

			out.append("' onkeydown='submitHtmlOnEnter(event)'></p><p>Results per page (max. ").append(MAX_ITEMS_PER_PAGE_STR)
				.append("): <input name='itemsPerPage' type = 'text' style='width:80px;' value = '");

			if (itemsPerPage != null)
			{
				out.append(itemsPerPage);
			}

			out.append("'></p><p><input type='submit' value='Go!' onclick=\"submitHtmlSearch();\">"
					+ "<input type='button' value='Export' style=\"margin-left: 20px;\" onclick=\"submitCsvSearch();\" ></p></form>");
		}

		private void buildTable(BufferedReader reader, StringBuilder out, List<String> keysToDisplay, String postValues, String searchQuery,
				int offset, Instant startInstant, Instant endInstant, int itemsPerPage, final boolean simpleSearch, final boolean regexSearch,
				boolean exactSearch) throws IOException
		{
			Map<String, Integer> keyDistribution = new HashMap<String, Integer>();
			List<KeyValueStore> rowsToDisplay = new ArrayList<KeyValueStore>();
			int rowsToSkip = itemsPerPage * offset;

			int foundRows = TrackingFileService.writeGetRows(reader, out, keysToDisplay, null, keyDistribution, rowsToDisplay, searchQuery, offset,
				startInstant, endInstant, itemsPerPage, simpleSearch, regexSearch, exactSearch);

			out.append("<table id ='tabela' cellpadding='3px' style='border:none;border-collapse:collapse;font-family:monospace;font-size:9pt;white-space:nowrap;'><thead><tr>");
			List<String> tableOrder;

			if (keysToDisplay == null)
			{
				tableOrder = buildOrderedColumns(keyDistribution);
			}
			else
			{
				tableOrder = keysToDisplay;
			}
			for (String aTableOrder : tableOrder)
			{
				out.append("<th style= 'border: 1px solid black;cursor:move;'>").append(aTableOrder).append("</th>");
			}
			out.append("</thead></tr><tbody>\n");
			boolean noNextPage = foundRows < rowsToSkip + itemsPerPage;

			for (KeyValueStore row : rowsToDisplay)
			{
				writeRow(out, tableOrder, row, 0);
			}
			out.append("</tbody><tfoot border='0px'><tr border='0px'><td border='0px' colspan=\"").append(Integer.toString(tableOrder.size()))
				.append("\">");

			if (noNextPage)
			{
				out.append("<br>REACHED EOF<br><br>");
			}
			int lastRecordNumber = Math.min(foundRows, rowsToSkip + itemsPerPage);
			out.append("found ").append(Integer.toString(foundRows)).append(" records");
			if (foundRows > 0)
			{
				out.append(" - displaying record ").append(Integer.toString(rowsToSkip + 1)).append(" to ")
					.append(Integer.toString(lastRecordNumber)).append("<br>");
				int pages = foundRows / itemsPerPage;
				out.append("<table border='0px'><tr><td colspan=\"").append(Integer.toString(tableOrder.size())).append("\">");
				if (offset > 0)
				{
					out.append("&nbsp;");
					if (offset <= 0)
					{
						out.append("<br>");
					}
					else
					{
						out.append("<form style=\"display: inline;\" method='get'><input type ='hidden' name='offset' value='")
							.append(Integer.toString(offset - 1)).append("' >");
						out.append(postValues);
						out.append("<input type='submit' style='cursor:pointer;border:none;background-color:transparent;text-decoration:underline;padding:0px;margin:0px;' value='<<'></form>");
					}
					out.append("&nbsp;");
				}
				for (int i = 0; i <= pages; i++)
				{
					out.append("&nbsp;");
					out.append("<form style=\"display: inline;\" method='get' name='gotoPage").append(Integer.toString(i + 1))
						.append("'><input type ='hidden' name='offset' value='").append(Integer.toString(i)).append("' >");
					out.append(postValues);
					out.append("<input type='submit' style='");
					if (i == offset)
					{
						out.append("font-weight:bold;");
					}
					out.append("cursor:pointer;border:none;background-color:transparent;text-decoration:underline;padding:0px;margin:0px;' value='")
						.append(Integer.toString(i + 1)).append("'></form>");
					out.append("&nbsp;");
				}
				if (offset < pages)
				{
					out.append("&nbsp;");
					out.append("<form style=\"display: inline;\" method='get'><input type ='hidden' name='offset' value='")
						.append(Integer.toString(offset + 1)).append("' >");
					out.append(postValues);
					out.append("<input type='submit' style='cursor:pointer;border:none;background-color:transparent;text-decoration:underline;padding:0px;margin:0px;' value='>>'></form>");
					out.append("&nbsp;");
				}
				out.append("</td></tr></table>");
			}
			out.append("</td></tr></tfoot> </table> ");
		}

		private List<String> buildOrderedColumns(Map<String, Integer> density)
		{
			List<Tupel<Integer, String>> values = new ArrayList<Tupel<Integer, String>>();
			density.remove(DefaultKeys.SESSION);
			density.remove(DefaultKeys.URL);
			density.remove(DefaultKeys.TIMESTAMP);
			for (String s : density.keySet())
			{
				values.add(new Tupel<Integer, String>(density.get(s), s));
			}
			Collections.sort(values);

			List<String> result = new ArrayList<String>();
			result.add(DefaultKeys.TIMESTAMP);
			result.add(DefaultKeys.SESSION);
			result.add(DefaultKeys.URL);

			for (Tupel<Integer, String> value : values)
			{
				result.add(value.getSecond());
			}
			return result;
		}
	}

	private static class CsvOutputWritter implements OutputWritter, RowWritter
	{
		final Pattern numPattern = Pattern.compile("[-+]?[0-9]+.?[0-9]*");

		@Override
		public void write(StringBuilder out, BufferedReader reader, LocalDate date, LocalDateTime start, LocalDateTime end, int offset,
				String columns, int itemsPerPage, String search, boolean simpleSearch, boolean regexSearch, boolean exactSearch) throws IOException
		{
			Instant startInstant = start.atZone(ZoneId.systemDefault()).toInstant();
			Instant endInstant = end.atZone(ZoneId.systemDefault()).toInstant();

			Map<String, Integer> keyDistribution = new HashMap<String, Integer>();
			List<KeyValueStore> rowsToDisplay = new ArrayList<KeyValueStore>();
			List<String> cols = (null == columns || columns.isEmpty()) ? null : Arrays.asList(columns.split(","));

			TrackingFileService.writeGetRows(reader, out, cols, null, keyDistribution, rowsToDisplay, search, 0, startInstant, endInstant,
				itemsPerPage, simpleSearch, regexSearch, exactSearch, itemsPerPage);

			if (cols == null)
			{
				cols = buildOrderedColumns(keyDistribution);
			}

			for (String col : cols)
			{
				out.append("\"").append(col).append("\";");
			}
			out.append("\n");

			int rowcount = 0;

			for (KeyValueStore row : rowsToDisplay)

			{
				writeRow(out, cols, row, rowcount++);
			}
			keyDistribution = null;
			rowsToDisplay = null;
			System.gc();
			TrackingFileService.writeGetRows(reader, out, cols, this, null, null, search, 0, startInstant, endInstant, Integer.MAX_VALUE,
				simpleSearch, regexSearch, exactSearch);
		}

		@Override
		public void writeError(StringBuilder out, LocalDate date, LocalDateTime start, LocalDateTime end, String columns, int itemsPerPage,
				String search, boolean simpleSearch, boolean regexSearch, boolean exactSearch, String message)
		{
		}

		@Override
		public void writeRow(StringBuilder out, List<String> order, KeyValueStore row, int rownum) throws IOException
		{
			String date;
			Object fieldValue;
			String fieldValueStr;
			for (String anOrder : order)
			{
				fieldValue = row.get(anOrder);
				if (fieldValue != null)
				{
					if (anOrder.equals("last_visit") || anOrder.equals(DefaultKeys.TIMESTAMP))
					{
						try
						{
							date = dateFormat.format(new Date(row.getLong(DefaultKeys.TIMESTAMP)));
						}
						catch (Exception e)
						{
							date = "";
						}
						out.append(date);
					}
					else
					{
						fieldValueStr = fieldValue.toString().trim();
						if (!fieldValueStr.isEmpty())
						{
							if (numPattern.matcher(fieldValueStr).matches())
							{
								out.append(fieldValueStr);
							}
							else
							{
								out.append("\"").append(fieldValueStr).append("\"");
							}
						}
					}
				}
				out.append(";");
			}
			out.append("\n");
		}

		private List<String> buildOrderedColumns(Map<String, Integer> density)
		{
			List<Tupel<Integer, String>> values = new ArrayList<Tupel<Integer, String>>();
			density.remove(DefaultKeys.SESSION);
			density.remove(DefaultKeys.URL);
			density.remove(DefaultKeys.TIMESTAMP);
			for (String s : density.keySet())
			{
				values.add(new Tupel<Integer, String>(density.get(s), s));
			}
			Collections.sort(values);

			List<String> result = new ArrayList<String>();
			result.add(DefaultKeys.TIMESTAMP);
			result.add(DefaultKeys.SESSION);
			result.add(DefaultKeys.URL);

			for (Tupel<Integer, String> value : values)
			{
				result.add(value.getSecond());
			}
			return result;
		}
	}
}
