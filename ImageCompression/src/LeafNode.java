import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class LeafNode extends TreeNode {
    short value;
    long frequency;
    public LeafNode(Pair valAndFreq) {
        value = valAndFreq.pixelPart;
        frequency = valAndFreq.getFrequency();
    }
    public long getFreq() {
        return frequency;
    }
    public short getValue() {
        return value;
    }
    @Override
    public void getMapToBits(HashMap<Short, ArrayList<Bit>> mapToBits, HashMap<ArrayList<Bit>, Short> bitsToMap, ArrayList<Bit> bits) {
        mapToBits.put(value, new ArrayList<Bit>(bits));
        bitsToMap.put(new ArrayList<Bit>(bits), value);
    }

}
