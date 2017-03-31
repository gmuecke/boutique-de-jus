package io.bdj.control;

import java.net.InetSocketAddress;

import javafx.fxml.Initializable;

/**
 *
 */
public class DatabaseController extends ProcessController implements Initializable {

    @Override
    protected InetSocketAddress getServiceAddress() {

        //TODO receive this port from the service itself
        return new InetSocketAddress(localhost, 11009);
    }

    protected String getCommandLine(final String classpath) {

        //TODO add the bootstrap-controller's signal port as parameter (-Dsignal.hub=10008)
        return "java -cp \"" + classpath + "\" io.bdj.user.db.DerbyStandalone";
    }
}
