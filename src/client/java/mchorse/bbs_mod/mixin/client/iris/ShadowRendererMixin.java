package mchorse.bbs_mod.mixin.client.iris;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.client.BBSRendering;
import net.irisshaders.iris.shadows.ShadowRenderer;

@Mixin(ShadowRenderer.class)
public class ShadowRendererMixin
{
    @ModifyVariable(method = "createShadowModelView", at = @At("HEAD"), ordinal = 0)
    private static float modifySunPathRotation(float original)
    {
        return BBSSettings.shaderCurvesEnabled.get() ? BBSRendering.getSunPathRotation() : original;
    }
}
