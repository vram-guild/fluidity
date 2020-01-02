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
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.TransactionParticipant;
import grondag.fluidity.api.transact.TransactionParticipant.TransactionDelegate;
import grondag.fluidity.base.article.AggregateStoredArticle;
import grondag.fluidity.base.storage.component.FlexibleArticleManager;

@API(status = Status.EXPERIMENTAL)
public abstract class AbstractAggregateStorage<V extends AggregateStoredArticle, T extends AbstractAggregateStorage<V, T>> extends AbstractStorage<V, T> implements StorageListener, TransactionDelegate  {
	protected final Consumer<TransactionContext> rollbackHandler = this::handleRollback;
	protected final FlexibleArticleManager<V> articles;
	protected final ObjectOpenHashSet<Storage> stores = new ObjectOpenHashSet<>();

	protected Consumer<TransactionParticipant> enlister = t -> {};
	protected boolean itMe = false;

	public AbstractAggregateStorage(int startingHandleCount) {
		articles = new FlexibleArticleManager<>(startingHandleCount, this::newArticle);
	}

	protected abstract V newArticle();

	protected abstract StorageListener listener();

	public void addStore(Storage store) {
		if(stores.add(store)) {
			store.startListening(listener(), true);
		}
	}

	protected boolean needsRebuild = false;

	public void removeStore(Storage store) {
		if(stores.contains(store)) {
			store.stopListening(listener(), true);
			stores.remove(store);
		}
	}

	public AbstractAggregateStorage() {
		this(32);
	}

	@Override
	public int handleCount() {
		return articles.handleCount();
	}

	protected void handleRollback(TransactionContext context) {
		enlister = context.getState();
	}

	@Override
	public TransactionDelegate getTransactionDelegate() {
		return this;
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		context.setState(enlister);
		enlister = context.enlister();
		return rollbackHandler;
	}

	@Override
	public StoredArticleView view(int handle) {
		return ObjectUtils.defaultIfNull(articles.get(handle), StoredArticleView.EMPTY);
	}

	@Override
	public void disconnect(Storage storage, boolean didNotify, boolean isValid) {
		//TODO: implement and remove warning
		System.out.println("Unhandled disconnect in aggregate storage");
	}
}
