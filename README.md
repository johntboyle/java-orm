# Java ORM

## Project Description
A java based ORM for simplifying connecting to and from an SQL database without the need for SQL or connection management. 

## Technologies Used

* PostgreSQL - version 42.2.12  
* Java - version 8.0  
* Apache commons - version 2.1  
* JUnit

## Features
  
* Easy to use and straightforward user API.  
* No need for SQL, HQL, or any databse specific language.  
* Straightforward and simple Annotation based for ease of use. 
* etc...

## Getting Started  
Currently project must be included as local dependency. to do so:
```shell
  git clone https://github.com/johntboyle/java-orm.git
  cd java-orm
  mvn install
```
Next, place the following inside your project pom.xml file:
```XML
  <dependency>
    <groupId>com.orm</groupId>
    <artifactId>java-orm</artifactId>
    <version>0.0.1-SNAPSHOT</version>
  </dependency>

```

Finally, inside your project structure you need a application.proprties file. 
 (typically located src/main/resources/)
 ``` 
  url=path/to/database
  admin-usr=username/of/database
  admin-pw=password/of/database  
  ```
  
## Usage  
  ### Annotating classes  
  All classes which represent objects in database must be annotated.
   - #### @Table(name = "table_name")  
      - Indicates that this class is associated with table 'table_name'  
   - #### @Column(name = "column_name")  
      - Indicates that the Annotated field is a column in the table with the name 'column_name'  
   - #### @Setter(name = "column_name")  
      - Indicates that the anotated method is a setter for 'column_name'.  
   - #### @Getter(name = "column_name")  
      - Indicates that the anotated method is a getter for 'column_name'.  
   - #### @PrimaryKey(name = "column_name") 
      - Indicates that the annotated field is the primary key for the table.

  ### User API  
  
  - #### `public Configuration configure(String dbUrl, String dbUsername, String dbPassword)`  
     - returns an instance of the Configuration class. It it used to configure the details of the data source.  
  - #### `public SessionFactory buildSessionFactory()`  
     - returns an instance of the sessionFactory class. It builds the session with the given configuration.
  - #### `public Session openSession()`
     - returns an instance of the session class. It is the starting point to calling any of the below methods. 
  - #### `public boolean addClass(final Class<?> clazz)`  
     - Adds a class to the ORM. This is the method to use to declare a Class as an object inside of the database.  
  - #### `public boolean UpdateObjectInDB(final Object obj, final String update_columns)`  
     - Updates the given object in the database. Update columns is a comma separated list for all columns in the object which need to be updated  
  - #### `public boolean removeObjectFromDB(final Object obj)`  
     - Removes the given object from the database.  
  - #### `public boolean addObjectToDB(final Object obj)`  
     - Adds the given object to the database.  
  - #### `public Object getObjectFromDB(final Object primaryKey, final Class<?> clazz)`  
     - returns the object with the corresponding key from the database.  




## License

This project uses the following license: [GNU Public License 3.0](https://www.gnu.org/licenses/gpl-3.0.en.html).