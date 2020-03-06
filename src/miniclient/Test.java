package miniclient;

import java.lang.reflect.Field;

import miniclient.jvm.*;

public class Test {
    private Field localPlayerField;
    private Field actor_pathXField;
    private Field actor_pathYField;
    private Field baseXField;
    private int baseXMult;
    private Field baseYField;
    private int baseYMult;

    private static final int[] opcodeLength = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 2, 3, 3, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 0, 0, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 5, 5, 3, 2, 3, 1, 1, 3, 3, 1, 1, 0, 4, 3, 3, 5, 5};

    public void test(String name, byte[] classBytes, ClassLoader classLoader) throws Exception {
        if (!name.equals("client")) return;

        ByteArray byteArray = new ByteArray(classBytes);
        ClassFile file = new ClassFile(byteArray);
        ConstPoolInfo[] constPool = file.constPool;

        for (int i = 0; i < file.methods.length; ++i) {
            MethodInfo methodInfo = file.methods[i];
            if (methodInfo.accessFlags != MethodInfo.ACC_FINAL) continue;
            String descriptor = (String) constPool[methodInfo.descriptorIndex].info;
            if (!descriptor.matches("\\(L\\w{1,2};.\\)Z")) continue;

            CodeAttribute codeAttribute = null;
            for (int j = 0; j < methodInfo.attributes.length; ++j) {
                AttributeInfo attributeInfo = methodInfo.attributes[j];
                if (attributeInfo.name.equals("Code")) codeAttribute = (CodeAttribute) attributeInfo.attribute;
            }

            byteArray.index = codeAttribute.codeStartIndex;

            boolean foundX = false;
            outerLoop:
            while (true) {
                if (byteArray.index >= codeAttribute.codeStartIndex + codeAttribute.codeLength) throw new RuntimeException("asd");
                int op = byteArray.readUByte();
                if (op != CodeAttribute.GETSTATIC) {
                    if (opcodeLength[op] == 0) throw new RuntimeException();
                    byteArray.index += opcodeLength[op] - 1;
                    continue;
                }
                RefInfo refInfo = (RefInfo) constPool[byteArray.readUShort()].info;
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
                    op = byteArray.readUByte();
                    switch (op) {
                        case CodeAttribute.GETSTATIC: {
                            refInfo = (RefInfo) constPool[byteArray.readUShort()].info;
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
                                    System.out.println("Find coord fields");
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
                                    this.actor_pathXField = actorClass.getDeclaredField(pathX);
                                    this.actor_pathXField.setAccessible(true);
                                    this.actor_pathYField = actorClass.getDeclaredField(pathY);
                                    this.actor_pathYField.setAccessible(true);
                                    return;
                                } else if (localPlayerOwner != null && localPlayer != null && player != null && pathX != null && baseXOwner != null && baseX != null && baseXMult != 0) {
                                    foundX = true;
                                }
                            } else {
                                continue outerLoop;
                            }
                            byteArray.index -= 2;
                            break;
                        }
                        case CodeAttribute.LDC_W: {
                            ConstPoolInfo constPoolInfo = constPool[byteArray.readUShort()];
                            if (constPoolInfo.tag != ConstPoolInfo.TAG_INTEGER) {
                                continue outerLoop;
                            }
                            IntegerInfo integerInfo = (IntegerInfo) constPoolInfo.info;
                            if (foundX) {
                                baseYMult = integerInfo.value;
                            } else {
                                baseXMult = integerInfo.value;
                            }
                            byteArray.index -= 2;
                            break;
                        }
                        case CodeAttribute.GETFIELD: {
                            refInfo = (RefInfo) constPool[byteArray.readUShort()].info;
                            nameAndTypeInfo = (NameAndTypeInfo) constPool[refInfo.nameAndTypeIndex].info;
                            if (foundX) {
                                pathY = (String) constPool[nameAndTypeInfo.nameIndex].info;
                            } else {
                                ClassInfo classInfo = (ClassInfo) constPool[refInfo.classIndex].info;
                                player = (String) constPool[classInfo.nameIndex].info;
                                pathX = (String) constPool[nameAndTypeInfo.nameIndex].info;
                            }
                            byteArray.index -= 2;
                            break;
                        }
                        case CodeAttribute.INVOKEVIRTUAL: {
                            refInfo = (RefInfo) constPool[byteArray.readUShort()].info;
                            nameAndTypeInfo = (NameAndTypeInfo) constPool[refInfo.nameAndTypeIndex].info;
                            String methodName = (String) constPool[nameAndTypeInfo.nameIndex].info;
                            if (!methodName.equals("append")) {
                                continue outerLoop;
                            }
                            byteArray.index -= 2;
                            break;
                        }
                        case CodeAttribute.ICONST_0:
                        case CodeAttribute.IALOAD:
                        case CodeAttribute.IMUL:
                        case CodeAttribute.IADD:
                            break;
                        default:
                            --byteArray.index;
                            continue outerLoop;
                    }
                    if (opcodeLength[op] == 0) throw new RuntimeException();
                    byteArray.index += opcodeLength[op] - 1;
                }
            }
        }
    }
}