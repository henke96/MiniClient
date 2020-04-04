package miniclient.jvm;

public class ClassFile {
    public final ByteArray bytes;
    public final ConstPool constPool;
    public final FieldInfo[] fields;
    public final MethodInfo[] methods;

    public ClassFile(ByteArray bytes) {
        this.bytes = bytes;
        bytes.index = 8;
        constPool = new ConstPool(this);

        bytes.index += 6;
        int interfaceCount = bytes.readUShort();
        bytes.index += interfaceCount * 2;

        int fieldCount = bytes.readUShort();
        fields = new FieldInfo[fieldCount];
        for (int i = 0; i < fieldCount; ++i) {
            fields[i] = new FieldInfo(bytes);
        }

        int methodCount = bytes.readUShort();
        methods = new MethodInfo[methodCount];
        for (int i = 0; i < methodCount; ++i) {
            methods[i] = new MethodInfo(this);
        }

        int attributeCount = bytes.readUShort();
        for (int i = 0; i < attributeCount; ++i) {
            bytes.index += 2;
            int length = bytes.readInt();
            bytes.index += length;
        }
        if (bytes.index != bytes.bytes.length) throw new RuntimeException();
    }

    // Add gap before index.
    public void addGap(int index, int length) {
        bytes.addGap(index, length);
        if (constPool.poolEndIndex >= index) {
            constPool.poolEndIndex += length;
        }
        for (int i = 0; i < methods.length; ++i) {
            CodeAttribute codeAttribute = methods[i].codeAttribute;
            if (codeAttribute.codeStartIndex >= index) {
                codeAttribute.codeStartIndex += length;
            }
        }
    }
}