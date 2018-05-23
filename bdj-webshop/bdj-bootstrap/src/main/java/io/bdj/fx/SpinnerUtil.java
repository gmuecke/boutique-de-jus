package io.bdj.fx;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;

/**
 * Created on 05.04.2017.
 */
public final class SpinnerUtil {

    private SpinnerUtil() {}

    public  static void initializeSpinner(final Spinner<Integer> spinner, final int minValue, final int maxValue, final int initialValue) {
        spinner.getEditor().setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP:
                    spinner.increment(1);
                    break;
                case DOWN:
                    spinner.decrement(1);
                    break;
            }
        });
        spinner.setOnScroll(e -> {
            spinner.increment((int) (e.getDeltaY() / e.getMultiplierY()));
        });

        SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue, initialValue);
        spinner.setValueFactory(factory);
        spinner.setEditable(true);

        TextFormatter<Integer> formatter = new TextFormatter<>(factory.getConverter(), factory.getValue());
        spinner.getEditor().setTextFormatter(formatter);
        factory.valueProperty().bindBidirectional(formatter.valueProperty());

    }
}
