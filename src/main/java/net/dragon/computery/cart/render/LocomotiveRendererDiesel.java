package net.dragon.computery.cart.render;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import mods.railcraft.api.carts.locomotive.IRenderer;
import mods.railcraft.client.render.TexturedQuadAdv;
import mods.railcraft.client.render.carts.LocomotiveRendererDefault;
import mods.railcraft.client.render.models.ModelSimple;
import mods.railcraft.common.carts.EntityLocomotive;
import mods.railcraft.common.core.RailcraftConstants;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class LocomotiveRendererDiesel extends LocomotiveRendererDefault {

    private static final ModelBase LAMP_OFF = new ModelLampOff();
    private static final ModelBase LAMP_ON = new ModelLampOn();
    private final ResourceLocation LAMP_TEX_ON;
    private final ResourceLocation LAMP_TEX_OFF;

    public LocomotiveRendererDiesel(String tag, String modelTag) {
        super("railcraft:" + tag, modelTag, new ModelLocomotiveDiesel());
        LAMP_TEX_ON = new ResourceLocation(RailcraftConstants.LOCOMOTIVE_TEXTURE_FOLDER + modelTag + ".lamp.on.png");
        LAMP_TEX_OFF = new ResourceLocation(RailcraftConstants.LOCOMOTIVE_TEXTURE_FOLDER + modelTag + ".lamp.off.png");
        setEmblemPosition(0.2F, -0.03F, -0.41F, -0.505F);
    }

    @Override
    public void renderLocomotive(IRenderer renderer, EntityMinecart cart, int primaryColor, int secondaryColor, ResourceLocation emblemTexture, float light, float time) {
        super.renderLocomotive(renderer, cart, primaryColor, secondaryColor, emblemTexture, light, time);
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glScalef(-1F, -1F, 1.0F);
        GL11.glTranslatef(0.05F, 0.0F, 0.0F);
        int color = cart.getDataWatcher().getWatchableObjectInt(31);

        if (((EntityLocomotive) cart).getMode() == EntityLocomotive.LocoMode.RUNNING
                && (cart.motionX != 0 || cart.motionZ != 0)) {
            renderer.bindTex(LAMP_TEX_ON);
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_DST_ALPHA);
            LAMP_ON.render(cart, -0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

        } else {

            renderer.bindTex(LAMP_TEX_OFF);
            GL11.glColor3f(((color >> 16) & 0xFF) / 256.0F, ((color >> 8) & 0xFF) / 256.0F, ((color) & 0xFF) / 256.0F);
            LAMP_OFF.render(cart, -0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        }

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    private static class ModelLampOff extends ModelSimple {

        public ModelLampOff() {
            super("lamp");
            renderer.setTextureSize(16, 16);
            setTextureOffset("lamp.bulb", 1, 1);
            renderer.addBox("bulb", -22F, -17F, -9F, 1, 2, 2);
            renderer.rotationPointX = 8F;
            renderer.rotationPointY = 8F;
            renderer.rotationPointZ = 8F;

        }

    }

    private static class ModelLampOn extends ModelLampOff {

        public ModelLampOn() {
            for (Object box : renderer.cubeList) {
                TexturedQuadAdv[] quadsNew = new TexturedQuadAdv[6];
                TexturedQuad[] quadsOld = ObfuscationReflectionHelper.getPrivateValue(ModelBox.class, (ModelBox) box, 1);
                for (int i = 0; i < 6; i++) {
                    quadsNew[i] = new TexturedQuadAdv(quadsOld[i].vertexPositions);
                    quadsNew[i].setBrightness(210);
                    quadsNew[i].setColorRGBA(255, 255, 255, 255);
                }
                ObfuscationReflectionHelper.setPrivateValue(ModelBox.class, (ModelBox) box, quadsNew, 1);
            }
        }

    }

}
