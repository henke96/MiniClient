package miniclient.jvm;

public class ClassFile {
    public final ByteArray bytes;
    public final ConstPoolInfo[] constPool;
    public final FieldInfo[] fields;
    public final MethodInfo[] methods;

    public ClassFile(ByteArray bytes) {
        this.bytes = bytes;
        bytes.index = 8;
        int constPoolCount = bytes.readUShort();
        constPool = new ConstPoolInfo[constPoolCount];
        for (int i = 1; i < constPoolCount; ++i) {
            constPool[i] = new ConstPoolInfo(bytes);
            if (constPool[i].isPadded()) ++i;
        }

        bytes.index += 6;
        int interfaceCount = bytes.readUShort();
        bytes.index += interfaceCount * 2;

        int fieldCount = bytes.readUShort();
        fields = new FieldInfo[fieldCount];
        for (int i = 0; i < fieldCount; ++i) {
            fields[i] = new FieldInfo(bytes, constPool);
        }

        int methodCount = bytes.readUShort();
        methods = new MethodInfo[methodCount];
        for (int i = 0; i < methodCount; ++i) {
            methods[i] = new MethodInfo(bytes, constPool);
        }

        int attributeCount = bytes.readUShort();
        for (int i = 0; i < attributeCount; ++i) {
            bytes.index += 2;
            int length = bytes.readInt();
            bytes.index += length;
        }
        if (bytes.index != bytes.bytes.length) throw new RuntimeException("rip");
    }
}