import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class TreeNode implements Comparable<TreeNode>, Serializable {
    public abstract long getFreq();
    @Override
    public int compareTo(TreeNode other) {
        return Long.compare(getFreq(), other.getFreq());
    }
    public abstract void getMapToBits(HashMap<Short, ArrayList<Bit>> mapToBits, HashMap<ArrayList<Bit>, Short> bitsToMap, ArrayList<Bit> bits);
}
