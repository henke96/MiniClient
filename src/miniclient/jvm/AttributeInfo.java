package miniclient.jvm;

public class AttributeInfo {
    public AttributeInfo(ByteArray bytes) {
        bytes.index += 2;
        int attributeLength = bytes.readInt();
        bytes.index += attributeLength;
    }
}