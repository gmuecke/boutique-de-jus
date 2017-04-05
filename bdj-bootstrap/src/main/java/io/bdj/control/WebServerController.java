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

import io.bdj.util.signals.Signal;
import javafx.event.ActionEvent;
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
    public Spinner debugPort;

    private SocketAddress addr = new InetSocketAddress(localhost, 11008);

    public void restartServer(ActionEvent actionEvent) {

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
                + " io.bdj.web.BoutiqueDeJusWebServer -w "+warChooser.getValue();

    }
}
