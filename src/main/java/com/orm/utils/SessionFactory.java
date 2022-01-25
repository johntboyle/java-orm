package com.orm.utils;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SessionFactory {
	private Configuration configuration;
	private static Logger logger = LogManager.getLogger(SessionFactory.class);
	
	public SessionFactory(Configuration configuration) {
		super();
		this.configuration = configuration;
	}

	public Session openSession() {
		try {
			return new Session(DriverManager.getConnection(configuration.getDbUrl(), configuration.getDbUsername(), configuration.getDbPassword()));
		} catch (SQLException e) {
			logger.warn("error establishing connection to the database");
			e.printStackTrace();
			return null;
		}
	}
}
