package mapwriter.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mapwriter.Mw;
import mapwriter.util.Reference;
import mapwriter.util.Utils;
import net.minecraftforge.common.config.Configuration;

public class WorldConfig {
    private static WorldConfig instance = null;

    public static WorldConfig getInstance () {

        if (instance == null) {
            synchronized (WorldConfig.class) {
                if (instance == null) {
                    instance = new WorldConfig();
                }
            }
        }

        return instance;
    }

    public Configuration worldConfiguration = null;

    // list of available dimensions
    public List<Integer> dimensionList = new ArrayList<>();

    private WorldConfig () {

        // load world specific config file
        final File worldConfigFile = new File(Mw.getInstance().worldDir, Reference.worldDirConfigName);
        this.worldConfiguration = new Configuration(worldConfigFile);

        this.InitDimensionList();
    }

    public void addDimension (int dimension) {

        final int i = this.dimensionList.indexOf(dimension);
        if (i < 0) {
            this.dimensionList.add(dimension);
        }
    }

    public void cleanDimensionList () {

        final List<Integer> dimensionListCopy = new ArrayList<>(this.dimensionList);
        this.dimensionList.clear();
        for (final int dimension : dimensionListCopy) {
            this.addDimension(dimension);
        }
    }

    // Dimension List
    public void InitDimensionList () {

        this.dimensionList.clear();
        this.worldConfiguration.get(Reference.catWorld, "dimensionList", Utils.integerListToIntArray(this.dimensionList));
        this.addDimension(0);
        this.cleanDimensionList();
    }

    public void saveWorldConfig () {

        this.worldConfiguration.save();
    }

}
