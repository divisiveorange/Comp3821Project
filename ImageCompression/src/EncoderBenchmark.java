import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static java.util.Map.entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class EncoderBenchmark {
    public static void main(String[] args) {
        for (var arg : args) {
            try (Stream<Path> paths = Files.walk(Paths.get(arg))) {
                paths
                        .filter(Files::isDirectory)
                        .forEach(folder -> {
                            if (String.valueOf(folder).equals("Images/BenchmarkImages")) {
                                return;
                            }
                            System.out.print("\n" + folder + "\n");
                            System.out.print("  decode ms   size kB   raw size kB    rate\n");
                            try (Stream<Path> paths2 = Files.walk(Paths.get(String.valueOf(folder)))) {
                                paths2
                                        .filter(Files::isRegularFile)
                                        .forEach(path -> {
                                            if (String.valueOf(path.getFileName()).equals("LICENSE.txt")) {
                                                return;
                                            }
                                            System.out.print(path.getFileName() + "\n");
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
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void encode(PixelsAndDimensions pixelsAndDimensions, String filename) {
        var startTime = System.nanoTime();

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
        write(savable, TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime));
    }
    public static void write(SavableData savable, long time) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(savable);
            oos.close();
            baos.close();
            printTimeAndRate(Map.ofEntries(
                    entry("decodeTime", time),
                    entry("size", (long)baos.size()),
                    entry("rawSize", ((long)savable.getHeight() * (long)savable.getWidth() * 3))
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void printTimeAndRate(Map<String, Long> result) {
        System.out.printf(
                "   %8.3f  %8.3f      %8.3f   %4.1f%%\n",
                (double)result.get("decodeTime")/1000,
                result.get("size")/Math.pow(1024, 2),
                result.get("rawSize")/Math.pow(1024, 2),
                ((double)result.get("size")/(double)result.get("rawSize")) * 100.0
        );
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
