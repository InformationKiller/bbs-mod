package mchorse.bbs_mod.ui.forms.editors.panels;

import mchorse.bbs_mod.forms.forms.BlockForm;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.forms.UIForm;
import mchorse.bbs_mod.ui.forms.editors.panels.widgets.UIBlockStateEditor;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIButton;
import mchorse.bbs_mod.ui.framework.elements.input.UIColor;
import mchorse.bbs_mod.ui.utils.context.ItemStackContextAction;
import mchorse.bbs_mod.utils.colors.Color;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class UIBlockFormPanel extends UIFormPanel<BlockForm>
{
    public UIColor color;
    public UIButton copyFromInventory;
    public UIBlockStateEditor stateEditor;

    public UIBlockFormPanel(UIForm editor)
    {
        super(editor);

        this.color = new UIColor((c) -> this.form.color.set(Color.rgba(c))).withAlpha();
        this.copyFromInventory = new UIButton(UIKeys.ITEM_STACK_CONTEXT_HOTBAR, this::copyFormInventory);
        this.stateEditor = new UIBlockStateEditor((blockState) -> this.form.blockState.set(blockState));

        this.options.add(this.color, this.copyFromInventory, this.stateEditor);
    }

    @Override
    public void startEdit(BlockForm form)
    {
        super.startEdit(form);

        BlockState blockState = this.form.blockState.get();

        this.color.setColor(form.color.get().getARGBColor());
        this.stateEditor.setBlockState(blockState);
    }

    public void copyFormInventory(UIButton btn)
    {
        this.getContext().replaceContextMenu((newMenu) ->
        {
            PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();

            /* First nine slots are hotbar items */
            for (int i = 0; i < 9; i++)
            {
                ItemStack s = inventory.getStack(i);

                if (s.getItem() instanceof BlockItem blockItem)
                {
                    newMenu.action(new ItemStackContextAction(s, IKey.constant(s.getName().getString()), () ->
                    {
                        BlockState state = blockItem.getBlock().getDefaultState();
                        this.stateEditor.setBlockState(state);
                        this.form.blockState.set(state);
                    }));
                }
            }
        });
    }
}