package miniclient.jvm;

public class MethodInfo {
    private AttributeInfo[] attributes;
    public MethodInfo(ByteArray bytes) {
        bytes.index += 6;
        int attributeCount = bytes.readUShort();
        attributes = new AttributeInfo[attributeCount];
        for (int i = 0; i < attributeCount; ++i) {
            attributes[i] = new AttributeInfo(bytes);
        }
    }
}