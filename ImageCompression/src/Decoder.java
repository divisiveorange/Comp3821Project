import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Decoder {
    private Decoder() {};

    public static void main(String[] args) {
        for (var arg : args) {
            try {
                FileInputStream fileInStream = new FileInputStream(arg);
                ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
                SavableData data = (SavableData) objectInStream.readObject();
                decode(data);
            } catch (IOException e) {
                System.err.println("Error reading image: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void decode(SavableData data) {
        ArrayList<Integer> pixels = new ArrayList<>(data.getHeight()*data.getWidth());
        var bits = Bit.Expand(data.getBits());
        LinkedList<Bit> queue = new LinkedList<>(bits);
        var headNode = data.getHeadNode();
        var mapToTrails = data.getMapToTrails();
        while (!queue.isEmpty()) {
            TreeXplorer xplorer = new TreeXplorer(headNode);
            while (!xplorer.isEnd()) {
                xplorer.advance(queue.removeFirst());
            }
            var leading = Bit.bitsToShort(xplorer.getBits());
            ArrayList<Bit> trailingBits;
            if (mapToTrails.containsKey(leading)) {
                TreeXplorer trailXplorer = new TreeXplorer(mapToTrails.get(leading));
                while (!trailXplorer.isEnd()) {
                    trailXplorer.advance(queue.removeFirst());
                }
                trailingBits = trailXplorer.getBits();

            } else {
                trailingBits = new ArrayList<>();
                for (int i = 0; i < 12; i++) {
                    trailingBits.add(queue.removeFirst());
                }
            }
            var trailing = Bit.bitsToShort(trailingBits);
            pixels.add((new Pixel(leading, trailing)).getInt());
        }
        write(pixels, data.getWidth(), data.getHeight(), data.getFilename());
    }
    public static void write(ArrayList<Integer> pixels, int width, int height, String filename) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] intPixels = Arrays.stream(pixels.toArray()).mapToInt(a -> (int) a).toArray();
        image.setRGB(0, 0, width, height, intPixels, 0, width);
        try {
            File directory = new File("Decoded");
            if (!directory.exists()) {
                directory.mkdir();
            }
            ImageIO.write(image, "png", new File("Decoded_"+ filename));
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }
}
