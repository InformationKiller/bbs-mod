package mchorse.bbs_mod.utils.pose;

import java.io.FileNotFoundException;
import java.io.InputStream;

import mchorse.bbs_mod.BBSMod;
import mchorse.bbs_mod.cubic.model.ModelManager;
import mchorse.bbs_mod.data.DataToString;
import mchorse.bbs_mod.data.types.MapType;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.utils.IOUtils;
import mchorse.bbs_mod.utils.presets.DataManager;

public class PoseManager extends DataManager
{
    public static final PoseManager INSTANCE = new PoseManager();

    @Override
    protected Link getFile(String group)
    {
        return Link.assets(ModelManager.MODELS_PREFIX + group + "/poses.json");
    }

    protected Link getConfigFile(String group)
    {
        return Link.assets(ModelManager.MODELS_PREFIX + group + "/config.json");
    }

    @Override
    public MapType getData(String group) {
        MapType map = super.getData(group);

        if (!map.has("default_sneaking"))
        {
            try (InputStream stream = BBSMod.getProvider().getAsset(getConfigFile(group)))
            {
                MapType configData = DataToString.mapFromString(IOUtils.readText(stream));
                map.elements.put("default_sneaking", configData.get("sneaking_pose"));
            }
            catch (FileNotFoundException e)
            {}
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return map;
    }
}