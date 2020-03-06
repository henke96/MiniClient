package miniclient.jvm;

public class MethodInfo {
    public static final int ACC_PUBLIC = 0x0001;
    public static final int ACC_PRIVATE = 0x0002;
    public static final int ACC_PROTECTED = 0x0004;
    public static final int ACC_STATIC = 0x0008;
    public static final int ACC_FINAL = 0x0010;
    public static final int ACC_SYNCHRONIZED = 0x0020;
    public static final int ACC_BRIDGE = 0x0040;
    public static final int ACC_VARARGS = 0x0080;
    public static final int ACC_NATIVE = 0x0100;
    public static final int ACC_ABSTRACT = 0x0400;
    public static final int ACC_STRICT = 0x0800;
    public static final int ACC_SYNTHETIC = 0x1000;

    public final int accessFlags;
    public final int nameIndex;
    public final int descriptorIndex;
    public final AttributeInfo[] attributes;

    public MethodInfo(ByteArray bytes, ConstPoolInfo[] constPool) {
        accessFlags = bytes.readUShort();
        nameIndex = bytes.readUShort();
        descriptorIndex = bytes.readUShort();

        int attributeCount = bytes.readUShort();
        attributes = new AttributeInfo[attributeCount];
        for (int i = 0; i < attributeCount; ++i) {
            attributes[i] = new AttributeInfo(bytes, constPool);
        }
    }
}