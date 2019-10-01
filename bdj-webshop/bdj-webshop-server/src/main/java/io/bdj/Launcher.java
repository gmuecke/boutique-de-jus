package io.bdj;

import io.bdj.db.DerbyStandalone;
import io.bdj.web.WebServer;

/**
 *
 */
public class Launcher {

  public static void main(String... args) throws Exception {

    DerbyStandalone dbServer = new DerbyStandalone();
    WebServer webServer = new WebServer();

    dbServer.start();
    webServer.start();

    while(true){
      Thread.sleep(5000);
    }
  }
}
