package io.bdj.db;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.bdj.Config;
import org.apache.derby.drda.NetworkServerControl;
import org.slf4j.Logger;

/**
 *
 */
public class DerbyStandalone {

  private static final Logger LOG = getLogger(DerbyStandalone.class);
  private final Config config;
  private NetworkServerControl server;

  public DerbyStandalone(final Config config) {

    this.config = config;

  }

  public void start() throws Exception {

    System.setProperty("derby.system.home", ".");
    System.setProperty("derby.authentication.provider", "BUILTIN");
    System.setProperty("derby.user.admin", "admin");

    this.server = new NetworkServerControl(InetAddress.getByName("localhost"), 1527);
    server.start(new PrintWriter(new LogWriter()));

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
    server.setTimeSlice(config.getDerbyTimeSlice());
    server.setMaxThreads(config.getDerbyThreads());

    initDB();
  }

  public void stop() throws Exception {

    this.server.shutdown();
  }

  private void initDB() throws SQLException {

    //init the driver
    DriverManager.registerDriver(new org.apache.derby.jdbc.ClientDriver());

    try (Connection conn = DriverManager.getConnection("jdbc:derby://localhost:1527/testdb;create=true")) {

      LOG.info("Initialize DB");
      DBInitializer.createTable(conn);

      LOG.info("Initialize Product Table");
      ProductInitializer.populateProducts(conn);

    }
  }

  private static class LogWriter extends Writer {

    private static final Logger LOG = getLogger("DerbyDB");

    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {

      if(len > 0){
        LOG.info(new String(cbuf, off, len).trim());
      }
    }

    @Override
    public void flush() throws IOException {
      //noop
    }

    @Override
    public void close() throws IOException {
      //noop
    }
  }
}
