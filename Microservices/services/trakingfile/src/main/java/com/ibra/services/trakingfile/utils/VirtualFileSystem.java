package com.ibra.services.trakingfile.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;

@Component("virtualFileSystem")
public class VirtualFileSystem
{
	private final Log LOGGER = LogFactory.getLog(getClass());

	private boolean useHDFS = false;
	private Configuration config = null;

	public VirtualFileSystem()
	{
		try
		{
			URL url = getClass().getClassLoader().getResource("core-site.xml");
			if (url == null)
			{
				LOGGER.warn("");
				LOGGER.warn("could not find core-site.xml in classpath, thus, not using HDFS.");
				LOGGER.warn("");
				useHDFS = false;
				return;
			}

			config = new Configuration();
			config.setQuietMode(false);
			useHDFS = true;
		}
		catch (Exception e)
		{
			LOGGER.warn("not using HDFS, because of " + e.getMessage());
			useHDFS = false;
		}
	}

	public boolean exists(String filename) throws IOException
	{
		if (useHDFS)
		{
			return FileSystem.get(config).exists(new Path(filename));
		}
		else
		{
			return new File(filename).exists();
		}
	}

	public boolean isHdfsEnabled()
	{
		return useHDFS;
	}

	public InputStream openInputStream(String filename) throws IOException
	{
		return this.openInputStreamFrom(filename, 0);
	}

	private InputStream openInputStreamFrom(String filename, long position) throws IOException
	{
		if (useHDFS)
		{
			FSDataInputStream is = FileSystem.get(config).open(new Path(filename));
			is.seek(position);
			return is;
		}
		else
		{
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename));
			long remaining = position;
			while (0 < remaining)
			{
				remaining -= is.skip(remaining);
			}
			return is;
		}
	}

}
