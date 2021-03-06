package mapwriter.map;

import java.util.List;

import mapwriter.Mw;
import mapwriter.api.IMapMode;
import mapwriter.api.IMapView;
import mapwriter.api.MwAPI;
import mapwriter.config.Config;

public class MapView implements IMapView {
    private int zoomLevel = 0;
    private int dimension = 0;
    private int textureSize = 2048;

    // the position of the centre of the 'view' of the map using game (block)
    // coordinates
    private double x = 0;
    private double z = 0;

    // width and height of map to display in pixels
    private int mapW = 0;
    private int mapH = 0;

    // the width and height of the map in blocks at zoom level 0.
    // updated when map width, map height, or texture size changes.
    private int baseW = 1;
    private int baseH = 1;

    // the width and height of the map in blocks at the current
    // zoom level.
    private double w = 1;
    private double h = 1;

    private final int minZoom;
    private final int maxZoom;

    private boolean undergroundMode;
    private final boolean fullscreenMap;

    public MapView (Mw mw, boolean fullscreenMap) {

        this.minZoom = Config.zoomInLevels;
        this.maxZoom = Config.zoomOutLevels;
        this.undergroundMode = Config.undergroundMode;
        this.fullscreenMap = fullscreenMap;
        if (this.fullscreenMap) {
            this.setZoomLevel(Config.fullScreenZoomLevel);
        }
        this.setZoomLevel(Config.overlayZoomLevel);
        this.setViewCentre(mw.playerX, mw.playerZ);
    }

    @Override
    public int adjustZoomLevel (int n) {

        return this.setZoomLevel(this.zoomLevel + n);
    }

    @Override
    public int getDimension () {

        return this.dimension;
    }

    @Override
    public double getDimensionScaling (int playerDimension) {

        double scale;
        if (this.dimension != -1 && playerDimension == -1) {
            scale = 8.0;
        }
        else if (this.dimension == -1 && playerDimension != -1) {
            scale = 0.125;
        }
        else {
            scale = 1.0;
        }
        return scale;
    }

    @Override
    public double getHeight () {

        return this.h;
    }

    @Override
    public double getMaxX () {

        return this.x + this.w / 2;
    }

    @Override
    public double getMaxZ () {

        return this.z + this.h / 2;
    }

    @Override
    public double getMinX () {

        return this.x - this.w / 2;
    }

    @Override
    public double getMinZ () {

        return this.z - this.h / 2;
    }

    @Override
    public int getPixelsPerBlock () {

        return this.mapW / this.baseW;
    }

    @Override
    public int getRegionZoomLevel () {

        return Math.max(0, this.zoomLevel);
    }

    @Override
    public boolean getUndergroundMode () {

        return this.undergroundMode;
    }

    @Override
    public double getWidth () {

        return this.w;
    }

    @Override
    public double getX () {

        return this.x;
    }

    @Override
    public double getZ () {

        return this.z;
    }

    @Override
    public int getZoomLevel () {

        return this.zoomLevel;
    }

    @Override
    public boolean isBlockWithinView (double bX, double bZ, boolean circular) {

        boolean inside;
        if (!circular) {
            inside = bX > this.getMinX() || bX < this.getMaxX() || bZ > this.getMinZ() || bZ < this.getMaxZ();
        }
        else {
            final double dX = bX - this.x;
            final double dZ = bZ - this.z;
            final double dR = this.getHeight() / 2;
            inside = dX * dX + dZ * dZ < dR * dR;
        }
        return inside;
    }

    @Override
    public void nextDimension (List<Integer> dimensionList, int n) {

        int i = dimensionList.indexOf(this.dimension);
        i = Math.max(0, i);
        final int size = dimensionList.size();
        final int nextDim = dimensionList.get((i + size + n) % size);
        this.setDimensionAndAdjustZoom(nextDim);
    }

    @Override
    public void panView (double relX, double relZ) {

        this.setViewCentre(this.x + relX * this.w, this.z + relZ * this.h);
    }

    @Override
    public void setDimension (int dimension) {

        double scale = 1.0;
        if (dimension != this.dimension) {
            if (this.dimension != -1 && dimension == -1) {
                scale = 0.125;
            }
            else if (this.dimension == -1 && dimension != -1) {
                scale = 8.0;
            }
            this.dimension = dimension;
            this.setViewCentre(this.x * scale, this.z * scale);
        }

        if (MwAPI.getCurrentDataProvider() != null) {
            MwAPI.getCurrentDataProvider().onDimensionChanged(this.dimension, this);
        }
    }

    @Override
    public void setDimensionAndAdjustZoom (int dimension) {

        int zoomLevelChange = 0;
        if (this.dimension != -1 && dimension == -1) {
            zoomLevelChange = -3;
        }
        else if (this.dimension == -1 && dimension != -1) {
            zoomLevelChange = 3;
        }
        this.setZoomLevel(this.getZoomLevel() + zoomLevelChange);
        this.setDimension(dimension);
    }

    @Override
    public void setMapWH (IMapMode mapMode) {

        this.setMapWH(mapMode.getWPixels(), mapMode.getHPixels());
    }

    @Override
    public void setMapWH (int w, int h) {

        if (this.mapW != w || this.mapH != h) {
            this.mapW = w;
            this.mapH = h;
            this.updateBaseWH();
        }
    }

    @Override
    public void setTextureSize (int n) {

        if (this.textureSize != n) {
            this.textureSize = n;
            this.updateBaseWH();
        }
    }

    @Override
    public void setUndergroundMode (boolean enabled) {

        if (enabled && this.zoomLevel >= 0) {

            this.setZoomLevel(-1);
        }

        this.undergroundMode = enabled;
    }

    @Override
    public void setViewCentre (double vX, double vZ) {

        this.x = vX;
        this.z = vZ;

        if (MwAPI.getCurrentDataProvider() != null) {
            MwAPI.getCurrentDataProvider().onMapCenterChanged(vX, vZ, this);
        }

    }

    @Override
    public void setViewCentreScaled (double vX, double vZ, int playerDimension) {

        final double scale = this.getDimensionScaling(playerDimension);
        this.setViewCentre(vX * scale, vZ * scale);
    }

    @Override
    public int setZoomLevel (int zoomLevel) {

        final int prevZoomLevel = this.zoomLevel;
        if (this.undergroundMode) {
            this.zoomLevel = Math.min(Math.max(this.minZoom, zoomLevel), 0);
        }
        else {
            this.zoomLevel = Math.min(Math.max(this.minZoom, zoomLevel), this.maxZoom);
        }
        if (prevZoomLevel != this.zoomLevel) {
            this.updateZoom();
        }

        if (this.fullscreenMap) {
            Config.fullScreenZoomLevel = this.zoomLevel;
        }
        Config.overlayZoomLevel = this.zoomLevel;

        return this.zoomLevel;
    }

    // bX and bZ are the coordinates of the block the zoom is centred on.
    // The relative position of the block in the view will remain the same
    // as before the zoom.
    @Override
    public void zoomToPoint (int newZoomLevel, double bX, double bZ) {

        final int prevZoomLevel = this.zoomLevel;
        newZoomLevel = this.setZoomLevel(newZoomLevel);
        final double zF = Math.pow(2, newZoomLevel - prevZoomLevel);
        this.setViewCentre(bX - (bX - this.x) * zF, bZ - (bZ - this.z) * zF);
    }

    private void updateBaseWH () {

        int nW = this.mapW;
        int nH = this.mapH;
        final int halfTextureSize = this.textureSize / 2;

        // if we cannot display the map at 1x1 pixel per block, then
        // try 2x2 pixels per block, then 4x4 and so on
        while (nW > halfTextureSize || nH > halfTextureSize) {
            nW /= 2;
            nH /= 2;
        }

        this.baseW = nW;
        this.baseH = nH;

        this.updateZoom();
    }

    private void updateZoom () {

        if (this.zoomLevel >= 0) {
            this.w = this.baseW << this.zoomLevel;
            this.h = this.baseH << this.zoomLevel;
        }
        else {
            this.w = this.baseW >> -this.zoomLevel;
            this.h = this.baseH >> -this.zoomLevel;
        }

        if (MwAPI.getCurrentDataProvider() != null) {
            MwAPI.getCurrentDataProvider().onZoomChanged(this.getZoomLevel(), this);
        }
    }
}
