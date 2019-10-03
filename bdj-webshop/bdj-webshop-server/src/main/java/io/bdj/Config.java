package io.bdj;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 */
public final class Config {

  private static final Boolean USE_EMBEDDED_DB = Boolean.valueOf(System.getProperty("db.embdedded", "true"));
  private static final String DB_JDBC_URL = System.getProperty("db.jdbc.url", "jdbc:derby://localhost:1527/testdb");
  private static final String DB_JDBC_DRIVER = System.getProperty("db.jdbc.driver", "org.apache.derby.jdbc.ClientDriver");
  private static final Integer DERBY_THREADS = Integer.getInteger("db.derby.threads", 10);
  private static final Integer DERBY_TIMESLICE = Integer.getInteger("db.derby.timeslice", 5);
  private static final String DEPLOY_WAR = System.getProperty("http.jetty.warFile");
  private static final String JETTY_CONFIG_FILE = System.getProperty("http.jetty.configFile");
  private static final Integer HTTP_PORT = Integer.getInteger("http.port", 8080);
  private static final Integer HTTP_ACCEPTOR_QUEUE_SIZE = Integer.getInteger("http.jetty.acceptQueue.size", 0);
  private static final Integer HTTP_THREADS_MIN = Integer.getInteger("http.jetty.threads.min", 8);
  private static final Integer HTTP_THREADS_MAX = Integer.getInteger("http.jetty.threads.max", 32);
  private final JsonObject config;

  private Config(final JsonObject config){
    this.config = config;
  }

  public static Config defaultConfig(){
    return new Config(Json.createObjectBuilder().build());
  }

  public static Config fromJsonFile(Path jsonConfig){

    try(BufferedReader reader = Files.newBufferedReader(jsonConfig);
        JsonReader jsonReader = Json.createReader(reader)){
        JsonObject config = jsonReader.readObject();
        return new Config(config);

    } catch (IOException e) {
      throw new RuntimeException("Could not read file " + jsonConfig, e);
    }
  }


  public boolean useEmbeddedDatabase(){
    JsonObject db = config.getJsonObject("db");
    if(db != null){
      return db.getBoolean("embedded", USE_EMBEDDED_DB);
    }
    return USE_EMBEDDED_DB;
  }

  public String getDatabaseUrl() {

    JsonObject db = config.getJsonObject("db");
    if(db != null){
      JsonObject jdbc = config.getJsonObject("jdbc");
      return jdbc.getString("url", DB_JDBC_URL);
    }
    return DB_JDBC_URL;
  }

  public String getDatabaseDriver() {

    JsonObject db = config.getJsonObject("db");
    if(db != null){
      JsonObject jdbc = config.getJsonObject("jdbc");
      return jdbc.getString("driver", DB_JDBC_DRIVER);
    }
    return DB_JDBC_DRIVER;
  }

  public String getDeploymentWar() {
    JsonObject http = config.getJsonObject("http");
    if(http != null){
      JsonObject jetty = http.getJsonObject("jetty");
      if(jetty != null){
        return jetty.getString("warFile", DEPLOY_WAR);
      }
    }
    return DEPLOY_WAR;
  }

  public int getHttpPort() {
    JsonObject http = config.getJsonObject("http");
    if(http != null){
      return http.getInt("port", HTTP_PORT);
    }
    return HTTP_PORT;
  }

  public int getHttpAcceptorQueueSize() {
    JsonObject http = config.getJsonObject("http");
    if(http != null){
      JsonObject jetty = http.getJsonObject("jetty");
      if(jetty != null){
        return jetty.getInt("acceptQueueSize", HTTP_ACCEPTOR_QUEUE_SIZE);
      }
    }
    return HTTP_ACCEPTOR_QUEUE_SIZE;
  }

  public int getHttpMinThreads() {
    JsonObject http = config.getJsonObject("http");
    if(http != null){
      JsonObject jetty = http.getJsonObject("jetty");
      if(jetty != null){
        JsonObject threads = jetty.getJsonObject("threads");
        return threads.getInt("min", HTTP_THREADS_MIN);
      }
    }
    return HTTP_THREADS_MIN;
  }

  public int getHttpMaxThreads() {
    JsonObject http = config.getJsonObject("http");
    if(http != null){
      JsonObject jetty = http.getJsonObject("jetty");
      if(jetty != null){
        JsonObject threads = jetty.getJsonObject("threads");
        return threads.getInt("max", HTTP_THREADS_MAX);
      }
    }

    return HTTP_THREADS_MAX;
  }

  public String getJettyConfigFile() {
    JsonObject http = config.getJsonObject("http");
    if(http != null){
      JsonObject jetty = http.getJsonObject("jetty");
      if(jetty != null){
        return jetty.getString("configFile", JETTY_CONFIG_FILE);
      }
    }
    return JETTY_CONFIG_FILE;
  }

  public int getDerbyTimeSlice() {
    JsonObject db = config.getJsonObject("db");
    if(db != null){
      JsonObject derby = config.getJsonObject("derby");
      return derby.getInt("timeslice", DERBY_TIMESLICE);
    }
    return DERBY_TIMESLICE;
  }

  public int getDerbyThreads() {
    JsonObject db = config.getJsonObject("db");
    if(db != null){
      JsonObject derby = config.getJsonObject("derby");
      return derby.getInt("threads", DERBY_THREADS);
    }
    return DERBY_THREADS;
  }
}
