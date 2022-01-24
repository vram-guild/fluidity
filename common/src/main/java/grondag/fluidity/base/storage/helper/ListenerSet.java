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

package grondag.fluidity.base.storage.helper;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

@Experimental
public class ListenerSet<L> implements Iterable<L>, Iterator<L> {
	protected final ObjectArrayList<WeakReference<L>> listeners = new ObjectArrayList<>();
	protected int index = -1;
	protected L next = null;
	protected boolean hasMissing = false;

	protected final Consumer<L> additionHandler;
	protected final Consumer<L> removalHandler;
	protected final @Nullable Runnable onEmptyCallback;

	public ListenerSet(Consumer<L> additionHandler, Consumer<L> removalHandler, @Nullable Runnable onEmptyCallback) {
		this.additionHandler = additionHandler;
		this.removalHandler = removalHandler;
		this.onEmptyCallback = onEmptyCallback;
	}

	public void startListening(L listener, boolean sendNotifications) {
		listeners.add(new WeakReference<>(listener));

		if (sendNotifications) {
			additionHandler.accept(listener);
		}
	}

	protected void cleanMissing() {
		if (hasMissing) {
			final int limit = listeners.size();

			for (int i = limit - 1; i >= 0; i--) {
				if (listeners.get(i).get() == null) {
					listeners.remove(i);
				}
			}

			hasMissing = false;

			if (listeners.isEmpty() && onEmptyCallback != null) {
				onEmptyCallback.run();
			}
		}
	}

	public void stopListening(L listener, boolean sendNotifications) {
		final int limit = listeners.size();

		if (limit > 0) {
			for (int i = limit - 1; i >= 0; i--) {
				final L l = listeners.get(i).get();

				if (l == null) {
					listeners.remove(i);
				} else if (l == listener) {
					if (sendNotifications) {
						removalHandler.accept(listener);
					}

					listeners.remove(i);
					break;
				}
			}

			if (listeners.isEmpty() && onEmptyCallback != null) {
				onEmptyCallback.run();
			}
		}
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public L next() {
		final L result = next;
		moveNext();
		return result;
	}

	@Override
	public Iterator<L> iterator() {
		cleanMissing();
		index = -1;
		moveNext();
		return this;
	}

	protected void moveNext() {
		next = null;
		final int limit = listeners.size();

		while (++index < limit && next == null) {
			next = listeners.get(index).get();

			if (next == null) {
				hasMissing = true;
			}
		}
	}

	public boolean isEmpty() {
		cleanMissing();
		return listeners.isEmpty();
	}
}
