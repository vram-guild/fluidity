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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

import net.fabricmc.fabric.api.networking.v1.PacketSender;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.MutableFraction;

@Experimental
public class BulkStorageClientDelegate extends AbstractStorageClientDelegate<BulkDisplayDelegate> {
	public static final BulkStorageClientDelegate INSTANCE = new BulkStorageClientDelegate();

	protected final MutableFraction capacity = new MutableFraction();
	protected final MutableFraction usedCapacity = new MutableFraction();

	/**
	 * Incorporates changes and updates sort order. Returns true if a refresh was
	 * performed.
	 */


	protected BulkDisplayDelegate[] readItems(PacketByteBuf buf) {
		final int limit = buf.readInt();
		final BulkDisplayDelegate[] items = new BulkDisplayDelegate[limit];

		for (int i = 0; i < limit; i++) {
			items[i] = BulkDisplayDelegate.create(Article.fromPacket(buf), new Fraction(buf), buf.readVarInt());
		}

		return items;
	}

	@Override
	public void handleUpdateWithCapacity(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
		final BulkDisplayDelegate[] items = readItems(buffer);
		final Fraction newCapacity = new Fraction(buffer);

		if (client.isOnThread()) {
			handleUpdateInner(items, newCapacity);
		} else {
			client.execute(() -> handleUpdateInner(items, newCapacity));
		}
	}

	@Override
	public void handleUpdate(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
		final BulkDisplayDelegate[] items = readItems(buffer);

		if (client.isOnThread()) {
			handleUpdateInner(items, null);
		} else {
			client.execute(() -> handleUpdateInner(items, null));
		}
	}

	@Override
	public void handleFullRefresh(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
		final BulkDisplayDelegate[] items = readItems(buffer);
		final Fraction newCapacity = new Fraction(buffer);

		if (client.isOnThread()) {
			handleFullRefreshInner(items, newCapacity);
		} else {
			client.execute(() -> handleFullRefreshInner(items, newCapacity));
		}
	}

	protected void handleFullRefreshInner(BulkDisplayDelegate[] items, Fraction newCapacity) {
		capacity.set(newCapacity);
		MAP.clear();
		LIST.clear();
		usedCapacity.set(0);

		final int limit = items.length;

		for (int i = 0; i < limit; i++) {
			final BulkDisplayDelegate item = items[i];

			if(!item.getAmount().isZero()) {
				MAP.put(item.handle(), item);
				LIST.add(item);
				usedCapacity.add(item.getAmount());
			}
		}

		isSortDirty = true;
	}

	protected void handleUpdateInner(BulkDisplayDelegate[] items, Fraction newCapacity) {
		final int limit = items.length;

		if(newCapacity != null) {
			capacity.set(newCapacity);
		}

		for (int i = 0; i < limit; i++) {
			final BulkDisplayDelegate update = items[i];
			final BulkDisplayDelegate prior = MAP.get(update.handle());

			assert !update.getAmount().isNegative();

			if (prior == null) {
				if(!update.getAmount().isZero()) {
					MAP.put(update.handle(), update);
					addToListIfIncluded(update);
					usedCapacity.add(update.getAmount());
				}
			} else if (update.getAmount().isZero()) {
				MAP.remove(update.handle());
				LIST.remove(prior);
				usedCapacity.subtract(prior.getAmount());
			} else {
				usedCapacity.add(update.getAmount()).subtract(prior.getAmount());
				prior.setAmount(update.getAmount());
			}
		}

		isSortDirty = true;
	}

	public Fraction capacity() {
		return capacity;
	}

	public Fraction usedCapacity() {
		return usedCapacity;
	}

	@Override
	public int fillPercentage() {
		return capacity.isZero() ? 0 : (int) Math.round(usedCapacity.toDouble() * 100 / capacity.toDouble());
	}
}
