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

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.TransactionParticipant.TransactionDelegate;
import grondag.fluidity.base.article.AbstractStoredArticle;
import grondag.fluidity.base.transact.LazyRollbackHandler;

@Experimental
public abstract class AbstractLazyRollbackStore<V extends AbstractStoredArticle, T extends AbstractLazyRollbackStore<V, T>> extends AbstractStore<V, T> implements TransactionDelegate {
	protected final LazyRollbackHandler rollbackHandler = new LazyRollbackHandler(this::createRollbackState, this::applyRollbackState, this);

	protected abstract Object createRollbackState();

	protected abstract void applyRollbackState(Object state, boolean isCommitted);

	@Override
	public TransactionDelegate getTransactionDelegate() {
		return this;
	}

	@Override
	public final Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		return rollbackHandler.prepareExternal(context);
	}

	@Override
	public boolean isSelfEnlisting() {
		return true;
	}
}
