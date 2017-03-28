package io.bdj.user.db;

import static java.util.logging.Logger.getLogger;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import io.bdj.util.Signal;
import io.bdj.util.SignalReceiver;
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

        final int stopPort = Integer.parseInt(System.getProperty("bdj.db.stopPort", "11009"));
        try (SignalReceiver rcv = SignalReceiver.newReceiver(stopPort)) {
            final AtomicBoolean running = new AtomicBoolean(true);
            rcv.onReceive(Signal.SHUTDOWN, (s, a) -> {
                LOG.info("Received stop signal from " + a);
                running.set(false);
            });
            while (running.get()) {
                Thread.sleep(1000);
            }
        }

        //Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
        /*
        try (Connection connect = DriverManager.getConnection("jdbc:derby://localhost:1527/testdb;create=true");
             Statement statement = connect.createStatement()) {

            //statement.executeUpdate("CREATE TABLE USERS (NAME VARCHAR(255), NUMBER INTEGER )");
            statement.executeUpdate("INSERT INTO USERS VALUES ('TIM', 1)");
            statement.executeUpdate("INSERT INTO USERS VALUES ('TOM', 2)");
            statement.executeUpdate("INSERT INTO USERS VALUES ('TONY', 3)");
            statement.executeUpdate("INSERT INTO USERS VALUES ('TINO', 4)");

            ResultSet resultSet = connect.prepareStatement("SELECT * from USERS").executeQuery();
            while (resultSet.next()) {
                String user = resultSet.getString("name");
                String number = resultSet.getString("number");
                System.out.println("User: " + user);
                System.out.println("ID: " + number);
            }
        }
        */
    }
}
