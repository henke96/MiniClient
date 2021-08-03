package miniclient.jvm;

public class FieldInfo {
    public FieldInfo(ByteArray bytes) {
        bytes.index += 6;
        int attributeCount = bytes.readUShort();
        for (int i = 0; i < attributeCount; ++i) {
            bytes.index += 2;
            int attributeLength = bytes.readInt();
            bytes.index += attributeLength;
        }
    }
}