package com.orm.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.orm.annotations.Column;
import com.orm.annotations.PrimaryKey;

public class Session {
	
	private static Logger logger = LogManager.getLogger(Session.class);
	private HashMap<Class<?>, MetaModel> classes;
	private Connection connection;
	
	Session(Connection connection) {
		super();
		classes = new HashMap<Class<?>, MetaModel>();
		this.connection = connection;
		logger.info("session created");
	}
	
	private String sqlTypeSelector(Class<?> clazz) {
		switch(clazz.getName()) {
			case "java.lang.String": return "VARCHAR";
			case "java.math.BigDecimal": return "NUMERIC";
			case "boolean": return "BIT";
			case "byte": return "TINYINT";
			case "short": return "SMALLINT";
			case "int": return "INTEGER";
			case "long": return "BIGINT";
			case "float": return "REAL";
			case "double": return "DOUBLE";
			case "java.lang.Boolean": return "BIT";
			case "java.lang.Integer": return "INTEGER";
			case "java.lang.Long": return "BIGINT";
			case "java.lang.Float": return "REAL";
			case "java.lang.Double": return "DOUBLE";
			case "java.sql.Date": return "DATE";
			case "java.sql.Time": return "TIME";
			case "java.sql.Timestamp": return "TIMESTAMP";
		}
		return null;
	}
	
	public boolean addClass(final Class<?> clazz) { 
		if(classes.containsKey(clazz)) {
			return true;
		}
		MetaModel classModel;
		try {
			classModel = MetaModel.of(clazz);
		} catch(IllegalStateException e) {
			logger.warn("Class \"" + clazz.getName() +"\" does not follow the correct specifications to be added.");
			e.printStackTrace();
			return false;
		}
		// Sanitation Needed
		String sql = "create table if not exists " + classModel.getName();
		sql += "(" + classModel.getPrimaryKey().getAnnotation(PrimaryKey.class).name() + " " + sqlTypeSelector(classModel.getPrimaryKey().getType()) + " primary key";
		for(Field field : classModel.getColumns()) {
			sql += ","+ field.getAnnotation(Column.class).name() + " " + sqlTypeSelector(field.getType());
		}
		sql += ");";
		try {
			PreparedStatement createTable = connection.prepareStatement(sql);
			logger.info(createTable.toString());
			boolean existed = !createTable.execute();
			if(!existed) {
				logger.info("Table for \"" + clazz.getName() +"\" created.");
			}
			classes.put(clazz, classModel);
			return true;
			
		} catch (SQLException e) {
			e.printStackTrace();
			logger.warn("Table for \"" + clazz.getName() +"\" could not be created.");
			return false;
		}
	}

	public boolean UpdateObjectInDB(final Object obj, final String update_columns){ 
		// Checks if class has been added
		if(!classes.containsKey(obj.getClass())) {
			// Class can't be mapped
			if(addClass(obj.getClass()) == false) {
				return false;
			}
		}
		
		MetaModel classModel = classes.get(obj.getClass());
		String pkName = classModel.getPrimaryKey().getAnnotation(PrimaryKey.class).name();
		
		// Sanitation Needed
		try {
			String sql = "update " + classModel.getName() + " set ";
			
			String[] columns = update_columns.split(",");
			for(String col : columns) {
				sql += col + " = ?, ";
			}
			if(sql.charAt(sql.length()-2) == ',') {
				sql = sql.substring(0, sql.length()-2);
			}
			sql += " where " + pkName + " = " + classModel.getGetters().get(pkName).invoke(obj) + ";";
			
			HashMap<String, Field> fields = new HashMap<>();
			fields.put(pkName, classModel.getPrimaryKey());
			for(Field field : classModel.getColumns()) {
				fields.put(field.getAnnotation(Column.class).name(), field);
			}
	
			PreparedStatement stmt = connection.prepareStatement(sql);
			int index = 1;	
			for(String col : columns) {
				if(fields.get(col).getAnnotation(PrimaryKey.class) != null) {
					stmt.setObject(index, classModel.getGetters().get(fields.get(col).getAnnotation(PrimaryKey.class).name()).invoke(obj));
				}
				else if(fields.get(col).getAnnotation(Column.class) != null) {
					stmt.setObject(index, classModel.getGetters().get(fields.get(col).getAnnotation(Column.class).name()).invoke(obj));
				}
				index++;
			}
			logger.info(stmt.toString());
			return stmt.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean removeObjectFromDB(final Object primaryKey, final Class<?> clazz) {
		// Checks if class has been added
		if (!classes.containsKey(clazz)) {
			// Class can't be mapped
			if (addClass(clazz) == false) {
				return false;
			}
		}

		MetaModel classModel = classes.get(clazz);
		String pkName = classModel.getPrimaryKey().getAnnotation(PrimaryKey.class).name();


		// Sanitation Needed
		String sql = "delete from " + classModel.getName() + " where ";

		sql += pkName;
		sql += " = ?;";

		try {
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setObject(1, primaryKey);
			logger.info(stmt.toString());
			return stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean addObjectToDB(final Object obj){ 
		// Checks if class has been added
		if(!classes.containsKey(obj.getClass())) {
			// Class can't be mapped
			if(addClass(obj.getClass()) == false) {
				return false;
			}
		}
		
		MetaModel classModel = classes.get(obj.getClass());
		// Sanitization Needed
		String sql = "insert into " + classModel.getName();
		
		
		
		sql += "(" + classModel.getPrimaryKey().getAnnotation(PrimaryKey.class).name();	
		for(Field field : classModel.getColumns()) {
			sql += ","+ field.getAnnotation(Column.class).name();
		}
		sql += ") values(?";
		for(int i = 0; i < classModel.getColumns().size(); i++) {
			sql += ",?";
		}
		sql += ");";
		
		try {
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setObject(1, classModel.getGetters().get(classModel.getPrimaryKey().getAnnotation(PrimaryKey.class).name()).invoke(obj));
			int index = 2;
			for(Field field : classModel.getColumns()) {
				stmt.setObject(index, classModel.getGetters().get(field.getAnnotation(Column.class).name()).invoke(obj));
				index++;
			}
			logger.info(stmt.toString());
			return stmt.execute();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}

	public Object getObjectFromDB(final Object primaryKey, final Class<?> clazz) {
		// Checks if class has been added
		if (!classes.containsKey(clazz)) {
			// Class can't be mapped
			if (addClass(clazz) == false) {
				return null;
			}
		}

		MetaModel classModel = classes.get(clazz);
		String pkName = classModel.getPrimaryKey().getAnnotation(PrimaryKey.class).name();

		// Sanitation Needed
		String sql = "select * from " + classModel.getName() + " where ";

		sql += pkName;
		sql += " = ?;";

		try {
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setObject(1, primaryKey);
			logger.info(stmt.toString());
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				Constructor<?> constructor = clazz.getConstructor();
				Object instance = constructor.newInstance();
				classModel.getSetters().get(pkName).invoke(instance, rs.getObject(pkName));
				for (Field field : classModel.getColumns()) {
					String fieldName = field.getAnnotation(Column.class).name();
					classModel.getSetters().get(fieldName).invoke(instance, rs.getObject(fieldName));
				}
				return instance;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}
}
