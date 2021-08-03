package miniclient.jvm;

import java.util.Arrays;

public class ConstPool {
    public ConstPoolInfo[] constPoolInfos;
    public int poolEndIndex;
    public final ClassFile classFile;

    public ConstPool(ClassFile classFile) {
        this.classFile = classFile;
        int constPoolCount = classFile.bytes.readUShort();
        constPoolInfos = new ConstPoolInfo[constPoolCount];
        for (int i = 1; i < constPoolCount; ++i) {
            constPoolInfos[i] = new ConstPoolInfo(classFile.bytes);
            if (constPoolInfos[i].isPadded()) ++i;
        }
        poolEndIndex = classFile.bytes.index;
    }

    public String getUtf8Info(int index) {
        return (String) constPoolInfos[index].info;
    }

    public String getRefDescriptor(int index) {
        RefInfo refInfo = (RefInfo) constPoolInfos[index].info;
        NameAndTypeInfo nameAndTypeInfo = (NameAndTypeInfo) constPoolInfos[refInfo.nameAndTypeIndex].info;
        return (String) constPoolInfos[nameAndTypeInfo.descriptorIndex].info;
    }

    public String getRefName(int index) {
        RefInfo refInfo = (RefInfo) constPoolInfos[index].info;
        NameAndTypeInfo nameAndTypeInfo = (NameAndTypeInfo) constPoolInfos[refInfo.nameAndTypeIndex].info;
        return (String) constPoolInfos[nameAndTypeInfo.nameIndex].info;
    }

    public String getRefClassName(int index) {
        RefInfo refInfo = (RefInfo) constPoolInfos[index].info;
        ClassInfo classInfo = (ClassInfo) constPoolInfos[refInfo.classIndex].info;
        return (String) constPoolInfos[classInfo.nameIndex].info;
    }

    public int addRefInfo(String className, String refName, String refDescriptor, int refTag) {
        ByteArray bytes = classFile.bytes;
        int newInfosIndex = constPoolInfos.length;
        int newInfosByteIndex = poolEndIndex;
        int utfLengths = ByteArray.calcUTFLength(className) + ByteArray.calcUTFLength(refName) + ByteArray.calcUTFLength(refDescriptor);

        classFile.addGap(newInfosByteIndex, 16 + utfLengths);
        bytes.index = newInfosByteIndex;
        bytes.writeByte(ConstPoolInfo.TAG_UTF8);
        bytes.writeUTF(className);
        bytes.writeByte(ConstPoolInfo.TAG_UTF8);
        bytes.writeUTF(refName);
        bytes.writeByte(ConstPoolInfo.TAG_UTF8);
        bytes.writeUTF(refDescriptor);
        bytes.writeByte(ConstPoolInfo.TAG_CLASS);
        bytes.writeShort(newInfosIndex);
        bytes.writeByte(ConstPoolInfo.TAG_NAME_AND_TYPE);
        bytes.writeShort(newInfosIndex + 1);
        bytes.writeShort(newInfosIndex + 2);
        bytes.writeByte(refTag);
        bytes.writeShort(newInfosIndex + 3);
        bytes.writeShort(newInfosIndex + 4);

        constPoolInfos = Arrays.copyOf(constPoolInfos, newInfosIndex + 6);
        bytes.index = newInfosByteIndex;
        for (int i = newInfosIndex; i < constPoolInfos.length; ++i) {
            constPoolInfos[i] = new ConstPoolInfo(bytes);
        }
        bytes.index = 8;
        bytes.writeShort(constPoolInfos.length);
        return newInfosIndex + 5;
    }
}