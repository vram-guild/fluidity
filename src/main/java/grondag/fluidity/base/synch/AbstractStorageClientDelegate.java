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

import java.util.Comparator;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.util.PacketByteBuf;

import net.fabricmc.fabric.api.network.PacketContext;

@API(status = Status.EXPERIMENTAL)
public abstract class AbstractStorageClientDelegate<T extends DisplayDelegate> {
	protected final Int2ObjectOpenHashMap<T> MAP = new Int2ObjectOpenHashMap<>();

	public final ObjectArrayList<T> LIST = new ObjectArrayList<>();

	protected boolean isSortDirty = false;

	protected int sortIndex = 0;
	protected String filter = "";
	protected String lastFilter = "";

	/**
	 * Incorporates changes and updates sort order. Returns true if a refresh was
	 * performed.
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean refreshListIfNeeded() {
		if (!isSortDirty) {
			return false;
		}

		final Comparator sort = DisplayDelegate.getSort(sortIndex);
		LIST.sort(sort);
		isSortDirty = false;
		return true;
	}

	public abstract void handleUpdateWithCapacity(PacketContext context, PacketByteBuf buf);

	public abstract void handleUpdate(PacketContext context, PacketByteBuf buf);

	public abstract void handleFullRefresh(PacketContext context, PacketByteBuf buf);

	public int getSortIndex() {
		return sortIndex;
	}

	public void setSortIndex(int sortIndex) {
		this.sortIndex = sortIndex;
		isSortDirty = true;
	}

	public void setFilter(@Nullable String filter) {
		if (filter == null) {
			filter = "";
		}

		this.filter = filter.toLowerCase();
		updateFilter();
	}

	private void updateFilter() {
		if(!filter.equals(lastFilter)) {

			if(!lastFilter.equals("") && filter.startsWith(lastFilter)) {
				applyFilter();
			} else {
				LIST.clear();

				if(filter.equals("")) {
					LIST.addAll(MAP.values());
				} else {
					for(final T item : MAP.values()) {
						addToListIfIncluded(item);
					}
				}

				isSortDirty = true;
			}

			lastFilter = filter;
		}
	}

	protected void applyFilter() {
		for(int i = LIST.size() - 1; i >= 0; --i) {
			final T delegate = LIST.get(i);

			if(delegate.isEmpty() || !delegate.lowerCaseLocalizedName().contains(filter)) {
				LIST.remove(i);
			}
		}
	}

	protected void addToListIfIncluded(T delegate) {
		if(!delegate.isEmpty() && (filter.equals("") || delegate.lowerCaseLocalizedName().contains(filter))) {
			LIST.add(delegate);
		}
	}

	public String getFilter() {
		return filter;
	}

	public abstract int fillPercentage();
}
