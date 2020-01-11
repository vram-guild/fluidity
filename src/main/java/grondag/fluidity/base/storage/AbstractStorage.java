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
package grondag.fluidity.base.storage;

import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.google.common.util.concurrent.Runnables;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.base.article.StoredArticle;
import grondag.fluidity.base.storage.helper.ListenerSet;

@API(status = Status.EXPERIMENTAL)
public abstract class AbstractStorage<V extends StoredArticle, T extends AbstractStorage<V, T>> implements Storage {
	public final ListenerSet<StorageListener> listeners = new ListenerSet<>(this::sendFirstListenerUpdate, this::sendLastListenerUpdate, this::onListenersEmpty);
	protected Predicate<Article> filter = Predicates.alwaysTrue();
	protected Runnable dirtyNotifier = Runnables.doNothing();

	@SuppressWarnings("unchecked")
	public T filter(Predicate<Article> filter) {
		this.filter = filter == null ? Predicates.alwaysTrue() : filter;
		return (T) this;
	}

	@Override
	public final void startListening(StorageListener listener, boolean sendNotifications) {
		listeners.startListening(listener, sendNotifications);
	}

	protected abstract void sendFirstListenerUpdate(StorageListener listener);

	protected abstract void sendLastListenerUpdate(StorageListener listener);

	protected abstract void onListenersEmpty();

	@Override
	public final void stopListening(StorageListener listener, boolean sendNotifications) {
		listeners.stopListening(listener, sendNotifications);
	}

	public Iterable<StorageListener> listeners() {
		return listeners;
	}

	public void onDirty(Runnable dirtyNotifier) {
		this.dirtyNotifier = dirtyNotifier == null ? Runnables.doNothing() : dirtyNotifier;
	}
}
