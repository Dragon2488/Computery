package net.dragon.computery.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Map;

@IFMLLoadingPlugin.Name("RailcraftIsFuckingStupid")
public class CoreMod implements IFMLLoadingPlugin, IClassTransformer {
    @Override
    public byte[] transform(String s, String s1, byte[] bytes) {
        if(s1.equals("mods.railcraft.common.carts.EntityCartEnergy")) {
            ClassReader reader = new ClassReader(bytes);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            for(MethodNode methodNode : classNode.methods) {
                if(methodNode.name.equals("getMaxCartSpeedOnRail")) {
                    InsnList insnList = methodNode.instructions;
                    insnList.clear();
                    insnList.add(new LdcInsnNode(0.95f));
                    insnList.add(new InsnNode(Opcodes.FRETURN));
                    methodNode.maxLocals = 1;
                    methodNode.maxStack = 1;
                    System.out.println("Transformed");
                }
            }

            ClassWriter writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }
        return bytes;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {getClass().getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
