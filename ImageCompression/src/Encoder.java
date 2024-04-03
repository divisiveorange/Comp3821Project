import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Encoder {
    public static void main(String[] args) {
        for (var arg : args) {
            try {
                File inputFile = new File(arg);
                Pixel[][] pixels = convertToPixelArray(inputFile);
                encode(pixels, inputFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void encode(Pixel[][] pixels, String filename) {
        var leading = FrequencyTable.getLeading(pixels);
        var trailing = FrequencyTable.getTrailing(pixels);
        var table = new FrequencyTable(leading);
        var mostCommon = table.getNMostCommon(5);
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
        for (var commonLead : hashMap.keySet()) {
            var trails = hashMap.get(commonLead);
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
        SavableData savable = new SavableData(mapToTree, encoder.getTreeHead(), Bit.Compress(bitsList), pixels[0].length, pixels.length, filename);
        System.out.println("Array is " + savable.getBits().bytes().size() / (double) 1000000 + "mb");
        write(savable);
    }
    public static void write(SavableData savable) {
        try {
            File directory = new File("Encoded");
            if (!directory.exists()) {
                directory.mkdir();
            }
            FileOutputStream fileOutStream = new FileOutputStream("Encoded/Encoded_" + savable.getFilename());
            ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
            objectOutStream.writeObject(savable);
            System.out.println("File Written as: " + "Encoded/Encoded_" + savable.getFilename());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static Pixel[][] convertToPixelArray(File inputFile) throws IOException {
        BufferedImage image = ImageIO.read(inputFile);
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        Pixel[][] pixels2D = new Pixel[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels2D[y][x] = new Pixel(pixels[y * width + x]);
            }
        }
        return pixels2D;
    }
}
