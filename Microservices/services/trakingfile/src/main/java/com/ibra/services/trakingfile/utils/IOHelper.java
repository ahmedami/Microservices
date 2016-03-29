package com.ibra.services.trakingfile.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public class IOHelper
{
	private static final Log LOGGER = LogFactory.getLog(IOHelper.class);

	public static final int READ_BUFFER_SIZE = 4096;

	/**
	 * The encoding favours small positive numbers which can be represented in fewer bytes at the expense that big numbers and negative numbers need
	 * more bytes for their representation. If your stream consists predominantly of small positive numbers values this encoding may reduce the
	 * overall stream size.
	 *
	 * @param value
	 *        The int value.
	 * @param out
	 *        The output stream the value is written to.
	 * @throws IOException
	 *         if the underlying stream throws an exception.
	 */
	public static void writeEncodedIntToStream(int value, OutputStream out) throws IOException
	{
		do
		{
			int byteValue = 0x7f & value;
			value = value >>> 7;
			if (value > 0)
			{
				byteValue |= 0x80;
			}

			out.write(byteValue);

		}
		while (value > 0);
	}

	/**
	 * Reads an int value from a stream that was encoded by {@link #writeEncodedIntToStream(int, OutputStream)}.
	 *
	 * @param in
	 *        The input stream to read from.
	 * @return The read int value.
	 * @throws IOException
	 *         if the underlying stream throws an exception.
	 */
	public static int readEncodedIntFromStream(InputStream in) throws IOException
	{
		int byteValue = in.read();
		int returnValue = (byteValue & 0x7f);
		int shiftValue = 0;
		while ((byteValue & 0x80) > 0)
		{
			byteValue = in.read();
			shiftValue += 7;
			returnValue |= ((byteValue & 0x7f) << shiftValue);
		}

		return returnValue;
	}

	/**
	 * The encoding favours small positive numbers which can be represented in fewer bytes at the expense that big numbers and negative numbers need
	 * more bytes for their representation. If your stream consists predominantly of small positive values this encoding may reduce the overall stream
	 * size.
	 *
	 * @param value
	 *        The long value.
	 * @param out
	 *        The output stream the value is written to.
	 * @throws IOException
	 *         if the underlying stream throws an exception.
	 */
	public static void writeEncodedLongToStream(long value, OutputStream out) throws IOException
	{
		do
		{
			int byteValue = (int) (0x7f & value);
			value = value >>> 7;
			if (value > 0)
			{
				byteValue |= 0x80;
			}

			out.write(byteValue);

		}
		while (value > 0);
	}

	/**
	 * Reads a long value from a stream that was encoded by {@link #writeEncodedLongToStream(long, OutputStream)}.
	 *
	 * @param in
	 *        The input stream to read from.
	 * @return The read long value.
	 * @throws IOException
	 *         if the underlying stream throws an exception.
	 */
	public static long readEncodedLongFromStream(InputStream in) throws IOException
	{
		long byteValue = in.read();
		long returnValue = (byteValue & 0x7f);
		int shiftValue = 0;
		while ((byteValue & 0x80) > 0)
		{
			byteValue = in.read();
			shiftValue += 7;
			returnValue |= ((byteValue & 0x7f) << shiftValue);
		}

		return returnValue;
	}

	public static void writeStreamToStream(InputStream in, OutputStream out) throws IOException
	{
		writeStreamToStream(in, out, true);
	}

	public static void writeStreamToStream(InputStream in, OutputStream out, boolean closeOut) throws IOException
	{
		writeStreamToStream(in, out, true, closeOut);
	}

	public static void writeStreamToStream(InputStream in, OutputStream out, boolean closeIn, boolean closeOut) throws IOException
	{
		byte[] buffer = new byte[READ_BUFFER_SIZE];
		int length;
		while ((length = in.read(buffer)) >= 0)
		{
			out.write(buffer, 0, length);
		}

		if (closeIn)
		{
			IOHelper.close(in);
		}
		if (closeOut)
		{
			IOHelper.close(out);
		}
	}

	public static OutputStream createOutputSteam(String path)
	{
		BufferedOutputStream out = null;
		try
		{
			out = new BufferedOutputStream(new FileOutputStream(path));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
		return out;
	}

	/**
	 * Closes a stream doing the proper null checks.
	 *
	 * @return true if no error occurred, false otherwise.
	 */
	public static boolean close(Closeable closeable)
	{
		if (closeable == null)
		{
			return true;
		}

		try
		{
			closeable.close();
		}
		catch (IOException ioe)
		{
			LOGGER.error("Failed to close connection.", ioe);
			return false;
		}

		return true;
	}

	public static InputStream textToInputStream(String text)
	{
		InputStream is = null;
		try
		{
			is = new ByteArrayInputStream(text.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException("While trying to convert input string to UTF-8, I encountered an UnsupportedEncodingException ", e);
		}
		return is;
	}
}