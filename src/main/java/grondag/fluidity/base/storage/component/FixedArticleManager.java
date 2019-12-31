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
package grondag.fluidity.base.storage.component;

import java.lang.reflect.Array;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.base.article.AbstractStoredArticle;

@SuppressWarnings("unchecked")
@API(status = Status.EXPERIMENTAL)
public class FixedArticleManager<V extends AbstractStoredArticle> extends AbstractArticleManager<V> {
	protected V[] articles;
	protected final int handleCount;

	public FixedArticleManager(int handleCount, Supplier<V> articleFactory) {
		super(articleFactory);
		this.handleCount = handleCount;
		final V[] handles = (V[]) Array.newInstance(articleFactory.get().getClass(), handleCount);

		for(int i = 0; i < handleCount; i++) {
			final V a = articleFactory.get();
			a.setHandle(i);
			handles[i] = a;
		}

		this.articles = handles;
	}

	@Override
	public V findOrCreateArticle(Article key) {
		int firstUnused = -1;

		for(int i = 0; i < handleCount; i++) {
			final V candidate = articles[i];

			if(candidate.article().equals(key)) {
				return candidate;
			} else if (firstUnused == -1 && candidate.isEmpty()) {
				firstUnused = i;
			}
		}

		if(firstUnused > -1) {
			final V result = articles[firstUnused];
			result.setArticle(key);
			return result;
		} else {
			return null;
		}
	}

	@Override
	public void compact() {
		// NOOP
	}

	@Override
	public int handleCount() {
		return handleCount;
	}

	@Override
	public V get(int handle) {
		return handle >= 0 && handle < handleCount ? articles[handle] : null;
	}

	@Override
	public V get(Article key) {
		for(int i = 0; i < handleCount; i++) {
			final V candidate = articles[i];

			if(candidate.article().equals(key)) {
				return candidate;
			}
		}

		return null;
	}

	@Override
	public void clear() {
		for(int i = 0; i < handleCount; i++) {
			articles[i].zero();
		}
	}
}
