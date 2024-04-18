import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Map.entry;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class EncoderBenchmark {
    public static String outputName = "BenchmarkResult/output.md";

    public static void main(String[] args) {
        for (var arg : args) {
            try (Stream<Path> paths = Files.walk(Paths.get(arg))) {
                Set<String> resultKeys = new HashSet<>(Arrays.asList("decodeTime", "size", "rawSize"));
                Map<String, Long> sumOfResults = new HashMap<>();
                resultKeys.forEach(key -> sumOfResults.put(key, (long)0));
                AtomicInteger numImages = new AtomicInteger();

                Files.deleteIfExists(Path.of(outputName));
                OutputStream os = new FileOutputStream(outputName, false); // append mode
                Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                PrintWriter pwFile = new PrintWriter(writer);
                pwFile.write("# Benchmark Results\n\n");
                pwFile.close();

                paths
                        .filter(Files::isDirectory)
                        .forEach(folder -> {
                            if (String.valueOf(folder).equals(arg)) {
                                return;
                            }
                            Map<String, Long> resultOneFolder = mainPerImageFolder(folder, resultKeys);
                            assert resultOneFolder != null;
                            resultKeys.forEach(key -> sumOfResults.merge(key, resultOneFolder.get(key), Long::sum));
                            numImages.addAndGet(resultOneFolder.get("num").intValue());
                        });

                os = new FileOutputStream(outputName, true); // append mode
                writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                pwFile = new PrintWriter(writer);
                pwFile.write("\n## Sum of all " + numImages + " images\n");
                printForFolder(pwFile, resultKeys, sumOfResults, numImages);
                pwFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Prints summary of results for each subfolder, and whole benchmark set
     *
     * @param resultKeys set of keys
     * @param sumOfResults map between resultKeys and the result values
     * @param numImages the number of images benchmarked
     */
    private static void printForFolder(PrintWriter pwFile, Set<String> resultKeys, Map<String, Long> sumOfResults, AtomicInteger numImages) {
        pwFile.write("<pre>\n     decode ms   size MB   raw size MB     rate\n");
        printTimeAndRate(pwFile, sumOfResults, (long)4096);
        pwFile.write("Average\n");
        Map<String, Long> averageOfResults = new HashMap<>();
        resultKeys.forEach(key -> averageOfResults.put(key, sumOfResults.get(key)));
        resultKeys.forEach(key -> averageOfResults.merge(key, numImages.longValue(), Long::divideUnsigned));
        printTimeAndRate(pwFile, averageOfResults, (long)4096);
        pwFile.write("</pre>\n\n");
    }

    public static Map<String, Long> mainPerImageFolder(Path folder, Set<String> resultKeys) {
        try (Stream<Path> paths = Files.walk(Paths.get(String.valueOf(folder)))) {
            Map<String, Long> sumOfResults = new HashMap<>();
            resultKeys.forEach(key -> sumOfResults.put(key, (long)0));
            AtomicInteger numImagesInFolder = new AtomicInteger();

            OutputStream os = new FileOutputStream(outputName, true); // append mode
            Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            PrintWriter pwFile = new PrintWriter(writer);
            pwFile.write("## " + folder + "\n<pre>\n     decode ms   size MB   raw size MB     rate\n");
            pwFile.close();

            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        String fileType;
                        try {
                            fileType = Files.probeContentType(path);
                            if (fileType == null) {
                                return;
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (!fileType.startsWith("image")) {
                            return;
                        }

                        Long[] sizeDivRem = new Long[] {(long)0, (long)0};

                        File inputFile = new File(String.valueOf(path));
                        try {
                            Map<String, Long> resultOneFile = encode(
                                    convertToPixelArray(inputFile),
                                    inputFile.getName(),
                                    sizeDivRem
                            );
                            resultOneFile.put("size", resultOneFile.get("size") / 256);
                            resultOneFile.put("rawSize", resultOneFile.get("rawSize") / 256);
                            resultKeys.forEach(key -> sumOfResults.merge(key, resultOneFile.get(key), Long::sum));
                            numImagesInFolder.getAndIncrement();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

            os = new FileOutputStream(outputName, true); // append mode
            writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            pwFile = new PrintWriter(writer);
            pwFile.write("</pre>\n### Sum for " + folder + ", consisting of " + numImagesInFolder + " images\n");
            System.out.printf("Completed benchmarking the folder: %s\nTotal size of folder: %8.3f MB\n",
                    folder,
                    (float)sumOfResults.get("rawSize")/4096
            );
            printForFolder(pwFile, resultKeys, sumOfResults, numImagesInFolder);
            pwFile.close();

            sumOfResults.put("num", numImagesInFolder.longValue());
            return sumOfResults;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Encodes a single image
     *
     * @param pixelsAndDimensions contains pixel array and dimensions of image
     * @param filename to store in output
     */
    public static Map<String, Long> encode(PixelsAndDimensions pixelsAndDimensions, String filename, Long[] sizeDivRem) {
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
        SavableData savable = new SavableData(
                mapToTree,
                encoder.getTreeHead(),
                Bit.Compress(bitsList),
                pixelsAndDimensions.height(),
                pixelsAndDimensions.width(),
                filename
        );

        return new HashMap<>(write(savable, System.nanoTime() - startTime, sizeDivRem));
    }
    public static Map<String, Long> write(SavableData savable, long time, Long[] sizeDivRem) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(savable);
            oos.close();
            baos.close();
            Map<String, Long> resultMap = Map.ofEntries(
                    entry("decodeTime", time / 100),
                    entry("size", (baos.size() + sizeDivRem[0])),
                    entry("rawSize", (((long) savable.getHeight() * (long) savable.getWidth() * 3 + sizeDivRem[1])))
            );
            sizeDivRem[0] = (baos.size() + sizeDivRem[0]) % 256;
            sizeDivRem[1] = (((long) savable.getHeight() * (long) savable.getWidth() * 3 + sizeDivRem[1]) % 256);

            OutputStream os = new FileOutputStream(outputName, true); // append mode
            Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            PrintWriter pwFile = new PrintWriter(writer);
            pwFile.write(savable.getFilename() + "\n");
            printTimeAndRate(pwFile, resultMap, (long)Math.pow(1024, 2));
            pwFile.close();
            return resultMap;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void printTimeAndRate(PrintWriter pwFile, Map<String, Long> result, Long sizeDivider) {
        pwFile.write(String.format(
                "    %10.3f  %8.3f      %8.3f   %5.1f%%\n",
                (double)TimeUnit.NANOSECONDS.toMicros(result.get("decodeTime"))/10,
                (float)result.get("size")/sizeDivider,
                (float)result.get("rawSize")/sizeDivider,
                ((double)result.get("size")/(double)result.get("rawSize")) * 100.0
        ));
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
