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

    final Config config;
    if(args.length > 0){
      config = Config.fromJsonFile(Paths.get(args[0]));
    } else {
      config = Config.defaultConfig();
    }

    WebServer webServer = new WebServer(config).withWarfile(locateWarFile(config));

    if(config.useEmbeddedDatabase()){
      DerbyStandalone dbServer = new DerbyStandalone(config);
      dbServer.start();
    }
    webServer.start();

    while (true) {
      Thread.sleep(5000);
    }
  }

  private static Path locateWarFile(final Config config) {

    return Optional.ofNullable(config.getDeploymentWar())
                   .map(Paths::get)
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
