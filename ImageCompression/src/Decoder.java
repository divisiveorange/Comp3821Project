import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
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
                System.err.println(arg);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found");
                throw new RuntimeException(e);
            }
        }
    }
    public static void decode(SavableData data) {
        ArrayList<Integer> pixels = new ArrayList<>(data.getHeight()*data.getWidth());
        var bits = Bit.Expand(data.getBits());
        LinkedList<Bit> queue = new LinkedList<>();
        for (var bit : bits) {
            queue.add(new Bit(bit));
        }
        var headNode = data.getHeadNode();
        var mapToTrails = data.getMapToTrails();
        while (!queue.isEmpty()) {
            TreeXplorer xplorer = new TreeXplorer(headNode);
            while (!xplorer.isEnd()) {
                xplorer.advance(queue.removeFirst());
            }
            var leading = xplorer.getContent();
            short trailing;
            if (mapToTrails.containsKey(leading)) {
                TreeXplorer trailXplorer = new TreeXplorer(mapToTrails.get(leading));
                while (!trailXplorer.isEnd()) {
                    trailXplorer.advance(queue.removeFirst());
                }
                trailing= trailXplorer.getContent();

            } else {
                ArrayList<Bit> trailingBits = new ArrayList<>();
                try {
                    for (int i = 0; i < 12; i++) {
                        trailingBits.add(queue.removeFirst());
                    }
                } catch (Exception e) {

                }
                trailing = Bit.bitsToShort(trailingBits);
            }
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
            ImageIO.write(image, "png", new File("Decoded/Decoded_"+ filename));
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }
}
