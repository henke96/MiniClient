package miniclient.jvm;

public class ConstPoolInfo {
    public static final int TAG_CLASS = 7;
    public static final int TAG_FIELD_REF = 9;
    public static final int TAG_METHOD_REF = 10;
    public static final int TAG_INTERFACE_METHOD_REF = 11;
    public static final int TAG_STRING = 8;
    public static final int TAG_INTEGER = 3;
    public static final int TAG_FLOAT = 4;
    public static final int TAG_LONG = 5;
    public static final int TAG_DOUBLE = 6;
    public static final int TAG_NAME_AND_TYPE = 12;
    public static final int TAG_UTF8 = 1;
    public static final int TAG_METHOD_HANDLE = 15;
    public static final int TAG_METHOD_TYPE = 16;
    public static final int TAG_INVOKE_DYNAMIC = 18;

    public final int tag;
    public Object info;

    public ConstPoolInfo(ByteArray bytes) {
        tag = bytes.readUByte();
        info = null;
        switch (tag) {
            case TAG_CLASS: {
                info = new ClassInfo(bytes);
                break;
            }
            case TAG_FIELD_REF:
            case TAG_METHOD_REF:
            case TAG_INTERFACE_METHOD_REF: {
                info = new RefInfo(bytes);
                break;
            }
            case TAG_STRING: {
                bytes.index += 2;
                break;
            }
            case TAG_INTEGER: {
                info = new IntegerInfo(bytes);
                break;
            }
            case TAG_FLOAT: {
                bytes.index += 4;
                break;
            }
            case TAG_LONG: {
                bytes.index += 8;
                break;
            }
            case TAG_DOUBLE: {
                bytes.index += 8;
                break;
            }
            case TAG_NAME_AND_TYPE: {
                info = new NameAndTypeInfo(bytes);
                break;
            }
            case TAG_UTF8: {
                info = bytes.readUTF();
                break;
            }
            case TAG_METHOD_HANDLE: {
                bytes.index += 3;
                break;
            }
            case TAG_METHOD_TYPE: {
                bytes.index += 2;
                break;
            }
            case TAG_INVOKE_DYNAMIC: {
                bytes.index += 4;
                break;
            }
        }
    }

    public boolean isPadded() {
        return tag == TAG_LONG || tag == TAG_DOUBLE;
    }
}