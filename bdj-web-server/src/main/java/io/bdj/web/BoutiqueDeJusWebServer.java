package io.bdj.web;

import static java.util.logging.Logger.getLogger;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import io.bdj.util.signals.Signal;
import io.bdj.util.signals.SignalTransceiver;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

/**
 *
 */
public class BoutiqueDeJusWebServer {

    private static final Logger LOG = getLogger(BoutiqueDeJusWebServer.class.getName());

    public static void main(String... args) throws Exception {

        CommandLineParser cliParser = new DefaultParser();
        CommandLine cli = cliParser.parse(cliOptions(), args);

        final Server server;
        if (cli.hasOption("jettyConfig")) {
            List<URL> configs = Arrays.asList(new File(cli.getOptionValue("jettyConfig")).toURI().toURL());
            Map<String, String> props = new HashMap<>();
            props.put("jetty.home", new File(System.getProperty("user.dir")).getCanonicalPath());

            server = loadServerFromConfigFile(configs, props);

        } else {
            //TODO make cli-configurable
            int threadpool_max = 80; //make configurable
            int threadpool_min = 10; //make configurable
            int http_port = 18080; //make configurable

            final QueuedThreadPool threadPool = new QueuedThreadPool(threadpool_max, threadpool_min);
            server = new Server(threadPool);

            final ServerConnector connector = new ServerConnector(server);
            connector.setPort(http_port);
            server.addConnector(connector);

            Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
            classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration",
                               "org.eclipse.jetty.plus.webapp.EnvConfiguration",
                               "org.eclipse.jetty.plus.webapp.PlusConfiguration");
            classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                                "org.eclipse.jetty.annotations.AnnotationConfiguration");

            HashLoginService loginService = new HashLoginService("BoutiqueDeJusRealm");
            loginService.setConfig("jcgrealm.txt");
            server.addBean(loginService);
        }

        String webappWar = cli.getOptionValue("war");
        final WebAppContext webapp = new WebAppContext();
        //TODO make context path configurable
        webapp.setContextPath("/");
        webapp.setWar(webappWar);
        server.setHandler(webapp);

        server.start();

        final int stopPort = Integer.parseInt(System.getProperty("bdj.web.stopPort", "11008"));
        try (SignalTransceiver com = SignalTransceiver.create(stopPort).start()) {
            final AtomicBoolean running = new AtomicBoolean(true);
            com.onReceive(Signal.SHUTDOWN, e -> {
                LOG.info("Received stop signal from " + e.getReplyAddr());
                running.set(false);
                try {
                    server.stop();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
            while (running.get()) {
                Thread.sleep(1000);
            }
        }
        server.join();
    }

    /**
     * Defines the CLI options for the web server.
     * @return
     */
    private static Options cliOptions() {

        Options opts = new Options();

        opts.addOption("jettyConfig", true, "path to jetty config file");
        opts.addRequiredOption("w", "war", true, "path to the war file to deploy");
        return opts;
    }

    /**
     * Initializes the jetty server from XML configuratoin
     * @param xmlConfigUrls
     * @param props
     * @return
     * @throws Exception
     */
    public static Server loadServerFromConfigFile(List<URL> xmlConfigUrls, Map<String, String> props) throws Exception {

        //TODO simplify this method

        XmlConfiguration last = null;
        // Hold list of configured objects
        Object[] obj = new Object[xmlConfigUrls.size()];

        // Configure everything
        for (int i = 0; i < xmlConfigUrls.size(); i++) {
            URL configURL = xmlConfigUrls.get(i);
            XmlConfiguration configuration = new XmlConfiguration(configURL);
            if (last != null) {
                // Let configuration know about prior configured objects
                configuration.getIdMap().putAll(last.getIdMap());
            }
            configuration.getProperties().putAll(props);
            obj[i] = configuration.configure();
            last = configuration;
        }

        // Find Server Instance.
        Server foundServer = null;
        int serverCount = 0;
        for (int i = 0; i < xmlConfigUrls.size(); i++) {
            if (obj[i] instanceof Server) {
                if (obj[i].equals(foundServer)) {
                    // Identical server instance found
                    continue; // Skip
                }
                foundServer = (Server) obj[i];
                serverCount++;
            }
        }

        if (serverCount <= 0) {
            throw new IllegalStateException("Load failed to configure a " + Server.class.getName());
        }

        if (serverCount == 1) {
            return foundServer;
        }

        throw new IllegalStateException(String.format("Configured %d Servers, expected 1", serverCount));
    }
}
