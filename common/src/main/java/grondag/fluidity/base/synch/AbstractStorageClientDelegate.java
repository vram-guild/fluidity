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

import java.util.Comparator;
import java.util.Locale;

import dev.architectury.networking.NetworkManager.PacketContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;

@Experimental
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

	public abstract void handleUpdateWithCapacity(FriendlyByteBuf buffer, PacketContext ctx);

	public abstract void handleUpdate(FriendlyByteBuf buffer, PacketContext ctx);

	public abstract void handleFullRefresh(FriendlyByteBuf buffer, PacketContext ctx);

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

		this.filter = filter.toLowerCase(Locale.ROOT);
		updateFilter();
	}

	private void updateFilter() {
		if (!filter.equals(lastFilter)) {
			if (!lastFilter.equals("") && filter.startsWith(lastFilter)) {
				applyFilter();
			} else {
				LIST.clear();

				if (filter.equals("")) {
					LIST.addAll(MAP.values());
				} else {
					for (final T item : MAP.values()) {
						addToListIfIncluded(item);
					}
				}

				isSortDirty = true;
			}

			lastFilter = filter;
		}
	}

	protected void applyFilter() {
		for (int i = LIST.size() - 1; i >= 0; --i) {
			final T delegate = LIST.get(i);

			if (delegate.isEmpty() || !delegate.lowerCaseLocalizedName().contains(filter)) {
				LIST.remove(i);
			}
		}
	}

	protected void addToListIfIncluded(T delegate) {
		if (!delegate.isEmpty() && (filter.equals("") || delegate.lowerCaseLocalizedName().contains(filter))) {
			LIST.add(delegate);
		}
	}

	public String getFilter() {
		return filter;
	}

	public abstract int fillPercentage();
}
