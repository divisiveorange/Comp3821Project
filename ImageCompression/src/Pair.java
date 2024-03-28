public class Pair implements Comparable<Pair>{
    short pixelPart;
    long frequency;

    public Pair(short pixelPart, long frequency) {
        this.pixelPart = pixelPart;
        this.frequency = frequency;
    }

    public short getPixelPart() {
        return pixelPart;
    }
    public long getFrequency() {
        return frequency;
    }
    @Override
    public int compareTo(Pair other) {
        return Long.compare(other.getFrequency(), getFrequency());
    }
}