/*
 * Copyright © 2020 by Sk1er LLC
 *
 * All rights reserved.
 *
 * Sk1er LLC
 * 444 S Fulton Ave
 * Mount Vernon, NY
 * sk1er.club
 */

package club.sk1er.patcher.asm.world;

import club.sk1er.patcher.tweaker.transform.PatcherTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class WorldTransformer implements PatcherTransformer {

    /**
     * The class name that's being transformed
     *
     * @return the class name
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.world.World"};
    }

    /**
     * Perform any asm in order to transform code
     *
     * @param classNode the transformed class node
     * @param name      the transformed class name
     */
    @Override
    public void transform(ClassNode classNode, String name) {
        List<String> brightness = Arrays.asList(
            "checkLightFor", "func_180500_c",
            "getLightFromNeighborsFor", "func_175671_l",
            "getLightFromNeighbors", "func_175705_a",
            "getRawLight", "func_175638_a",
            "getLight", "func_175699_k", "func_175721_c"
        );

        for (MethodNode methodNode : classNode.methods) {
            String methodName = mapMethodName(classNode, methodNode);

            if (brightness.contains(methodName)) {
                methodNode.instructions.insert(setLightLevel());
            }

            switch (methodName) {
                case "getHorizon":
                case "func_72919_O":
                    clearInstructions(methodNode);
                    methodNode.instructions.insert(setSkyHeight());
                    break;

                case "updateEntityWithOptionalForce":
                case "func_72866_a": {
                    Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                    while (iterator.hasNext()) {
                        AbstractInsnNode node = iterator.next();
                        if (node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                            MethodInsnNode methodInsnNode = (MethodInsnNode) node;
                            if (methodInsnNode.name.equals("getPersistentChunks")) {
                                AbstractInsnNode prevNode = node.getPrevious();
                                methodNode.instructions.insertBefore(prevNode, new VarInsnNode(Opcodes.ALOAD, 0));
                                methodNode.instructions.insertBefore(prevNode, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/world/World", "field_72995_K", "Z"));
                                methodNode.instructions.insertBefore(prevNode, new InsnNode(Opcodes.ICONST_1));
                                methodNode.instructions.insertBefore(prevNode, new InsnNode(Opcodes.IXOR));
                            }
                        } else if (node.getOpcode() == Opcodes.ISTORE && ((VarInsnNode) node).var == 5) {
                            methodNode.instructions.insertBefore(node, new InsnNode(Opcodes.IAND));
                            break;
                        }
                    }
                    break;
                }

                case "updateEntities":
                case "func_72939_s": {
                    ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();

                    while (iterator.hasNext()) {
                        AbstractInsnNode next = iterator.next();

                        if (next instanceof LdcInsnNode && ((LdcInsnNode) next).cst.equals("blockEntities")) {
                            methodNode.instructions.insertBefore(next.getNext().getNext(), removeTileEntities());
                            break;
                        }
                    }
                    break;
                }

                case "getSkyColor":
                case "func_72833_a":
                    methodNode.instructions.insert(getFasterSkyColor());
                    break;

                case "func_72945_a":
                case "getCollidingBoundingBoxes": {
                    ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();

                    while (iterator.hasNext()) {
                        AbstractInsnNode next = iterator.next();

                        if (next instanceof LdcInsnNode && ((LdcInsnNode) next).cst.equals(0.25D)) {
                            methodNode.instructions.insertBefore(next, filterEntities());
                            break;
                        }
                    }

                    break;
                }
            }
        }
    }

    private InsnList filterEntities() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new TypeInsnNode(Opcodes.INSTANCEOF, "net/minecraft/entity/item/EntityTNTPrimed"));
        LabelNode ifne = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFNE, ifne));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new TypeInsnNode(Opcodes.INSTANCEOF, "net/minecraft/entity/item/EntityFallingBlock"));
        list.add(new JumpInsnNode(Opcodes.IFNE, ifne));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new TypeInsnNode(Opcodes.INSTANCEOF, "net/minecraft/entity/item/EntityItem"));
        list.add(new JumpInsnNode(Opcodes.IFNE, ifne));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new TypeInsnNode(Opcodes.INSTANCEOF, "net/minecraft/client/particle/EntityFX"));
        LabelNode ifeq = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFEQ, ifeq));
        list.add(ifne);
        list.add(new VarInsnNode(Opcodes.ALOAD, 3));
        list.add(new InsnNode(Opcodes.ARETURN));
        list.add(ifeq);
        return list;
    }

    private InsnList getFasterSkyColor() {
        InsnList list = new InsnList();
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "club/sk1er/patcher/util/world/WorldHandler", "skyColorVector", "Lnet/minecraft/util/Vec3;"));
        list.add(new InsnNode(Opcodes.ARETURN));
        return list;
    }

    private InsnList removeTileEntities() {
        InsnList list = new InsnList();
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD,
            "net/minecraft/world/World",
            "field_147483_b", // tileEntitiesToBeRemoved
            "Ljava/util/List;"));
        list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "isEmpty", "()Z", true));
        LabelNode labelNode = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFNE, labelNode));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD,
            "net/minecraft/world/World",
            "field_175730_i", // tickableTileEntities
            "Ljava/util/List;"));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD,
            "net/minecraft/world/World",
            "field_147483_b", // tileEntitiesToBeRemoved
            "Ljava/util/List;"));
        list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "removeAll", "(Ljava/util/Collection;)Z", true));
        list.add(new InsnNode(Opcodes.POP));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD,
            "net/minecraft/world/World",
            "field_147482_g", // loadedTileEntityList
            "Ljava/util/List;"));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD,
            "net/minecraft/world/World",
            "field_147483_b", // tileEntitiesToBeRemoved
            "Ljava/util/List;"));
        list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "removeAll", "(Ljava/util/Collection;)Z", true));
        list.add(new InsnNode(Opcodes.POP));
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD,
            "net/minecraft/world/World",
            "field_147483_b", // tileEntitiesToBeRemoved
            "Ljava/util/List;"));
        list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "clear", "()V", true));
        list.add(labelNode);
        return list;
    }

    private InsnList setLightLevel() {
        InsnList insns = new InsnList();
        insns.add(
            new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "net/minecraft/client/Minecraft",
                "func_71410_x", // getMinecraft
                "()Lnet/minecraft/client/Minecraft;",
                false));
        insns.add(
            new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "net/minecraft/client/Minecraft",
                "func_152345_ab", // isCallingFromMinecraftThread
                "()Z",
                false));
        LabelNode ifeq = new LabelNode();
        insns.add(new JumpInsnNode(Opcodes.IFEQ, ifeq));
        insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "club/sk1er/patcher/util/world/render/FullbrightTicker", "isFullbright", "()Z", false));
        insns.add(new JumpInsnNode(Opcodes.IFEQ, ifeq));
        insns.add(new IntInsnNode(Opcodes.BIPUSH, 15));
        insns.add(new InsnNode(Opcodes.IRETURN));
        insns.add(ifeq);
        return insns;
    }

    private InsnList setSkyHeight() {
        InsnList list = new InsnList();
        list.add(new InsnNode(Opcodes.DCONST_0));
        list.add(new InsnNode(Opcodes.DRETURN));
        return list;
    }
}