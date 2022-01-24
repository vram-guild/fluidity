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

package grondag.fluidity.base.storage;

import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.google.common.util.concurrent.Runnables;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.world.level.material.Fluid;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.api.storage.StorageEventStream;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.api.storage.Store;
import grondag.fluidity.api.util.AmbiguousBoolean;
import grondag.fluidity.base.article.StoredArticle;
import grondag.fluidity.base.storage.helper.ListenerSet;

@Experimental
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
