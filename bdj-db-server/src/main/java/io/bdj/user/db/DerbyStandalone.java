package io.bdj.user.db;

import static java.util.logging.Logger.getLogger;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.bdj.util.signals.Payloads;
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
        final PrintWriter out = new PrintWriter(System.out);
        server.start(out);

        LOG.info("Waiting for server to be started");
        for (int i = 0; i < 10; ++i) {
            try {
                LOG.info("Pinging server ... ");
                server.ping();
                break;
            } catch (Exception e) {
                LOG.warning("Server is not started yet ..., retrying after 20 ms");
            }
            Thread.sleep(20);
        }

        server.setTimeSlice(Integer.getInteger("db.timeslice", 5));
        server.setMaxThreads(Integer.getInteger("db.threads", 10));

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

        final int stopPort = Integer.parseInt(System.getProperty("bdj.db.signalPort", "11009"));
        SignalTransceiver.acceptAndWait(stopPort, (com, fut) -> com.onReceive(Signal.SHUTDOWN, e -> {
            LOG.info("Received stop signal from " + e.getReplyAddr());
            try {
                server.shutdown();
            } catch (Exception e1) {
                LOG.log(Level.SEVERE, e1, () -> "Server Shutdown failed");
            } finally {
                fut.complete(e);
            }
        }).onReceive(Signal.RESTART, e -> {
            LOG.info("Restarting Server");
            try {
                server.shutdown();
                server.start(out);
                com.send(Signal.OK, e.getReplyAddr());
            } catch (Exception e1) {
                LOG.log(Level.SEVERE, e1, () -> "Restart failed, stopping server");
                fut.complete(e);
            }
        }).onReceive(Signal.SET, e -> {
            LOG.info("Reconfiguring server: " + e);
            String[] nameValuePair = Payloads.nameValuePair(e.getPayload());
            switch(nameValuePair[0]){
                case "threads":
                    try {
                        server.setMaxThreads(Integer.valueOf(nameValuePair[1]));
                    } catch (Exception e1) {
                        LOG.log(Level.SEVERE, e1, () -> "Failed to change Threads to " + nameValuePair[1]);
                    }
                    break;
                case "tslice":
                    try {
                        server.setTimeSlice(Integer.valueOf(nameValuePair[1]));
                    } catch (Exception e1) {
                        LOG.log(Level.SEVERE, e1, () -> "Failed to change TimeSlice to " + nameValuePair[1]);
                    }
                    break;
            }

        }));

    }
}
