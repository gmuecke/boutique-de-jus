package io.bdj;

import static java.util.logging.Logger.getLogger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import io.bdj.control.ProcessController;
import io.bdj.util.process.ConsolePipe;
import io.bdj.util.process.ProcessManager;
import io.bdj.util.signals.SignalTransceiver;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
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

    private SignalTransceiver com;
    private ConsolePipe consolePipe;
    private ScheduledExecutorService scheduler;
    private ProcessManager processManager;

    public static void main(String... args) throws Exception {

        BoutiqueDeJusBootstrap.launch(BoutiqueDeJusBootstrap.class);
    }

    @Override
    public void init() {

        this.scheduler = Executors.newScheduledThreadPool(2);
        this.com = SignalTransceiver.create(10111);
        this.consolePipe = new ConsolePipe();
        this.processManager = new ProcessManager(this.scheduler);
    }

    @Override
    public void start(final Stage stage) throws Exception {

        this.com.start();
        this.consolePipe.open();
        this.processManager.start();

        stage.setTitle("Boutique-de-jus Bootstrap Control");
        stage.initStyle(StageStyle.DECORATED);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        loader.setControllerFactory((c) -> {
            try {
                Object controller = c.newInstance();
                if (ProcessController.class.isAssignableFrom(c)) {
                    ((ProcessController) controller).setup(this.com,
                                                           this.consolePipe,
                                                           this.scheduler,
                                                           this.processManager);
                }
                return controller;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });

        TabPane tabPane = loader.load();

        Scene scene = new Scene(tabPane, 1024, 768, Color.AQUA);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        Thread.sleep(1000);
        Notifications.create()
                     .title("Achievement unlocked")
                     .text("Bring up a Desktop Notification!")
                     .hideAfter(Duration.seconds(2))
                     .showWarning();
    }

    @Override
    public void stop() throws Exception {

        this.com.close();

        super.stop();
        try (SignalTransceiver c = this.com;
             ConsolePipe pipe = this.consolePipe) {
        }
    }
}
