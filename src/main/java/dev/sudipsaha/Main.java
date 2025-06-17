package dev.sudipsaha;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.ByteBuffer;

public class Main extends Application {
    public static void main(String[] args) {
//        System.out.println("Hello, World!");
//       var result =  mylib_h.add(4,6);
//       var multiplyResult = mylib_h.multiply(4,6);
//       println(" result of addition is useing FFM" + result +
//               "result of multiplication using FFM" + multiplyResult);

       launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
//        stage.setTitle("Hello JavaFX!");
//        stage.setScene(new Scene(new Label("It works!"), 300, 200));
//        stage.show();


        Image image = new Image("Sample.png");
//        Image image = new Image("Sample2.jpg");
        WritableImage writableImage = new WritableImage(image.getPixelReader(),  (int) image.getWidth(), (int) image.getHeight());
        PixelReader reader = writableImage.getPixelReader();
        PixelWriter writer = writableImage.getPixelWriter();

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        ByteBuffer buffer = ByteBuffer.allocate(width * height * 3); // ARGB

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int argb = reader.getArgb(x, y);
                byte r = (byte)((argb >> 16) & 0xFF);
                byte g = (byte)((argb >> 8) & 0xFF);
                byte b = (byte)(argb & 0xFF);
                buffer.put(r).put(g).put(b);
            }
        buffer.flip();
        try {
//            NativeImageProcessor.invertImage(buffer);
            NativeImageProcessor.tintImage(buffer, (byte)128, (byte)128, (byte)255);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        buffer.flip();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int r = buffer.get() & 0xFF;
                int g = buffer.get() & 0xFF;
                int b = buffer.get() & 0xFF;
                int argb = (0xFF << 24) | (r << 16) | (g << 8) | b;
                writer.setArgb(x, y, argb);
            }

        VBox root = new VBox(new ImageView(writableImage));
        stage.setScene(new Scene(root));
        stage.setTitle("Image Inverter (C + FFM + JavaFX)");
        stage.show();
    }
}