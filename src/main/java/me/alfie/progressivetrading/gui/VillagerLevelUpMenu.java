package me.alfie.progressivetrading.gui;

import me.alfie.progressivetrading.datapack.codec.ItemCost;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class VillagerLevelUpMenu extends AbstractContainerMenu {

    private final Level level;
    private final Container container = new SimpleContainer(1);

    public static final int COST_SLOT_X = 156;
    public static final int COST_SLOT_Y = 51;

    private final Merchant trader;

    //Server
    public VillagerLevelUpMenu(int containerId, Inventory playerInventory, @Nullable Level level, @Nullable Merchant trader) {
        super(ModMenus.VILLAGER_LEVEL_UP_MENU.get(), containerId);
        this.level = level;
        this.trader = trader;

        this.container.startOpen(playerInventory.player);
        this.addSlot(new Slot(container, 0, COST_SLOT_X, COST_SLOT_Y));

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 108 + j * 18, 84 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 108 + k * 18, 142));
        }
    }

    //Client
    public VillagerLevelUpMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null, null);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (level == null) return;

        trader.setTradingPlayer(null);

        for (int i = 0; i < this.container.getContainerSize(); i++) {
            ItemStack stack = this.container.removeItemNoUpdate(i);
            if (!stack.isEmpty()) player.getInventory().placeItemBackInInventory(stack);
        }

    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        if (level == null) return ItemStack.EMPTY;
        Slot slot = getSlot(i);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack originalStack = stackInSlot.copy();


        if (i == 0) {
            if (!this.moveItemStackTo(stackInSlot, 1, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) { return ItemStack.EMPTY;}
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        slot.onTake(player, stackInSlot);
        return originalStack;
    }



    @Override
    public boolean stillValid(Player player) {
        return this.trader.getTradingPlayer() == player;
    }

    public ItemStack getItemInSlot() {
        return getSlot(0).getItem();
    }

    /**
     *
     * @param validCost The valid cost
     * @param slotStack The stack in the slot
     * @return
     */
    public boolean tryConsume(ItemCost validCost, ItemStack slotStack) {
        if (!validCost.anyMatch(slotStack)) return false;
        if (slotStack.getCount() < validCost.count()) return false;

        slotStack.shrink(validCost.count());
        return true;
    }
}
