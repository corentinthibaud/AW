package net.shadowmage.ancientwarfare.npc.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIDoor;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIFleeHostiles;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIFollowPlayer;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIMoveHome;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIWander;
import net.shadowmage.ancientwarfare.npc.ai.owned.NpcAIPlayerOwnedAlarmResponse;
import net.shadowmage.ancientwarfare.npc.ai.owned.NpcAIPlayerOwnedFollowCommand;
import net.shadowmage.ancientwarfare.npc.ai.owned.NpcAIPlayerOwnedGetFood;
import net.shadowmage.ancientwarfare.npc.ai.owned.NpcAIPlayerOwnedIdleWhenHungry;
import net.shadowmage.ancientwarfare.npc.ai.owned.NpcAIPlayerOwnedRideHorse;
import net.shadowmage.ancientwarfare.npc.ai.owned.NpcAIPlayerOwnedTrader;
import net.shadowmage.ancientwarfare.npc.item.ItemCommandBaton;
import net.shadowmage.ancientwarfare.npc.item.ItemTradeOrder;
import net.shadowmage.ancientwarfare.npc.orders.TradeOrder;
import net.shadowmage.ancientwarfare.npc.trade.POTradeList;

public class NpcTrader extends NpcPlayerOwned {

	private EntityPlayer trader;//used by guis/containers to prevent further interaction
	private POTradeList tradeList = new POTradeList();
	private NpcAIPlayerOwnedTrader tradeAI;

	public NpcTrader(World par1World) {
		super(par1World);

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(0, new EntityAIRestrictOpenDoor(this));
		this.tasks.addTask(0, new NpcAIDoor(this, true));
		this.tasks.addTask(0, (horseAI = new NpcAIPlayerOwnedRideHorse(this)));
		this.tasks.addTask(2, new NpcAIFollowPlayer(this));
		this.tasks.addTask(2, new NpcAIPlayerOwnedFollowCommand(this));
		this.tasks.addTask(3, new NpcAIFleeHostiles(this));
		this.tasks.addTask(3, new NpcAIPlayerOwnedAlarmResponse(this));
		this.tasks.addTask(4, tradeAI = new NpcAIPlayerOwnedTrader(this));
		this.tasks.addTask(5, new NpcAIPlayerOwnedGetFood(this));
		this.tasks.addTask(6, new NpcAIPlayerOwnedIdleWhenHungry(this));
		this.tasks.addTask(7, new NpcAIMoveHome(this, 50F, 3F, 30F, 3F));

		//post-100 -- used by delayed shared tasks (look at random stuff, wander)
		this.tasks.addTask(101, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
		this.tasks.addTask(102, new NpcAIWander(this));
		this.tasks.addTask(103, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
	}

	@Override
	public boolean isValidOrdersStack(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof ItemTradeOrder;
	}

	@Override
	public void onOrdersInventoryChanged() {
		tradeList = null;
		if (isValidOrdersStack(ordersStack)) {
			tradeList = TradeOrder.getTradeOrder(ordersStack).getTradeList();
		}
		tradeAI.onOrdersUpdated();
	}

	@Override
	public String getNpcSubType() {
		return "";
	}

	@Override
	public String getNpcType() {
		return "trader";
	}

	@Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand) {
		boolean baton = !player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof ItemCommandBaton;
		if (baton) {
			return false;
		}
		if (player.world.isRemote) {
			return true;
		}
		if (isOwner(player))//owner
		{
			return tryCommand(player);
		} else//non-owner
		{
			if (trader != null && !trader.isEntityAlive()) {
				closeTrade();
			}
			if (getFoodRemaining() > 0 && trader == null) {
				startTrade(player);
				openAltGui(player);
			}
		}
		return true;
	}

	public void startTrade(EntityPlayer player) {
		trader = player;
	}

	public void closeTrade() {
		trader = null;
	}

	@Override
	public void openAltGui(EntityPlayer player) {
		NetworkHandler.INSTANCE.openGui(player, NetworkHandler.GUI_NPC_PLAYER_OWNED_TRADE, getEntityId(), 0, 0);
	}

	@Override
	public boolean hasAltGui() {
		return true;
	}

	@Override
	public boolean shouldBeAtHome() {
		return (world.provider.hasSkyLight() && !world.isDaytime()) || world.isRainingAt(getPosition());
	}

	@Override
	public boolean isHostileTowards(Entity e) {
		return false;
	}

	public POTradeList getTradeList() {
		return tradeList;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setTag("tradeAI", tradeAI.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		tradeAI.readFromNBT(tag.getCompoundTag("tradeAI"));
	}

}
