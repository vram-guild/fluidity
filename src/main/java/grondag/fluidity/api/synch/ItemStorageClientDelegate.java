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

package grondag.fluidity.api.synch;

import java.util.Comparator;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.item.ItemStack;
import net.minecraft.util.PacketByteBuf;

import net.fabricmc.fabric.api.network.PacketContext;

@API(status = Status.EXPERIMENTAL)
public final class ItemStorageClientDelegate {
	private ItemStorageClientDelegate() {}

	private static final Int2ObjectOpenHashMap<ItemDisplayDelegate> MAP = new Int2ObjectOpenHashMap<>();

	public static final ObjectArrayList<ItemDisplayDelegate> LIST = new ObjectArrayList<>();

	private static boolean isSortDirty = false;

	private static int sortIndex = 0;
	private static String filter = "";
	private static String lastFilter = "";

	private static long capacity;
	private static long usedCapacity;

	/**
	 * Incorporates changes and updates sort order. Returns true if a refresh was
	 * performed.
	 */

	public static boolean refreshListIfNeeded() {
		if (!isSortDirty) {
			return false;
		}

		final Comparator<ItemDisplayDelegate> sort = ItemDisplayDelegate.getSort(sortIndex);
		LIST.sort(sort);
		isSortDirty = false;
		return true;
	}

	public static void handleUpdateWithCapacity(PacketContext context, PacketByteBuf buf) {
		handleUpdateInner(context, buf, true);
	}

	public static void handleUpdate(PacketContext context, PacketByteBuf buf) {
		handleUpdateInner(context, buf, false);
	}

	public static void handleFullRefresh(PacketContext context, PacketByteBuf buf) {
		MAP.clear();
		LIST.clear();
		usedCapacity = 0;

		final int limit = buf.readInt();

		for (int i = 0; i < limit; i++) {
			final ItemDisplayDelegate item = ItemDisplayDelegate.create(buf.readItemStack(), buf.readVarLong(), buf.readVarInt());
			MAP.put(item.handle(), item);
			LIST.add(item);
			usedCapacity += item.getCount();
		}

		capacity = buf.readVarLong();
		isSortDirty = true;
	}

	private static void handleUpdateInner(PacketContext context, PacketByteBuf buf, boolean hasCapacity) {
		final int limit = buf.readInt();

		for (int i = 0; i < limit; i++) {
			final ItemStack stack = buf.readItemStack();
			final long count = buf.readVarLong();
			final int handle = buf.readVarInt();
			final ItemDisplayDelegate prior = MAP.get(handle);

			if (prior == null) {
				final ItemDisplayDelegate update = ItemDisplayDelegate.create(stack, count, handle);
				MAP.put(handle, update);
				addToListIfIncluded(update);
				usedCapacity += count;
			} else if (count == 0) {
				MAP.remove(handle);
				LIST.remove(prior);
				usedCapacity -= prior.getCount();
			} else {
				usedCapacity += count - prior.getCount();
				prior.setCount(count);
			}
		}

		isSortDirty = true;
	}

	public static int getSortIndex() {
		return sortIndex;
	}

	public static void setSortIndex(int sortIndex) {
		ItemStorageClientDelegate.sortIndex = sortIndex;
		isSortDirty = true;
	}

	public static void setFilter(@Nullable String filter) {
		if (filter == null) {
			filter = "";
		}

		ItemStorageClientDelegate.filter = filter.toLowerCase();
		updateFilter();
	}

	private static void updateFilter() {
		if(!filter.equals(lastFilter)) {

			if(!lastFilter.equals("") && filter.startsWith(lastFilter)) {
				applyFilter();
			} else {
				LIST.clear();

				if(filter.equals("")) {
					LIST.addAll(MAP.values());
				} else {
					for(final ItemDisplayDelegate item : MAP.values()) {
						addToListIfIncluded(item);
					}
				}

				isSortDirty = true;
			}

			lastFilter = filter;
		}
	}

	private static void applyFilter() {
		for(int i = LIST.size() - 1; i >= 0; --i) {
			if(!LIST.get(i).lowerCaseLocalizedName().contains(filter)) {
				LIST.remove(i);
			}
		}
	}

	private static void addToListIfIncluded(ItemDisplayDelegate delegate) {
		if(filter.equals("") || delegate.lowerCaseLocalizedName().contains(filter)) {
			LIST.add(delegate);
		}
	}

	public static String getFilter() {
		return filter;
	}

	public static long capacity() {
		return capacity;
	}

	public static long usedCapacity() {
		return usedCapacity;
	}

	public static int fillPercentage() {
		return capacity == 0 ? 0 : (int) (usedCapacity * 100 / capacity);
	}
}
