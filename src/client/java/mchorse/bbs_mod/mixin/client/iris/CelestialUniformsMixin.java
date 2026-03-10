package mchorse.bbs_mod.mixin.client.iris;

import net.irisshaders.iris.uniforms.CelestialUniforms;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.client.BBSRendering;

@Mixin(CelestialUniforms.class)
public class CelestialUniformsMixin
{
    @Shadow
    @Final
    private float sunPathRotation;

    @Redirect(
        method = "getCelestialPositionInWorldSpace",
        at = @At(
            value = "FIELD",
            target = "Lnet/irisshaders/iris/uniforms/CelestialUniforms;sunPathRotation:F"
        )
    )
    private float redirectSunPathRotationWorld(CelestialUniforms instance)
    {
        return BBSSettings.shaderCurvesEnabled.get() ? BBSRendering.getSunPathRotation() : this.sunPathRotation;
    }

    @Redirect(
        method = "getCelestialPosition",
        at = @At(
            value = "FIELD",
            target = "Lnet/irisshaders/iris/uniforms/CelestialUniforms;sunPathRotation:F"
        )
    )
    private float redirectSunPathRotation(CelestialUniforms instance)
    {
        return BBSSettings.shaderCurvesEnabled.get() ? BBSRendering.getSunPathRotation() : this.sunPathRotation;
    }
}
