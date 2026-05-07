package mchorse.bbs_mod.mixin.client;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.camera.controller.CameraController;
import mchorse.bbs_mod.ui.dashboard.UIDashboard;
import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.utils.PlayerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin
{
    @Shadow protected abstract void setRotation(float yaw, float pitch);
    @Shadow protected abstract void setPos(double x, double y, double z);

    @Inject(method = "update", at = @At(value = "RETURN"))
    public void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci)
    {
        CameraController controller = BBSModClient.getCameraController();

        if (controller.getCurrent() != null)
        {
            Vector3d position = controller.getPosition();
            float yaw = controller.getYaw();
            float pitch = controller.getPitch();

            this.setPos(position.x, position.y, position.z);
            this.setRotation(yaw, pitch);

            boolean controlling = true;
            if (BBSModClient.getDashboardIfCreated() != null)
            {
                UIDashboard dashboard = BBSModClient.getDashboard();
                if (dashboard.getPanels().panel instanceof UIFilmPanel panel)
                {
                    controlling = panel.getController().isControlling();
                }
            }

            if (!thirdPerson && !controlling)
            {
                PlayerUtils.teleport(position.x, Math.max(position.y, -64), position.z, MinecraftClient.getInstance().player.getYaw(), MinecraftClient.getInstance().player.getPitch());
            }
        }
    }
}