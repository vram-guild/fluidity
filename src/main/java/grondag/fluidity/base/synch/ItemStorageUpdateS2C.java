/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.fluidity.base.synch;

import io.netty.buffer.Unpooled;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;

import grondag.fluidity.Fluidity;

@API(status = Status.EXPERIMENTAL)
public final class ItemStorageUpdateS2C {
	private ItemStorageUpdateS2C() {}

	public static PacketByteBuf begin(int count) {
		final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeInt(count);
		return buf;
	}

	public static PacketByteBuf append(PacketByteBuf buf, ItemStack stack, long count, int handle) {
		buf.writeItemStack(stack);
		buf.writeVarLong(count);
		buf.writeVarInt(handle);
		return buf;
	}

	public static void sendFullRefresh(ServerPlayerEntity player, PacketByteBuf buf, long capacity) {
		buf.writeVarLong(capacity);
		send(ID_FULL_REFRESH, player, buf);
	}

	public static void sendUpdateWithCapacity(ServerPlayerEntity player, PacketByteBuf buf, long capacity) {
		buf.writeVarLong(capacity);
		send(ID_UPDATE_WITH_CAPACITY, player, buf);
	}

	public static void sendUpdate(ServerPlayerEntity player, PacketByteBuf buf) {
		send(ID_UPDATE, player, buf);
	}

	private static void send(Identifier id, ServerPlayerEntity player, PacketByteBuf buf) {
		final Packet<?> packet = ServerSidePacketRegistry.INSTANCE.toPacket(id, buf);
		ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packet);
	}

	public static Identifier ID_FULL_REFRESH = new Identifier(Fluidity.MOD_ID, "ifrs2c");
	public static Identifier ID_UPDATE = new Identifier(Fluidity.MOD_ID, "iuds2c");
	public static Identifier ID_UPDATE_WITH_CAPACITY = new Identifier(Fluidity.MOD_ID, "iucs2c");
}
