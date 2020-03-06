package miniclient.jvm;

public class FieldInfo {
    private AttributeInfo[] attributes;
    public FieldInfo(ByteArray bytes) {
        bytes.index += 6;
        int attributeCount = bytes.readUShort();
        attributes = new AttributeInfo[attributeCount];
        for (int i = 0; i < attributeCount; ++i) {
            attributes[i] = new AttributeInfo(bytes);
        }
    }
}