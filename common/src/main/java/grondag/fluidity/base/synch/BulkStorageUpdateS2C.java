/*******************************************************************************
 * Copyright 2019, 2020 grondag
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
	private BulkStorageUpdateS2C() {}

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
