package mapwriter;

import mapwriter.region.BlockColours;
import mapwriter.util.Logging;
import mapwriter.util.Render;
import mapwriter.util.Texture;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.biome.Biome;

// Static class to generate BlockColours.
// This is separate from BlockColours because it needs to run in the GL
// rendering thread
// whereas the generated BlockColours object is used only in the background
// thread.
// So basically split to make it clear that BlockColourGen and the generated
// BlockColours
// must not have any interaction after it is generated.

public class BlockColourGen {
    public static void genBlockColours (BlockColours bc) {

        Logging.log("generating block map colours from textures");

        // copy terrain texture to MwRender pixel bytebuffer

        // bind the terrain texture
        // Minecraft.getMinecraft().func_110434_K().func_110577_a(TextureMap.field_110575_b);
        // get the bound texture id
        // int terrainTextureId = Render.getBoundTextureId();

        final int terrainTextureId = Minecraft.getMinecraft().renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).getGlTextureId();

        // create texture object from the currently bound GL texture
        if (terrainTextureId == 0) {
            Logging.log("error: could get terrain texture ID");
            return;
        }
        final Texture terrainTexture = new Texture(terrainTextureId);

        double u1Last = 0;
        double u2Last = 0;
        double v1Last = 0;
        double v2Last = 0;
        int blockColourLast = 0;
        int e_count = 0;
        int b_count = 0;
        int s_count = 0;

        for (final Object oblock : Block.REGISTRY) {
            final Block block = (Block) oblock;
            final int blockID = Block.getIdFromBlock(block);

            for (int dv = 0; dv < 16; dv++) {
                int blockColour = 0;

                if (block != null && block.getRenderType(block.getDefaultState()) != EnumBlockRenderType.INVISIBLE) {

                    TextureAtlasSprite icon = null;
                    try {
                        icon = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(block.getStateFromMeta(dv));
                    }
                    catch (final Exception e) {
                        // MwUtil.log("genFromTextures: exception caught when
                        // requesting block texture for %03x:%x",
                        // blockID, dv);
                        // e.printStackTrace();
                        e_count++;
                    }

                    if (icon != null) {
                        final double u1 = icon.getMinU();
                        final double u2 = icon.getMaxU();
                        final double v1 = icon.getMinV();
                        final double v2 = icon.getMaxV();

                        if (u1 == u1Last && u2 == u2Last && v1 == v1Last && v2 == v2Last) {
                            blockColour = blockColourLast;
                            s_count++;
                        }
                        else {
                            blockColour = getIconMapColour(icon, terrainTexture);
                            // request icon with meta 16, carpenterblocks uses
                            // this method to get the real texture
                            // this makes the carpenterblocks render as brown
                            // blocks on the map
                            if (Block.REGISTRY.getNameForObject(block).getResourceDomain().contains("CarpentersBlocks")) {
                                // icon = block.getIcon(1, 16);
                                // blockColour = getIconMapColour(icon,
                                // terrainTexture);
                            }

                            u1Last = u1;
                            u2Last = u2;
                            v1Last = v1;
                            v2Last = v2;
                            blockColourLast = blockColour;
                            b_count++;
                        }
                    }
                }
                bc.setColour(block.delegate.name().toString(), String.valueOf(dv), blockColour);
            }
        }

        Logging.log("processed %d block textures, %d skipped, %d exceptions", b_count, s_count, e_count);

        genBiomeColours(bc);
    }

    private static void genBiomeColours (BlockColours bc) {
        // generate array of foliage, grass, and water colour multipliers
        // for each biome.

        for (final Biome biome : Biome.REGISTRY) {
            if (biome != null) {
                final double temp = MathHelper.clamp(biome.getDefaultTemperature(), 0.0F, 1.0F);
                final double rain = MathHelper.clamp(biome.getRainfall(), 0.0F, 1.0F);
                final int grasscolor = ColorizerGrass.getGrassColor(temp, rain);
                final int foliagecolor = ColorizerFoliage.getFoliageColor(temp, rain);
                final int watercolor = biome.getWaterColorMultiplier();

                bc.setBiomeData(biome.getBiomeName(), watercolor & 0xffffff, grasscolor & 0xffffff, foliagecolor & 0xffffff);
            }
        }
    }

    private static int getIconMapColour (TextureAtlasSprite icon, Texture terrainTexture) {

        // flipped icons have the U and V coords reversed (minU > maxU, minV >
        // maxV).
        // thanks go to taelnia for fixing this.
        final int iconX = Math.round(terrainTexture.w * Math.min(icon.getMinU(), icon.getMaxU()));
        final int iconY = Math.round(terrainTexture.h * Math.min(icon.getMinV(), icon.getMaxV()));
        final int iconWidth = Math.round(terrainTexture.w * Math.abs(icon.getMaxU() - icon.getMinU()));
        final int iconHeight = Math.round(terrainTexture.h * Math.abs(icon.getMaxV() - icon.getMinV()));

        final int[] pixels = new int[iconWidth * iconHeight];

        // MwUtil.log("(%d, %d) %dx%d", iconX, iconY, iconWidth, iconHeight);

        terrainTexture.getRGB(iconX, iconY, iconWidth, iconHeight, pixels, 0, iconWidth, icon);

        // need to use custom averaging routine rather than scaling down to one
        // pixel to
        // stop transparent pixel colours being included in the average.
        return Render.getAverageColourOfArray(pixels);
    }
}
