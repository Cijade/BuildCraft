/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.core.lib.network.PacketCoordinates;
import buildcraft.core.network.PacketIds;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.render.tile.PipeRendererPower;

import io.netty.buffer.ByteBuf;

public class PacketPowerUpdate extends PacketCoordinates {

    public boolean overload;
    public short[] displayPower;
    public byte[] displayFlow;

    public PacketPowerUpdate() {}

    public PacketPowerUpdate(TileEntity tile) {
        super(PacketIds.PIPE_POWER, tile.getWorld().provider.getDimensionId(), tile.getPos());
    }

    @Override
    public void readData(ByteBuf data, World world, EntityPlayer player) {
        super.readData(data, world, player);
        displayPower = new short[6];
        displayFlow = new byte[6];
        overload = data.readBoolean();
        for (int i = 0; i < displayPower.length; i++) {
            displayPower[i] = data.readUnsignedByte();
            displayFlow[i] = data.readByte();
        }
    }

    @Override
    public void writeData(ByteBuf data, World world, EntityPlayer player) {
        super.writeData(data, world, player);
        data.writeBoolean(overload);
        for (int i = 0; i < displayPower.length; i++) {
            data.writeByte(Math.min(PipeRendererPower.POWER_STAGES, (int) Math.ceil(displayPower[i] * PipeRendererPower.DISPLAY_MULTIPLIER)));
            data.writeByte(displayFlow[i]);
        }
    }

    @Override
    public void applyData(World world) {
        if (world.isAirBlock(pos)) {
            return;
        }

        TileEntity entity = world.getTileEntity(pos);
        if (!(entity instanceof TileGenericPipe)) {
            return;
        }

        TileGenericPipe pipe = (TileGenericPipe) entity;
        if (pipe.pipe == null) {
            return;
        }

        if (!(pipe.pipe.transport instanceof PipeTransportPower)) {
            return;
        }

        ((PipeTransportPower) pipe.pipe.transport).handlePowerPacket(this);

    }
}
