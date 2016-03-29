package com.ibra.support.authserver.repository.persistence;

import com.visualmeta.util.jdbc.impl.JdbcTemplateDaoImpl;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("visualmetaSharedSchemaJdbcTemplateDao")
public final class VisualMetaSharedSchemaJdbcTemplateDaoImpl extends JdbcTemplateDaoImpl
{
	private static Log LOGGER = LogFactory.getLog(VisualMetaSharedSchemaJdbcTemplateDaoImpl.class);

	@Override
	@Autowired(required = false)
	public void setDataSource(@Qualifier("visualmetaSharedDataSource") BasicDataSource dataSource)
	{
		String url = dataSource.getUrl();
		String username = dataSource.getUsername();
		url = url.replace("//", "//" + username + "@");
		LOGGER.warn("Initialized dataSource: " + url);
		super.setDataSource(dataSource);
	}
}
