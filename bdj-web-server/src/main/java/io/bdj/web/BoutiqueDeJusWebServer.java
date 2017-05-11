package io.bdj.web;

import static java.util.logging.Logger.getLogger;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.bdj.config.ConfigChangeListener;
import io.bdj.config.Configuration;
import io.bdj.util.signals.Payloads;
import io.bdj.util.signals.Signal;
import io.bdj.util.signals.SignalTransceiver;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

/**
 *
 */
public class BoutiqueDeJusWebServer {

    private static final byte[] EMPTY_MD5 = new byte[0];

    private static final Logger LOG = getLogger(BoutiqueDeJusWebServer.class.getName());

    public static void main(String... args) throws Exception {

        CommandLineParser cliParser = new DefaultParser();
        CommandLine cli = cliParser.parse(cliOptions(), args);

        final int stopPort = Integer.parseInt(System.getProperty("bdj.web.signalPort", "11008"));

        final Server server = createServer(cli);
        final Path warFilePath = Paths.get(cli.getOptionValue("war"));
        final WebAppContext webapp = createWebApp(warFilePath);

        //TODO make the database url configurable
        server.addBean(new EnvEntry("databaseUrl", "jdbc:derby://localhost:1527/testdb"));
        server.addBean(new EnvEntry("databaseDriver", "org.apache.derby.jdbc.ClientDriver"));
        server.setHandler(webapp);
        server.start();

        try (FileWatcher watcher = new FileWatcher(warFilePath)) {
            final AtomicReference<byte[]> md5 = new AtomicReference<>(FileWatcher.md5(warFilePath));
            watcher.on(StandardWatchEventKinds.ENTRY_MODIFY, p -> {
                //TODO redesign the refresh cycle to prevent locks on file when building with maven
                /*
                 * An option would be to let the server run on a copy of the file (in a temp dir)
                 * and watch the maven output file. When the output changes, the server is stopped,
                 * the file copied and server restarted with the new file
                 */
                waitUntilStable(p, 500, 2000);
                final byte[] newMD5 = FileWatcher.md5(p);
                LOG.info("Checking updated war");
                if (!Arrays.equals(md5.getAndSet(newMD5), newMD5)) {
                    try {
                        restartWebServer(server, createWebApp(p));
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, e, () -> "Server restart failed");
                    }
                }
            });

            SignalTransceiver.acceptAndWait(stopPort, (com, fut) -> com.onReceive(Signal.QUERY_STATUS, e -> {
                if (server.isRunning()) {
                    com.send(Signal.STATUS_OK, e.getReplyAddr());
                }
            }).onReceive(Signal.SET, e -> {
                String[] nameValuePair = Payloads.nameValuePair(e.getPayload());
                LOG.info("Received setVal " + Arrays.toString(nameValuePair));
                Configuration.setProperty(nameValuePair[0], nameValuePair[1]);
            }).onReceive(Signal.RESTART, e -> {
                LOG.info("Restarting Server");
                try {

                    WebAppContext webApp = createWebApp(warFilePath);
                    restartWebServer(server, webApp);
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
                //TODO send init OK to hub
            }));
            server.join();
        }
    }

    /**
     * Defines the CLI options for the web server.
     *
     * @return
     */
    private static Options cliOptions() {

        Options opts = new Options();
        opts.addRequiredOption("w", "war", true, "path to the war file to deploy");
        opts.addOption("jettyConfig", true, "path to jetty config file");
        return opts;
    }

    private static Server createServer(final CommandLine cli) throws Exception {

        final Server server;
        if (cli.hasOption("jettyConfig")) {
            URI configFile = Paths.get(cli.getOptionValue("jettyConfig")).toUri();
            LOG.info("Initializing server using config file" + configFile);

            Map<String, String> props = new HashMap<>();
            props.put("jetty.home", new File(System.getProperty("user.dir")).getCanonicalPath());

            server = loadServerFromConfigFile(Paths.get(cli.getOptionValue("jettyConfig")).toUri(), props);

        } else {
            LOG.info("Initializing server using command line args");
            final QueuedThreadPool threadPool = createThreadPool();
            server = new Server(threadPool);
            attachServerConnector(server);

            org.eclipse.jetty.webapp.Configuration.ClassList classlist = org.eclipse.jetty.webapp.Configuration.ClassList
                    .setServerDefault(server);
            classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration",
                               "org.eclipse.jetty.plus.webapp.EnvConfiguration",
                               "org.eclipse.jetty.plus.webapp.PlusConfiguration");
            classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                                "org.eclipse.jetty.annotations.AnnotationConfiguration");

            //System.setProperty("jetty.jaas.login.conf", "login.conf");
            System.setProperty("jetty.base", ".");

            final JAASLoginService loginService = prepareJaasLoginModule();
            server.addBean(loginService);
        }
        return server;
    }

    private static WebAppContext createWebApp(final Path webappWar) {

        final WebAppContext webapp = new WebAppContext();
        webapp.setSystemClasses(new String[] {
                Configuration.class.getName(), ConfigChangeListener.class.getName()
        });
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
        webapp.setWar(webappWar.toString());
        return webapp;
    }

    /**
     * Waits until there is no more size increase within a given interval
     *
     * @param p
     * @param timeout
     */
    private static void waitUntilStable(final Path p, long intervalMs, final long timeout) {

        long timeoutAt = timeout + System.currentTimeMillis();
        long initialSize;
        long newSize = FileWatcher.filesize(p);
        //wait until there are no more size increases
        do {
            initialSize = newSize;
            if (!sleep(intervalMs)) {
                break;
            }
            newSize = FileWatcher.filesize(p);
        } while (newSize != -1 && initialSize < newSize && (timeoutAt > System.currentTimeMillis()));
    }

    private static void restartWebServer(final Server server, final WebAppContext webApp) throws Exception {

        server.stop();
        LOG.info("Sent stop");
        server.join();
        LOG.info("Server joined");
        LOG.info("Starting web server");
        server.setHandler(webApp);
        server.start();
        LOG.info("Server restarted");
    }

    /**
     * Initializes the jetty server from XML configuratoin
     *
     * @param configFile
     *         jetty configuration file. The file must configure a server, otherwise the method will fail
     * @param props
     *
     * @return the configured server instace
     *
     * @throws Exception
     */
    public static Server loadServerFromConfigFile(URI configFile, Map<String, String> props) throws Exception {

        XmlConfiguration configuration = new XmlConfiguration(configFile.toURL());
        configuration.getProperties().putAll(props);

        return (Server) configuration.configure();
    }

    /**
     * Creates a new thread pool for the server and hooks it's properties to the configuration events
     *
     * @return a new thread pool
     */
    private static QueuedThreadPool createThreadPool() {

        final QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(Configuration.getInteger("jetty.threads.min", 10));
        threadPool.setMaxThreads(Configuration.getInteger("jetty.threads.max", 80));
        Configuration.addListener(ConfigChangeListener.forConfigProperty("jetty.threads.min", (k, v) -> {
            threadPool.setMinThreads(Integer.parseInt(v));
            restartThreadPool(threadPool);
            LOG.info("Successfully resized threadpool " + k + "=" + v);
        }));
        Configuration.addListener(ConfigChangeListener.forConfigProperty("jetty.threads.max", (k, v) -> {
            threadPool.setMaxThreads(Integer.parseInt(v));
            restartThreadPool(threadPool);
            LOG.info("Successfully resized threadpool " + k + "=" + v);
        }));
        return threadPool;
    }

    /**
     * Attaches a server connector listening on the specified port to the server. Further the connector
     * is hooked into the configuration system to received runtime port changes
     *
     * @param server
     *         the Jetty server
     *
     * @return the created conector
     */
    private static ServerConnector attachServerConnector(final Server server) {

        int initialHttpPort = Configuration.getInteger("jetty.http.port", 8080);

        //TODO config set accept queue size
        //TODO config acceptor num
        //TODO config selector num
        //TODO config idle timeout
        final ServerConnector connector = new ServerConnector(server);
        connector.setPort(initialHttpPort);
        server.addConnector(connector);

        Configuration.addListener(ConfigChangeListener.forConfigProperty("jetty.http.port", (k, v) -> {
            LOG.info("Changing http port to " + v);
            connector.setPort(Integer.parseInt(v));
            try {
                connector.stop();
                connector.start();
                LOG.info("HTTP Port changed");
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Restarting connector failed", e);
            }
        }));
        return connector;
    }

    /**
     * Creeates a the JAAS login module and sets up the configuration
     * @return
     *  the new jaas login module
     */
    private static JAASLoginService prepareJaasLoginModule() {

        final JAASLoginService loginService = new JAASLoginService();
        if (System.getProperty("java.security.auth.login.config") == null) {
            URL jaasConfigURL = BoutiqueDeJusWebServer.class.getResource("/login.conf");
            if (jaasConfigURL != null) {
                System.setProperty("java.security.auth.login.config", jaasConfigURL.toString());
            }
        }
        loginService.setName("BoutiqueDeJusRealm");
        loginService.setLoginModuleName("Boutique");
        return loginService;
    }

    /**
     * Initializer to activate JSP support in Jetty.
     *
     * @return list of initializer to support jsp compiling
     */
    private static List<ContainerInitializer> jspInitializers() {

        final JettyJasperInitializer sci = new JettyJasperInitializer();
        final ContainerInitializer initializer = new ContainerInitializer(sci, null);
        final List<ContainerInitializer> initializers = new ArrayList<>();
        initializers.add(initializer);
        return initializers;
    }

    public static boolean sleep(long time) {

        try {
            Thread.sleep(time);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * Restarts the web servers thread pool
     *
     * @param threadPool
     */
    private static void restartThreadPool(final QueuedThreadPool threadPool) {

        try {
            LOG.info("Restarting threadpool");
            threadPool.stop();
            threadPool.start();
            LOG.info("Thread pool restarted");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to resize thread pool");
        }
    }
}
