import java.util.ArrayList;

public class TreeXplorer {
    public TreeXplorer(TreeNode head) {
        this.head = head;
        curr = head;
    }
    private TreeNode head;
    private TreeNode curr;
    public boolean isEnd() {
        return curr instanceof LeafNode;
    }
    public void advance(Bit bit) {
        curr = ((BranchNode) curr).getChildren()[bit.getValue()];
        bits.add(bit);
    }
    public ArrayList<Bit> getBits() {
        return bits;
    }
    public short getContent() {
        return ((LeafNode) curr).getValue();
    }

    private ArrayList<Bit> bits = new ArrayList<>();

}
