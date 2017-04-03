package io.bdj.control;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 *
 */
public class WebServerController extends ProcessController {

    @Override
    protected SocketAddress getServiceAddress() {

        return new InetSocketAddress(localhost, 11008);
    }

    @Override
    protected String getCommandLine(final String classpath) {

        return "java -cp \"" + classpath + "\" io.bdj.web.BoutiqueDeJusWebServer -w ./target/wars/bdj-shop-web.war -jettyConfig ./jetty.xml";
    }
}
