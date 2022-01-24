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

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.impl.Fluidity;

@Experimental
public final class BulkStorageUpdateS2C {
	private BulkStorageUpdateS2C() { }

	public static FriendlyByteBuf begin(int count) {
		final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeInt(count);
		return buf;
	}

	public static FriendlyByteBuf append(FriendlyByteBuf buf, Article article, Fraction amount, int handle) {
		article.toPacket(buf);
		amount.writeBuffer(buf);
		buf.writeVarInt(handle);
		return buf;
	}

	public static void sendFullRefresh(ServerPlayer player, FriendlyByteBuf buf, Fraction capacity) {
		capacity.writeBuffer(buf);
		send(ID_FULL_REFRESH, player, buf);
	}

	public static void sendUpdateWithCapacity(ServerPlayer player, FriendlyByteBuf buf, Fraction capacity) {
		capacity.writeBuffer(buf);
		send(ID_UPDATE_WITH_CAPACITY, player, buf);
	}

	public static void sendUpdate(ServerPlayer player, FriendlyByteBuf buf) {
		send(ID_UPDATE, player, buf);
	}

	private static void send(ResourceLocation id, ServerPlayer player, FriendlyByteBuf buf) {
		NetworkManager.sendToPlayer(player, id, buf);
	}

	public static ResourceLocation ID_FULL_REFRESH = new ResourceLocation(Fluidity.MOD_ID, "ffrs2c");
	public static ResourceLocation ID_UPDATE = new ResourceLocation(Fluidity.MOD_ID, "fuds2c");
	public static ResourceLocation ID_UPDATE_WITH_CAPACITY = new ResourceLocation(Fluidity.MOD_ID, "fucs2c");
}
