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
    public static final int INVOKESTATIC = 0xB8;

    // -1 for variable length instructions.
    public static final int[] OPERAND_SIZES = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 2, 2, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, -1, -1, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 4, 4, 2, 1, 2, 0, 0, 2, 2, 0, 0, -1, 3, 2, 2, 4, 5};

    public int codeStartIndex;
    public int attributeLength;
    public int codeLength;
    public final ClassFile classFile;
    public CodeAttribute(ClassFile classFile) {
        this.classFile = classFile;
        ByteArray bytes = classFile.bytes;
        attributeLength = bytes.readInt();
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

    // Returns offset to cave.
    public int addCodeCave(int index, int length) {
        ByteArray bytes = classFile.bytes;
        int caveStart = codeStartIndex + codeLength;

        // Create the cave.
        bytes.index = index;
        int replacedSize = 1 + OPERAND_SIZES[bytes.peekUByte()];
        if (replacedSize < 3) throw new RuntimeException();
        int caveLength = length + replacedSize + 3;
        classFile.addGap(caveStart, caveLength);

        // Copy replaced instruction into cave.
        System.arraycopy(bytes.array, index, bytes.array, caveStart + length, replacedSize);

        // Write jump to the cave.
        bytes.writeByte(CodeAttribute.GOTO);
        bytes.writeShort(caveStart - index);

        // Write jump back.
        bytes.index = caveStart + length + replacedSize;
        bytes.writeByte(CodeAttribute.GOTO);
        bytes.writeShort((index + replacedSize) - (bytes.index - 1));

        // Update lengths.
        codeLength += caveLength;
        attributeLength += caveLength;
        classFile.bytes.index = codeStartIndex - 4;
        classFile.bytes.writeInt(codeLength);
        classFile.bytes.index = codeStartIndex - 12;
        classFile.bytes.writeInt(attributeLength);
        return caveStart - codeStartIndex;
    }
}