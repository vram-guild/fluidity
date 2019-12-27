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
package grondag.fluidity.base.storage.component;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.storage.StorageListener;

@API(status = Status.EXPERIMENTAL)
public class ListenerSet implements Iterable<StorageListener>, Iterator<StorageListener> {
	protected final ObjectArrayList<WeakReference<StorageListener>> listeners = new ObjectArrayList<>();
	protected int index = -1;
	protected StorageListener next = null;
	protected boolean hasMissing = false;

	protected final Consumer<StorageListener>  additionHandler;
	protected final Consumer<StorageListener>  removalHandler;
	protected final @Nullable Runnable onEmptyCallback;

	public ListenerSet(Consumer<StorageListener>  additionHandler, Consumer<StorageListener> removalHandler, @Nullable Runnable onEmptyCallback) {
		this.additionHandler = additionHandler;
		this.removalHandler = additionHandler;
		this.onEmptyCallback = onEmptyCallback;
	}

	public void startListening(StorageListener listener, boolean sendNotifications) {
		listeners.add(new WeakReference<>(listener));

		if(sendNotifications) {
			additionHandler.accept(listener);
		}
	}

	protected void cleanMissing() {
		if(hasMissing) {
			final int limit = listeners.size();

			for(int i = limit - 1; i >= 0; i--) {
				if(listeners.get(i).get() ==null) {
					listeners.remove(i);
				}
			}

			hasMissing = false;

			if(listeners.isEmpty() && onEmptyCallback != null) {
				onEmptyCallback.run();
			}
		}
	}

	public void stopListening(StorageListener listener, boolean sendNotifications) {
		final int limit = listeners.size();

		if (limit > 0) {
			for(int i = limit - 1; i >= 0; i--) {
				final StorageListener l = listeners.get(i).get();

				if(l ==null || l == listener) {
					if(sendNotifications) {
						removalHandler.accept(listener);
					}

					listeners.remove(i);
				}
			}

			if(listeners.isEmpty() && onEmptyCallback != null) {
				onEmptyCallback.run();
			}
		}
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public StorageListener next() {
		final StorageListener result = next;
		moveNext();
		return result;
	}

	@Override
	public Iterator<StorageListener> iterator() {
		cleanMissing();
		index = -1;
		moveNext();
		return this;
	}

	protected void moveNext() {
		next = null;
		final int limit = listeners.size();
		while(++index < limit && next == null) {
			next = listeners.get(index).get();

			if(next == null) {
				hasMissing = true;
			}
		}
	}

	public boolean isEmpty() {
		cleanMissing();
		return listeners.isEmpty();
	}
}
