package miniclient.jvm;

public class CodeAttribute {
    public static final int ICONST_0 = 0x03;
    public static final int LDC_W = 0x13;
    public static final int IALOAD = 0x2E;
    public static final int IADD = 0x60;
    public static final int IMUL = 0x68;
    public static final int GOTO = 0xA7;
    public static final int GETSTATIC = 0xB2;
    public static final int PUTSTATIC = 0xB3;
    public static final int GETFIELD = 0xB4;
    public static final int INVOKEVIRTUAL = 0xB6;

    // -1 for variable length instructions.
    public static final int[] OPERAND_SIZES = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 2, 2, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, -1, -1, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 4, 4, 2, 1, 2, 0, 0, 2, 2, 0, 0, -1, 3, 2, 2, 4, 5};

    public final int codeStartIndex;
    public int codeLength;
    public final ByteArray bytes;
    public CodeAttribute(ByteArray bytes) {
        this.bytes = bytes;
        bytes.index += 4;
        codeLength = bytes.readInt();
        codeStartIndex = bytes.index;
        bytes.index += codeLength;

        int exceptionCount = bytes.readUShort();
        bytes.index += exceptionCount * 8;

        int attributeCount = bytes.readUShort();
        for (int i = 0; i < attributeCount; ++i) {
            bytes.index += 2;
            int length = bytes.readInt();
            bytes.index += length;
        }
    }

    // Returns index to gap.
    public int addEndGap(int length) {
        int gapStart = codeStartIndex + codeLength;
        bytes.addGap(gapStart, length);
        codeLength += length;
        return gapStart;
    }
}