/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.fluidity.base.synch;

import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.impl.Fluidity;

@Experimental
public final class DiscreteStorageUpdateS2C {
	private DiscreteStorageUpdateS2C() { }

	public static FriendlyByteBuf begin(int count) {
		final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeInt(count);
		return buf;
	}

	public static FriendlyByteBuf append(FriendlyByteBuf buf, Article article, long count, int handle) {
		article.toPacket(buf);
		buf.writeVarLong(count);
		buf.writeVarInt(handle);
		return buf;
	}

	public static void sendFullRefresh(ServerPlayer player, FriendlyByteBuf buf, long capacity) {
		buf.writeVarLong(capacity);
		send(ID_FULL_REFRESH, player, buf);
	}

	public static void sendUpdateWithCapacity(ServerPlayer player, FriendlyByteBuf buf, long capacity) {
		buf.writeVarLong(capacity);
		send(ID_UPDATE_WITH_CAPACITY, player, buf);
	}

	public static void sendUpdate(ServerPlayer player, FriendlyByteBuf buf) {
		send(ID_UPDATE, player, buf);
	}

	private static void send(ResourceLocation id, ServerPlayer player, FriendlyByteBuf buf) {
		final Packet<?> packet = ServerPlayNetworking.createS2CPacket(id, buf);
		player.connection.send(packet);
	}

	public static ResourceLocation ID_FULL_REFRESH = new ResourceLocation(Fluidity.MOD_ID, "dfrs2c");
	public static ResourceLocation ID_UPDATE = new ResourceLocation(Fluidity.MOD_ID, "duds2c");
	public static ResourceLocation ID_UPDATE_WITH_CAPACITY = new ResourceLocation(Fluidity.MOD_ID, "ducs2c");
}
