package io.bdj;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 *
 */
public final class Config {

  private final JsonObject config;

  private Config(final JsonObject config) {

    this.config = config;
  }

  public static Config defaultConfig() {

    return new Config(Json.createObjectBuilder().build());
  }

  public static Config fromJsonFile(Path jsonConfig) {

    try (BufferedReader reader = Files.newBufferedReader(jsonConfig);
         JsonReader jsonReader = Json.createReader(reader)) {
      JsonObject config = jsonReader.readObject();
      return new Config(config);

    } catch (IOException e) {
      throw new RuntimeException("Could not read file " + jsonConfig, e);
    }
  }

  public boolean useEmbeddedDatabase() {

    return db().filter(p -> p.containsKey("embedded"))
               .map(d -> d.getBoolean("embedded"))
               .orElseGet(() -> Boolean.valueOf(System.getProperty("db.embdedded", "true")));
  }

  public String getDatabaseUrl() {

    return jdbc().filter(p -> p.containsKey("url"))
                 .map(j -> j.getString("url", null))
                 .orElseGet(() -> System.getProperty("db.jdbc.url", "jdbc:derby://localhost:1527/testdb"));
  }

  public String getDatabaseDriver() {

    return db().filter(p -> p.containsKey("driver"))
               .map(d -> d.getString("driver", null))
               .orElseGet(() -> System.getProperty("db.jdbc.driver", "org.apache.derby.jdbc.ClientDriver"));
  }

  public String getDeploymentWar() {

    return jetty().filter(p -> p.containsKey("warFile"))
                  .map(j -> j.getString("warFile", null))
                  .orElseGet(() -> System.getProperty("http.jetty.warFile"));
  }

  public int getHttpPort() {

    return http().filter(p -> p.containsKey("port"))
                 .map(h -> h.getInt("port"))
                 .orElseGet(() -> Integer.getInteger("http.port", 8080));
  }

  public int getHttpAcceptorQueueSize() {

    return jetty().filter(p -> p.containsKey("acceptQueueSize"))
                  .map(j -> j.getInt("acceptQueueSize"))
                  .orElseGet(() -> Integer.getInteger("http.jetty.acceptQueue" + ".size", 0));
  }

  public int getHttpMinThreads() {

    return jetty_threads().filter(p -> p.containsKey("min"))
                          .map(t -> t.getInt("min"))
                          .orElseGet(() -> Integer.getInteger("http.jetty.threads.min", 8));
  }

  public int getHttpMaxThreads() {

    return jetty_threads().filter(p -> p.containsKey("max"))
                          .map(t -> t.getInt("max"))
                          .orElseGet(() -> Integer.getInteger("http.jetty.threads.max", 32));
  }

  public String getJettyConfigFile() {

    return jetty().filter(p -> p.containsKey("configFile"))
                  .map(j -> j.getString("configFile", null))
                  .orElseGet(() -> System.getProperty("http.jetty.configFile"));
  }

  public int getDerbyTimeSlice() {

    return derby().filter(p -> p.containsKey("timeslice"))
                  .map(d -> d.getInt("timeslice"))
                  .orElseGet(() -> Integer.getInteger("db.derby.timeslice", 5));
  }

  public int getDerbyThreads() {

    return derby().filter(p -> p.containsKey("threads"))
                  .map(d -> d.getInt("threads"))
                  .orElseGet(() -> Integer.getInteger("db.derby.threads", 10));
  }

  private Optional<JsonObject> http() {

    return Optional.ofNullable(config.getJsonObject("http"));
  }

  private Optional<JsonObject> db() {

    return Optional.ofNullable(config.getJsonObject("db"));
  }

  private Optional<JsonObject> jdbc() {

    return db().filter(p -> p.containsKey("jdbc")).map(db -> db.getJsonObject("jdbc"));
  }

  private Optional<JsonObject> jetty() {

    return http().filter(p -> p.containsKey("jetty")).map(http -> http.getJsonObject("jetty"));
  }

  private Optional<JsonObject> jetty_threads() {

    return jetty().filter(p -> p.containsKey("threads")).map(jetty -> jetty.getJsonObject("threads"));
  }

  private Optional<JsonObject> derby() {

    return db().filter(p -> p.containsKey("derby")).map(db -> db.getJsonObject("derby"));
  }
}
