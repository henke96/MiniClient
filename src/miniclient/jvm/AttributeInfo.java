package miniclient.jvm;

public class AttributeInfo {
    public final String name;
    public final Object attribute;
    public AttributeInfo(ByteArray bytes, ConstPoolInfo[] constPool) {
        int nameIndex = bytes.readUShort();
        name = (String) constPool[nameIndex].info;
        int attributeLength = bytes.readInt();
        if (name.equals("Code")) {
            attribute = new CodeAttribute(bytes);
        } else {
            attribute = null;
            bytes.index += attributeLength;
        }
    }
}