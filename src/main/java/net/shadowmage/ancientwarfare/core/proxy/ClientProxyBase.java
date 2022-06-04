package net.shadowmage.ancientwarfare.core.proxy;

import com.google.common.collect.Sets;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

@SideOnly(Side.CLIENT)
public class ClientProxyBase extends CommonProxyBase {

	private Set<IClientRegister> clientRegisters = Sets.newHashSet();

	public ClientProxyBase() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event) {
		for (IClientRegister register : clientRegisters) {
			register.registerClient();
		}
	}

	@Override
	public void addClientRegister(IClientRegister register) {
		clientRegisters.add(register);
	}
}
