package io.bdj.web;

import static java.util.logging.Logger.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
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
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.plus.jndi.EnvEntry;
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

    private static final byte[] EMPTY_MD5 = new byte[0];

    private static final Logger LOG = getLogger(BoutiqueDeJusWebServer.class.getName());

    public static void main(String... args) throws Exception {

        CommandLineParser cliParser = new DefaultParser();
        CommandLine cli = cliParser.parse(cliOptions(), args);

        Server server = createServer(cli);
        final Path warFilePath = Paths.get(cli.getOptionValue("war"));
        WebAppContext webapp = createWebApp(warFilePath);

        //TODO make the database url configurable
        server.addBean(new EnvEntry("databaseUrl","jdbc:derby://localhost:1527/testdb"));
        server.addBean(new EnvEntry("databaseDriver","org.apache.derby.jdbc.ClientDriver"));

        server.setHandler(webapp);
        server.start();

        final ExecutorService pool = Executors.newFixedThreadPool(1);
        final WatchService watcher = FileSystems.getDefault().newWatchService();
        final Path warPath = warFilePath.getParent();
        final WatchKey watchKey = warPath.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
        final CompletableFuture done = new CompletableFuture();
        final AtomicReference<byte[]> md5 = new AtomicReference<>(md5(warFilePath));

        pool.submit(() -> {
            while (!done.isDone() && watchKey.reset()) {
                watchKey.pollEvents()
                        .stream()
                        .map(event -> ((WatchEvent<Path>) event).context())
                        .map(warPath::resolve)
                        .filter(warFilePath::equals)
                        .filter(p -> filesize(p) > 0)
                        .findFirst()
                        .ifPresent(p -> {
                            waitUntilStable(p, 500, 2000);
                            final byte[] newMD5 = md5(p);
                            LOG.info("Checking updated war");
                            if (!Arrays.equals(md5.getAndSet(newMD5), newMD5)) {
                                try {
                                    restartWebServer(server, createWebApp(p));
                                } catch (Exception e) {
                                    LOG.log(Level.SEVERE, e, () -> "Server restart failed");
                                    done.complete(null);
                                }
                            }
                        });
                if (!sleep(500)) {
                    done.complete(null);
                }
            }
        });

        final int stopPort = Integer.parseInt(System.getProperty("bdj.web.signalPort", "11008"));
        SignalTransceiver.acceptAndWait(stopPort, (com, fut) -> com.onReceive(Signal.QUERY_STATUS, e -> {
            if (server.isRunning()) {
                com.send(Signal.STATUS_OK, e.getReplyAddr());
            }
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
        done.complete(null);
        pool.shutdown();
        server.join();

    }

    /**
     * Defines the CLI options for the web server.
     *
     * @return
     */
    private static Options cliOptions() {

        Options opts = new Options();
        opts.addOption("tpmx", "threadPoolMax", true, "Thread Pool Max size");
        opts.addOption("tpmn", "threadPoolMin", true, "Thread Pool Min size");
        opts.addOption("p", "port", true, "The http port to listen");
        opts.addRequiredOption("w", "war", true, "path to the war file to deploy");
        opts.addOption("jettyConfig", true, "path to jetty config file");
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
            int threadpool_max = Integer.parseInt(cli.getOptionValue("tpmx", "80"));
            int threadpool_min = Integer.parseInt(cli.getOptionValue("tpmn", "10"));
            int http_port = Integer.parseInt(cli.getOptionValue("p", "8080"));

            final QueuedThreadPool threadPool = new QueuedThreadPool(threadpool_max, threadpool_min);
            server = new Server(threadPool);

            final ServerConnector connector = new ServerConnector(server);
            //TODO config set accept queue size
            //TODO config acceptor num
            //TODO config selector num
            //TODO config idle timeout
            connector.setPort(http_port);
            server.addConnector(connector);

            Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
            classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration",
                               "org.eclipse.jetty.plus.webapp.EnvConfiguration",
                               "org.eclipse.jetty.plus.webapp.PlusConfiguration");
            classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                                "org.eclipse.jetty.annotations.AnnotationConfiguration");

            System.setProperty("jetty.jaas.login.conf", "login.conf");
            System.setProperty("jetty.base", ".");

            JAASLoginService loginService = new JAASLoginService();
            if(System.getProperty("java.security.auth.login.config") == null){
                System.setProperty("java.security.auth.login.config", "bdj-web-server/login.conf");
            }
            //TODO change to DB login
            loginService.setName("BoutiqueDeJusRealm");
//            loginService.setLoginModuleName("PropertyFile");
            //TODO support admin
            loginService.setLoginModuleName("Boutique");
            /*
            <New class="org.eclipse.jetty.jaas.JAASLoginService">
                <Set name="Name">BoutiqueDeJusRealm</Set>
                <Set name="LoginModuleName">PropertyFile</Set>
            </New>
            */
            server.addBean(loginService);
        }
        return server;
    }

    private static WebAppContext createWebApp(final Path webappWar) {

        final WebAppContext webapp = new WebAppContext();
        //webapp.setClassLoader(Thread.currentThread().getContextClassLoader());
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

    public static byte[] md5(Path file) {

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = Files.newInputStream(file);
                 DigestInputStream dis = new DigestInputStream(is, md)) {
                while (dis.read() != -1) {
                    //noop
                }
                return md.digest();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return EMPTY_MD5;
    }

    public static long filesize(Path p) {

        try {
            return Files.size(p);
        } catch (IOException e) {
            return -1;
        }
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
        long newSize = filesize(p);
        //wait until there are no more size increases
        do {
            initialSize = newSize;
            if (!sleep(intervalMs)) {
                break;
            }
            newSize = filesize(p);
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

    public static boolean sleep(long time) {

        try {
            Thread.sleep(time);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
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

    /**
     * Initializer to activate JSP support in Jetty.
     *
     * @return
     */
    private static List<ContainerInitializer> jspInitializers() {

        JettyJasperInitializer sci = new JettyJasperInitializer();
        ContainerInitializer initializer = new ContainerInitializer(sci, null);
        List<ContainerInitializer> initializers = new ArrayList<>();
        initializers.add(initializer);
        return initializers;
    }
}
