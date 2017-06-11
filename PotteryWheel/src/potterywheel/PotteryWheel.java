/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package potterywheel;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 *
 * @author jfellows
 */
public class PotteryWheel extends Application {

    boolean play = true; // whether or not the animation is running. This needs to be a field so it doesn't throw an "variable must be effectively final" error when I try to change it in a lambda

    // The size of the main drawing area (both x and y of the full rectangle)
    final int CANVAS_SIZE = 900;

    // The distance in between each gridline for the background
    final int GRID_SIZE = 30;

    // Initialize the main canvas and its graphics context, which is used for drawing
    Canvas canvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
    GraphicsContext gc = canvas.getGraphicsContext2D();

    // Setup a way to "spin" or repeatedly rotate the canvas
    Timeline rotate = new Timeline(new KeyFrame(
            Duration.millis(5),
            e -> {
                canvas.setRotate(canvas.getRotate() + 1);
            }));

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Pottery Wheel");

        // Start the spinning at its base rate
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.play();

        // Create a circle to apply as a clipping mask to the canvas
        // This is what makes it a spinning wheel, not square
        Circle c = new Circle(CANVAS_SIZE / 2, CANVAS_SIZE / 2, CANVAS_SIZE / 2); // Circle with a diameter the size of the canvas, in the middle of the canvas
        canvas.setClip(c);

        // Initialize the starting color and width of the pen
        gc.setStroke(Color.RED);
        gc.setLineWidth(2);

        // Draw the background grid
        drawGrid();

        // And set it up so that I can draw
        setupDrawing();

        // A BorderPane contains different sections already arranged in a layout, such as left, center, right, etc.
        BorderPane all = new BorderPane();

        all.setLeft(createLeftPanel()); // This has the clear button
        all.setCenter(createMainPanel()); // The wheel and speed controls
        all.setRight(createRightPanel()); // And the pen controls

        // Display the content
        Group root = new Group();
        root.getChildren().add(all);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private VBox createLeftPanel() { // Returns a VBox with the left panel of my application
        // This button wipes the current drawing
        Button clear = new Button("Clear");
        clear.setOnAction((e) -> {
            gc.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE); // It clears the entire screen
            drawGrid(); // Then puts the grid back, so that it looks like only your drawing was erased
        });

        // Add this to a VBox
        VBox left = new VBox();
        left.getChildren().add(clear);

        return left;
    }

    private VBox createMainPanel() { // Returns a VBox with the main panel of my application
        Text rate = new Text("The rate is: 1.0"); // This will display the rate of the spinning wheel

        // These three buttons will control the speed
        Button slower = new Button("<");
        Button stop = new Button("Stop"); // This button will toggle in between stop and start, depending on the state of the animation
        Button faster = new Button(">");

        stop.setOnAction((e) -> { // When the button is clicked
            if (play) { // If the wheel is spinning (this is a stop button)
                rotate.stop(); // Halt the rotation
                stop.setText("Start"); // Now it's a start button
                updateRateCounter(rate, 0); // Have the rate counter reflect this
                slower.setDisable(true); // Make the other speed controls unusable, as you cannot ajust the speed while the wheel is stopped
                faster.setDisable(true);

            } else { // This is a start button
                rotate.play(); // Start the rotation
                stop.setText("Stop"); // Now it's a stop button
                updateRateCounter(rate, rotate.getCurrentRate()); // Reset the rate counter
                slower.setDisable(false); // Enable the other speed controls
                faster.setDisable(false);
            }
            play = !play; // Toggle the state
        });

        slower.setOnAction((e) -> { // When the slower button is clicked
            double currentRate = rotate.getCurrentRate(); // wheel's current speed
            if (faster.isDisable()) { // When we click slower, and faster is disabled, we should reenable it
                faster.setDisable(false);
            }
            if (currentRate > .3) { // If we're not bottomed out (this means that we can go down to a rate of .2)
                rotate.setRate(currentRate - .2); // decrease the rate
            } else {
                slower.setDisable(true); // If we are below the threshold, disable this button
            }
            updateRateCounter(rate, rotate.getCurrentRate()); // Update the rate counter
        });

        faster.setOnAction((e) -> {
            double currentRate = rotate.getCurrentRate();
            if (slower.isDisable()) { // When we click faster, and slower is disabled, we should reenable it
                slower.setDisable(false);
            }
            if (currentRate < 9.9) { // If we're not at the max (this meanas that we can go up to a rate of 10)
                rotate.setRate(currentRate + .2); // increase the rate
            } else {
                faster.setDisable(true); // If we are above the threshold, disable this button
            }
            updateRateCounter(rate, rotate.getCurrentRate()); // Update the rate counter
        });

        // Create the button bar with the controls
        HBox bottom = new HBox(10); // The 10 is spacing which is applied between each button
        bottom.getChildren().addAll(slower, stop, faster);
        bottom.setAlignment(Pos.CENTER); // This centers the buttons

        // Create the center panel
        VBox main = new VBox();
        main.getChildren().addAll(canvas, bottom, rate); // The rate counter is not centered, and on the bottom of the screen

        return main;
    }

    private VBox createRightPanel() { // Returns a VBox with the right panel of my application
        // Create our color picker, which is a defualt system dialog which allows the user to pick the pen color
        ColorPicker colorPicker = new ColorPicker(Color.RED); // Starts on red, like the pen
        colorPicker.getStyleClass().add("split-button"); // Button that opens menu

        // Create a slider which lets users change the pen tip width
        Slider width = new Slider(0, 12, 2); // The value can be between 0 and 12, and starts at 2, just like the pen width 
        width.setShowTickMarks(true);
        width.setShowTickLabels(true);
        width.setMajorTickUnit(2);
        width.setBlockIncrement(1);
        width.setOrientation(Orientation.VERTICAL);

        // On this canvas, display a circle the size and color of the pen tip, so that the user can see what they are drawing with
        Canvas showPen = new Canvas(115, 150);
        GraphicsContext gcpen = showPen.getGraphicsContext2D(); // This is the graphics context of the canvas
        showPenTipOnCanvas(gcpen); // Initialize the pen tip display with the current pen tip

        colorPicker.setOnAction((e) -> { // When someone picks a new color
            Color col = colorPicker.getValue();
            gc.setStroke(col); // Set the stroke to that color
            showPenTipOnCanvas(gcpen); // Update the pen tip display
        });

        width.valueProperty().addListener((observableValue, sliderValue, newValue) -> { // When the pen tip width slider is changed
            gc.setLineWidth(sliderValue.doubleValue()); // Set the line width to the value of the slider
            showPenTipOnCanvas(gcpen); // Update the pen tip display
        });

        // Make an HBox with the width slider and the canvas to display the pen tip
        HBox chooseWidth = new HBox();
        chooseWidth.getChildren().addAll(showPen, width);

        // Make the right panel VBox
        VBox right = new VBox(10); // The 10 is spacing applied between each elemenent
        right.getChildren().addAll(colorPicker, chooseWidth);

        return right;
    }

    private void drawGrid() { // Draw the background grid on the wheel
        // Rememeber the past pen settings, so that we can reset the pen back to what it was after we change the settings to draw the grid
        Paint pastStroke = gc.getStroke();
        double pastWidth = gc.getLineWidth();

        // Fill the canvas with an off-white color
        gc.setFill(Color.SNOW);
        gc.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        // Draw the actual grid (dark grey and thin lines)
        gc.setStroke(Color.DARKSLATEGREY);
        gc.setLineWidth(.25);
        for (int i = 0; i < CANVAS_SIZE; i += GRID_SIZE) { // On increments of grid size, across the canvas
            gc.strokeLine(0, i, CANVAS_SIZE, i); // Draw a line from the x axis across the canvas
            gc.strokeLine(i, 0, i, CANVAS_SIZE); // Draw a line from the y axis across the canvas
        }

        gc.strokeOval((CANVAS_SIZE / 2) - (GRID_SIZE / 2), (CANVAS_SIZE / 2) - (GRID_SIZE / 2), GRID_SIZE, GRID_SIZE); // Draw a little circle on the middle of the grid

        // Revert the past settings
        gc.setStroke(pastStroke);
        gc.setLineWidth(pastWidth);
    }

    private void setupDrawing() { // Set up drawing functionality
        canvas.setOnMousePressed((e) -> { // When the mouse is pressed
            gc.beginPath(); // Start the path
            gc.moveTo(e.getX(), e.getY()); // Move the start of the path to the mouse position
            gc.stroke(); // Start stroking
        });

        canvas.setOnMouseDragged((e) -> { // When the mouse is moved, while the button is pushed
            gc.lineTo(e.getX(), e.getY()); // Extend the line to the mouse position
            gc.stroke(); // Stroke that line
        });
    }

    private void showPenTipOnCanvas(GraphicsContext pengc) { // Update the canvas displaying the pen width, 
        double width = gc.getLineWidth() * 2; // This makes the pen tip displayed better match what the user sees

        pengc.clearRect(0, 0, 115, 150); // Clear the canvas
        pengc.setFill(gc.getStroke()); // Set the fill to the stroke of main canvas
        pengc.fillOval((115 - width) / 2, (150 - width) / 2, width, width); // Draw the tip in the middle of the canvas
    }

    private void updateRateCounter(Text text, double rate) { // Update the passed rate counter to the passed rate
        text.setText("The rate is: " + String.format("%.2g", rate)); // This rounds the rate to 2 decimal places
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
