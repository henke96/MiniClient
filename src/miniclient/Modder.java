package miniclient;

import java.lang.reflect.Field;

import miniclient.jvm.*;

public class Modder {
    private ClassFile clientClass;

    public Field localPlayerField;
    public Field actorPathXField;
    public Field actorPathYField;
    public Field baseXField;
    public int baseXMult;
    public Field baseYField;
    public int baseYMult;

    public byte[] processClass(String name, byte[] bytes) {
        if (name.equals("client")) clientClass = new ClassFile(new ByteArray(bytes));
        return bytes;
    }

    // Big mess :-)
    public void finalize(ClassLoader classLoader) throws Exception {
        ByteArray bytes = clientClass.bytes;
        ConstPoolInfo[] constPool = clientClass.constPool;

        for (int i = 0; i < clientClass.methods.length; ++i) {
            MethodInfo methodInfo = clientClass.methods[i];
            if (methodInfo.accessFlags != MethodInfo.ACC_FINAL) continue;
            String descriptor = (String) constPool[methodInfo.descriptorIndex].info;
            if (!descriptor.matches("\\(L\\w{1,2};.\\)Z")) continue;

            CodeAttribute codeAttribute = null;
            for (int j = 0; j < methodInfo.attributes.length; ++j) {
                AttributeInfo attributeInfo = methodInfo.attributes[j];
                if (attributeInfo.name.equals("Code")) codeAttribute = (CodeAttribute) attributeInfo.attribute;
            }

            bytes.index = codeAttribute.codeStartIndex;

            boolean foundX = false;
            outerLoop:
            while (true) {
                int op = bytes.readUByte();
                if (op != CodeAttribute.GETSTATIC) {
                    bytes.index += CodeAttribute.OPERAND_SIZES[op];
                    continue;
                }
                RefInfo refInfo = (RefInfo) constPool[bytes.readUShort()].info;
                NameAndTypeInfo nameAndTypeInfo = (NameAndTypeInfo) constPool[refInfo.nameAndTypeIndex].info;
                String fieldType = (String) constPool[nameAndTypeInfo.descriptorIndex].info;
                if (!fieldType.equals("Ljava/lang/String;")) continue;
                String localPlayerOwner = null;
                String localPlayer = null;
                String player = null;
                String pathX = null;
                String pathY = null;
                String baseXOwner = null;
                String baseX = null;
                int baseXMult = 0;
                String baseYOwner = null;
                String baseY = null;
                int baseYMult = 0;

                while (true) {
                    op = bytes.readUByte();
                    switch (op) {
                        case CodeAttribute.GETSTATIC: {
                            refInfo = (RefInfo) constPool[bytes.readUShort()].info;
                            nameAndTypeInfo = (NameAndTypeInfo) constPool[refInfo.nameAndTypeIndex].info;
                            fieldType = (String) constPool[nameAndTypeInfo.descriptorIndex].info;
                            ClassInfo classInfo = (ClassInfo) constPool[refInfo.classIndex].info;
                            if (fieldType.matches("L\\w{1,2};")) {
                                localPlayerOwner = (String) constPool[classInfo.nameIndex].info;
                                localPlayer = (String) constPool[nameAndTypeInfo.nameIndex].info;
                            } else if (fieldType.equals("I")) {
                                if (foundX) {
                                    baseYOwner = (String) constPool[classInfo.nameIndex].info;
                                    baseY = (String) constPool[nameAndTypeInfo.nameIndex].info;
                                } else {
                                    baseXOwner = (String) constPool[classInfo.nameIndex].info;
                                    baseX = (String) constPool[nameAndTypeInfo.nameIndex].info;
                                }
                            } else if (fieldType.equals("Ljava/lang/String;")) {
                                if (foundX && pathY != null && baseYOwner != null && baseY != null && baseYMult != 0) {
                                    System.out.println("Found!");
                                    Class<?> baseXOwnerClass = classLoader.loadClass(baseXOwner);
                                    this.baseXField = baseXOwnerClass.getDeclaredField(baseX);
                                    this.baseXField.setAccessible(true);
                                    this.baseXMult = baseXMult;
                                    Class<?> baseYOwnerClass = classLoader.loadClass(baseYOwner);
                                    this.baseYField = baseYOwnerClass.getDeclaredField(baseY);
                                    this.baseYField.setAccessible(true);
                                    this.baseYMult = baseYMult;
                                    Class<?> localPlayerOwnerClass = classLoader.loadClass(localPlayerOwner);
                                    this.localPlayerField = localPlayerOwnerClass.getDeclaredField(localPlayer);
                                    this.localPlayerField.setAccessible(true);
                                    Class<?> actorClass = classLoader.loadClass(player).getSuperclass();
                                    this.actorPathXField = actorClass.getDeclaredField(pathX);
                                    this.actorPathXField.setAccessible(true);
                                    this.actorPathYField = actorClass.getDeclaredField(pathY);
                                    this.actorPathYField.setAccessible(true);
                                    return;
                                } else if (localPlayerOwner != null && localPlayer != null && player != null && pathX != null && baseXOwner != null && baseX != null && baseXMult != 0) {
                                    foundX = true;
                                }
                            } else {
                                continue outerLoop;
                            }
                            bytes.index -= 2;
                            break;
                        }
                        case CodeAttribute.LDC_W: {
                            ConstPoolInfo constPoolInfo = constPool[bytes.readUShort()];
                            if (constPoolInfo.tag != ConstPoolInfo.TAG_INTEGER) {
                                continue outerLoop;
                            }
                            IntegerInfo integerInfo = (IntegerInfo) constPoolInfo.info;
                            if (foundX) {
                                baseYMult = integerInfo.value;
                            } else {
                                baseXMult = integerInfo.value;
                            }
                            bytes.index -= 2;
                            break;
                        }
                        case CodeAttribute.GETFIELD: {
                            refInfo = (RefInfo) constPool[bytes.readUShort()].info;
                            nameAndTypeInfo = (NameAndTypeInfo) constPool[refInfo.nameAndTypeIndex].info;
                            if (foundX) {
                                pathY = (String) constPool[nameAndTypeInfo.nameIndex].info;
                            } else {
                                ClassInfo classInfo = (ClassInfo) constPool[refInfo.classIndex].info;
                                player = (String) constPool[classInfo.nameIndex].info;
                                pathX = (String) constPool[nameAndTypeInfo.nameIndex].info;
                            }
                            bytes.index -= 2;
                            break;
                        }
                        case CodeAttribute.INVOKEVIRTUAL: {
                            refInfo = (RefInfo) constPool[bytes.readUShort()].info;
                            nameAndTypeInfo = (NameAndTypeInfo) constPool[refInfo.nameAndTypeIndex].info;
                            String methodName = (String) constPool[nameAndTypeInfo.nameIndex].info;
                            if (!methodName.equals("append")) {
                                continue outerLoop;
                            }
                            bytes.index -= 2;
                            break;
                        }
                        case CodeAttribute.ICONST_0:
                        case CodeAttribute.IALOAD:
                        case CodeAttribute.IMUL:
                        case CodeAttribute.IADD:
                            break;
                        default:
                            --bytes.index;
                            continue outerLoop;
                    }
                    bytes.index += CodeAttribute.OPERAND_SIZES[op];
                }
            }
        }
        throw new RuntimeException();
    }
}