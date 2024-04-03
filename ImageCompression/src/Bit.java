import java.util.ArrayList;
import java.util.Arrays;

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
        Byte[] bitsList = new Byte[(Math.ceilDiv(bits.size(), 8))];
        for (int i = 0; i < bits.size(); i++) {
            if (i % 8 == 0) {
               bitsList[i/8] = (byte) (bits.get(i).getValue() << 7);
            } else {
                bitsList[i/8] = (byte) ((int) bitsList[i/8] | (bits.get(i).getValue() << 7 - (i % 8)));
            }
        }

        ArrayList<Byte> bitsArrayList = new ArrayList<>(bitsList.length);
        bitsArrayList.addAll(Arrays.stream(bitsList).toList());
        return new ListAndSize(bitsArrayList, bits.size());
    }
    public static byte[] Expand(ListAndSize bytes) {
        byte[] bits = new byte[(int) bytes.size()];
        for (int i = 0; i < bytes.bytes().size(); i++) {
           for (int j = 0; j < 8; j++) {
               if (8*i+j >= bytes.size()) return bits;
               bits[8*i+j] = (byte) (bytes.bytes().get(i) >> (7 - j) & 0x01);
           }
        }
        return bits;
    }
    public static ArrayList<Bit> ShortToBits(short target) {
        ArrayList<Bit> bits = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            bits.add(new Bit((byte)((target >> (11-i)) & 0x01)));
        }
        return bits;
    }
    public static short bitsToShort(ArrayList<Bit> bits) {
        short result = 0;
        for (int i = 0; i < 12; i++) {
            result |= (short) (bits.get(i).getValue() << (11-i));
        }
        return result;
    }
}
