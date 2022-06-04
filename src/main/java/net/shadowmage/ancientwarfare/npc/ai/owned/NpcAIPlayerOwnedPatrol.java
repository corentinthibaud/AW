package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.orders.CombatOrder;

public class NpcAIPlayerOwnedPatrol extends NpcAI<NpcBase> {
	private static final int MAX_TICKS_AT_POINT = 50;//default 2.5 second idle at each point

	private boolean init = false;
	private int patrolIndex;
	private boolean atPoint;
	private int ticksAtPoint;
	private CombatOrder orders;
	private ItemStack ordersStack;

	public NpcAIPlayerOwnedPatrol(NpcBase npc) {
		super(npc);
		setMutexBits(ATTACK + MOVE);
	}

	public void onOrdersInventoryChanged() {
		patrolIndex = 0;
		ordersStack = npc.ordersStack;
		orders = CombatOrder.getCombatOrder(ordersStack);
	}

	@Override
	public boolean shouldExecute() {
		if (!super.shouldExecute()) {
			return false;
		}
		if (!init) {
			init = true;
			ordersStack = npc.ordersStack;
			orders = CombatOrder.getCombatOrder(ordersStack);
			if (orders == null || patrolIndex >= orders.size()) {
				patrolIndex = 0;
			}
		}
		if (npc.getAttackTarget() != null) {
			return false;
		}
		return orders != null && !ordersStack.isEmpty() && orders.getPatrolDimension() == npc.world.provider.getDimension() && !orders.isEmpty();
	}

	@Override
	public void startExecuting() {
		npc.addAITask(TASK_PATROL);
	}

	@Override
	public void updateTask() {
		if (atPoint) {
			npc.removeAITask(TASK_MOVE);
			ticksAtPoint++;
			if (ticksAtPoint > MAX_TICKS_AT_POINT) {
				setMoveToNextPoint();
			}
		} else {
			BlockPos pos = orders.get(patrolIndex);
			double dist = npc.getDistanceSq(pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d);
			if (dist > 2.d * 2.d) {
				moveToPosition(pos, dist);
			} else {
				atPoint = true;
				ticksAtPoint = 0;
			}
		}
	}

	private void setMoveToNextPoint() {
		atPoint = false;
		ticksAtPoint = 0;
		patrolIndex++;
		moveRetryDelay = 0;
		if (patrolIndex >= orders.size()) {
			patrolIndex = 0;
		}
	}

	@Override
	public void resetTask() {
		ticksAtPoint = 0;
		moveRetryDelay = 0;
		npc.removeAITask(TASK_PATROL + TASK_MOVE);
	}

	public void readFromNBT(NBTTagCompound tag) {
		patrolIndex = tag.getInteger("patrolIndex");
		atPoint = tag.getBoolean("atPoint");
		ticksAtPoint = tag.getInteger("ticksAtPoint");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag.setInteger("patrolIndex", patrolIndex);
		tag.setBoolean("atPoint", atPoint);
		tag.setInteger("ticksAtPoint", ticksAtPoint);
		return tag;
	}

}
