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

    private boolean addedTickEvent;

    public byte[] processClass(String name, byte[] bytes) {
        ClassFile classFile = null;
        if (name.equals("client")) {
            clientClass = classFile = new ClassFile(new ByteArray(bytes));
        } else if (!addedTickEvent) {
            classFile = new ClassFile(new ByteArray(bytes));
        } else {
            return bytes;
        }

        if (!addedTickEvent) {
            ConstPool constPool = classFile.constPool;
            for (int i = 0; i < classFile.methods.length; ++i) {
                MethodInfo methodInfo = classFile.methods[i];
                if (tryAddTickEvent(methodInfo, constPool)) {
                    addedTickEvent = true;
                    break;
                }
            }
        }
        return classFile.bytes.array;
    }

    public void finalize(ClassLoader classLoader) throws Exception {
        if (!addedTickEvent) throw new RuntimeException();

        ByteArray bytes = clientClass.bytes;
        ConstPool constPool = clientClass.constPool;

        // Find local player and coordinate fields. Big mess :-)
        for (int i = 0; i < clientClass.methods.length; ++i) {
            MethodInfo methodInfo = clientClass.methods[i];
            if (methodInfo.accessFlags != MethodInfo.ACC_FINAL) continue;
            String descriptor = constPool.getUtf8Info(methodInfo.descriptorIndex);
            if (!descriptor.matches("\\(L\\w{1,2};.\\)Z")) continue;

            CodeAttribute codeAttribute = methodInfo.codeAttribute;
            bytes.index = codeAttribute.codeStartIndex;

            while (true) {
                int op = bytes.readUByte();
                if (op != CodeAttribute.GETSTATIC) {
                    bytes.index += CodeAttribute.OPERAND_SIZES[op];
                    continue;
                }
                String fieldDescriptor = constPool.getRefDescriptor(bytes.readUShort());
                if (!fieldDescriptor.equals("Ljava/lang/String;")) continue;

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

                boolean foundX = false;

                boolean stillPotential = true;
                while (stillPotential) {
                    op = bytes.readUByte();
                    switch (op) {
                        case CodeAttribute.GETSTATIC: {
                            int refInfoIndex = bytes.peekUShort();
                            fieldDescriptor = constPool.getRefDescriptor(refInfoIndex);
                            if (fieldDescriptor.matches("L\\w{1,2};")) {
                                localPlayerOwner = constPool.getRefClassName(refInfoIndex);
                                localPlayer = constPool.getRefName(refInfoIndex);
                            } else if (fieldDescriptor.equals("I")) {
                                if (foundX) {
                                    baseYOwner = constPool.getRefClassName(refInfoIndex);
                                    baseY = constPool.getRefName(refInfoIndex);
                                } else {
                                    baseXOwner = constPool.getRefClassName(refInfoIndex);
                                    baseX = constPool.getRefName(refInfoIndex);
                                }
                            } else if (fieldDescriptor.equals("Ljava/lang/String;")) {
                                if (foundX && pathY != null && baseYOwner != null && baseY != null && baseYMult != 0) {
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
                                stillPotential = false;
                            }
                            break;
                        }
                        case CodeAttribute.LDC_W: {
                            ConstPoolInfo constPoolInfo = constPool.constPoolInfos[bytes.peekUShort()];
                            if (constPoolInfo.tag != ConstPoolInfo.TAG_INTEGER) {
                                stillPotential = false;
                            }
                            int value = (int) constPoolInfo.info;
                            if (foundX) {
                                baseYMult = value;
                            } else {
                                baseXMult = value;
                            }
                            break;
                        }
                        case CodeAttribute.GETFIELD: {
                            int refInfoIndex = bytes.peekUShort();
                            if (foundX) {
                                pathY = constPool.getRefName(refInfoIndex);
                            } else {
                                player = constPool.getRefClassName(refInfoIndex);
                                pathX = constPool.getRefName(refInfoIndex);
                            }
                            break;
                        }
                        case CodeAttribute.INVOKEVIRTUAL: {
                            String methodName = constPool.getRefName(bytes.peekUShort());
                            if (!methodName.equals("append")) {
                                stillPotential = false;
                            }
                            break;
                        }
                        case CodeAttribute.ICONST_0:
                        case CodeAttribute.IALOAD:
                        case CodeAttribute.IMUL:
                        case CodeAttribute.IADD:
                            break;
                        default:
                            stillPotential = false;
                    }
                    bytes.index += CodeAttribute.OPERAND_SIZES[op];
                }
            }
        }
        throw new RuntimeException();
    }

    private boolean tryAddTickEvent(MethodInfo methodInfo, ConstPool constPool) {
        if (methodInfo.accessFlags != (MethodInfo.ACC_STATIC | MethodInfo.ACC_FINAL)) return false;

        String descriptor = constPool.getUtf8Info(methodInfo.descriptorIndex);
        if (!descriptor.matches("\\(ZL\\w{1,2};.\\)V")) return false;

        CodeAttribute codeAttribute = methodInfo.codeAttribute;

        ByteArray bytes = codeAttribute.classFile.bytes;
        bytes.index = codeAttribute.codeStartIndex;

        int[] pattern = { CodeAttribute.ICONST_0, CodeAttribute.PUTSTATIC, CodeAttribute.ICONST_0, CodeAttribute.PUTSTATIC };
        int putstaticIndex = checkPattern(bytes, pattern, 1);
        if (putstaticIndex >= 0) {
            int caveOffset = codeAttribute.addCodeCave(putstaticIndex, 3);
            int methodRefIndex = constPool.addRefInfo("miniclient/MiniClient", "onTick", "()V", ConstPoolInfo.TAG_METHOD_REF);

            bytes.index = codeAttribute.codeStartIndex + caveOffset;
            bytes.writeByte(CodeAttribute.INVOKESTATIC);
            bytes.writeShort(methodRefIndex);
            return true;
        }
        return false;
    }

    private int checkPattern(ByteArray bytes, int[] pattern, int returnIndexOf) {
        int returnIndex = -1;
        int patternIndex = 0;
        while (patternIndex < pattern.length) {
            int op = bytes.readUByte();
            if (op == CodeAttribute.GOTO) {
                bytes.index = (bytes.index - 1) + bytes.readUShort();
                continue;
            } else if (pattern[patternIndex] == op) {
                if (patternIndex == returnIndexOf) {
                    returnIndex = bytes.index - 1;
                }
                ++patternIndex;
                bytes.index += CodeAttribute.OPERAND_SIZES[op];
            } else {
                --bytes.index;
                return -1;
            }
        }
        return returnIndex;
    }
}