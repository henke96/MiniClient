package miniclient.jvm;

public class NameAndTypeInfo {
    public final int nameIndex;
    public final int descriptorIndex;

    public NameAndTypeInfo(ByteArray bytes) {
        nameIndex = bytes.readUShort();
        descriptorIndex = bytes.readUShort();
    }
}