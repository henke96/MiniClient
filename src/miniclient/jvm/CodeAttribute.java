package miniclient.jvm;

public class CodeAttribute {
    public static final int ICONST_0 = 0x03;
    public static final int LDC_W = 0x13;
    public static final int IALOAD = 0x2E;
    public static final int IADD = 0x60;
    public static final int IMUL = 0x68;
    public static final int GETSTATIC = 0xB2;
    public static final int GETFIELD = 0xB4;
    public static final int INVOKEVIRTUAL = 0xB6;

    public final int codeStartIndex;
    public final int codeLength;
    public CodeAttribute(ByteArray bytes) {
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
}