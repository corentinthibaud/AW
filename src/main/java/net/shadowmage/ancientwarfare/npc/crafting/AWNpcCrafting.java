package net.shadowmage.ancientwarfare.npc.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.init.AWNPCItems;

@Mod.EventBusSubscriber(modid = AncientWarfareNPC.MOD_ID)
public class AWNpcCrafting {

	@SubscribeEvent
	public static void register(RegistryEvent.Register<IRecipe> event) {
		IForgeRegistry<IRecipe> registry = event.getRegistry();
		registry.register(new OrderCopyingRecipe("upkeep_order_copy", AWNPCItems.UPKEEP_ORDER));
		registry.register(new OrderCopyingRecipe("routing_order_copy", AWNPCItems.ROUTING_ORDER));
		registry.register(new OrderCopyingRecipe("combat_order_copy", AWNPCItems.COMBAT_ORDER));
		registry.register(new OrderCopyingRecipe("work_order_copy", AWNPCItems.WORK_ORDER));
	}

	private static class OrderCopyingRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
		private final Item item;

		private OrderCopyingRecipe(String name, Item item) {
			setRegistryName(new ResourceLocation(AncientWarfareNPC.MOD_ID, name));
			this.item = item;
		}

		@Override
		public boolean matches(InventoryCrafting var1, World var2) {
			ItemStack order1 = ItemStack.EMPTY;
			ItemStack order2 = ItemStack.EMPTY;
			boolean foundOtherStuff = false;
			ItemStack stack;
			for (int i = 0; i < var1.getSizeInventory(); i++) {
				stack = var1.getStackInSlot(i);
				if (stack.isEmpty()) {
					continue;
				}
				if (stack.getItem() == item) {
					if (order1.isEmpty()) {
						order1 = stack;
					} else if (order2.isEmpty()) {
						order2 = stack;
					} else {
						foundOtherStuff = true;
						break;
					}
				} else {
					foundOtherStuff = true;
					break;
				}
			}
			return !foundOtherStuff && !order1.isEmpty() && !order2.isEmpty();
		}

		@Override
		public ItemStack getCraftingResult(InventoryCrafting var1) {
			ItemStack order1 = ItemStack.EMPTY;
			ItemStack order2 = ItemStack.EMPTY;
			boolean foundOtherStuff = false;
			ItemStack stack;
			for (int i = 0; i < var1.getSizeInventory(); i++) {
				stack = var1.getStackInSlot(i);
				if (stack.isEmpty()) {
					continue;
				}
				if (stack.getItem() == item) {
					if (order1.isEmpty()) {
						order1 = stack;
					} else if (order2.isEmpty()) {
						order2 = stack;
					} else {
						foundOtherStuff = true;
						break;
					}
				} else {
					foundOtherStuff = true;
					break;
				}
			}
			if (foundOtherStuff || order1.isEmpty() || order2.isEmpty()) {
				return ItemStack.EMPTY;
			}
			ItemStack retStack = order2.copy();
			if (order1.getTagCompound() != null) {
				retStack.setTagCompound(order1.getTagCompound().copy());
			} else {
				retStack.setTagCompound(null);
			}
			retStack.setCount(2);
			return retStack;
		}

		@Override
		public boolean canFit(int width, int height) {
			return width * height >= 2;
		}

		@Override
		public ItemStack getRecipeOutput() {
			return new ItemStack(item);
		}
	}
}
