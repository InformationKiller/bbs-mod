package mchorse.bbs_mod.mixin.client.iris;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import mchorse.bbs_mod.client.BBSRendering;
import net.irisshaders.iris.pathways.CenterDepthSampler;

@Mixin(CenterDepthSampler.class)
public class CenterDepthSamplerMixin {

    @Overwrite
    public int getCenterDepthTexture() {
        return BBSRendering.getCenterDepthTexture().orElse(Integer.valueOf(this.altTexture)).intValue();
    }

    @Shadow
    @Final
    private int altTexture;
}
