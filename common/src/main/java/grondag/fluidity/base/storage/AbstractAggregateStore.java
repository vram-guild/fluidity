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

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.api.storage.Store;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.TransactionParticipant.TransactionDelegate;
import grondag.fluidity.base.article.AggregateStoredArticle;
import grondag.fluidity.base.storage.helper.FlexibleArticleManager;
import grondag.fluidity.impl.Fluidity;

/**
 *  Transaction support: expects all member stores to be self-enlisting and relies on member store
 *  notifications for transaction handling.  Reports as self-enlisting but never enlists because is has no
 *  internal state that would participate in transactions that isn't redundant of members.
 *
 * @param <V>
 * @param <T>
 */
@Experimental
public abstract class AbstractAggregateStore<V extends AggregateStoredArticle, T extends AbstractAggregateStore<V, T>> extends AbstractStore<V, T> implements StorageListener, TransactionDelegate  {
	protected final Consumer<TransactionContext> rollbackHandler = this::handleRollback;
	protected final FlexibleArticleManager<V> articles;
	protected final ObjectOpenHashSet<Store> stores = new ObjectOpenHashSet<>();

	public AbstractAggregateStore(int startingHandleCount) {
		articles = new FlexibleArticleManager<>(startingHandleCount, this::newArticle);
	}

	protected abstract V newArticle();

	protected abstract StorageListener listener();

	public void addStore(Store store) {
		if(stores.add(store)) {
			store.eventStream().startListening(listener(), true);
		}
	}

	protected boolean needsRebuild = false;

	public void removeStore(Store store) {
		if(stores.contains(store)) {
			store.eventStream().stopListening(listener(), true);
			stores.remove(store);
		}
	}

	public AbstractAggregateStore() {
		this(32);
	}

	@Override
	public int handleCount() {
		return articles.handleCount();
	}

	/** Relies on members - see header */
	protected void handleRollback(TransactionContext context) {
		// NOOP
	}

	/** Relies on members - see header */
	@Override
	public TransactionDelegate getTransactionDelegate() {
		return this;
	}

	/** Relies on members - see header */
	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		return rollbackHandler;
	}

	/** Relies on members - see header */
	@Override
	public boolean isSelfEnlisting() {
		return true;
	}

	@Override
	public StoredArticleView view(int handle) {
		return ObjectUtils.defaultIfNull(articles.get(handle), StoredArticleView.EMPTY);
	}

	@Override
	public void disconnect(Store storage, boolean didNotify, boolean isValid) {
		//TODO: Implement and remove warning
		Fluidity.LOG.warn("Unhandled disconnect in aggregate storage.");
	}

	@Override
	public boolean isAggregate() {
		return true;
	}

	@Override
	public boolean isView() {
		return true;
	}
}
