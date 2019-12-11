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
package grondag.fluidity.api.storage.base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.api.storage.view.ArticleView;
import grondag.fluidity.api.transact.TransactionContext;

@API(status = Status.EXPERIMENTAL)
public abstract class AbstractStorage implements Storage {
	protected final List<Consumer<? super ArticleView>> listeners = new ArrayList<>();
	protected final Consumer<TransactionContext> rollbackHandler = this::handleRollback;

	protected abstract void handleRollback(TransactionContext context);

	@Override
	public void startListening(Consumer<? super ArticleView> listener, Object connection, Predicate<? super ArticleView> articleFilter) {
		listeners.add(listener);

		this.forEach(v -> {
			listener.accept(v);
			return true;
		});
	}

	@Override
	public void stopListening(Consumer<? super ArticleView> listener) {
		listeners.remove(listener);
	}

	@Override
	public void notifyListeners(int slot) {
		final List<Consumer<? super ArticleView>> listeners = this.listeners;
		final int limit = listeners.size();
		final ArticleView view = this.view(slot);

		for (int i = 0; i < limit; i++) {
			listeners.get(i).accept(view);
		}
	}
}
