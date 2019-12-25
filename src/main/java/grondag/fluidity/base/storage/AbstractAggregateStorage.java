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
package grondag.fluidity.base.storage;

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.StoredArticleView;
import grondag.fluidity.api.item.Article;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.Transactor;
import grondag.fluidity.base.article.AbstractStoredArticle;
import grondag.fluidity.base.storage.component.FlexibleArticleManager;

@SuppressWarnings({"rawtypes", "unchecked"})
@API(status = Status.EXPERIMENTAL)
public abstract class AbstractAggregateStorage<A extends StoredArticleView, L extends StorageListener<L>, K extends AbstractStoredArticle, S extends Storage<A, L>> extends AbstractStorage<A, L> {
	protected final Consumer<TransactionContext> rollbackHandler = this::handleRollback;
	protected final FlexibleArticleManager<Article, K> articles;
	protected final ObjectOpenHashSet<S> stores = new ObjectOpenHashSet<>();

	protected Consumer<Transactor> enlister = t -> {};
	protected boolean itMe = false;

	public AbstractAggregateStorage(int startingHandleCount) {
		articles = new FlexibleArticleManager<>(startingHandleCount, this::newArticle);
	}

	protected abstract K newArticle();

	protected abstract L listener();

	public void addStore(S store) {
		if(stores.add(store)) {
			store.forEach(Storage.NOT_EMPTY, a -> {
				articles.findOrCreateArticle(a.item()).addStore(store);
				return true;
			});
		}

		store.startListening(listener());
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
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		context.setState(enlister);
		enlister = context.enlister();
		return rollbackHandler;
	}

	@Override
	public A view(int handle) {
		return (A) articles.get(handle);
	}
}
