package net.shadowmage.ancientwarfare.automation.tile.torque;

import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;

public final class TileConduitMedium extends TileTorqueSidedCell {

	@Override
	protected double getEfficiency() {
		return AWAutomationStatics.med_efficiency_factor;
	}

	@Override
	protected double getMaxTransfer() {
		return AWAutomationStatics.med_transfer_max;
	}

}
