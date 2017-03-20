package io.bdj;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

/**
 *
 */
public class BoutiqueDeJusBootstrap extends Application {

    public static void main(String... args) throws Exception {

        BoutiqueDeJusBootstrap.launch(BoutiqueDeJusBootstrap.class);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        Platform.setImplicitExit(false);
        stage.setWidth(640);
        stage.setHeight(480);
        stage.setX(10);
        stage.setY(10);
        stage.initStyle(StageStyle.DECORATED);
        stage.setOpacity(.25);
        stage.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> stage.setOpacity(1));
        stage.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, e -> stage.setOpacity(.25));
        stage.setScene(new Scene(new StackPane(), 640, 480, Color.AQUA));
        stage.show();

        Thread.sleep(1000);
        Notifications.create()
                     .title("Achievement unlocked")
                     .text("Bring up a Desktop Notification!")
                     .hideAfter(Duration.seconds(2))
                     .showWarning();
    }
}
