/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumPowerStage;

import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.ModelItemSimple;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.minecraft.ExpressionCompat;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import buildcraft.lib.misc.data.ModelVariableData;

import buildcraft.core.client.render.RenderEngineCreative;
import buildcraft.core.client.render.RenderEngineWood;
import buildcraft.core.client.render.RenderMarkerVolume;
import buildcraft.core.tile.TileEngineCreative;
import buildcraft.core.tile.TileEngineRedstone_BC8;
import buildcraft.core.tile.TileMarkerVolume;

public class BCCoreModels {
    private static final NodeVariableDouble ENGINE_PROGRESS;
    private static final NodeVariableObject<EnumPowerStage> ENGINE_STAGE;
    private static final NodeVariableObject<EnumFacing> ENGINE_FACING;

    private static final ModelHolderVariable ENGINE_REDSTONE;
    private static final ModelHolderVariable ENGINE_CREATIVE;

    static {
        FunctionContext fnCtx = new FunctionContext(ExpressionCompat.ENUM_POWER_STAGE, DefaultContexts.createWithAll());
        ENGINE_PROGRESS = fnCtx.putVariableDouble("progress");
        ENGINE_STAGE = fnCtx.putVariableObject("stage", EnumPowerStage.class);
        ENGINE_FACING = fnCtx.putVariableObject("direction", EnumFacing.class);

        ENGINE_REDSTONE = new ModelHolderVariable(
            "buildcraftcore:models/block/engine_redstone.json",
            fnCtx
        );
        ENGINE_CREATIVE = new ModelHolderVariable(
            "buildcraftcore:models/block/engine_creative.json",
            fnCtx
        );
    }

    public static void fmlPreInit() {
        MinecraftForge.EVENT_BUS.register(BCCoreModels.class);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelRegistry(ModelRegistryEvent event) {
        if (BCCoreBlocks.engine != null) {
            ModelLoader.setCustomStateMapper(BCCoreBlocks.engine, b -> Collections.emptyMap());
        }
    }

    public static void fmlInit() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileMarkerVolume.class, RenderMarkerVolume.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEngineRedstone_BC8.class, RenderEngineWood.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEngineCreative.class, RenderEngineCreative.INSTANCE);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        ENGINE_PROGRESS.value = 0.2;
        ENGINE_STAGE.value = EnumPowerStage.BLUE;
        ENGINE_FACING.value = EnumFacing.UP;
        ModelVariableData varData = new ModelVariableData();
        varData.setNodes(ENGINE_REDSTONE.createTickableNodes());
        varData.tick();
        varData.refresh();
        event.getModelRegistry().putObject(
            new ModelResourceLocation(EnumEngineType.WOOD.getItemModelLocation(), "inventory"),
            new ModelItemSimple(
                Arrays.stream(ENGINE_REDSTONE.getCutoutQuads())
                    .map(MutableQuad::toBakedItem)
                    .collect(Collectors.toList()),
                ModelItemSimple.TRANSFORM_BLOCK,
                true
            )
        );
        ENGINE_STAGE.value = EnumPowerStage.BLACK;
        varData.setNodes(ENGINE_CREATIVE.createTickableNodes());
        varData.tick();
        varData.refresh();
        event.getModelRegistry().putObject(
            new ModelResourceLocation(EnumEngineType.CREATIVE.getItemModelLocation(), "inventory"),
            new ModelItemSimple(
                Arrays.stream(ENGINE_CREATIVE.getCutoutQuads())
                    .map(MutableQuad::toBakedItem)
                    .collect(Collectors.toList()),
                ModelItemSimple.TRANSFORM_BLOCK,
                true
            )
        );
    }

    private static MutableQuad[] getEngineQuads(ModelHolderVariable model,
                                                TileEngineBase_BC8 tile,
                                                float partialTicks) {
        ENGINE_PROGRESS.value = tile.getProgressClient(partialTicks);
        ENGINE_STAGE.value = tile.getPowerStage();
        ENGINE_FACING.value = tile.getCurrentFacing();
        if (tile.clientModelData.hasNoNodes()) {
            tile.clientModelData.setNodes(model.createTickableNodes());
        }
        tile.clientModelData.refresh();
        return model.getCutoutQuads();
    }

    public static MutableQuad[] getRedstoneEngineQuads(TileEngineRedstone_BC8 tile, float partialTicks) {
        return getEngineQuads(ENGINE_REDSTONE, tile, partialTicks);
    }

    public static MutableQuad[] getCreativeEngineQuads(TileEngineCreative tile, float partialTicks) {
        return getEngineQuads(ENGINE_CREATIVE, tile, partialTicks);
    }
}
