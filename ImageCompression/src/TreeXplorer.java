import java.util.ArrayList;

public class TreeXplorer {
    public TreeXplorer(TreeNode head) {
        this.head = head;
        curr = head;
    }
    private TreeNode head;
    private TreeNode curr;
    public boolean isEnd() {
        return curr instanceof BranchNode;
    }
    public void advance(Bit bit) {
        curr = ((BranchNode) curr).getChildren()[bit.getValue()];
        bits.add(bit);
    }
    public ArrayList<Bit> getBits() {
        return bits;
    }

    private ArrayList<Bit> bits;

}
