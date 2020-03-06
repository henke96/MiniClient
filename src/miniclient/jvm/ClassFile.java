package miniclient.jvm;

public class ClassFile {
    private ConstPoolInfo[] constPool;
    private FieldInfo[] fields;
    private MethodInfo[] methods;
    private AttributeInfo[] attributes;

    public ClassFile(ByteArray bytes) {
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
            fields[i] = new FieldInfo(bytes);
        }

        int methodCount = bytes.readUShort();
        methods = new MethodInfo[methodCount];
        for (int i = 0; i < methodCount; ++i) {
            methods[i] = new MethodInfo(bytes);
        }

        int attributeCount = bytes.readUShort();
        attributes = new AttributeInfo[attributeCount];
        for (int i = 0; i < attributeCount; ++i) {
            attributes[i] = new AttributeInfo(bytes);
        }
        if (bytes.index != bytes.bytes.length) throw new RuntimeException("rip");
    }
}