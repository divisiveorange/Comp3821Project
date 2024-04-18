import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

public class Encoder {
    public static void main(String[] args) {
        for (var arg : args) {
            try (Stream<Path> paths = Files.walk(Paths.get(arg))) {
                paths
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            System.out.print(path);
                            File inputFile = new File(String.valueOf(path));
                            try {
                                encode(
                                        convertToPixelArray(inputFile),
                                        inputFile.getName()
                                );
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void encode(PixelsAndDimensions pixelsAndDimensions, String filename) {
        var pixels = pixelsAndDimensions.pixels();
        var leading = FrequencyTable.getLeading(pixels);
        var trailing = FrequencyTable.getTrailing(pixels);
        var table = new FrequencyTable(leading);
        var mostCommon = table.getNMostCommon(100);
        HashMap<Short, ArrayList<Short>> hashMap = new HashMap<>();
        for (var common : mostCommon) {
            hashMap.put(common, new ArrayList<>());
        }
        for (int i = 0; i < leading.size(); i++) {
            if (hashMap.containsKey(leading.get(i))) {
                hashMap.get(leading.get(i)).add(trailing.get(i));
            }
        }
        HashMap<Short, HuffmanEncoder> mapToEncoder = new HashMap<>();
        var count = 0;
        for (var commonLead : hashMap.keySet()) {
            var trails = hashMap.get(commonLead);
            count += (new FrequencyTable(trails)).getSorted().size();
            var encoder = new HuffmanEncoder((new FrequencyTable(trails)).getSorted());
            mapToEncoder.put(commonLead, encoder);
        }
        var encoder = new HuffmanEncoder(table.getSorted());
        var bitsMap = encoder.getMapToBits();
        ArrayList<Bit> bitsList = new ArrayList<>();
        for (int i = 0; i < leading.size(); i++) {
            bitsList.addAll(bitsMap.get(leading.get(i)));
            if (mapToEncoder.containsKey(leading.get(i))) {
                bitsList.addAll((mapToEncoder.get(leading.get(i))).getMapToBits().get(trailing.get(i)));
            } else {
                bitsList.addAll(Bit.ShortToBits(trailing.get(i)));
            }
        }
        HashMap<Short, TreeNode> mapToTree = new HashMap<>();
        for (var commonLead : mapToEncoder.keySet()) {
            mapToTree.put(commonLead, mapToEncoder.get(commonLead).getTreeHead());
        }
        SavableData savable = new SavableData(mapToTree, encoder.getTreeHead(), Bit.Compress(bitsList), pixelsAndDimensions.height(), pixelsAndDimensions.width(), filename);
        System.out.println("Array is " + savable.getBits().bytes().size() / (double) 1000000 + "MB");
        System.out.println("Compressed " + count + table.getSorted().size() + " sets of bits");
        write(savable);
    }
    public static void write(SavableData savable) {
        try {
            File directory = new File("Encoded");
            if (!directory.exists()) {
                directory.mkdir();
            }
            FileOutputStream fileOutStream = new FileOutputStream("Encoded/" + savable.getFilename() + ".encoded");
            ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
            objectOutStream.writeObject(savable);
            System.out.println("File Written as: " + "Encoded/" + savable.getFilename() + ".encoded");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static PixelsAndDimensions convertToPixelArray(File inputFile) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        Pixel[] pixels1D = new Pixel[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            pixels1D[i] = new Pixel(pixels[i]);
        }
        return new PixelsAndDimensions(flipper(pixels1D, width, height), width, height);
    }
    public static Pixel[] flipper(Pixel[] original, int width, int height) {
        Pixel[][] pixels = new Pixel[height][width];
        int i = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y][x] = original[i];
                i++;
            }
        }
        Pixel[][] flipped2D = new Pixel[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                flipped2D[x][height-1-y] = pixels[y][x];
            }
        }
        i = 0;
        Pixel[] returnable = new Pixel[width*height];
        for (var row : flipped2D) {
            for (var pixel : row) {
                returnable[i] = pixel;
                i++;
            }
        }
        return returnable;
    }
}
