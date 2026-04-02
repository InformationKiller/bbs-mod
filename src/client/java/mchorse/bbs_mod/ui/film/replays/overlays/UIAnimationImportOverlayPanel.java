package mchorse.bbs_mod.ui.film.replays.overlays;

import mchorse.bbs_mod.cubic.ModelInstance;
import mchorse.bbs_mod.cubic.data.animation.Animation;
import mchorse.bbs_mod.forms.forms.ModelForm;
import mchorse.bbs_mod.forms.renderers.ModelFormRenderer;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.film.replays.overlays.UIAnimationToPoseOverlayPanel.IUIAnimationPoseCallback;
import mchorse.bbs_mod.ui.framework.elements.UIScrollView;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIToggle;
import mchorse.bbs_mod.ui.framework.elements.input.UITrackpad;
import mchorse.bbs_mod.ui.framework.elements.input.keyframes.UIKeyframeSheet;
import mchorse.bbs_mod.ui.framework.elements.input.list.UIStringList;
import mchorse.bbs_mod.ui.framework.elements.overlay.UIOverlayPanel;
import mchorse.bbs_mod.ui.utils.UI;

public class UIAnimationImportOverlayPanel extends UIOverlayPanel
{
    public UIStringList list;
    public UIButton generate;

    private final IUIAnimationImportCallback callback;
    private final ModelForm modelForm;

    public UIAnimationImportOverlayPanel(IUIAnimationImportCallback callback, ModelForm modelForm, UIKeyframeSheet sheet)
    {
        super(UIKeys.FILM_REPLAY_ANIMATION_TO_POSE_TITLE);

        this.callback = callback;
        this.modelForm = modelForm;

        ModelInstance model = ModelFormRenderer.getModel(modelForm);

        this.list = new UIStringList(null);
        this.list.h(UIStringList.DEFAULT_HEIGHT * 10);
        this.list.background();
        this.list.add(model.animations.animations.keySet());
        this.list.sort();
        this.list.setIndex(0);
        this.generate = new UIButton(UIKeys.FILM_REPLAY_ANIMATION_TO_POSE_GENERATE, (b) ->
        {
            this.callback.animationImport(
                this.list.getCurrentFirst()
            );

            this.close();
        });

        UIScrollView scroll = UI.scrollView(5, 6, this.list, this.generate);

        scroll.full(this.content);
        this.content.add(scroll);
    }

    public static interface IUIAnimationImportCallback
    {
        public void animationImport(String animationKey);
    }
}