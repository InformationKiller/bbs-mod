package mchorse.bbs_mod.mixin.client.iris;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.irisshaders.iris.targets.RenderTargets;

@Mixin(RenderTargets.class)
public interface RenderTargetsAccessor {

    @Accessor(value = "cachedDepthBufferVersion", remap = false)
    public void bbs$setCachedDepthBufferVersion(int version);
}
