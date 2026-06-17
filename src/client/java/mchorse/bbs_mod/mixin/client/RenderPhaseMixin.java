package mchorse.bbs_mod.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import mchorse.bbs_mod.client.BBSRendering;

@Environment(EnvType.CLIENT)
@Mixin(RenderPhase.class)
public abstract class RenderPhaseMixin
{
    @Redirect(method = "setupGlintTexturing", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J"))
    private static long redirectGlintTime()
    {
        if (BBSRendering.measuringTimeOverride)
        {
            return BBSRendering.measuringTimeValue;
        }
        else
        {
            return Util.getMeasuringTimeMs();
        }
    }
}