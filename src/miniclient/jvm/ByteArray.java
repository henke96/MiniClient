package miniclient.jvm;

public class ByteArray {
    public final byte[] bytes;
    public int index = 0;
    
    public ByteArray(byte[] bytes) {
        this.bytes = bytes;
    }

    public int readUByte() {
        return bytes[index++] & 0xFF;
    }

    public int readUShort() {
        int b1 = bytes[index] & 0xFF;
        int b2 = bytes[index + 1] & 0xFF;
        index += 2;
        return (b1 << 8) | b2;
    }

    public int readInt() {
        int b1 = bytes[index] & 0xFF;
        int b2 = bytes[index + 1] & 0xFF;
        int b3 = bytes[index + 2] & 0xFF;
        int b4 = bytes[index + 3] & 0xFF;
        index += 4;
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    public String readUTF() {
        int length = readUShort();
        int endIndex = index + length;

        char[] chars = new char[length];
        int charCount = 0;
        int currByte;

        while (index < endIndex) {
            currByte = readUByte();
            if (currByte > 127) {
                if (currByte < 224) {
                    int secondByte = readUByte();
                    chars[charCount++] = (char) (
                        ((currByte & 0x1F) << 6) | (secondByte & 0x3F)
                    );
                } else {
                    int secondByte = readUByte();
                    int thirdByte = readUByte();
                    chars[charCount++] = (char) (
                        ((currByte & 0x0F) << 12) | ((secondByte & 0x3F) << 6) | (thirdByte & 0x3F)
                    );
                }
            } else {
                chars[charCount++] = (char) currByte;
            }
        }
        return new String(chars, 0, charCount);
    }
}