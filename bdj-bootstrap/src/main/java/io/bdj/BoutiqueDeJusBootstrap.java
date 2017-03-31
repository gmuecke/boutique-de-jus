package io.bdj;

import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toList;

import java.net.InetAddress;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.bdj.control.ProcessController;
import io.bdj.util.process.ConsolePipe;
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
    private ExecutorService processPool;
    private InetAddress localhost;
    private ConsolePipe consolePipe;
    private ScheduledExecutorService scheduler;
    private Deque<Process> processQueue;

    public static void main(String... args) throws Exception {

        BoutiqueDeJusBootstrap.launch(BoutiqueDeJusBootstrap.class);
    }

    @Override
    public void init() {

        this.com = SignalTransceiver.create(10111);
        this.com.startReceiving();
        this.processQueue = new ConcurrentLinkedDeque<>();
        this.scheduler = Executors.newScheduledThreadPool(2);

        //purge terminated processes
        this.scheduler.scheduleAtFixedRate(() -> processQueue.stream()
                                                             .filter(p -> !p.isAlive())
                                                             .collect(toList())
                                                             .forEach(process -> processQueue.remove(process)),
                                           1,
                                           5,
                                           TimeUnit.SECONDS);

        this.localhost = InetAddress.getLoopbackAddress();
        this.consolePipe = new ConsolePipe().consume();

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            this.processQueue.forEach(Process::destroy);
        }));
    }

    @Override
    public void start(final Stage stage) throws Exception {

        stage.setTitle("Boutique-de-jus Bootstrap Control");
        stage.initStyle(StageStyle.DECORATED);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        loader.setControllerFactory((c) -> {
            try {
                Object controller = c.newInstance();
                if (ProcessController.class.isAssignableFrom(c)) {
                    ((ProcessController) controller).init(this.com, this.consolePipe, this.scheduler, this.processQueue);
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

        super.stop();
        try (SignalTransceiver c = this.com;
             ConsolePipe pipe = this.consolePipe) {
        }

        this.processPool.shutdown();

    }
}
