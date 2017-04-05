package io.bdj.web;

import static java.util.logging.Logger.getLogger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.bdj.util.signals.Signal;
import io.bdj.util.signals.SignalTransceiver;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
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

        Server server = createServer(cli);
        WebAppContext webapp = createWebApp(cli);

        server.setHandler(webapp);
        server.start();

        final int stopPort = Integer.parseInt(System.getProperty("bdj.web.signalPort", "11008"));
        SignalTransceiver.acceptAndWait(stopPort, (com, fut) -> com.onReceive(Signal.QUERY_STATUS, e -> {
            if (server.isRunning()) {
                com.send(Signal.STATUS_OK, e.getReplyAddr());
            }
        }).onReceive(Signal.RESTART, e -> {
            LOG.info("Restarting Server");
            try {
                server.stop();
                LOG.info("Sent stop");
                server.join();
                LOG.info("Server joined");

                LOG.info("Starting web server");
                server.setHandler(createWebApp(cli));
                server.start();
                LOG.info("Server restarted");
                com.send(Signal.OK, e.getReplyAddr());
            } catch (Exception e1) {
                LOG.log(Level.SEVERE, e1, () -> "Restart failed, stopping server");
                fut.complete(e);
            }

        }).onReceive(Signal.SHUTDOWN, e -> {
            LOG.info("Received stop signal from " + e.getReplyAddr());
            fut.complete(e);
            try {
                server.stop();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }));
        server.join();
    }

    /**
     * Defines the CLI options for the web server.
     *
     * @return
     */
    private static Options cliOptions() {

        Options opts = new Options();

        opts.addOption("jettyConfig", true, "path to jetty config file");
        opts.addRequiredOption("w", "war", true, "path to the war file to deploy");
        return opts;
    }

    private static Server createServer(final CommandLine cli) throws Exception {

        final Server server;
        if (cli.hasOption("jettyConfig")) {
            LOG.info("Initializing server using config file");
            List<URL> configs = Arrays.asList(new File(cli.getOptionValue("jettyConfig")).toURI().toURL());
            Map<String, String> props = new HashMap<>();
            props.put("jetty.home", new File(System.getProperty("user.dir")).getCanonicalPath());

            server = loadServerFromConfigFile(configs, props);

        } else {
            LOG.info("Initializing server using command line args");
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
        return server;
    }

    private static WebAppContext createWebApp(final CommandLine cli) {

        String webappWar = cli.getOptionValue("war");
        final WebAppContext webapp = new WebAppContext();
        //TODO make context path configurable
        webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                            ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$");

		/*
         * Configure the application to support the compilation of JSP files.
		 * We need a new class loader and some stuff so that Jetty can call the
		 * onStartup() methods as required.
		 */
        webapp.setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
        webapp.setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
        webapp.addBean(new ServletContainerInitializersStarter(webapp), true);

        webapp.setContextPath("/");
        webapp.setWar(webappWar);
        return webapp;
    }

    /**
     * Initializes the jetty server from XML configuratoin
     *
     * @param xmlConfigUrls
     * @param props
     *
     * @return
     *
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

    private static List<ContainerInitializer> jspInitializers() {

        JettyJasperInitializer sci = new JettyJasperInitializer();
        ContainerInitializer initializer = new ContainerInitializer(sci, null);
        List<ContainerInitializer> initializers = new ArrayList<>();
        initializers.add(initializer);
        return initializers;
    }

    public static class JspStarter extends AbstractLifeCycle
            implements ServletContextHandler.ServletContainerInitializerCaller {

        JettyJasperInitializer sci;
        ServletContextHandler context;

        public JspStarter(ServletContextHandler context) {

            this.sci = new JettyJasperInitializer();
            this.context = context;
            this.context.setAttribute("org.apache.tomcat.JarScanner", new StandardJarScanner());
        }

        @Override
        protected void doStart() throws Exception {

            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(context.getClassLoader());
            try {
                sci.onStartup(null, context.getServletContext());
                super.doStart();
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }
}
