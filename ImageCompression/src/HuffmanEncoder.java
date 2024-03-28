import java.util.*;

public class HuffmanEncoder {
    ArrayList<Pair> freqs;
    public HuffmanEncoder(ArrayList<Pair> freqs) {
        this.freqs = freqs;
        Encode();
    };

    public Map<Short, ArrayList<Bit>> getMapToBits() {
        return mapToBits;
    }

    public Map<ArrayList<Bit>, Short> getBitsToMap() {
        return bitsToMap;
    }
    private TreeNode treeHead;

    public TreeNode getTreeHead() {
        return treeHead;
    }

    private HashMap<Short, ArrayList<Bit>> mapToBits;
    private HashMap<ArrayList<Bit>, Short> bitsToMap;
    public void Encode() {
        ArrayList<TreeNode> treeNodeList = new ArrayList<>();
        for (int i = freqs.size() - 1; i >= 0; i--) {
            treeNodeList.add(new LeafNode(freqs.get(i)));
        }
        PriorityQueue<TreeNode> pqueue = new PriorityQueue<>(treeNodeList);
        while (pqueue.size() > 1) {
            pqueue.add(new BranchNode(pqueue.poll(), pqueue.poll()));
        }
        TreeNode finalTree = pqueue.poll();
        mapToBits = new HashMap<>();
        bitsToMap = new HashMap<>();
        treeHead = finalTree;
        assert finalTree != null;
        finalTree.getMapToBits(mapToBits, bitsToMap, new ArrayList<Bit>());
    }
}
