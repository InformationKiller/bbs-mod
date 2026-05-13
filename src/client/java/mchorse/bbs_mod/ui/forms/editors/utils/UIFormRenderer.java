package mchorse.bbs_mod.ui.forms.editors.utils;

import com.mojang.blaze3d.systems.RenderSystem;

import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.forms.renderers.FormRenderType;
import mchorse.bbs_mod.forms.renderers.FormRenderingContext;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.ui.framework.elements.utils.UIModelRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.MatrixStack;

public class UIFormRenderer extends UIModelRenderer
{
    public Form form;

    @Override
    protected void renderUserModel(UIContext context)
    {
        if (this.form == null)
        {
            return;
        }

        FormRenderingContext formContext = new FormRenderingContext()
            .set(FormRenderType.PREVIEW, this.entity, context.batcher.getContext().getMatrices(), LightmapTextureManager.pack(15, 15), OverlayTexture.DEFAULT_UV, context.getTransition())
            .camera(this.camera)
            .modelRenderer();

        MatrixStack renderStack = RenderSystem.getModelViewStack();
        renderStack.push();
        renderStack.loadIdentity();
        RenderSystem.applyModelViewMatrix();

        FormUtilsClient.render(this.form, formContext);

        renderStack.pop();
        RenderSystem.applyModelViewMatrix();
    }
}