package me.alfie.progressivetrading.mixin;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface AddWidgetAccessor<T extends AbstractContainerMenu> {

    @Invoker("addRenderableWidget")
    <W extends GuiEventListener & Renderable & NarratableEntry>
    W progressivetrading$addRenderableWidget(W widget);
}
