package miniclient.jvm;

public class ByteArray {
    public byte[] array;
    public int index = 0;

    public ByteArray(byte[] array) {
        this.array = array;
    }

    public int peekUByte() {
        int value = readUByte();
        --index;
        return value;
    }

    public int readUByte() {
        return array[index++] & 0xFF;
    }

    public int peekUShort() {
        int value = readUShort();
        index -= 2;
        return value;
    }

    public int readUShort() {
        int b1 = array[index] & 0xFF;
        int b2 = array[index + 1] & 0xFF;
        index += 2;
        return (b1 << 8) | b2;
    }

    public int readInt() {
        int b1 = array[index] & 0xFF;
        int b2 = array[index + 1] & 0xFF;
        int b3 = array[index + 2] & 0xFF;
        int b4 = array[index + 3] & 0xFF;
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

    public void writeByte(int value) {
        array[index++] = (byte) value;
    }

    public void writeShort(int value) {
        array[index] = (byte) (value >> 8);
        array[index + 1] = (byte) (value & 0xFF);
        index += 2;
    }

    public void writeInt(long value) {
        array[index] = (byte) (value >> 24);
        array[index + 1] = (byte) ((value >> 16) & 0xFF);
        array[index + 2] = (byte) ((value >> 8) & 0xFF);
        array[index + 3] = (byte) (value & 0xFF);
        index += 4;
    }

    public static int calcUTFLength(String string) {
        int stringLength = string.length();
        int utfLength = 0;

        for (int i = 0; i < stringLength; i++) {
            int c = string.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                ++utfLength;
            } else if (c > 0x07FF) {
                utfLength += 3;
            } else {
                utfLength += 2;
            }
        }
        return utfLength + 2;
    }

    public void writeUTF(String string) {
        int stringLength = string.length();
        int startIndex = index;
        index += 2;

        for (int i = 0; i < stringLength; ++i) {
            int c = string.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                array[index++] = (byte) c;
            } else if (c > 0x07FF) {
                array[index] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                array[index + 1] = (byte) (0x80 | ((c >> 6) & 0x3F));
                array[index + 2] = (byte) (0x80 | ((c >> 0) & 0x3F));
                index += 3;
            } else {
                array[index] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                array[index + 1] = (byte) (0x80 | ((c >> 0) & 0x3F));
                index += 2;
            }
        }
        int length = index - (startIndex + 2);
        array[startIndex] = (byte) (length >>> 8);
        array[startIndex + 1] = (byte) (length & 0xFF);
    }

    // Add gap before index.
    public void addGap(int index, int length) {
        byte[] newBytes = new byte[array.length + length];
        System.arraycopy(array, 0, newBytes, 0, index);
        System.arraycopy(array, index, newBytes, index + length, array.length - index);
        array = newBytes;
    }
}