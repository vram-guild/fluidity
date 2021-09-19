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

import org.jetbrains.annotations.ApiStatus.Experimental;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import grondag.fluidity.api.article.Article;

@Experimental
public class DiscreteStorageClientDelegate extends AbstractStorageClientDelegate<DiscreteDisplayDelegate> {
	public static final DiscreteStorageClientDelegate INSTANCE = new  DiscreteStorageClientDelegate();

	protected long capacity;
	protected long usedCapacity;

	/**
	 * Incorporates changes and updates sort order. Returns true if a refresh was
	 * performed.
	 */


	protected DiscreteDisplayDelegate[] readItems(FriendlyByteBuf buf) {
		final int limit = buf.readInt();
		final DiscreteDisplayDelegate[] items = new DiscreteDisplayDelegate[limit];

		for (int i = 0; i < limit; i++) {
			items[i] = DiscreteDisplayDelegate.create(Article.fromPacket(buf), buf.readVarLong(), buf.readVarInt());
		}

		return items;
	}

	@Override
	public void handleUpdateWithCapacity(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender responseSender) {
		final DiscreteDisplayDelegate[] items = readItems(buffer);
		final long newCapacity = buffer.readVarLong();

		if (client.isSameThread()) {
			handleUpdateInner(items, newCapacity);
		} else {
			client.execute(() -> handleUpdateInner(items, newCapacity));
		}
	}

	@Override
	public void handleUpdate(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender responseSender) {
		final DiscreteDisplayDelegate[] items = readItems(buffer);

		if (client.isSameThread()) {
			handleUpdateInner(items, -1);
		} else {
			client.execute(() -> handleUpdateInner(items, -1));
		}
	}

	@Override
	public void handleFullRefresh(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buffer, PacketSender responseSender) {
		final DiscreteDisplayDelegate[] items = readItems(buffer);
		final long newCapacity = buffer.readVarLong();

		if (client.isSameThread()) {
			handleFullRefreshInner(items, newCapacity);
		} else {
			client.execute(() -> handleFullRefreshInner(items, newCapacity));
		}
	}

	protected void handleFullRefreshInner(DiscreteDisplayDelegate[] items, long newCapacity) {
		capacity = newCapacity;
		MAP.clear();
		LIST.clear();
		usedCapacity = 0;

		final int limit = items.length;

		for (int i = 0; i < limit; i++) {
			final DiscreteDisplayDelegate item = items[i];

			if(item.getCount() > 0) {
				MAP.put(item.handle(), item);
				addToListIfIncluded(item);
				usedCapacity += item.getCount();
			}
		}

		isSortDirty = true;
	}

	protected void handleUpdateInner(DiscreteDisplayDelegate[] items, long newCapacity) {
		final int limit = items.length;

		if(newCapacity >= 0) {
			capacity = newCapacity;
		}

		for (int i = 0; i < limit; i++) {
			final DiscreteDisplayDelegate update = items[i];
			final DiscreteDisplayDelegate prior = MAP.get(update.handle());

			assert update.getCount() >= 0;

			if (prior == null) {
				if(update.getCount() > 0) {
					MAP.put(update.handle(), update);
					addToListIfIncluded(update);
					usedCapacity += update.getCount();
				}
			} else if (update.getCount() == 0) {
				MAP.remove(update.handle());
				LIST.remove(prior);
				usedCapacity -= prior.getCount();
			} else {
				usedCapacity += update.getCount() - prior.getCount();
				prior.setCount(update.getCount());
			}
		}

		isSortDirty = true;
	}

	public long capacity() {
		return capacity;
	}

	public long usedCapacity() {
		return usedCapacity;
	}

	@Override
	public int fillPercentage() {
		return capacity == 0 ? 0 : (int) (usedCapacity * 100 / capacity);
	}
}
