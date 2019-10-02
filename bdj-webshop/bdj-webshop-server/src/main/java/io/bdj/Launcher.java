package io.bdj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import io.bdj.db.DerbyStandalone;
import io.bdj.web.WebServer;

/**
 *
 */
public class Launcher {

  public static void main(String... args) throws Exception {

    DerbyStandalone dbServer = new DerbyStandalone();
    WebServer webServer = new WebServer().withWarfile(locateWarFile());

    dbServer.start();
    webServer.start();

    while (true) {
      Thread.sleep(5000);
    }
  }

  private static Path locateWarFile() throws IOException {

    return Optional.ofNullable(System.getProperty("jetty.deploy.war"))
                   .map(s -> Paths.get(s))
                   .orElseGet(() -> {
      try {
        return Files.find(Paths.get("."), 4, (p, attr) -> Files.isReadable(p) && p.toString().endsWith(".war"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No war file found"));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

  }

}
