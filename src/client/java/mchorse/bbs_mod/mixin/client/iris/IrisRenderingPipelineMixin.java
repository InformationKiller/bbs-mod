package mchorse.bbs_mod.mixin.client.iris;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.client.BBSRendering;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;

@Mixin(IrisRenderingPipeline.class)
public class IrisRenderingPipelineMixin
{
    @Shadow
    @Final
    private float sunPathRotation;

    @Overwrite
    public float getSunPathRotation()
    {
        return BBSSettings.shaderCurvesEnabled.get() ? BBSRendering.getSunPathRotation() : this.sunPathRotation;
    }
}
