package io.bdj.db;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.derby.drda.NetworkServerControl;
import org.slf4j.Logger;

/**
 *
 */
public class DerbyStandalone {

    private static final Logger LOG = getLogger(DerbyStandalone.class);
    private NetworkServerControl server;

    public void start() throws Exception {

        System.setProperty("derby.system.home", ".");
        System.setProperty("derby.authentication.provider", "BUILTIN");
        System.setProperty("derby.user.admin", "admin");

        this.server = new NetworkServerControl(InetAddress.getByName("localhost"), 1527);
        final PrintWriter out = new PrintWriter(System.out);
        server.start(out);

        LOG.info("Waiting for server to be started");
        final int maxRetries = 10;
        for (int i = 1; i <= maxRetries; ++i) {
            try {
                LOG.info("Pinging server ... ");
                server.ping();
                break;
            } catch (Exception e) {
                LOG.warn("Server is not started yet ..., retrying after {}s", i * 1000);
            }
            Thread.sleep(1000 * i);
        }

        server.setTimeSlice(Integer.getInteger("db.timeslice", 5));
        server.setMaxThreads(Integer.getInteger("db.threads", 10));

        initDB();
    }

    public void stop() throws Exception {
        this.server.shutdown();
    }

    private void initDB() throws SQLException {

        //init the driver
        DriverManager.registerDriver(new org.apache.derby.jdbc.ClientDriver());

        try (Connection conn = DriverManager.getConnection("jdbc:derby://localhost:1527/testdb;create=true");
             Statement statement = conn.createStatement()) {

            try {
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

            LOG.info("Initialize Product Table");
            ProductInitializer.populateProducts(conn);

        } catch (SQLException e) {
            if (!DerbyHelper.tableAlreadyExists(e) && !DerbyHelper.schemaAlreadyExists(e)) {
                throw e;
            }
        }
    }
}
