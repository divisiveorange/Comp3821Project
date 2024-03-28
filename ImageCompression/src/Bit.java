import java.util.ArrayList;

public class Bit {
    byte bit;
    public Bit(byte bit) {
        assert (bit == 0 || bit == 1) : "Wasn't a bit";
        this.bit = bit;
    }
    public byte getValue() {
        return bit;
    }

    public static ListAndSize Compress(ArrayList<Bit> bits) {
        ArrayList<Byte> bitsList = new ArrayList<>();
        for (int i = 0; i < bits.size(); i++) {
            if (i % 8 == 0) {
                bitsList.add((byte) (bits.get(i).getValue() << 7));
            } else {
                bitsList.set(bitsList.size() - 1,(byte) ((bitsList.size() - 1) | (bits.get(i).getValue() << 7 - (i % 8))));
            }
        }
        return new ListAndSize(bitsList, bits.size());
    }
    public static ArrayList<Bit> Expand(ListAndSize bytes) {
        ArrayList<Bit> bits = new ArrayList<>();
        for (int i = 0; i < bytes.size(); i++) {
            byte bite = bytes.bytes().get(i / 8);
            bits.add(new Bit ((byte) ((bite >> (7 - (i % 8))) & 0x0F)));
        }
        return bits;
    }
    public static ArrayList<Bit> ShortToBits(short target) {
        ArrayList<Bit> bits = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            bits.add(new Bit((byte)((target >> (11-i)) & 0x0F)));
        }
        return bits;
    }
    public static short bitsToShort(ArrayList<Bit> bits) {
        short result = 0;
        for (int i = 0; i < 12; i++) {
            result |= (short) (1 << (11-i));
        }
        return result;
    }
}
