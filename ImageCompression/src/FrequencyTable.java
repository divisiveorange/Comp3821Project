import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

public class FrequencyTable {
    public long[] getTable() {
        return table;
    }
    long[] table;
    public FrequencyTable(ArrayList<Short> pixels) {
        long[] table = new long[(int) Math.pow(2,12)];
            for (var pixel : pixels) {
                table[pixel]++;
            }
        this.table = table;
    }
    public ArrayList<Pair> getSorted() {
        ArrayList<Pair> pairs = new ArrayList<>();
        for (short i = 0; i < table.length; i++) {
            if (table[i] != 0) {
                pairs.add(new Pair(i, table[i]));
            }
        }
        Collections.sort(pairs);
        return pairs;
    }
    public ArrayList<Short> getNMostCommon(int n) {
        var pairs = getSorted();
        ArrayList<Short> mostCommon = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            try {
                mostCommon.add(pairs.get(i).pixelPart);
            } catch (Exception e) {
                return mostCommon;
            }
        }
        return mostCommon;
    }
    public static ArrayList<Short> getLeading(Pixel[] pixels) {
        ArrayList<Short> returnable = new ArrayList<>();
        for (var pixel : pixels) {
            returnable.add(pixel.getLeading());
        }
        return returnable;
    }
    public static ArrayList<Short> getTrailing(Pixel[] pixels) {
        ArrayList<Short> returnable = new ArrayList<>();
        for (var pixel : pixels) {
            returnable.add(pixel.getTrailing());
        }
        return returnable;
    }
}
