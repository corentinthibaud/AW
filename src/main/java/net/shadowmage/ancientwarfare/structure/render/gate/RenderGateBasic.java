/*
 Copyright 2012 John Cummens (aka Shadowmage, Shadowmage4513)
 This software is distributed under the terms of the GNU General Public License.
 Please see COPYING for precise license information.

 This file is part of Ancient Warfare.

 Ancient Warfare is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Ancient Warfare is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Ancient Warfare.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.shadowmage.ancientwarfare.structure.render.gate;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.shadowmage.ancientwarfare.structure.entity.EntityGate;
import net.shadowmage.ancientwarfare.structure.model.ModelGateBasic;

public class RenderGateBasic extends Render {

	protected final ModelGateBasic model = new ModelGateBasic();

	public RenderGateBasic(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public final void doRender(Entity entity, double d0, double d1, double d2, float f, float f1) {
		GlStateManager.pushMatrix();
		EntityGate g = (EntityGate) entity;
		BlockPos min = getMin(g);
		BlockPos max = getMax(g);

		boolean wideOnXAxis = min.getX() != max.getX();
		float width;
		float height = max.getY() - min.getY() + 1;
		float xOffset = 0f;
		float zOffset = 0;
		float tx = 0;
		float ty = -1;
		float tz = 0;
		float axisRotation = 0;
		if (wideOnXAxis) {
			width = max.getX() - min.getX() + 1;
			xOffset = width * 0.5f - 0.5f;
			tx = 1;
			axisRotation = 90;
		} else {
			tz = 1;
			width = max.getZ() - min.getZ() + 1;
			zOffset = -width * 0.5f + 0.5f;
		}
		GlStateManager.translate(-xOffset, 0, zOffset);
		for (int y = 0; y < height; y++) {
			GlStateManager.pushMatrix();
			for (int x = 0; x < width; x++) {
				model.setModelRotation(axisRotation);
				if (y == height - 1 && x > 0 && x < width - 1) {
					model.renderTop();
				} else if (y == height - 1 && x == 0) {
					model.renderCorner();
				} else if (y == height - 1 && x == width - 1) {
					model.setModelRotation(axisRotation + 180);
					model.renderCorner();
				} else if (x == 0) {
					model.renderSide();
				} else if (x == width - 1) {
					model.setModelRotation(axisRotation + 180);
					model.renderSide();
				}
				postRender(g, x, width, y, height, wideOnXAxis, axisRotation, f1);
				GlStateManager.translate(tx, 0, tz);
			}
			GlStateManager.popMatrix();
			GlStateManager.translate(0, ty, 0);
		}
		GlStateManager.popMatrix();
	}

	protected BlockPos getMin(EntityGate gate) {
		return gate.pos1;
	}

	protected BlockPos getMax(EntityGate gate) {
		return gate.pos2;
	}

	protected void postRender(EntityGate gate, int x, float width, int y, float height, boolean wideOnXAxis, float axisRotation, float frame) {
		if (y + gate.edgePosition <= height - 0.475f) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, -gate.edgePosition - gate.openingSpeed * (1 - frame), 0);
			model.setModelRotation(axisRotation);
			if (gate.getGateType().getModelType() == 0) {
				model.renderSolidWall();
			} else {
				model.renderBars();
			}
			GlStateManager.popMatrix();
		}
	}

	@Override
	protected final ResourceLocation getEntityTexture(Entity entity) {
		return ((EntityGate) entity).getGateType().getTexture();
	}

}
