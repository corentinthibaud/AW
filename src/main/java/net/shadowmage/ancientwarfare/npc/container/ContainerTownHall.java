package net.shadowmage.ancientwarfare.npc.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfare.core.container.ContainerTileBase;
import net.shadowmage.ancientwarfare.npc.tile.TileTownHall;
import net.shadowmage.ancientwarfare.npc.tile.TileTownHall.NpcDeathEntry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ContainerTownHall extends ContainerTileBase<TileTownHall> {

	List<NpcDeathEntry> deathList = new ArrayList<>();

	public ContainerTownHall(EntityPlayer player, int x, int y, int z) {
		super(player, x, y, z);
		int xPos, yPos;
		IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		for (int i = 0; i < handler.getSlots(); i++) {
			xPos = (i % 9) * 18 + 8;
			yPos = (i / 9) * 18 + 8 + 16;
			addSlotToContainer(new SlotItemHandler(handler, i, xPos, yPos));
		}
		addPlayerSlots(8 + 3 * 18 + 8 + 16);
		if (!player.world.isRemote) {
			deathList.addAll(tileEntity.getDeathList());
			tileEntity.addViewer(this);
		}
	}

	@Override
	public void handlePacketData(NBTTagCompound tag) {
		if (tag.hasKey("deathList")) {
			deathList.clear();
			NBTTagList list = tag.getTagList("deathList", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				deathList.add(new NpcDeathEntry(list.getCompoundTagAt(i)));
			}
			refreshGui();
		} else if (tag.hasKey("clear")) {
			tileEntity.clearDeathNotices();
		}

		if (tag.hasKey("range")) {
			tileEntity.setRange(tag.getInteger("range"));
			refreshGui();
		}

		if (tag.hasKey("name")) {
			tileEntity.name = tag.getString("name");
			refreshGui();
		}

		if (!tileEntity.getWorld().isRemote) {
			tileEntity.markDirty();
		}
	}

	@Override
	public void sendInitData() {
		sendTownHallDataToClient(false);
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		super.onContainerClosed(par1EntityPlayer);
		tileEntity.removeViewer(this);
	}

	public void onTownHallDeathListUpdated() {
		this.deathList.clear();
		this.deathList.addAll(tileEntity.getDeathList());
		sendTownHallDataToClient(true);
	}

	public void setRange(int value) {
		tileEntity.setRange(value);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("range", value);
		sendDataToServer(tag);
	}

	public void setName(String name) {
		tileEntity.name = name;
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("name", name);
		sendDataToServer(tag);
	}

	public void teleportPlayer(String playerName) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("playerName", playerName);
		sendDataToServer(tag);
	}

	public void clearList() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setBoolean("clear", true);
		sendDataToServer(tag);
	}

	private void sendTownHallDataToClient(boolean onlyDeathList) {
		NBTTagList list = new NBTTagList();
		for (NpcDeathEntry entry : deathList) {
			list.appendTag(entry.writeToNBT(new NBTTagCompound()));
		}
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag("deathList", list);
		if (!onlyDeathList) {
			tag.setInteger("range", tileEntity.getRange());
			tag.setString("name", tileEntity.name);
		}
		sendDataToClient(tag);
	}

	public List<NpcDeathEntry> getDeathList() {
		return deathList;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotClickedIndex) {
		ItemStack slotStackCopy = ItemStack.EMPTY;
		Slot theSlot = this.getSlot(slotClickedIndex);
		if (theSlot.getHasStack()) {
			ItemStack slotStack = theSlot.getStack();
			slotStackCopy = slotStack.copy();
			IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			if (slotClickedIndex < handler.getSlots())//book slot
			{
				if (!this.mergeItemStack(slotStack, handler.getSlots(), handler.getSlots() + playerSlots, false))//merge into player inventory
				{
					return ItemStack.EMPTY;
				}
			} else {
				if (!this.mergeItemStack(slotStack, 0, handler.getSlots(), false))//merge into player inventory
				{
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

}
