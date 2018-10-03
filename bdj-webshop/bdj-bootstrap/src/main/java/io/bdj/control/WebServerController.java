package io.bdj.control;

import static java.util.logging.Logger.getLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
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
    public ChoiceBox<Path> warChooser;
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
    @FXML
    public Spinner<Integer> xmx;
    @FXML
    public Spinner<Integer> xms;
    @FXML
    public ChoiceBox<String> xmxUnit;
    @FXML
    public ChoiceBox<String> xmsUnit;
    @FXML
    public Spinner<Integer> printerCount;
    @FXML
    public ChoiceBox<String> printJobSizeUnit;
    @FXML
    public Spinner<Integer> printJobSize;
    @FXML
    public Spinner<Integer> printerDuration;

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
                      .setAll(findWarFiles());
            warChooser.getSelectionModel().selectFirst();

        } catch (IOException e) {
            LOG.warning("Could not populate chooser list");
        }
        bind(warChooser, "jetty.deploy.war", ChoiceBox::getValue);

        SpinnerUtil.initializeSpinner(debugPort, 1024, 65535, 1044);

        SpinnerUtil.initializeSpinner(httpPort, 1024, 65535, 8080);
        bind(httpPort, "jetty.http.port", Spinner::getValue);

        SpinnerUtil.initializeSpinner(minThreads, 10, 65535, 10);
        SpinnerUtil.initializeSpinner(maxThreads, 10, 65535, 80);
        bind(minThreads, "jetty.threads.min", Spinner::getValue);
        bind(maxThreads, "jetty.threads.max", Spinner::getValue);


        SpinnerUtil.initializeSpinner(xmx, 1, 65535, 512);
        SpinnerUtil.initializeSpinner(xms, 1, 65535, 128);
        this.xmsUnit.getItems().addAll("", "K", "M", "G");
        this.xmsUnit.setValue("M");
        this.xmxUnit.getItems().addAll("", "K", "M", "G");
        this.xmxUnit.setValue("M");

        //setup the printer service
        SpinnerUtil.initializeSpinner(printerCount, 0, 255, 1);
        SpinnerUtil.initializeSpinner(printerDuration, 1, 3600, 60);
        SpinnerUtil.initializeSpinner(printJobSize, 1, 1024 * 1024, 1);
        this.printJobSizeUnit.getItems().addAll("", "K", "M", "G");
        this.printJobSizeUnit.setValue("M");

        bind(printerCount, "bdj.qpos.count", Spinner::getValue);
        bind(printerDuration, "bdj.qpos.printTimeS", Spinner::getValue);
        bind(printJobSize, "bdj.qpos.jobSize", s -> calculateJobSize(s.getValue(), this.printJobSizeUnit.getValue()));
        bind(printJobSizeUnit,
             "bdj.qpos.jobSize",
             s -> calculateJobSize(this.printJobSize.getValue(), String.valueOf(s.getValue())));


    }

    private List<Path> findWarFiles() throws IOException {

        return Files.find(Paths.get("."),
                          4,
                          (p, attr) -> Files.isReadable(p) && p.toString().endsWith(".war"))
                    .collect(Collectors.toList());
    }

    @Override
    protected SocketAddress getServiceAddress() {

        return addr;
    }

    @Override
    protected String getCommandLine(final String classpath) {

        //TODO improve building the command line / or use the process builder
        return "java -cp \""
                + classpath
                + "\""
                + (debugMode.isSelected() ? " -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address="
                + debugPort.getValue() : "")
                + " -Xms"
                + this.xms.getValue()
                + this.xmsUnit.getValue()
                + " -Xmx"
                + this.xmx.getValue()
                + this.xmxUnit.getValue()
                + " -Djetty.threads.min="
                + minThreads.getValue()
                + " -Djetty.threads.max="
                + maxThreads.getValue()
                + " -Djetty.http.port="
                + httpPort.getValue()
                + " -Djetty.deploy.war="
                + warChooser.getValue()
                + " -Dbdj.qpos.count="
                + this.printerCount.getValue()
                + " -Dbdj.qpos.jobSize="
                + calculateJobSize(this.printJobSize.getValue(), this.printJobSizeUnit.getValue())
                + " -Dbdj.qpos.printTimeS="
                + this.printerDuration.getValue()
                + " io.bdj.web.BoutiqueDeJusWebServer ";

    }

    private long calculateJobSize(long base, String unit) {

        long factor;
        switch (unit) {
            case "K":
                factor = 1024;
                break;
            case "M":
                factor = 1024 * 1024;
                break;
            case "G":
                factor = 1024 * 1024 * 1024;
                break;
            default:
                factor = 1;
        }

        return base * factor;
    }

    private <T, E extends Spinner<?>> void bind(E spinner, String configName, Function<E, T> value) {

        spinner.valueProperty().addListener((obs, ov, nv) -> setVal(configName, value.apply(spinner)));
    }

    private <T, E extends ChoiceBox<?>> void bind(E choiceBox, String configName, Function<E, T> value) {

        choiceBox.valueProperty().addListener((obs, ov, nv) -> setVal(configName, value.apply(choiceBox)));
    }

    private <T> void setVal(String name, T value) {

        LOG.info("setting " + name + "=" + value);
        com().send(Signal.SET, name + "=" + value, getServiceAddress());
    }
}