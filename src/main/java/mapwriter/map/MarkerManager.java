package mapwriter.map;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.ARBDepthClamp;
import org.lwjgl.opengl.GL11;

import mapwriter.config.Config;
import mapwriter.config.WorldConfig;
import mapwriter.forge.MwForge;
import mapwriter.map.mapmode.MapMode;
import mapwriter.util.Reference;
import mapwriter.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.common.config.Configuration;

public class MarkerManager {

    public List<Marker> markerList = new ArrayList<>();
    public List<String> groupList = new ArrayList<>();

    public List<Marker> visibleMarkerList = new ArrayList<>();

    private String visibleGroupName = "none";

    public Marker selectedMarker = null;

    public MarkerManager () {

    }

    public void addMarker (Marker marker) {

        this.markerList.add(marker);
    }

    public void addMarker (String name, String groupName, int x, int y, int z, int dimension, int colour) {

        this.addMarker(new Marker(name, groupName, x, y, z, dimension, colour));
        this.save(WorldConfig.getInstance().worldConfiguration, Reference.catMarkers);
    }

    public void clear () {

        this.markerList.clear();
        this.groupList.clear();
        this.visibleMarkerList.clear();
        this.visibleGroupName = "none";
    }

    public int countMarkersInGroup (String group) {

        int count = 0;
        if (group.equals("all")) {
            count = this.markerList.size();
        }
        else {
            for (final Marker marker : this.markerList) {
                if (marker.groupName.equals(group)) {
                    count++;
                }
            }
        }
        return count;
    }

    // returns true if the marker exists in the arraylist.
    // safe to pass null.
    public boolean delMarker (Marker markerToDelete) {

        if (this.selectedMarker == markerToDelete) {
            this.selectedMarker = null;
        }
        final boolean result = this.markerList.remove(markerToDelete);

        this.save(WorldConfig.getInstance().worldConfiguration, Reference.catMarkers);

        return result;
    }

    // deletes the first marker with matching name and group.
    // if null is passed as either name or group it means "any".
    public boolean delMarker (String name, String group) {

        Marker markerToDelete = null;
        for (final Marker marker : this.markerList) {
            if ((name == null || marker.name.equals(name)) && (group == null || marker.groupName.equals(group))) {
                markerToDelete = marker;
                break;
            }
        }
        // will return false if a marker matching the criteria is not found
        // (i.e. if markerToDelete is null)
        return this.delMarker(markerToDelete);
    }

    public void drawBeam (Marker m, float partialTicks) {

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder vertexbuffer = tessellator.getBuffer();

        final float f2 = Minecraft.getMinecraft().world.getTotalWorldTime() + partialTicks;
        final double d3 = f2 * 0.025D * -1.5D;
        // the height of the beam always to the max height
        final double d17 = 255.0D;

        final double x = m.x - TileEntityRendererDispatcher.staticPlayerX;
        final double y = 0.0D - TileEntityRendererDispatcher.staticPlayerY;
        final double z = m.z - TileEntityRendererDispatcher.staticPlayerZ;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.depthMask(false);

        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        // size of the square from middle to edge
        double d4 = 0.2D;

        double d5 = 0.5D + Math.cos(d3 + 2.356194490192345D) * d4;
        double d6 = 0.5D + Math.sin(d3 + 2.356194490192345D) * d4;
        double d7 = 0.5D + Math.cos(d3 + Math.PI / 4D) * d4;
        double d8 = 0.5D + Math.sin(d3 + Math.PI / 4D) * d4;
        double d9 = 0.5D + Math.cos(d3 + 3.9269908169872414D) * d4;
        double d10 = 0.5D + Math.sin(d3 + 3.9269908169872414D) * d4;
        double d11 = 0.5D + Math.cos(d3 + 5.497787143782138D) * d4;
        double d12 = 0.5D + Math.sin(d3 + 5.497787143782138D) * d4;

        final float fRed = m.getRed();
        final float fGreen = m.getGreen();
        final float fBlue = m.getBlue();
        final float fAlpha = 0.125f;

        vertexbuffer.pos(x + d5, y + d17, z + d6).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d5, y, z + d6).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d7, y, z + d8).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d7, y + d17, z + d8).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d11, y + d17, z + d12).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d11, y, z + d12).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d9, y, z + d10).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d9, y + d17, z + d10).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d7, y + d17, z + d8).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d7, y, z + d8).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d11, y, z + d12).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d11, y + d17, z + d12).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d9, y + d17, z + d10).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d9, y, z + d10).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d5, y, z + d6).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d5, y + d17, z + d6).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        tessellator.draw();

        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        // size of the square from middle to edge
        d4 = 0.5D;

        d5 = 0.5D + Math.sin(d3 + 2.356194490192345D) * d4;
        d6 = 0.5D + Math.cos(d3 + 2.356194490192345D) * d4;
        d7 = 0.5D + Math.sin(d3 + Math.PI / 4D) * d4;
        d8 = 0.5D + Math.cos(d3 + Math.PI / 4D) * d4;
        d9 = 0.5D + Math.sin(d3 + 3.9269908169872414D) * d4;
        d10 = 0.5D + Math.cos(d3 + 3.9269908169872414D) * d4;
        d11 = 0.5D + Math.sin(d3 + 5.497787143782138D) * d4;
        d12 = 0.5D + Math.cos(d3 + 5.497787143782138D) * d4;

        vertexbuffer.pos(x + d5, y + d17, z + d6).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d5, y, z + d6).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d7, y, z + d8).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d7, y + d17, z + d8).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d11, y + d17, z + d12).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d11, y, z + d12).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d9, y, z + d10).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d9, y + d17, z + d10).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d7, y + d17, z + d8).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d7, y, z + d8).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d11, y, z + d12).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d11, y + d17, z + d12).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d9, y + d17, z + d10).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d9, y, z + d10).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d5, y, z + d6).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(x + d5, y + d17, z + d6).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        tessellator.draw();

        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public void drawLabel (Marker m) {

        final float growFactor = 0.17F;
        final Minecraft mc = Minecraft.getMinecraft();
        final RenderManager renderManager = mc.getRenderManager();
        final FontRenderer fontrenderer = mc.fontRenderer;

        final double x = 0.5D + m.x - TileEntityRendererDispatcher.staticPlayerX;
        final double y = 0.5D + m.y - TileEntityRendererDispatcher.staticPlayerY;
        final double z = 0.5D + m.z - TileEntityRendererDispatcher.staticPlayerZ;

        final float fRed = m.getRed();
        final float fGreen = m.getGreen();
        final float fBlue = m.getBlue();
        final float fAlpha = 0.2f;

        final double distance = m.getDistanceToMarker(renderManager.renderViewEntity);

        final String strText = m.name;
        final String strDistance = " (" + (int) distance + "m)";

        final int strTextWidth = fontrenderer.getStringWidth(strText) / 2;
        final int strDistanceWidth = fontrenderer.getStringWidth(strDistance) / 2;
        final int offstet = 9;

        final float f = (float) (1.0F + distance * growFactor);
        final float f1 = 0.016666668F * f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-f1, -f1, f1);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glEnable(ARBDepthClamp.GL_DEPTH_CLAMP);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder vertexbuffer = tessellator.getBuffer();

        GlStateManager.disableTexture2D();

        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(-strTextWidth - 1, -1, 0.0D).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(-strTextWidth - 1, 8, 0.0D).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(strTextWidth + 1, 8, 0.0D).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(strTextWidth + 1, -1, 0.0D).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        tessellator.draw();

        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(-strDistanceWidth - 1, -1 + offstet, 0.0D).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(-strDistanceWidth - 1, 8 + offstet, 0.0D).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(strDistanceWidth + 1, 8 + offstet, 0.0D).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        vertexbuffer.pos(strDistanceWidth + 1, -1 + offstet, 0.0D).color(fRed, fGreen, fBlue, fAlpha).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);

        fontrenderer.drawString(strText, -strTextWidth, 0, -1);
        fontrenderer.drawString(strDistance, -strDistanceWidth, offstet, -1);

        GL11.glDisable(ARBDepthClamp.GL_DEPTH_CLAMP);
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public void drawMarkers (MapMode mapMode, MapView mapView) {

        for (final Marker marker : this.visibleMarkerList) {
            // only draw markers that were set in the current dimension
            if (mapView.getDimension() == marker.dimension) {
                marker.draw(mapMode, mapView, 0xff000000);
            }
        }
        if (this.selectedMarker != null) {
            this.selectedMarker.draw(mapMode, mapView, 0xffffffff);
        }
    }

    public void drawMarkersWorld (float partialTicks) {

        if (!Config.drawMarkersInWorld && !Config.drawMarkersNameInWorld || Minecraft.getMinecraft().getRenderManager().renderViewEntity == null) {
            return;
        }

        for (final Marker m : this.visibleMarkerList) {
            if (m.dimension == Minecraft.getMinecraft().player.dimension) {
                if (Config.drawMarkersInWorld) {
                    this.drawBeam(m, partialTicks);
                }
                if (Config.drawMarkersNameInWorld) {
                    this.drawLabel(m);
                }
            }
        }
    }

    public Marker getNearestMarker (int x, int z, int maxDistance) {

        int nearestDistance = maxDistance * maxDistance;
        Marker nearestMarker = null;
        for (final Marker marker : this.visibleMarkerList) {
            final int dx = x - marker.x;
            final int dz = z - marker.z;
            final int d = dx * dx + dz * dz;
            if (d < nearestDistance) {
                nearestMarker = marker;
                nearestDistance = d;
            }
        }
        return nearestMarker;
    }

    public Marker getNearestMarkerInDirection (int x, int z, double desiredAngle) {

        int nearestDistance = 10000 * 10000;
        Marker nearestMarker = null;
        for (final Marker marker : this.visibleMarkerList) {
            final int dx = marker.x - x;
            final int dz = marker.z - z;
            final int d = dx * dx + dz * dz;
            final double angle = Math.atan2(dz, dx);
            // use cos instead of abs as it will wrap at 2 * Pi.
            // cos will be closer to 1.0 the closer desiredAngle and angle are.
            // 0.8 is the threshold corresponding to a maximum of
            // acos(0.8) = 37 degrees difference between the two angles.
            if (Math.cos(desiredAngle - angle) > 0.8D && d < nearestDistance && d > 4) {
                nearestMarker = marker;
                nearestDistance = d;
            }
        }
        return nearestMarker;
    }

    public String getVisibleGroupName () {

        return this.visibleGroupName;
    }

    public void load (Configuration config, String category) {

        this.markerList.clear();

        if (config.hasCategory(category)) {
            final int markerCount = config.get(category, "markerCount", 0).getInt();
            this.visibleGroupName = config.get(category, "visibleGroup", "").getString();

            if (markerCount > 0) {
                for (int i = 0; i < markerCount; i++) {
                    final String key = "marker" + i;
                    final String value = config.get(category, key, "").getString();
                    final Marker marker = this.stringToMarker(value);
                    if (marker != null) {
                        this.addMarker(marker);
                    }
                    else {
                        MwForge.logger.info("error: could not load {} from config file", key);
                    }
                }
            }
        }

        this.update();
    }

    public String markerToString (Marker marker) {

        return String.format("%s:%d:%d:%d:%d:%06x:%s", marker.name, marker.x, marker.y, marker.z, marker.dimension, marker.colour & 0xffffff, marker.groupName);
    }

    public void nextGroup () {

        this.nextGroup(1);
    }

    public void nextGroup (int n) {

        if (this.groupList.size() > 0) {
            int i = this.groupList.indexOf(this.visibleGroupName);
            final int size = this.groupList.size();
            if (i != -1) {
                i = (i + size + n) % size;
            }
            else {
                i = 0;
            }
            this.visibleGroupName = this.groupList.get(i);
        }
        else {
            this.visibleGroupName = "none";
            this.groupList.add("none");
        }
    }

    public void save (Configuration config, String category) {

        config.removeCategory(config.getCategory(category));
        config.get(category, "markerCount", 0).set(this.markerList.size());
        config.get(category, "visibleGroup", "").set(this.visibleGroupName);

        int i = 0;
        for (final Marker marker : this.markerList) {
            final String key = "marker" + i;
            final String value = this.markerToString(marker);
            config.get(category, key, "").set(value);
            i++;
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    public void selectNextMarker () {

        if (this.visibleMarkerList.size() > 0) {
            int i = 0;
            if (this.selectedMarker != null) {
                i = this.visibleMarkerList.indexOf(this.selectedMarker);
                if (i == -1) {
                    i = 0;
                }
            }
            i = (i + 1) % this.visibleMarkerList.size();
            this.selectedMarker = this.visibleMarkerList.get(i);
        }
        else {
            this.selectedMarker = null;
        }
    }

    public void setVisibleGroupName (String groupName) {

        if (groupName != null) {
            this.visibleGroupName = Utils.mungeStringForConfig(groupName);
        }
        else {
            this.visibleGroupName = "none";
        }
    }

    public Marker stringToMarker (String s) {

        // new style delimited with colons
        String[] split = s.split(":");
        if (split.length != 7) {
            // old style was space delimited
            split = s.split(" ");
        }
        Marker marker = null;
        if (split.length == 7) {
            try {
                final int x = Integer.parseInt(split[1]);
                final int y = Integer.parseInt(split[2]);
                final int z = Integer.parseInt(split[3]);
                final int dimension = Integer.parseInt(split[4]);
                final int colour = 0xff000000 | Integer.parseInt(split[5], 16);

                marker = new Marker(split[0], split[6], x, y, z, dimension, colour);

            }
            catch (final NumberFormatException e) {
                marker = null;
            }
        }
        else {
            MwForge.logger.info("Marker.stringToMarker: invalid marker '{}'", s);
        }
        return marker;
    }

    public void update () {

        this.visibleMarkerList.clear();
        this.groupList.clear();
        this.groupList.add("none");
        this.groupList.add("all");
        for (final Marker marker : this.markerList) {
            if (marker.groupName.equals(this.visibleGroupName) || this.visibleGroupName.equals("all")) {
                this.visibleMarkerList.add(marker);
            }
            if (!this.groupList.contains(marker.groupName)) {
                this.groupList.add(marker.groupName);
            }
        }
        if (!this.groupList.contains(this.visibleGroupName)) {
            this.visibleGroupName = "none";
        }
    }
}
