import java.io.Serializable;
import java.util.ArrayList;

public record ListAndSize(ArrayList<Byte> bytes, long size) implements Serializable {
}
