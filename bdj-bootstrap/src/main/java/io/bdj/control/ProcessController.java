package io.bdj.control;

import static io.bdj.util.signals.Signal.STATUS_OK;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import io.bdj.util.process.ConsolePipe;
import io.bdj.util.process.ProcessManager;
import io.bdj.util.signals.Signal;
import io.bdj.util.signals.SignalTransceiver;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 */
public abstract class ProcessController implements Initializable {

    protected final InetAddress localhost = InetAddress.getLoopbackAddress();

    @FXML
    public Button startButton;
    @FXML
    public Button stopButton;
    @FXML
    public Button restartButton;
    @FXML
    public Circle processStatus;
    @FXML
    public TextFlow consoleArea;
    @FXML
    public ScrollPane scrollPane;

    //property to track if the started process is running
    private BooleanProperty processRunning = new SimpleBooleanProperty(false);

    private ObjectProperty<Color> consoleStdColor = new SimpleObjectProperty<>(Color.YELLOW);
    private ObjectProperty<Color> consoleErrColor = new SimpleObjectProperty<>(Color.RED);
    private int consoleTextElements = 50;
    private Charset charset = Charset.forName("ISO-8859-1");

    private SignalTransceiver com;
    private ConsolePipe console;
    private ScheduledExecutorService scheduler;
    private ProcessManager processManager;

    /**
     * Init method that is invoked during application initialization to share common facilities.
     *
     * @param com
     *         the communication hub for sending and receiving signals from the managed processes
     * @param console
     * @param scheduler
     * @param processManager
     */
    public void setup(SignalTransceiver com,
                      ConsolePipe console,
                      final ScheduledExecutorService scheduler,
                      final ProcessManager processManager) {

        this.com = com;
        this.console = console;
        this.scheduler = scheduler;
        this.processManager = processManager;
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        startButton.disableProperty().bind(processRunning);
        stopButton.disableProperty().bind(processRunning.not());
        restartButton.disableProperty().bind(processRunning.not());
        processRunning.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                processStatus.setFill(Color.GREEN);
            } else {
                processStatus.setFill(Color.RED);
            }
        });
        consoleArea.getChildren().addListener((ListChangeListener<Node>) c -> {
            final ObservableList<Node> textElements = consoleArea.getChildren();
            while (textElements.size() > consoleTextElements) {
                textElements.remove(0, textElements.size() - consoleTextElements);
        }
        });
        //TODO support mouse scroll on console area
        scrollPane.vvalueProperty().bind(consoleArea.heightProperty());

    }


    public void stopServer(ActionEvent actionEvent) {

        com().send(Signal.SHUTDOWN, getServiceAddress());
    }

    protected abstract SocketAddress getServiceAddress();
    protected abstract String getCommandLine(final String classpath);
    protected String getClasspath() {

        return System.getProperty("java.class.path");
    }

    public void startServer(ActionEvent actionEvent) {

        processStatus.setFill(Color.YELLOW);

        final ProcessManager pm = getProcessManager();

        final Process process = pm.startProcess(getCommandLine(getClasspath()));

        //bind the process' out streams to the console area
        getConsolePipe().register(process, consoleUpdater(consoleErrColor), consoleUpdater(consoleStdColor));

        //register the process end handler to receive notification when process dies
        pm.addProcessEndListener(process, p -> processRunning.set(false));
        pm.observe(process, getServiceAddress(), sig -> {
            if(sig == STATUS_OK){
                updateProcessStatus(ProcessStatus.OK);
            }
        });

        //notify UI by updating the propery
        processRunning.set(process.isAlive());
    }

    protected Consumer<ByteBuffer> consoleUpdater(ObjectProperty<Color> colorBinding){
        return  b -> {
            Text text = new Text(getCharset().decode(b).toString());
            text.fillProperty().bind(colorBinding);
            //need to use platform here as the consumer is invoked by the console pipe's thread
            Platform.runLater(() -> consoleArea.getChildren().add(text));
        };
    }

    protected SignalTransceiver com() {

        return com;
    }

    protected ConsolePipe getConsolePipe() {

        return console;
    }

    protected ScheduledExecutorService getScheduler() {

        return scheduler;
    }

    protected ProcessManager getProcessManager() {

        return processManager;
    }

    public Charset getCharset() {

        return charset;
    }

    public void setCharset(final Charset charset) {

        this.charset = charset;
    }

    public Color getConsoleStdColor() {

        return consoleStdColor.get();
    }

    public ObjectProperty<Color> consoleStdColorProperty() {

        return consoleStdColor;
    }

    public void setConsoleStdColor(final Color consoleStdColor) {

        this.consoleStdColor.set(consoleStdColor);
    }

    public Color getConsoleErrColor() {

        return consoleErrColor.get();
    }

    public ObjectProperty<Color> consoleErrColorProperty() {

        return consoleErrColor;
    }

    public void setConsoleErrColor(final Color consoleErrColor) {

        this.consoleErrColor.set(consoleErrColor);
    }

    public int getConsoleTextElements() {

        return consoleTextElements;
    }

    public void setConsoleTextElements(final int consoleTextElements) {

        this.consoleTextElements = consoleTextElements;
    }

    public void updateProcessStatus(ProcessStatus newStatus){
        Platform.runLater(() -> this.processStatus.setFill(newStatus.getColor()));
    }

    public enum ProcessStatus {
        OK(Color.GREEN),
        STOPPED(Color.RED),
        RESTARTING(Color.YELLOW)
        ;

        private final Color color;

        ProcessStatus(final Color color) {
            this.color = color;
        }

        Color getColor() {

            return color;
        }
    }
}
