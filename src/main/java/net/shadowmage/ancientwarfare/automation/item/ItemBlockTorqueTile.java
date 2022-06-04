package net.shadowmage.ancientwarfare.automation.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueBase;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableBlock;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import net.shadowmage.ancientwarfare.core.owner.IOwnable;

public class ItemBlockTorqueTile extends ItemBlockBase {
	private IRotatableBlock rotatable;

	public ItemBlockTorqueTile(Block block) {
		super(block);
		if (!(block instanceof IRotatableBlock)) {
			throw new IllegalArgumentException("Must be a rotatable block!!");
		}
		rotatable = (IRotatableBlock) block;
		NonNullList<ItemStack> subBlocks = NonNullList.create();
		block.getSubBlocks(block.getCreativeTabToDisplayOn(), subBlocks);
		setHasSubtypes(subBlocks.size() > 1);
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		boolean val = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
		if (val) {
			TileTorqueBase te = (TileTorqueBase) player.world.getTileEntity(pos);
			if (te instanceof IOwnable) {
				((IOwnable) te).setOwner(player);
			}
			//noinspection ConstantConditions
			te.setPrimaryFacing(BlockRotationHandler.getFaceForPlacement(player, rotatable, side));
		}
		return val;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + stack.getItemDamage();
	}
}
