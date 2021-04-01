package de.alewu.dsf.util;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4d;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

import java.awt.Color;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

public class RenderUtils {

    public static void drawBoundingBox(final AxisAlignedBB bb, final double r, final double g, final double b) {
        glPushMatrix();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glLineWidth(1.5f);
        glDisable(GL_LIGHTING);
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        glColor4d(r, g, b, 0.1825f);
        drawBoundingBox(bb);
        glColor4d(r, g, b, 1f);
        drawOutlineBoundingBox(bb);
        glLineWidth(2f);
        glDisable(GL_LINE_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_LIGHTING);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glPopMatrix();
    }

    public static void drawBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(3, DefaultVertexFormats.POSITION);
        renderer.pos(aa.minX, aa.minY, aa.minZ);
        renderer.pos(aa.minX, aa.maxY, aa.minZ);
        renderer.pos(aa.maxX, aa.minY, aa.minZ);
        renderer.pos(aa.maxX, aa.maxY, aa.minZ);
        renderer.pos(aa.maxX, aa.minY, aa.maxZ);
        renderer.pos(aa.maxX, aa.maxY, aa.maxZ);
        renderer.pos(aa.minX, aa.minY, aa.maxZ);
        renderer.pos(aa.minX, aa.maxY, aa.maxZ);
        tessellator.draw();
        renderer.begin(3, DefaultVertexFormats.POSITION);
        renderer.pos(aa.maxX, aa.maxY, aa.minZ);
        renderer.pos(aa.maxX, aa.minY, aa.minZ);
        renderer.pos(aa.minX, aa.maxY, aa.minZ);
        renderer.pos(aa.minX, aa.minY, aa.minZ);
        renderer.pos(aa.minX, aa.maxY, aa.maxZ);
        renderer.pos(aa.minX, aa.minY, aa.maxZ);
        renderer.pos(aa.maxX, aa.maxY, aa.maxZ);
        renderer.pos(aa.maxX, aa.minY, aa.maxZ);
        tessellator.draw();
        renderer.begin(3, DefaultVertexFormats.POSITION);
        renderer.pos(aa.minX, aa.maxY, aa.minZ);
        renderer.pos(aa.maxX, aa.maxY, aa.minZ);
        renderer.pos(aa.maxX, aa.maxY, aa.maxZ);
        renderer.pos(aa.minX, aa.maxY, aa.maxZ);
        renderer.pos(aa.minX, aa.maxY, aa.minZ);
        renderer.pos(aa.minX, aa.maxY, aa.maxZ);
        renderer.pos(aa.maxX, aa.maxY, aa.maxZ);
        renderer.pos(aa.maxX, aa.maxY, aa.minZ);
        tessellator.draw();
        renderer.begin(3, DefaultVertexFormats.POSITION);
        renderer.pos(aa.minX, aa.minY, aa.minZ);
        renderer.pos(aa.maxX, aa.minY, aa.minZ);
        renderer.pos(aa.maxX, aa.minY, aa.maxZ);
        renderer.pos(aa.minX, aa.minY, aa.maxZ);
        renderer.pos(aa.minX, aa.minY, aa.minZ);
        renderer.pos(aa.minX, aa.minY, aa.maxZ);
        renderer.pos(aa.maxX, aa.minY, aa.maxZ);
        renderer.pos(aa.maxX, aa.minY, aa.minZ);
        tessellator.draw();
        renderer.begin(3, DefaultVertexFormats.POSITION);
        renderer.pos(aa.minX, aa.minY, aa.minZ);
        renderer.pos(aa.minX, aa.maxY, aa.minZ);
        renderer.pos(aa.minX, aa.minY, aa.maxZ);
        renderer.pos(aa.minX, aa.maxY, aa.maxZ);
        renderer.pos(aa.maxX, aa.minY, aa.maxZ);
        renderer.pos(aa.maxX, aa.maxY, aa.maxZ);
        renderer.pos(aa.maxX, aa.minY, aa.minZ);
        renderer.pos(aa.maxX, aa.maxY, aa.minZ);
        tessellator.draw();
        renderer.begin(3, DefaultVertexFormats.POSITION);
        renderer.pos(aa.minX, aa.maxY, aa.maxZ);
        renderer.pos(aa.minX, aa.minY, aa.maxZ);
        renderer.pos(aa.minX, aa.maxY, aa.minZ);
        renderer.pos(aa.minX, aa.minY, aa.minZ);
        renderer.pos(aa.maxX, aa.maxY, aa.minZ);
        renderer.pos(aa.maxX, aa.minY, aa.minZ);
        renderer.pos(aa.maxX, aa.maxY, aa.maxZ);
        renderer.pos(aa.maxX, aa.minY, aa.maxZ);
        tessellator.draw();
    }

    public static void drawOutlineBoundingBox(AxisAlignedBB boundingBox) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(3, DefaultVertexFormats.POSITION);
        renderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        renderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        renderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        renderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        renderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        tessellator.draw();
        renderer.begin(3, DefaultVertexFormats.POSITION);
        renderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        renderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        renderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        renderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        renderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        tessellator.draw();
        renderer.begin(1, DefaultVertexFormats.POSITION);
        renderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        renderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        renderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        renderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        renderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        renderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        renderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        renderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawDebugMarker() {
        if (RuntimeContext.getInstance().getMarkerData() != null) {
            DebugMarkerData markerData = RuntimeContext.getInstance().getMarkerData();
            Triple<Float, Float, Float> t = markerData.getTranslation();
            Minecraft mc = Minecraft.getMinecraft();
            if (markerData.isOnCursor()) {
                MovingObjectPosition mouseOver = mc.objectMouseOver;
                if (mouseOver.typeOfHit == MovingObjectType.BLOCK) {
                    BlockPos blockPos = mouseOver.getBlockPos();
                    drawBoundingBox(Triple.of((double) blockPos.getX(), (double) blockPos.getY(), (double) blockPos.getZ()),
                        markerData.getBoundingBox(), markerData.getMarkerColor().getColor());
                } else if (mouseOver.typeOfHit == MovingObjectType.ENTITY) {
                    Entity entity = mouseOver.entityHit;
                    drawBoundingBox(Triple.of(entity.posX - 0.5, entity.posY, entity.posZ - 0.5), markerData.getBoundingBox(), markerData.getMarkerColor().getColor());
                } else if (mouseOver.typeOfHit == MovingObjectType.MISS) {
                    Vec3 vec = MathUtils.calculateLookVector(mc.thePlayer);
                    Triple<Double, Double, Double> pos = Triple.of(mc.thePlayer.posX + vec.xCoord * 3 - 0.5 + t.getLeft(),
                        mc.thePlayer.posY + vec.yCoord * 3 + 1 + t.getMiddle(), mc.thePlayer.posZ + vec.zCoord * 3 - 0.5 + t.getRight());
                    drawBoundingBox(pos, markerData.getBoundingBox(), markerData.getMarkerColor().getColor());
                }
            } else {
                World worldObj = mc.thePlayer.worldObj;
                if (!mc.thePlayer.isSneaking()) {
                    IBlockState blockState = worldObj.getBlockState(new BlockPos(mc.thePlayer.posX + t.getLeft(), mc.thePlayer.posY + t.getMiddle(), mc.thePlayer.posZ + t.getRight()));
                    if (blockState.getBlock() == Blocks.air) {
                        drawBoundingBox(Triple.of(mc.thePlayer.posX - 0.5 + t.getLeft(), mc.thePlayer.posY + t.getMiddle(), mc.thePlayer.posZ - 0.5 + t.getRight()),
                            markerData.getBoundingBox(), markerData.getMarkerColor().getColor());
                    } else {
                        BlockPos blockPos = new BlockPos((int) mc.thePlayer.posX + 0.5 + t.getLeft(), (int) mc.thePlayer.posY + t.getMiddle(), (int) mc.thePlayer.posZ + 0.5 + t.getRight());
                        drawBoundingBox(Triple.of((double) blockPos.getX(), (double) blockPos.getY(), (double) blockPos.getZ()),
                            markerData.getBoundingBox(), markerData.getMarkerColor().getColor());
                    }
                } else {
                    IBlockState blockState = worldObj.getBlockState(new BlockPos(mc.thePlayer.posX + t.getLeft(), mc.thePlayer.posY - 1 + t.getMiddle(), mc.thePlayer.posZ + t.getRight()));
                    if (blockState.getBlock() == Blocks.air) {
                        drawBoundingBox(Triple.of(mc.thePlayer.posX - 0.5 + t.getLeft(), mc.thePlayer.posY - 1 + t.getMiddle(), mc.thePlayer.posZ - 0.5 + t.getRight()),
                            markerData.getBoundingBox(), markerData.getMarkerColor().getColor());
                    } else {
                        BlockPos blockPos = new BlockPos((int) mc.thePlayer.posX + 0.5 + t.getLeft(), (int) mc.thePlayer.posY - 1 + t.getMiddle(), (int) mc.thePlayer.posZ + 0.5 + t.getRight());
                        drawBoundingBox(Triple.of((double) blockPos.getX(), (double) blockPos.getY(), (double) blockPos.getZ()),
                            markerData.getBoundingBox(), markerData.getMarkerColor().getColor());
                    }
                }
            }
        }
    }

    public static void drawBoundingBox(Triple<Double, Double, Double> location, AxisAlignedBB aabb, Color markerColor) {
        Minecraft mc = Minecraft.getMinecraft();
        double viewerPosX = mc.getRenderManager().viewerPosX;
        double viewerPosY = mc.getRenderManager().viewerPosY;
        double viewerPosZ = mc.getRenderManager().viewerPosZ;
        GlStateManager.translate(-viewerPosX + location.getLeft(), -viewerPosY + location.getMiddle(), -viewerPosZ + location.getRight());
        RenderUtils.drawBoundingBox(aabb, markerColor.getRed(), markerColor.getGreen(), markerColor.getBlue());
    }

}
