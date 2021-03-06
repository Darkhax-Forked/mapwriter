package mapwriter.gui;

import java.awt.Point;
import java.io.IOException;
import java.net.URI;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import mapwriter.Mw;
import mapwriter.api.ILabelInfo;
import mapwriter.api.IMwDataProvider;
import mapwriter.api.MwAPI;
import mapwriter.config.Config;
import mapwriter.config.WorldConfig;
import mapwriter.forge.MwForge;
import mapwriter.forge.MwKeyHandler;
import mapwriter.map.MapRenderer;
import mapwriter.map.MapView;
import mapwriter.map.Marker;
import mapwriter.map.mapmode.MapMode;
import mapwriter.tasks.MergeTask;
import mapwriter.tasks.RebuildRegionsTask;
import mapwriter.util.Reference;
import mapwriter.util.Utils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MwGui extends GuiScreen {
    private final static double PAN_FACTOR = 0.3D;
    private static final int menuY = 5;
    private static final int menuX = 5;
    public static MwGui instance;

    private final Mw mw;
    public MapMode mapMode;

    private final MapView mapView;

    private final MapRenderer map;
    private final String[] HelpText1 = new String[] { "mw.gui.mwgui.keys", "", "  Space", "  Delete", "  C", "  Home", "  End", "  N", "  T", "  P", "  R", "  U", "  L", "", "mw.gui.mwgui.helptext.1", "mw.gui.mwgui.helptext.2", "mw.gui.mwgui.helptext.3", "mw.gui.mwgui.helptext.4", "mw.gui.mwgui.helptext.5", "mw.gui.mwgui.helptext.6", "", "mw.gui.mwgui.helptext.7", "mw.gui.mwgui.helptext.8", "mw.gui.mwgui.helptext.9" };

    private final String[] HelpText2 = new String[] { "", "", "mw.gui.mwgui.helptext.nextmarkergroup", "mw.gui.mwgui.helptext.deletemarker", "mw.gui.mwgui.helptext.cyclecolour", "mw.gui.mwgui.helptext.centermap", "mw.gui.mwgui.helptext.centermapplayer", "mw.gui.mwgui.helptext.selectnextmarker", "mw.gui.mwgui.helptext.teleport", "mw.gui.mwgui.helptext.savepng", "mw.gui.mwgui.helptext.regenerate", "mw.gui.mwgui.helptext.undergroundmap", "mw.gui.mwgui.helptext.markerlist" };
    private int mouseLeftHeld = 0;
    private int mouseLeftDragStartX = 0;
    private int mouseLeftDragStartY = 0;
    private double viewXStart;
    private double viewZStart;
    private Marker movingMarker = null;
    private int movingMarkerXStart = 0;
    private int movingMarkerZStart = 0;
    private int mouseBlockX = 0;
    private int mouseBlockY = 0;

    private int mouseBlockZ = 0;
    private MwGuiLabel helpLabel;
    private MwGuiLabel optionsLabel;
    private MwGuiLabel dimensionLabel;
    private MwGuiLabel groupLabel;
    private MwGuiLabel overlayLabel;

    private final MwGuiMarkerListOverlay MarkerOverlay;
    private MwGuiLabel helpTooltipLabel;
    private MwGuiLabel statusLabel;

    private MwGuiLabel markerLabel;

    private URI clickedLinkURI;

    public MwGui (Mw mw) {

        this.mw = mw;
        this.mapMode = new MapMode(Config.fullScreenMap);
        this.mapView = new MapView(this.mw, true);
        this.map = new MapRenderer(this.mw, this.mapMode, this.mapView);

        this.mapView.setDimension(this.mw.miniMap.view.getDimension());
        this.mapView.setViewCentreScaled(this.mw.playerX, this.mw.playerZ, this.mw.playerDimension);
        this.mapView.setZoomLevel(Config.fullScreenZoomLevel);

        this.initLabels();

        this.MarkerOverlay = new MwGuiMarkerListOverlay(this, this.mw.markerManager);

        MwGui.instance = this;
    }

    public MwGui (Mw mw, int dim, int x, int z) {

        this(mw);
        this.mapView.setDimension(dim);
        this.mapView.setViewCentreScaled(x, z, dim);
        this.mapView.setZoomLevel(Config.fullScreenZoomLevel);
    }

    public void centerOnSelectedMarker () {

        if (this.mw.markerManager.selectedMarker != null) {
            this.mapView.setViewCentreScaled(this.mw.markerManager.selectedMarker.x, this.mw.markerManager.selectedMarker.z, 0);
        }
    }

    @Override
    public void confirmClicked (boolean result, int id) {

        if (id == 31102009) {
            if (result) {
                Utils.openWebLink(this.clickedLinkURI);
            }

            this.clickedLinkURI = null;
            this.mc.displayGuiScreen(this);
        }
    }

    public void deleteSelectedMarker () {

        if (this.mw.markerManager.selectedMarker != null) {

            this.mw.markerManager.delMarker(this.mw.markerManager.selectedMarker);
            this.mw.markerManager.update();
            this.mw.markerManager.selectedMarker = null;
        }
    }

    // also called every frame
    @Override
    public void drawScreen (int mouseX, int mouseY, float f) {

        // check every tick for a change in underground mode.
        // this makes it posible to change to underground mode in the config
        // screen.
        this.mapView.setUndergroundMode(Config.undergroundMode);

        this.drawDefaultBackground();
        double xOffset = 0.0d;
        double yOffset = 0.0d;

        if (this.mouseLeftHeld > 2) {
            xOffset = (this.mouseLeftDragStartX - mouseX) * this.mapView.getWidth() / this.mapMode.getW();
            yOffset = (this.mouseLeftDragStartY - mouseY) * this.mapView.getHeight() / this.mapMode.getH();

            if (this.movingMarker != null) {
                final double scale = this.mapView.getDimensionScaling(this.movingMarker.dimension);
                this.movingMarker.x = this.movingMarkerXStart - (int) (xOffset / scale);
                this.movingMarker.z = this.movingMarkerZStart - (int) (yOffset / scale);
            }
            else {
                this.mapView.setViewCentre(this.viewXStart + xOffset, this.viewZStart + yOffset);
            }
        }

        if (this.mouseLeftHeld > 0) {
            this.mouseLeftHeld++;
        }

        // draw the map
        this.map.draw();

        // get the block the mouse is currently hovering over
        final Point p = this.mapMode.screenXYtoBlockXZ(this.mapView, mouseX, mouseY);
        this.mouseBlockX = p.x;
        this.mouseBlockZ = p.y;
        this.mouseBlockY = this.getHeightAtBlockPos(this.mouseBlockX, this.mouseBlockZ);

        // draw the label near mousepointer
        this.drawMarkerLabel(mouseX, mouseY);

        // draw status message
        this.drawStatus(this.mouseBlockX, this.mouseBlockY, this.mouseBlockZ);

        // draw labels
        this.drawLabel(mouseX, mouseY);

        this.MarkerOverlay.drawScreen(mouseX, mouseY, f);

        super.drawScreen(mouseX, mouseY, f);
    }

    public void drawStatus (int bX, int bY, int bZ) {

        final StringBuilder builder = new StringBuilder();
        if (bY != 0) {
            builder.append(I18n.format("mw.gui.mwgui.status.cursor", bX, bY, bZ));
        }
        else {
            builder.append(I18n.format("mw.gui.mwgui.status.cursorNoY", bX, bZ));
        }

        if (this.mc.world != null && !this.mc.world.getChunkFromBlockCoords(new BlockPos(bX, 0, bZ)).isEmpty()) {

            builder.append(", ");
            builder.append(I18n.format("mw.gui.mwgui.status.biome", this.mc.world.getBiomeForCoordsBody(new BlockPos(bX, 0, bZ)).getBiomeName()));
        }

        final IMwDataProvider provider = MwAPI.getCurrentDataProvider();
        if (provider != null) {
            builder.append(provider.getStatusString(this.mapView.getDimension(), bX, bY, bZ));
        }
        final String s = builder.toString();
        final int x = this.width / 2 - 10 - this.fontRenderer.getStringWidth(s) / 2;

        this.statusLabel.setCoords(x, this.height - 21);
        this.statusLabel.setText(new String[] { builder.toString() }, null);
        this.statusLabel.draw();
    }

    // closes this gui
    public void exitGui () {

        this.mc.displayGuiScreen((GuiScreen) null);
    }

    public int getHeightAtBlockPos (int bX, int bZ) {

        int bY = 0;
        final int worldDimension = this.mw.mc.world.provider.getDimensionType().getId();
        if (worldDimension == this.mapView.getDimension() && worldDimension != -1) {
            bY = this.mw.mc.world.getHeight(new BlockPos(bX, 0, bZ)).getY();
        }
        return bY;
    }

    // get a marker near the specified block pos if it exists.
    // the maxDistance is based on the view width so that you need to click
    // closer
    // to a marker when zoomed in to select it.
    public Marker getMarkerNearScreenPos (int x, int y) {

        Marker nearMarker = null;
        for (final Marker marker : this.mw.markerManager.visibleMarkerList) {
            if (marker.screenPos != null && marker.screenPos.distanceSq(x, y) < 6.0) {
                nearMarker = marker;
            }
        }
        return nearMarker;
    }

    // override GuiScreen's handleMouseInput to process
    // the scroll wheel.
    @Override
    public void handleMouseInput () throws IOException {

        if (this.MarkerOverlay.isMouseInField() && this.mouseLeftHeld == 0) {
            this.MarkerOverlay.handleMouseInput();
        }
        else if (MwAPI.getCurrentDataProvider() != null && MwAPI.getCurrentDataProvider().onMouseInput(this.mapView, this.mapMode)) {
            return;
        }
        else {
            final int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
            final int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            final int direction = Mouse.getEventDWheel();
            if (direction != 0) {
                this.mouseDWheelScrolled(x, y, direction);
            }
        }
        super.handleMouseInput();
    }

    // called when gui is displayed and every time the screen
    // is resized
    @Override
    public void initGui () {

        this.helpLabel.setParentWidthAndHeight(this.width, this.height);
        this.optionsLabel.setParentWidthAndHeight(this.width, this.height);
        this.dimensionLabel.setParentWidthAndHeight(this.width, this.height);
        this.groupLabel.setParentWidthAndHeight(this.width, this.height);
        this.overlayLabel.setParentWidthAndHeight(this.width, this.height);

        this.helpTooltipLabel.setParentWidthAndHeight(this.width, this.height);
        this.statusLabel.setParentWidthAndHeight(this.width, this.height);
        this.markerLabel.setParentWidthAndHeight(this.width, this.height);

        this.MarkerOverlay.setDimensions(MwGuiMarkerListOverlay.listWidth, this.height - 20, MwGuiMarkerListOverlay.ListY, 10 + this.height - 20, this.width - 110);
    }

    public void initLabels () {

        this.helpLabel = new MwGuiLabel(new String[] { "[" + I18n.format("mw.gui.mwgui.help") + "]" }, null, MwGui.menuX, MwGui.menuY, true, false, this.width, this.height);
        this.optionsLabel = new MwGuiLabel(new String[] { "[" + I18n.format("mw.gui.mwgui.options") + "]" }, null, 0, 0, true, false, this.width, this.height);
        this.dimensionLabel = new MwGuiLabel(null, null, 0, 0, true, false, this.width, this.height);
        this.groupLabel = new MwGuiLabel(null, null, 0, 0, true, false, this.width, this.height);
        this.overlayLabel = new MwGuiLabel(null, null, 0, 0, true, false, this.width, this.height);
        this.helpTooltipLabel = new MwGuiLabel(this.HelpText1, this.HelpText2, 0, 0, true, false, this.width, this.height);

        this.statusLabel = new MwGuiLabel(null, null, 0, 0, true, false, this.width, this.height);
        this.markerLabel = new MwGuiLabel(null, null, 0, 0, true, true, this.width, this.height);

        this.optionsLabel.drawToRightOf(this.helpLabel);
        this.dimensionLabel.drawToRightOf(this.optionsLabel);
        this.groupLabel.drawToRightOf(this.dimensionLabel);
        this.overlayLabel.drawToRightOf(this.groupLabel);

        this.helpTooltipLabel.drawToBelowOf(this.helpLabel);
    }

    public boolean isPlayerNearScreenPos (int x, int y) {

        final Point.Double p = this.map.getPlayerArrowScreenPos();
        return p.distanceSq(x, y) < 9.0;
    }

    public void mergeMapViewToImage () {

        this.mw.chunkManager.saveChunks();
        this.mw.executor.addTask(new MergeTask(this.mw.regionManager, (int) this.mapView.getX(), (int) this.mapView.getZ(), (int) this.mapView.getWidth(), (int) this.mapView.getHeight(), this.mapView.getDimension(), this.mw.worldDir, this.mw.worldDir.getName()));

        Utils.printBoth(I18n.format("mw.gui.mwgui.chatmsg.merge", this.mw.worldDir.getAbsolutePath()));
    }

    // zoom on mouse direction wheel scroll
    public void mouseDWheelScrolled (int x, int y, int direction) {

        final Marker marker = this.getMarkerNearScreenPos(x, y);
        if (marker != null && marker == this.mw.markerManager.selectedMarker) {
            if (direction > 0) {
                marker.colourNext();
            }
            else {
                marker.colourPrev();
            }

        }
        else if (this.dimensionLabel.posWithin(x, y)) {
            final int n = direction > 0 ? 1 : -1;
            this.mapView.nextDimension(WorldConfig.getInstance().dimensionList, n);

        }
        else if (this.groupLabel.posWithin(x, y)) {
            final int n = direction > 0 ? 1 : -1;
            this.mw.markerManager.nextGroup(n);
            this.mw.markerManager.update();
        }
        else if (this.overlayLabel.posWithin(x, y)) {
            final int n = direction > 0 ? 1 : -1;
            if (MwAPI.getCurrentDataProvider() != null) {
                MwAPI.getCurrentDataProvider().onOverlayDeactivated(this.mapView);
            }

            if (n == 1) {
                MwAPI.setNextProvider();
            }
            else {
                MwAPI.setPrevProvider();
            }

            if (MwAPI.getCurrentDataProvider() != null) {
                MwAPI.getCurrentDataProvider().onOverlayActivated(this.mapView);
            }

        }
        else {
            final int zF = direction > 0 ? -1 : 1;
            this.mapView.zoomToPoint(this.mapView.getZoomLevel() + zF, this.mouseBlockX, this.mouseBlockZ);
            Config.fullScreenZoomLevel = this.mapView.getZoomLevel();
        }
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    @Override
    public void onGuiClosed () {

        Keyboard.enableRepeatEvents(false);
        this.mw.miniMap.view.setDimension(this.mapView.getDimension());
        Keyboard.enableRepeatEvents(false);
    }

    public void openMarkerGui (Marker m, int mouseX, int mouseY) {

        if (m != null && this.mw.markerManager.selectedMarker == m) {
            // right clicked previously selected marker.
            // edit the marker
            if (Config.newMarkerDialog) {
                this.mc.displayGuiScreen(new MwGuiMarkerDialogNew(this, this.mw.markerManager, m));
            }
            else {
                this.mc.displayGuiScreen(new MwGuiMarkerDialog(this, this.mw.markerManager, m));
            }

        }
        else if (m == null) {
            // open new marker dialog
            String group = this.mw.markerManager.getVisibleGroupName();
            if (group.equals("none")) {
                group = I18n.format("mw.gui.mwgui.group.2");
            }

            int mx, my, mz;
            if (this.isPlayerNearScreenPos(mouseX, mouseY)) {
                // marker at player's locations
                mx = this.mw.playerXInt;
                my = this.mw.playerYInt;
                mz = this.mw.playerZInt;

            }
            else {
                // marker at mouse pointer location
                mx = this.mouseBlockX;
                my = this.mouseBlockY > 0 ? this.mouseBlockY : Config.defaultTeleportHeight;
                mz = this.mouseBlockZ;
            }
            if (Config.newMarkerDialog) {
                this.mc.displayGuiScreen(new MwGuiMarkerDialogNew(this, this.mw.markerManager, "", group, mx, my, mz, this.mapView.getDimension()));
            }
            else {
                this.mc.displayGuiScreen(new MwGuiMarkerDialog(this, this.mw.markerManager, "", group, mx, my, mz, this.mapView.getDimension()));
            }
        }
    }

    public void regenerateView () {

        Utils.printBoth(I18n.format("mw.gui.mwgui.chatmsg.regenmap", (int) this.mapView.getWidth(), (int) this.mapView.getHeight(), (int) this.mapView.getMinX(), (int) this.mapView.getMinZ()));
        this.mw.executor.addTask(new RebuildRegionsTask(this.mw, (int) this.mapView.getMinX(), (int) this.mapView.getMinZ(), (int) this.mapView.getWidth(), (int) this.mapView.getHeight(), this.mapView.getDimension()));
    }

    // called every frame
    @Override
    public void updateScreen () {

    }

    private void drawLabel (int mouseX, int mouseY) {

        this.helpLabel.draw();
        this.optionsLabel.draw();
        final String dimString = "[" + I18n.format("mw.gui.mwgui.dimension", this.mapView.getDimension()) + "]";
        this.dimensionLabel.setText(new String[] { dimString }, null);
        this.dimensionLabel.draw();

        final String groupString = "[" + I18n.format("mw.gui.mwgui.group.1", this.mw.markerManager.getVisibleGroupName()) + "]";
        this.groupLabel.setText(new String[] { groupString }, null);
        this.groupLabel.draw();

        final String overlayString = "[" + I18n.format("mw.gui.mwgui.overlay", MwAPI.getCurrentProviderName()) + "]";
        this.overlayLabel.setText(new String[] { overlayString }, null);
        this.overlayLabel.draw();

        // help message on mouse over
        if (this.helpLabel.posWithin(mouseX, mouseY)) {
            this.helpTooltipLabel.draw();
        }
    }

    private void drawMarkerLabel (int mouseX, int mouseY) {

        // draw name of marker under mouse cursor
        final Marker marker = this.getMarkerNearScreenPos(mouseX, mouseY);
        if (marker != null) {
            this.markerLabel.setText(new String[] { marker.name, String.format("(%d, %d, %d)", marker.x, marker.y, marker.z) }, null);
            this.markerLabel.setCoords(mouseX + 8, mouseY);
            this.markerLabel.draw();
            return;
        }

        // draw name of player under mouse cursor
        if (this.isPlayerNearScreenPos(mouseX, mouseY)) {
            this.markerLabel.setText(new String[] { this.mc.player.getDisplayNameString(), String.format("(%d, %d, %d)", this.mw.playerXInt, this.mw.playerYInt, this.mw.playerZInt) }, null);
            this.markerLabel.setCoords(mouseX + 8, mouseY);
            this.markerLabel.draw();
            return;
        }

        final IMwDataProvider provider = MwAPI.getCurrentDataProvider();
        if (provider != null) {
            final ILabelInfo info = provider.getLabelInfo(mouseX, mouseY);
            if (info != null) {
                this.markerLabel.setText(info.getInfoText(), null);
                this.markerLabel.setCoords(mouseX + 8, mouseY);
                this.markerLabel.draw();
                return;
            }
        }
    }

    // called when a button is pressed
    @Override
    protected void actionPerformed (GuiButton button) {

    }

    // c is the ascii equivalent of the key typed.
    // key is the lwjgl key code.
    @Override
    protected void keyTyped (char c, int key) {

        switch (key) {
            case Keyboard.KEY_ESCAPE:
                this.exitGui();
                break;

            case Keyboard.KEY_DELETE:
                this.deleteSelectedMarker();
                break;

            case Keyboard.KEY_SPACE:
                // next marker group
                this.mw.markerManager.nextGroup();
                this.mw.markerManager.update();
                break;

            case Keyboard.KEY_C:
                // cycle selected marker colour
                if (this.mw.markerManager.selectedMarker != null) {
                    this.mw.markerManager.selectedMarker.colourNext();
                }
                break;

            case Keyboard.KEY_N:
                // select next visible marker
                this.mw.markerManager.selectNextMarker();
                break;

            case Keyboard.KEY_HOME:
                // centre map on player
                this.mapView.setViewCentreScaled(this.mw.playerX, this.mw.playerZ, this.mw.playerDimension);
                break;

            case Keyboard.KEY_END:
                // centre map on selected marker
                this.centerOnSelectedMarker();
                break;

            case Keyboard.KEY_P:
                this.mergeMapViewToImage();
                this.exitGui();
                break;

            case Keyboard.KEY_T:
                if (this.mw.markerManager.selectedMarker != null) {
                    this.mw.teleportToMarker(this.mw.markerManager.selectedMarker);
                    this.exitGui();
                }
                else {
                    this.mc.displayGuiScreen(new MwGuiTeleportDialog(this, this.mw, this.mapView, this.mouseBlockX, Config.defaultTeleportHeight, this.mouseBlockZ));
                }
                break;

            case Keyboard.KEY_LEFT:
                this.mapView.panView(-MwGui.PAN_FACTOR, 0);
                break;
            case Keyboard.KEY_RIGHT:
                this.mapView.panView(MwGui.PAN_FACTOR, 0);
                break;
            case Keyboard.KEY_UP:
                this.mapView.panView(0, -MwGui.PAN_FACTOR);
                break;
            case Keyboard.KEY_DOWN:
                this.mapView.panView(0, MwGui.PAN_FACTOR);
                break;

            case Keyboard.KEY_R:
                this.regenerateView();
                this.exitGui();
                break;

            case Keyboard.KEY_L:
                this.MarkerOverlay.setEnabled(!this.MarkerOverlay.getEnabled());
                break;

            default:
                if (key == MwKeyHandler.keyMapGui.getKeyCode()) {
                    this.exitGui();
                }
                else if (key == MwKeyHandler.keyZoomIn.getKeyCode()) {
                    this.mapView.adjustZoomLevel(-1);
                }
                else if (key == MwKeyHandler.keyZoomOut.getKeyCode()) {
                    this.mapView.adjustZoomLevel(1);
                }
                else if (key == MwKeyHandler.keyNextGroup.getKeyCode()) {
                    this.mw.markerManager.nextGroup();
                    this.mw.markerManager.update();
                }
                else if (key == MwKeyHandler.keyUndergroundMode.getKeyCode()) {
                    Mw.toggleUndergroundMode();
                }
                break;
        }
    }

    // mouse button clicked. 0 = LMB, 1 = RMB, 2 = MMB
    @Override
    protected void mouseClicked (int x, int y, int button) {

        final Marker marker = this.getMarkerNearScreenPos(x, y);
        final Marker prevMarker = this.mw.markerManager.selectedMarker;

        if (this.MarkerOverlay.isMouseInField() && this.mouseLeftHeld == 0) {
            this.MarkerOverlay.handleMouseInput();
        }
        else {
            if (button == 0) {
                if (this.dimensionLabel.posWithin(x, y)) {
                    this.mc.displayGuiScreen(new MwGuiDimensionDialog(this, this.mw, this.mapView, this.mapView.getDimension()));
                }
                else if (this.optionsLabel.posWithin(x, y)) {
                    try {
                        final GuiScreen newScreen = ModGuiConfig.class.getConstructor(GuiScreen.class).newInstance(this);
                        this.mc.displayGuiScreen(newScreen);
                    }
                    catch (final Exception e) {
                        MwForge.logger.error("There was a critical issue trying to build the config GUI for {}", Reference.MOD_ID);
                    }
                }
                else {
                    this.mouseLeftHeld = 1;
                    this.mouseLeftDragStartX = x;
                    this.mouseLeftDragStartY = y;
                    this.mw.markerManager.selectedMarker = marker;

                    if (marker != null && prevMarker == marker) {
                        // clicked previously selected marker.
                        // start moving the marker.
                        this.movingMarker = marker;
                        this.movingMarkerXStart = marker.x;
                        this.movingMarkerZStart = marker.z;
                    }
                }

            }
            else if (button == 1) {
                this.openMarkerGui(marker, x, y);
            }

            else if (button == 2) {
                final Point blockPoint = this.mapMode.screenXYtoBlockXZ(this.mapView, x, y);

                final IMwDataProvider provider = MwAPI.getCurrentDataProvider();
                if (provider != null) {
                    provider.onMiddleClick(this.mapView.getDimension(), blockPoint.x, blockPoint.y, this.mapView);
                }
            }

            this.viewXStart = this.mapView.getX();
            this.viewZStart = this.mapView.getZ();
        }
    }

    // mouse button released. 0 = LMB, 1 = RMB, 2 = MMB
    // not called on mouse movement.
    @Override
    protected void mouseReleased (int x, int y, int button) {

        if (button == 0) {
            this.mouseLeftHeld = 0;
            this.movingMarker = null;
        }
    }
}
