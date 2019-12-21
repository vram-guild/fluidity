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

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.ArticleView;
import grondag.fluidity.api.item.StorageItem;
import grondag.fluidity.api.storage.StorageListener;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.base.transact.LazyRollbackHandler;

@API(status = Status.EXPERIMENTAL)
public abstract class AbstractLazyRollbackStorage<A extends ArticleView<I>, L extends StorageListener<L>, I extends StorageItem> extends AbstractStorage<A, L, I> {
	protected final LazyRollbackHandler rollbackHandler = new LazyRollbackHandler(this::createRollbackState, this::applyRollbackState);

	protected abstract Object createRollbackState();

	protected abstract void applyRollbackState(Object state);

	@Override
	public final Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		return rollbackHandler.prepareExternal(context);
	}
}
