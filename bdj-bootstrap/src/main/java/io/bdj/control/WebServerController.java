package io.bdj.control;

import static java.util.logging.Logger.getLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.bdj.fx.SpinnerUtil;
import io.bdj.util.signals.Signal;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;

/**
 *
 */
public class WebServerController extends ProcessController {

    private static final Logger LOG = getLogger(WebServerController.class.getName());
    @FXML
    public ChoiceBox warChooser;
    @FXML
    public CheckBox debugMode;
    @FXML
    public Spinner<Integer> debugPort;
    @FXML
    public Spinner<Integer> httpPort;
    @FXML
    public Spinner<Integer> minThreads;
    @FXML
    public Spinner<Integer> maxThreads;

    private SocketAddress addr = new InetSocketAddress(localhost, 11008);

    public void restartServer() {

        updateProcessStatus(ProcessStatus.RESTARTING);
        com().send(Signal.RESTART, getServiceAddress());

    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        super.initialize(location, resources);
        com().onReceive(Signal.OK, e -> {
            if (addr.equals(e.getReplyAddr())) {
                updateProcessStatus(ProcessStatus.OK);
            }
        });

        try {
            warChooser.getItems()
                      .setAll(Files.find(Paths.get("."),
                                         4,
                                         (p, attr) -> Files.isReadable(p) && p.toString().endsWith(".war"))
                                   .collect(Collectors.toList()));
            warChooser.getSelectionModel().selectFirst();

        } catch (IOException e) {
            LOG.warning("Could not populate chooser list");
        }

        SpinnerUtil.initializeSpinner(debugPort, 1024, 65535, 1044);
        SpinnerUtil.initializeSpinner(httpPort, 1024, 65535, 8080);
        SpinnerUtil.initializeSpinner(minThreads, 10, 65535, 10);
        SpinnerUtil.initializeSpinner(maxThreads, 10, 65535, 80);

    }

    @Override
    protected SocketAddress getServiceAddress() {

        return addr;
    }

    @Override
    protected String getCommandLine(final String classpath) {

        //TODO add this to UI configuration
        return "java -cp \""
                + classpath
                + "\""
                + (debugMode.isSelected() ? " -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=" + debugPort.getValue() : "")
                + " -Djava.security.auth.login.config=login.conf"
                //TODO make memory params configurable
                + " -Xms128m"
                + " -Xmx768m"
                + " io.bdj.web.BoutiqueDeJusWebServer -w "+warChooser.getValue()
                + " -p " + httpPort.getValue()
                + " -tpmn " + minThreads.getValue()
                + " -tpmx " + maxThreads.getValue();

    }
}
