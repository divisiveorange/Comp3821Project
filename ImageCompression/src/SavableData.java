import java.io.Serializable;
import java.util.HashMap;

public class SavableData implements Serializable {
    public HashMap<Short, TreeNode> getMapToTrails() {
        return mapToTrails;
    }

    public TreeNode getHeadNode() {
        return headNode;
    }

    public ListAndSize getBits() {
        return bits;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    private int height;
    private int width;
    private String filename;
    public String getFilename() {
        return filename;
    }


    public SavableData(HashMap<Short, TreeNode> mapToTrails, TreeNode headNode, ListAndSize bits, int height, int width, String filename) {
        this.height = height;
        this.width = width;
        this.mapToTrails = mapToTrails;
        this.headNode = headNode;
        this.bits = bits;
        this.filename = filename;
    }

    private HashMap<Short, TreeNode> mapToTrails;
    private TreeNode headNode;
    private ListAndSize bits;
}
