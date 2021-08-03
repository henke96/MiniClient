package miniclient.jvm;

// Fieldref, Methodref, InterfaceMethodref.
public class RefInfo {
    public final int classIndex;
    public final int nameAndTypeIndex;

    public RefInfo(ByteArray bytes) {
        classIndex = bytes.readUShort();
        nameAndTypeIndex = bytes.readUShort();
    }
}