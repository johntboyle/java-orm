package com.orm.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import com.orm.annotations.Column;
import com.orm.annotations.Getter;
import com.orm.annotations.PrimaryKey;
import com.orm.annotations.Setter;
import com.orm.annotations.Table;

class MetaModel {
    
    private Class<?> clazz;
    private String name;
    private Field primaryKeyField;
    private List<Field> columnFields;
    private List<Field> foreignKeyFields;
    private HashMap<String, Method> getters;
    private HashMap<String, Method> setters;
    
    static MetaModel of(Class<?> clazz) throws IllegalStateException {
        if(clazz.getAnnotation(Table.class) == null) {
            throw new IllegalStateException("Cannot create MetaModel object! Provided class "
                    + clazz.getName() + " is not annotated with @Table");
        }
        
        return new MetaModel(clazz);
    }
    
    private MetaModel(Class<?> clazz) {
        this.clazz = clazz;
        this.name = clazz.getAnnotation(Table.class).name();
        this.columnFields = new LinkedList<>();
        this.foreignKeyFields = new LinkedList<>();
        this.getters = new HashMap<String, Method>();
        this.setters = new HashMap<String, Method>();
        
        Method[] methods = clazz.getDeclaredMethods();
        Stream.of(methods).forEach(method -> {
        	Getter getter = method.getAnnotation(Getter.class);
        	if(getter != null) {
        		getters.put(getter.name(), method);
        		return;
        	}
        	
        	Setter setter = method.getAnnotation(Setter.class);
        	if(setter != null) {
        		setters.put(setter.name(), method);
        	}
        });
        
        Field[] fields = clazz.getDeclaredFields();
        Stream.of(fields).forEach(field -> {
        	PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
            if(primaryKey != null) {
                primaryKeyField = field;
                if(!getters.containsKey(primaryKey.name()) || !setters.containsKey(primaryKey.name())) {
                	throw new RuntimeException("No matching getter and setter for " + field.getName());
                }
                return;
            }
            

            Column column = field.getAnnotation(Column.class);
            if(column != null) {
                columnFields.add(field);
                if(!getters.containsKey(column.name()) || !setters.containsKey(column.name())) {
                	throw new RuntimeException("No matching getter and setter for " + field.getName());
                }
            }
        });
        
        if(primaryKeyField == null) {
        	throw new RuntimeException("No primary key found in " + clazz.getName());
        }
    }

    public List<Field> getColumns(){
        return columnFields;
    }
    
    public List<Field> getForeignKeys(){
        return foreignKeyFields;
    }
    
    public Field getPrimaryKey() {
        return primaryKeyField;
    }
    
    public Class<?> getModeledClass() {
    	return clazz;
    }

	public String getName() {
		return name;
	}

	public HashMap<String, Method> getGetters() {
		return getters;
	}
	
	public HashMap<String, Method> getSetters() {
		return setters;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public Field getPrimaryKeyField() {
		return primaryKeyField;
	}

	public List<Field> getColumnFields() {
		return columnFields;
	}

	public List<Field> getForeignKeyFields() {
		return foreignKeyFields;
	}
	
	
}
