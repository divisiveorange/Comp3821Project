import java.util.Objects;

public class Pixel {
    private int pixel;
    public int getInt() {
        return pixel;
    }

    public byte getRed() {
        return (byte) ((pixel >> 16) & 0xFF);
    }

    public byte getGreen() {
        return (byte) ((pixel >> 8) & 0xFF);
    }

    public byte getBlue() {
        return (byte) (pixel & 0xFF);
    }
    public short getLeading() {
        return (short) (((pixel & 0x00F00000 ) >> 12) | ((pixel & 0x0000F000 ) >> 8) | ((pixel & 0x000000F0 ) >> 4));
    }
    public short getTrailing() {
        return (short) (((pixel & 0x000F0000 ) >> 8) | ((pixel & 0x00000F00 ) >> 4) | ((pixel & 0x0000000F )));
    }
    public Pixel(int pixel) {
        this.pixel = pixel;
    }
    public Pixel(short leading, short trailing) {
        int leadingInt = ((leading & 0x0F00) << 12) | ((leading & 0x00F0) << 8) | ((leading & 0x000F) << 4);
        int trailingInt = ((trailing & 0x0F00) << 8) | ((trailing & 0x00F0) << 4) | ((trailing & 0x000F));
        pixel = leadingInt | trailingInt;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pixel pixel1)) return false;
        return pixel == pixel1.pixel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pixel);
    }
}