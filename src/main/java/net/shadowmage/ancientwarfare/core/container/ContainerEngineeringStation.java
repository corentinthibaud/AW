package net.shadowmage.ancientwarfare.core.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.shadowmage.ancientwarfare.core.crafting.AWCraftingManager;
import net.shadowmage.ancientwarfare.core.crafting.ICraftingRecipe;
import net.shadowmage.ancientwarfare.core.tile.TileEngineeringStation;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import javax.annotation.Nonnull;
import java.util.List;

import static net.minecraft.util.EnumActionResult.PASS;
import static net.minecraft.util.EnumActionResult.SUCCESS;

public class ContainerEngineeringStation extends ContainerTileBase<TileEngineeringStation> implements ICraftingContainer {
	private static final int BOOK_SLOT = 1;
	private static final int CRAFTING_SLOT = 0;
	public ContainerCraftingRecipeMemory containerCrafting;
	private int currentCraftTotalSize = 0;

	public ContainerEngineeringStation(EntityPlayer player, int x, int y, int z) {
		super(player, x, y, z);

		containerCrafting = new ContainerCraftingRecipeMemory(tileEntity.craftingRecipeMemory, player) {
			@Override
			protected OnTakeResult handleOnTake(EntityPlayer player, ItemStack stack) {
				ICraftingRecipe recipe = tileEntity.craftingRecipeMemory.getRecipe();
				NonNullList<ItemStack> reusableStacks = AWCraftingManager.getReusableStacks(recipe, containerCrafting.getCraftMatrix());
				NonNullList<ItemStack> resources = InventoryTools.removeItems(AWCraftingManager.getRecipeInventoryMatch(recipe, getCraftingStacks(),
						new CombinedInvWrapper(tileEntity.extraSlots, new ItemStackHandler(reusableStacks))), reusableStacks);
				if (!resources.isEmpty()) {
					InventoryTools.removeItems(tileEntity.extraSlots, resources);

					ForgeHooks.setCraftingPlayer(player);
					NonNullList<ItemStack> remainingItems = InventoryTools.removeItems(
							tileEntity.craftingRecipeMemory.getRemainingItems(AWCraftingManager.fillCraftingMatrixFromInventory(resources)),
							reusableStacks);
					ForgeHooks.setCraftingPlayer(null);
					InventoryTools.insertOrDropItems(tileEntity.extraSlots, remainingItems, tileEntity.getWorld(), tileEntity.getPos());

					return new OnTakeResult(SUCCESS, stack);
				}
				return new OnTakeResult(PASS, stack);
			}

			@Override
			protected boolean canTakeStackFromOutput(EntityPlayer player) {
				return true;
			}
		};
		for (Slot slot : containerCrafting.getSlots()) {
			addSlotToContainer(slot);
		}

		Slot slot;
		for (int y1 = 0; y1 < 2; y1++) {
			int y2 = y1 * 18 + 8 + 3 * 18 + 4;
			for (int x1 = 0; x1 < 9; x1++) {
				int x2 = x1 * 18 + 8;
				int slotNum = y1 * 9 + x1;
				slot = new SlotItemHandler(tileEntity.extraSlots, slotNum, x2, y2);
				addSlotToContainer(slot);
			}
		}

		int y1 = 8 + 3 * 18 + 8 + 2 * 18 + 4;
		y1 = this.addPlayerSlots(y1);
	}

	@Override
	public void handlePacketData(NBTTagCompound tag) {
		if (tag.hasKey("recipe")) {
			containerCrafting.handleRecipeUpdate(tag);
		}
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		ItemStack result = super.slotClick(slotId, dragType, clickTypeIn, player);
		currentCraftTotalSize = 0;
		return result;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setAll(List<ItemStack> p_190896_1_) {
		containerCrafting.setOpening(true);
		super.setAll(p_190896_1_);
		containerCrafting.setOpening(false);
	}

	@Override
	public void putStackInSlot(int slotID, ItemStack stack) {
		containerCrafting.setOpening(true);
		super.putStackInSlot(slotID, stack);
		containerCrafting.setOpening(false);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotClickedIndex) {
		if (slotClickedIndex == CRAFTING_SLOT && !updateAndCheckCraftStackOrLessInTotal()) {
			return ItemStack.EMPTY;
		}

		ItemStack slotStackCopy = ItemStack.EMPTY;
		Slot theSlot = this.getSlot(slotClickedIndex);
		if (theSlot != null && theSlot.getHasStack()) {
			ItemStack slotStack = theSlot.getStack();
			slotStackCopy = slotStack.copy();
			int craftSlotStart = 2;
			//TODO replace the reference to craftMatrix here with something like crafting size reference on subcontainer itself
			int storageSlotsStart = craftSlotStart + tileEntity.craftingRecipeMemory.craftMatrix.getSizeInventory();
			int playerSlotStart = storageSlotsStart + tileEntity.extraSlots.getSlots();
			int playerSlotEnd = playerSlotStart + playerSlots;
			if (slotClickedIndex < craftSlotStart)//book or result slot
			{
				if (!this.mergeItemStack(slotStack, playerSlotStart, playerSlotEnd, false))//merge into player inventory
				{ return ItemStack.EMPTY; }
			} else {
				if (slotClickedIndex < storageSlotsStart) {//craft matrix
					if (!this.mergeItemStack(slotStack, storageSlotsStart, playerSlotStart, false))//merge into storage
						return ItemStack.EMPTY;
				} else if (slotClickedIndex < playerSlotStart) {//storage slots
					if (!this.mergeItemStack(slotStack, playerSlotStart, playerSlotEnd, false))//merge into player inventory
						return ItemStack.EMPTY;
				} else if (slotClickedIndex < playerSlotEnd && !mergeItemStack(slotStack, BOOK_SLOT, BOOK_SLOT + 1, false)
						&& !this.mergeItemStack(slotStack, storageSlotsStart, playerSlotStart, false)) {
					return ItemStack.EMPTY;
				}
			}
			if (slotStack.getCount() == 0) {
				theSlot.putStack(ItemStack.EMPTY);
			} else {
				theSlot.onSlotChanged();
			}
			if (slotStack.getCount() == slotStackCopy.getCount()) {
				return ItemStack.EMPTY;
			}
			theSlot.onTake(par1EntityPlayer, slotStack);
		}
		return slotStackCopy;
	}

	private boolean updateAndCheckCraftStackOrLessInTotal() {
		ItemStack craftedStack = getSlot(CRAFTING_SLOT).getStack();
		currentCraftTotalSize += craftedStack.getCount();
		return currentCraftTotalSize <= craftedStack.getMaxStackSize();
	}
	

	@Override
	public ContainerCraftingRecipeMemory getCraftingMemoryContainer() {
		return containerCrafting;
	}

	@Override
	public IItemHandlerModifiable[] getInventories() {
		return new IItemHandlerModifiable[] {tileEntity.extraSlots, new PlayerInvWrapper(player.inventory)};
	}

	@Override
	public boolean pushCraftingMatrixToInventories() {
		IItemHandler craftMatrixWrapper = new InvWrapper(tileEntity.craftingRecipeMemory.craftMatrix);
		NonNullList<ItemStack> craftingItems = InventoryTools.getItems(craftMatrixWrapper);

		IItemHandler inventories = new CombinedInvWrapper(tileEntity.extraSlots, new PlayerInvWrapper(player.inventory));

		if (InventoryTools.insertItems(inventories, craftingItems, true).isEmpty()) {
			InventoryTools.insertItems(inventories, craftingItems, false);
			InventoryTools.removeItems(craftMatrixWrapper, craftingItems);
			return true;
		}

		return false;
	}
}
