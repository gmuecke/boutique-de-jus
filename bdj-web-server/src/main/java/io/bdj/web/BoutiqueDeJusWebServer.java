package io.bdj.web;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 */
public class BoutiqueDeJusWebServer {

    public static void main(String... args) throws Exception {



        int threadpool_max = 80; //make configurable
        int threadpool_min = 10; //make configurable
        int http_port = 18080; //make configurable
        String webappWar;
        if(args.length > 0){
            webappWar = args[0];
        } else {
            webappWar = "./bdj-shop-web/target/web-app-1.0-SNAPSHOT.war";
        }

        final QueuedThreadPool threadPool = new QueuedThreadPool(threadpool_max, threadpool_min);
        final Server server = new Server(threadPool);

        final ServerConnector connector = new ServerConnector(server);
        connector.setPort(http_port);
        server.addConnector(connector);

        final WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar(webappWar);
//        webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*/[^/]*jstl.*\\.jar$");
        server.setHandler(webapp);

        Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
        classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration",
                           "org.eclipse.jetty.plus.webapp.EnvConfiguration",
                           "org.eclipse.jetty.plus.webapp.PlusConfiguration");
        classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                            "org.eclipse.jetty.annotations.AnnotationConfiguration");

        HashLoginService loginService = new HashLoginService("BoutiqueDeJusRealm");
        loginService.setConfig("jcgrealm.txt");
        server.addBean(loginService);


        server.start();
        server.join();
    }

}
