package com.orm.utils;

public class Transaction {

	public void beginCommit(){}

	// begin databse commit.
	
	public void Rollback(){}

	// Rollback to previous commit.
	
	public void Rollback(final String name){}

	// Rollback to previous commit with given name.
	
	public void setSavepoint(final String name){}

	// Set a savepoint with the given name.
	
	public void ReleaseSavepoint(final String name){}

	// Release the savepoint with the given name.
	
	public void enableAutoCommit(){}

	// Enable auto commits on the database.
	
	public void setTransaction(){}

	// Start a transaction block.
	
	
}
