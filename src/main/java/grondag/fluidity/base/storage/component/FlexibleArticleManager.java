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
package grondag.fluidity.base.storage.component;

import java.lang.reflect.Array;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.util.math.MathHelper;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.base.article.StoredArticle;

@SuppressWarnings("unchecked")
@API(status = Status.EXPERIMENTAL)
public class FlexibleArticleManager<V extends StoredArticle> extends AbstractArticleManager<V> {
	protected final Object2ObjectOpenHashMap<Article, V> articles = new Object2ObjectOpenHashMap<>();

	protected int nextUnusedHandle = 0;
	protected V[] handles;


	public FlexibleArticleManager(int startingHandleCount, Supplier<V> articleFactory) {
		super(articleFactory);


		startingHandleCount = MathHelper.smallestEncompassingPowerOfTwo(startingHandleCount);
		final V[] handles = (V[]) Array.newInstance(articleFactory.get().getClass(), startingHandleCount);

		for(int i = 0; i < startingHandleCount; i++) {
			final V a = articleFactory.get();
			a.setHandle(i);
			handles[i] = a;
		}

		this.handles = handles;
	}

	@Override
	public V findOrCreateArticle(Article key) {
		V candidate = articles.get(key);

		if(candidate == null) {
			candidate = getEmptyArticle();
			candidate.setArticle(key);
			articles.put(key, candidate);
		}

		return candidate;
	}

	protected V getEmptyArticle() {
		final int index = getEmptyHandle();
		return handles[index];
	}

	protected int getEmptyHandle() {
		// fill unused handle capacity
		final int handleCount = handles.length;
		final int result = nextUnusedHandle++;

		if(result < handleCount) {
			return result;
		}

		// add slot capacity
		final int newCount = handleCount * 2;
		final V[] newHandles = (V[]) Array.newInstance(articleFactory.get().getClass(), newCount);
		System.arraycopy(handles, 0, newHandles, 0, handleCount);

		for(int i = handleCount; i < newCount; i++) {
			final V a = articleFactory.get();
			a.setHandle(i);
			newHandles[i] = a;
		}

		handles = newHandles;

		return result;
	}

	@Override
	public void compact() {
		for (int i = nextUnusedHandle - 1; i > 0; --i) {
			final V a = handles[i];

			if(a.isEmpty()) {
				final int target = nextUnusedHandle - 1;

				if (i == target) {
					// already at end
					--nextUnusedHandle;
				} else {
					// swap with last non-empty and renumber
					final V swap = handles[i];
					swap.setHandle(target);

					handles[i] = handles[target];
					handles[i].setHandle(i);

					handles[target] = swap;
				}

				articles.remove(a.article());
			}
		}
	}

	@Override
	public int handleCount() {
		return nextUnusedHandle;
	}

	@Override
	public V get(int handle) {
		return handle >= 0 && handle < handles.length ? handles[handle] : null;
	}

	@Override
	public V get(Article key) {
		return articles.get(key);
	}

	@Override
	public void clear() {
		articles.clear();
		nextUnusedHandle = 0;
	}
}
