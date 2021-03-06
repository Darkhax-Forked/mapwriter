package mapwriter.overlay;

import java.awt.Point;
import java.util.ArrayList;

import mapwriter.api.ILabelInfo;
import mapwriter.api.IMapMode;
import mapwriter.api.IMapView;
import mapwriter.api.IMwChunkOverlay;
import mapwriter.api.IMwDataProvider;
import net.minecraft.util.math.MathHelper;

public class OverlayGrid implements IMwDataProvider {
    public class ChunkOverlay implements IMwChunkOverlay {

        Point coord;

        public ChunkOverlay (int x, int z) {

            this.coord = new Point(x, z);
        }

        @Override
        public int getBorderColor () {

            return 0xff000000;
        }

        @Override
        public float getBorderWidth () {

            return 0.5f;
        }

        @Override
        public int getColor () {

            return 0x00ffffff;
        }

        @Override
        public Point getCoordinates () {

            return this.coord;
        }

        @Override
        public float getFilling () {

            return 1.0f;
        }

        @Override
        public boolean hasBorder () {

            return true;
        }

    }

    @Override
    public ArrayList<IMwChunkOverlay> getChunksOverlay (int dim, double centerX, double centerZ, double minX, double minZ, double maxX, double maxZ) {

        final int minChunkX = (MathHelper.ceil(minX) >> 4) - 1;
        final int minChunkZ = (MathHelper.ceil(minZ) >> 4) - 1;
        final int maxChunkX = (MathHelper.ceil(maxX) >> 4) + 1;
        final int maxChunkZ = (MathHelper.ceil(maxZ) >> 4) + 1;
        final int cX = (MathHelper.ceil(centerX) >> 4) + 1;
        final int cZ = (MathHelper.ceil(centerZ) >> 4) + 1;

        final int limitMinX = Math.max(minChunkX, cX - 100);
        final int limitMaxX = Math.min(maxChunkX, cX + 100);
        final int limitMinZ = Math.max(minChunkZ, cZ - 100);
        final int limitMaxZ = Math.min(maxChunkZ, cZ + 100);

        final ArrayList<IMwChunkOverlay> chunks = new ArrayList<>();
        for (int x = limitMinX; x <= limitMaxX; x++) {
            for (int z = limitMinZ; z <= limitMaxZ; z++) {
                chunks.add(new ChunkOverlay(x, z));
            }
        }

        return chunks;
    }

    @Override
    public ILabelInfo getLabelInfo (int mouseX, int mouseY) {

        return null;
    }

    @Override
    public String getStatusString (int dim, int bX, int bY, int bZ) {

        return "";
    }

    @Override
    public void onDimensionChanged (int dimension, IMapView mapview) {

    }

    @Override
    public void onDraw (IMapView mapview, IMapMode mapmode) {

    }

    @Override
    public void onMapCenterChanged (double vX, double vZ, IMapView mapview) {

    }

    @Override
    public void onMiddleClick (int dim, int bX, int bZ, IMapView mapview) {

    }

    @Override
    public boolean onMouseInput (IMapView mapview, IMapMode mapmode) {

        return false;
    }

    @Override
    public void onOverlayActivated (IMapView mapview) {

    }

    @Override
    public void onOverlayDeactivated (IMapView mapview) {

    }

    @Override
    public void onZoomChanged (int level, IMapView mapview) {

    }
}
