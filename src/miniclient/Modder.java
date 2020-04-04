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
        return classFile.bytes.bytes;
    }

    // Big mess :-)
    public void finalize(ClassLoader classLoader) throws Exception {
        if (!addedTickEvent) throw new RuntimeException();

        ByteArray bytes = clientClass.bytes;
        ConstPool constPool = clientClass.constPool;

        for (int i = 0; i < clientClass.methods.length; ++i) {
            MethodInfo methodInfo = clientClass.methods[i];
            if (methodInfo.accessFlags != MethodInfo.ACC_FINAL) continue;
            String descriptor = constPool.getUtf8Info(methodInfo.descriptorIndex);
            if (!descriptor.matches("\\(L\\w{1,2};.\\)Z")) continue;

            CodeAttribute codeAttribute = methodInfo.codeAttribute;
            bytes.index = codeAttribute.codeStartIndex;

            boolean foundX = false;
            outerLoop:
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

                while (true) {
                    op = bytes.readUByte();
                    switch (op) {
                        case CodeAttribute.GETSTATIC: {
                            int refInfoIndex = bytes.readUShort();
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
                                continue outerLoop;
                            }
                            bytes.index -= 2;
                            break;
                        }
                        case CodeAttribute.LDC_W: {
                            ConstPoolInfo constPoolInfo = constPool.constPoolInfos[bytes.readUShort()];
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
                            int refInfoIndex = bytes.readUShort();
                            if (foundX) {
                                pathY = constPool.getRefName(refInfoIndex);
                            } else {
                                player = constPool.getRefClassName(refInfoIndex);
                                pathX = constPool.getRefName(refInfoIndex);
                            }
                            bytes.index -= 2;
                            break;
                        }
                        case CodeAttribute.INVOKEVIRTUAL: {
                            String methodName = constPool.getRefName(bytes.readUShort());
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

    private boolean tryAddTickEvent(MethodInfo methodInfo, ConstPool constPool) {
        if (methodInfo.accessFlags != (MethodInfo.ACC_STATIC | MethodInfo.ACC_FINAL)) return false;

        String descriptor = constPool.getUtf8Info(methodInfo.descriptorIndex);
        if (!descriptor.matches("\\(ZL\\w{1,2};.\\)V")) return false;

        CodeAttribute codeAttribute = methodInfo.codeAttribute;

        ByteArray bytes = codeAttribute.classFile.bytes;
        bytes.index = codeAttribute.codeStartIndex;

        int[] pattern = { CodeAttribute.ICONST_0, CodeAttribute.PUTSTATIC, CodeAttribute.ICONST_0, CodeAttribute.PUTSTATIC };
        if (checkPattern(bytes, pattern)) {
            int methodRefIndex = constPool.addRefInfo("miniclient/MiniClient", "onTick", "()V", ConstPoolInfo.TAG_METHOD_REF);
            // Replace the first putstatic.
            int putstaticIndex = codeAttribute.codeStartIndex + 1;
            bytes.index = putstaticIndex + 1;
            int putstaticOperand = bytes.readUShort();
            int gapIndex = codeAttribute.addEndGap(9);

            bytes.index = putstaticIndex;
            bytes.writeByte(CodeAttribute.GOTO);
            bytes.writeShort(gapIndex - putstaticIndex);
            bytes.index = gapIndex;
            bytes.writeByte(CodeAttribute.INVOKESTATIC);
            bytes.writeShort(methodRefIndex);
            bytes.writeByte(CodeAttribute.PUTSTATIC);
            bytes.writeShort(putstaticOperand);
            bytes.writeByte(CodeAttribute.GOTO);
            bytes.writeShort((putstaticIndex + 3) - (bytes.index - 1));
            return true;
        }
        return false;
    }

    public static void tick() {
        System.out.println("tick!");
    }

    private boolean checkPattern(ByteArray bytes, int[] pattern) {
        int patternIndex = 0;

        while (patternIndex < pattern.length) {
            int op = bytes.readUByte();
            if (op == CodeAttribute.GOTO) {
                bytes.index = (bytes.index - 1) + bytes.readUShort();
                continue;
            }
            if (pattern[patternIndex] == op) {
                ++patternIndex;
                bytes.index += CodeAttribute.OPERAND_SIZES[op];
                if (CodeAttribute.OPERAND_SIZES[op] < 0) throw new RuntimeException();
            } else {
                return false;
            }
        }
        return true;
    }
}