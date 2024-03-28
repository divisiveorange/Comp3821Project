import java.util.ArrayList;
import java.util.HashMap;

public class BranchNode extends TreeNode{
    long frequency;
    public TreeNode[] getChildren() {
        return children;
    }
    private final TreeNode[] children;
    public long getFreq() {
        return frequency;
    }
    @Override
    public void getMapToBits(HashMap<Short, ArrayList<Bit>> mapToBits, HashMap<ArrayList<Bit>, Short> bitsToMap, ArrayList<Bit> bits) {
        for (byte i = 0; i < children.length; i++) {
            bits.add(new Bit(i));
            children[i].getMapToBits(mapToBits, bitsToMap, bits);
            bits.remove(bits.size() - 1);
        }
    }

    public BranchNode(TreeNode zero, TreeNode one) {
        frequency = zero.getFreq() + one.getFreq();
        children = new TreeNode[2];
        children[0] = zero;
        children[1] = one;
    }
}
