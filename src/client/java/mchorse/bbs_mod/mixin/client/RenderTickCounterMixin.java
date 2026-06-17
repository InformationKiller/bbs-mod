package mchorse.bbs_mod.mixin.client;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.BBSSettings;
import mchorse.bbs_mod.client.BBSRendering;
import mchorse.bbs_mod.utils.VideoRecorder;
import net.minecraft.client.render.RenderTickCounter;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.class)
public class RenderTickCounterMixin
{
    @Shadow
    public float tickDelta;

    @Shadow
    public float lastFrameDuration;

    @Shadow
    private long prevTimeMillis;

    @Shadow
    @Final
	private float tickTime;

    private int heldFrames;

    @Inject(method = "beginRenderTick", at = @At("HEAD"), cancellable = true)
    public void onBeginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> info)
    {
        VideoRecorder videoRecorder = BBSModClient.getVideoRecorder();

        if (videoRecorder.isRecording())
        {
            BBSRendering.canRender = false;

            if (this.heldFrames == 0)
            {
                this.lastFrameDuration = 1000F / (float) BBSRendering.getVideoFrameRate() / this.tickTime;
                this.prevTimeMillis = timeMillis;

                if (videoRecorder.getCounter() == 0)
                {
                    this.tickDelta = 0;
                    BBSRendering.measuringTimeOverride = true;
                    BBSRendering.measuringTimeValue = timeMillis;
                }
                else
                {
                    this.tickDelta += this.lastFrameDuration;
                    BBSRendering.measuringTimeValue += 1000L / BBSRendering.getVideoFrameRate();
                }

                int i = (int) this.tickDelta;

                this.tickDelta -= (float) i;

                videoRecorder.serverTicks += i;

                info.setReturnValue(i);
            }
            else
            {
                this.lastFrameDuration = 0F;

                info.setReturnValue(0);
            }

            this.heldFrames += 1;

            if (this.heldFrames >= BBSSettings.videoHeldFrames.get())
            {
                this.heldFrames = 0;
                BBSRendering.canRender = true;
            }
        }
        else
        {
            this.heldFrames = 0;
            BBSRendering.measuringTimeOverride = false;
        }
    }
}