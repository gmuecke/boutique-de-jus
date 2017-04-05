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

        System.setProperty("derby.system.home", "testdb");
        NetworkServerControl server = new NetworkServerControl(InetAddress.getByName("localhost"), 1527);
        server.start(new PrintWriter(System.out));
        server.setTimeSlice(5);
        server.setMaxThreads(10);

        try (Connection conn = DriverManager.getConnection("jdbc:derby://localhost:1527/testDB;create=true");
             Statement statement = conn.createStatement()) {

            //create the users table
            statement.executeUpdate("CREATE TABLE USERS (NAME VARCHAR(255), NUMBER INTEGER )");

        } catch (SQLException e) {
            if (!DerbyHelper.tableAlreadyExists(e)) {
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
