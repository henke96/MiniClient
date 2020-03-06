package miniclient.jvm;

public class IntegerInfo {
    public final int value;

    public IntegerInfo(ByteArray bytes) {
        value = bytes.readInt();
    }
}