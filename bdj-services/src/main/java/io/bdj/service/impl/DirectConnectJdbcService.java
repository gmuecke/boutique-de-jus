package io.bdj.service.impl;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Base class for service implementations to open a new jdbc connection for each request (which is not recommended :)
 */
public abstract class DirectConnectJdbcService {

    private final String dbURL;
    private final String dbDriver;

    /**
     * Initializes the service by obtaining the configuration parameters for the database from JNDI
     */
    public DirectConnectJdbcService() {

        try {
            InitialContext ic = new InitialContext();
            this.dbURL = (String) ic.lookup("databaseUrl");
            this.dbDriver = (String) ic.lookup("databaseDriver");
        } catch (NamingException e) {
            throw new RuntimeException("Could not obtain database configuration",e);
        }
    }

    /**
     * Opens a new connection to the database server. The connection must be closed after use to prevent connection leak.
     * @return
     *  a new connection to the database server.
     * @throws SQLException
     */
    public Connection openConnection() throws SQLException {

        try {
            Class.forName(this.dbDriver).newInstance();
            return DriverManager.getConnection(dbURL);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException("Could not initialize db driver",e);
        }
    }

}
