package miniclient.jvm;

public class ClassInfo {
    public final int nameIndex;
    public ClassInfo(ByteArray bytes) {
        nameIndex = bytes.readUShort();
    }
}