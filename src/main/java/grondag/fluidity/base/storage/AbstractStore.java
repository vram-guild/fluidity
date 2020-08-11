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

import net.minecraft.fluid.Fluid;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.storage.StorageEventStream;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.api.storage.Store;
import grondag.fluidity.api.util.AmbiguousBoolean;
import grondag.fluidity.base.article.StoredArticle;
import grondag.fluidity.base.storage.helper.ListenerSet;

@API(status = Status.EXPERIMENTAL)
public abstract class AbstractStore<V extends StoredArticle, T extends AbstractStore<V, T>> implements Store, StorageEventStream {
	public final ListenerSet<StorageListener> listeners = new ListenerSet<>(this::sendFirstListenerUpdate, this::sendLastListenerUpdate, this::onListenersEmpty);
	protected Predicate<Article> filter = Predicates.alwaysTrue();
	protected Runnable dirtyNotifier = Runnables.doNothing();
	protected boolean isValid = true;
	protected Predicate<ArticleType<?>> typeFilter = null;

	@SuppressWarnings("unchecked")
	public T filter(Predicate<Article> filter) {
		this.filter = filter == null ? Predicates.alwaysTrue() : filter;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T typeFilter(Predicate<ArticleType<?>> typeFilter) {
		this.typeFilter = typeFilter;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T filter(ArticleType<Fluid> type) {
		filter(type.articlePredicate());
		typeFilter(type.typePredicate());
		return (T) this;
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

	@Override
	public AmbiguousBoolean allowsType(ArticleType<?> type) {
		return typeFilter == null ? AmbiguousBoolean.MAYBE : (typeFilter.test(type) ? AmbiguousBoolean.YES : AmbiguousBoolean.NO);
	}

	/**
	 * Marks store invalid and sends disconnect event to all listeners.
	 */
	public void disconnect() {
		isValid = false;
		listeners.forEach(l -> l.disconnect(this, false, false));
	}

	@Override
	public StorageEventStream eventStream() {
		return this;
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
