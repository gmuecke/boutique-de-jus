package io.bdj.db;

import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;

/**
 *
 */
public final class DBInitializer {

  private static final Logger LOG = getLogger(DBInitializer.class);
  private DBInitializer(){}

  public static void createTable(final Connection conn) throws SQLException {
    try (Statement statement = conn.createStatement()){
      //create the users table
      statement.executeUpdate("CREATE SCHEMA BOUTIQUE");
      statement.executeUpdate("CREATE TABLE BOUTIQUE.USERS ("
                                  + "USERNAME VARCHAR(32) NOT NULL CONSTRAINT USERS_PK PRIMARY KEY,  "
                                  + "PASSWORD VARCHAR(32), "
                                  + "LASTNAME VARCHAR(255), "
                                  + "FIRSTNAME VARCHAR(255), "
                                  + "EMAIL VARCHAR(255), "
                                  + "STREET VARCHAR(255), "
                                  + "CITY VARCHAR(255), "
                                  + "ZIP VARCHAR(10), "
                                  + "COUNTRY VARCHAR(2)"
                                  + ")");
      statement.executeUpdate("CREATE TABLE BOUTIQUE.ROLES ("
                                  + "ROLE VARCHAR(255) ,  "
                                  + "USERNAME VARCHAR(32)"
                                  + ")");
      conn.commit();
    }catch (SQLException e){
      if (!DerbyHelper.tableAlreadyExists(e) && !DerbyHelper.schemaAlreadyExists(e)) {
        throw e;
      }
      LOG.info("User table exists");
    }


  }
}
