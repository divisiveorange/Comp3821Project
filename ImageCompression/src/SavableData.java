import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SavableData implements Serializable {
    public HashMap<Short, TreeNode> getMapToTrails() {
        return mapToTrails;
    }

    public TreeNode getHeadNode() {
        return headNode;
    }

    public ListAndSize getBits() {
        ArrayList<Byte> byteList = new ArrayList<>();
        for (var bite : bytes) {
            byteList.add(bite);
        }
        return new ListAndSize(byteList, size);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    private int height;
    private int width;
    private byte[] bytes;
    private String filename;
    public String getFilename() {
        return filename;
    }
    private long size;


    public SavableData(HashMap<Short, TreeNode> mapToTrails, TreeNode headNode, ListAndSize bits, int height, int width, String filename) {
        this.height = height;
        this.width = width;
        this.mapToTrails = mapToTrails;
        this.headNode = headNode;
        this.size = bits.size();
        bytes = new byte[bits.bytes().size()];
        for (int i = 0; i < bits.bytes().size(); i++) {
            bytes[i] = bits.bytes().get(i);
        }
        this.filename = filename;
    }

    private HashMap<Short, TreeNode> mapToTrails;
    private TreeNode headNode;
    private ListAndSize bits;
}
