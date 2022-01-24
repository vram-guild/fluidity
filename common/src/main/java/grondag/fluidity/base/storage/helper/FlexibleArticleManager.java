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

package grondag.fluidity.base.storage.helper;

import java.lang.reflect.Array;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.util.Mth;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.base.article.StoredArticle;

@SuppressWarnings("unchecked")
@Experimental
public class FlexibleArticleManager<V extends StoredArticle> extends AbstractArticleManager<V> {
	protected final Object2ObjectOpenHashMap<Article, V> articles = new Object2ObjectOpenHashMap<>();

	protected int nextUnusedHandle = 0;
	protected V[] handles;

	public FlexibleArticleManager(int startingHandleCount, Supplier<V> articleFactory) {
		super(articleFactory);

		startingHandleCount = Mth.smallestEncompassingPowerOfTwo(startingHandleCount);
		final V[] handles = (V[]) Array.newInstance(articleFactory.get().getClass(), startingHandleCount);

		for (int i = 0; i < startingHandleCount; i++) {
			final V a = articleFactory.get();
			a.setHandle(i);
			handles[i] = a;
		}

		this.handles = handles;
	}

	@Override
	public V findOrCreateArticle(Article key) {
		V candidate = articles.get(key);

		if (candidate == null) {
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

		if (result < handleCount) {
			return result;
		}

		// add slot capacity
		final int newCount = handleCount * 2;
		final V[] newHandles = (V[]) Array.newInstance(articleFactory.get().getClass(), newCount);
		System.arraycopy(handles, 0, newHandles, 0, handleCount);

		for (int i = handleCount; i < newCount; i++) {
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

			if (a.isEmpty()) {
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
