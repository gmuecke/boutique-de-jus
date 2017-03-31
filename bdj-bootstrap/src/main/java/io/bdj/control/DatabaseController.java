package io.bdj.control;

import static java.util.logging.Logger.getLogger;

import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

import io.bdj.util.signals.Signal;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 */
public class DatabaseController extends ProcessController implements Initializable {

    private static final Logger LOG = getLogger(DatabaseController.class.getName());

    private final int MAX_LINES = 50;

    @FXML
    public Button startButton;
    @FXML
    public Button stopButton;
    @FXML
    public Circle status;
    @FXML
    public TextFlow consoleArea;

    private Process dbProcess;

    public void stopServer(ActionEvent actionEvent) {

        System.out.println("Stop DB");
        com().send(Signal.SHUTDOWN, new InetSocketAddress(localhost, 11009));

        stopButton.setDisable(true);
        getScheduler().schedule(() -> {
            if (dbProcess != null && !dbProcess.isAlive()) {
                startButton.setDisable(false);
                stopButton.setDisable(true);
            } else {
                stopButton.setDisable(false);
            }
        }, 5, TimeUnit.SECONDS);

    }

    public void startServer(ActionEvent actionEvent) {

        System.out.println("Start DB");

        String classpath = System.getProperty("java.class.path");

        this.dbProcess = startProcess("java -cp \"" + classpath + "\" io.bdj.user.db.DerbyStandalone");
        final Charset iso = Charset.forName("ISO-8859-1");
        Consumer<ByteBuffer> outUpdater = b -> {
            Text text = new Text(iso.decode(b).toString());
            text.setFill(Color.YELLOW);
            Platform.runLater(() -> {
                consoleArea.getChildren().add(text);
                while (consoleArea.getChildren().size() > MAX_LINES) {
                    consoleArea.getChildren().remove(0, consoleArea.getChildren().size() - MAX_LINES);
                }
            });
        };
        Consumer<ByteBuffer> errUpdater = b -> {
            Text text = new Text(iso.decode(b).toString());
            text.setFill(Color.RED);
            Platform.runLater(() -> {
                consoleArea.getChildren().add(text);
                while (consoleArea.getChildren().size() > MAX_LINES) {
                    consoleArea.getChildren().remove(0, consoleArea.getChildren().size() - MAX_LINES);
                }
            });
        };
        consolePipe().register(dbProcess, errUpdater, outUpdater);

        stopButton.setDisable(!dbProcess.isAlive());
        startButton.setDisable(dbProcess.isAlive());

    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

    }

}
