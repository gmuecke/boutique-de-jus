package io.bdj.control;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ResourceBundle;

import io.bdj.util.signals.Signal;
import javafx.event.ActionEvent;

/**
 *
 */
public class WebServerController extends ProcessController {

    private SocketAddress addr = new InetSocketAddress(localhost, 11008);
    @Override
    protected SocketAddress getServiceAddress() {

        return addr;
    }

    @Override
    protected String getCommandLine(final String classpath) {

        //TODO add this to UI configuration
        return "java -cp \"" + classpath + "\" io.bdj.web.BoutiqueDeJusWebServer -w ./target/wars/bdj-shop-web.war -jettyConfig ./jetty.xml";
    }

    public void restartServer(ActionEvent actionEvent) {
        updateProcessStatus(ProcessStatus.RESTARTING);
        com().send(Signal.RESTART, getServiceAddress());

    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        super.initialize(location, resources);
        com().onReceive(Signal.OK, e -> {
            if(addr.equals(e.getReplyAddr())){
                updateProcessStatus(ProcessStatus.OK);
            }
        });
    }
}
