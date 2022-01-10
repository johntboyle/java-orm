package com.orm.utils;

public class Configuration {
	private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    
	public Configuration() {
		super();
	}

	public Configuration configure(String dbUrl, String dbUsername, String dbPassword) {
		this.dbUrl = dbUrl;
		this.dbUsername = dbUsername;
		this.dbPassword = dbPassword;
        return this;
    }
	
	public SessionFactory buildSessionFactory() {
		return new SessionFactory(this);
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public String getDbUsername() {
		return dbUsername;
	}

	public String getDbPassword() {
		return dbPassword;
	}
}
