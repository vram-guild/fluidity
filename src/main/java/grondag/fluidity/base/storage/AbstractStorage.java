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

import java.util.ArrayList;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.ArticleView;
import grondag.fluidity.api.item.ArticleItem;
import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.api.storage.StorageListener;

@API(status = Status.EXPERIMENTAL)
public abstract class AbstractStorage<A extends ArticleView<I>, L extends StorageListener<L>, I extends ArticleItem> implements Storage<A, L, I> {
	protected final List<L> listeners = new ArrayList<>();

	@Override
	public final void startListening(L listener) {
		listeners.add(listener);

		sendFirstListenerUpdate(listener);
	}

	protected abstract void sendFirstListenerUpdate(L listener);

	@Override
	public final void stopListening(L listener) {
		listeners.remove(listener);
	}
}
