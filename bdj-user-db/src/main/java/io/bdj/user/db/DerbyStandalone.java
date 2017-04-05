package io.bdj.user.db;

import static java.util.logging.Logger.getLogger;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import io.bdj.util.signals.Signal;
import io.bdj.util.signals.SignalTransceiver;
import org.apache.derby.drda.NetworkServerControl;

/**
 *
 */
public class DerbyStandalone {

    private static final Logger LOG = getLogger(DerbyStandalone.class.getName());

    public static void main(String... args) throws Exception {

        System.setProperty("derby.system.home", ".");
        System.setProperty("derby.authentication.provider", "BUILTIN");
        System.setProperty("derby.user.admin", "admin");

        NetworkServerControl server = new NetworkServerControl(InetAddress.getByName("localhost"), 1527);
        server.start(new PrintWriter(System.out));
        server.setTimeSlice(5);
        server.setMaxThreads(10);

        try (Connection conn = DriverManager.getConnection("jdbc:derby://localhost:1527/testdb;create=true");
             Statement statement = conn.createStatement()) {

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

        } catch (SQLException e) {
            if (!DerbyHelper.tableAlreadyExists(e) && !DerbyHelper.schemaAlreadyExists(e)) {
                throw e;
            }
        }

        final int stopPort = Integer.parseInt(System.getProperty("bdj.db.signalPort", "11009"));
        SignalTransceiver.acceptAndWait(stopPort, (com, fut) -> {
            com.onReceive(Signal.SHUTDOWN, e -> {
                LOG.info("Received stop signal from " + e.getReplyAddr());
                fut.complete(e);
            });
        });

    }
}
