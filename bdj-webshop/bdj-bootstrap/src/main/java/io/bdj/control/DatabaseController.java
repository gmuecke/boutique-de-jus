package io.bdj.control;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ResourceBundle;

import io.bdj.fx.SpinnerUtil;
import io.bdj.util.signals.Signal;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;

/**
 *
 */
public class DatabaseController extends ProcessController implements Initializable {

    @FXML
    public Spinner<Integer> timeSlice;
    @FXML
    public Spinner<Integer> threads;

    private SocketAddress addr = new InetSocketAddress(localhost, 11009);

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        super.initialize(location, resources);
        SpinnerUtil.initializeSpinner(timeSlice, 1, 65535, 500);
        SpinnerUtil.initializeSpinner(threads, 10, 65535, 10);

        threads.valueProperty().addListener((obs, ov, nv) -> {
            com().send(Signal.SET, "threads=" + threads.getValue(), getServiceAddress());
        });
        timeSlice.valueProperty().addListener((obs, ov, nv) -> {
            com().send(Signal.SET, "tslice=" + timeSlice.getValue(), getServiceAddress());
        });

        com().onReceive(Signal.OK, e -> {
            if (addr.equals(e.getReplyAddr())) {
                updateProcessStatus(ProcessStatus.OK);
            }
        });

    }

    @Override
    protected SocketAddress getServiceAddress() {
        return addr;
    }

    protected String getCommandLine(final String classpath) {

        //TODO add the bootstrap-controller's signal port as parameter (-Dsignal.hub=10008)
        return "java -cp \"" + classpath + "\""
                + " -Ddb.timeslice=" + timeSlice.getValue()
                + " -Ddb.threads=" + threads.getValue()
                + " io.bdj.user.db.DerbyStandalone";
    }

    public void restartServer(ActionEvent actionEvent) {
        updateProcessStatus(ProcessStatus.RESTARTING);
        com().send(Signal.RESTART, getServiceAddress());
    }
}
