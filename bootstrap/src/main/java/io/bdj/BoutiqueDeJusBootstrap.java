package io.bdj;

import static java.util.logging.Logger.getLogger;

import java.net.InetSocketAddress;
import java.util.logging.Logger;

import io.bdj.util.Signal;
import io.bdj.util.SignalReceiver;
import io.bdj.util.SignalSender;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

/**
 *
 */
public class BoutiqueDeJusBootstrap extends Application {

    private static final Logger LOG = getLogger(BoutiqueDeJusBootstrap.class.getName());

    private SignalSender signalSender;
    private SignalReceiver signalReceiver;

    public static void main(String... args) throws Exception {

        BoutiqueDeJusBootstrap.launch(BoutiqueDeJusBootstrap.class);
    }



    @Override
    public void init() {
        this.signalSender = SignalSender.newSender();
        this.signalReceiver = SignalReceiver.newReceiver(10111);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        Platform.setImplicitExit(false);
        stage.setWidth(640);
        stage.setHeight(480);
        stage.setX(10);
        stage.setY(10);
        stage.initStyle(StageStyle.DECORATED);
        //TODO make opacity configurable
//        stage.setOpacity(.75);
        stage.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> stage.setOpacity(1));
        stage.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, e -> stage.setOpacity(.25));

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(addHBox());

        stage.setScene(new Scene(borderPane, 640, 480, Color.AQUA));
        stage.show();

        Thread.sleep(1000);
        Notifications.create()
                     .title("Achievement unlocked")
                     .text("Bring up a Desktop Notification!")
                     .hideAfter(Duration.seconds(2))
                     .showWarning();
    }

    public HBox addHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");

        Button button = new Button("Stop DB");
        button.setOnAction(e -> {
            LOG.info("Click!");
            this.signalSender.send(Signal.SHUTDOWN, new InetSocketAddress("localhost", 11009));
        });
        button.setPrefSize(100, 20);
        hbox.getChildren().addAll(button);

        return hbox;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if(signalSender != null) {
            signalSender.close();
        }
        if(signalReceiver != null) {
            signalReceiver.close();
        }
    }
}
