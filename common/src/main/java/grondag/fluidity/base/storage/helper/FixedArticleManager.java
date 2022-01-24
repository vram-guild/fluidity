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

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.base.article.AbstractStoredArticle;

@SuppressWarnings("unchecked")
@Experimental
public class FixedArticleManager<V extends AbstractStoredArticle> extends AbstractArticleManager<V> {
	protected V[] articles;
	protected final int handleCount;

	public FixedArticleManager(int handleCount, Supplier<V> articleFactory) {
		super(articleFactory);
		this.handleCount = handleCount;
		final V[] handles = (V[]) Array.newInstance(articleFactory.get().getClass(), handleCount);

		for (int i = 0; i < handleCount; i++) {
			final V a = articleFactory.get();
			a.setHandle(i);
			handles[i] = a;
		}

		this.articles = handles;
	}

	@Override
	public V findOrCreateArticle(Article key) {
		int firstUnused = -1;

		for (int i = 0; i < handleCount; i++) {
			final V candidate = articles[i];

			if (candidate.article().equals(key)) {
				return candidate;
			} else if (firstUnused == -1 && candidate.isEmpty()) {
				firstUnused = i;
			}
		}

		if (firstUnused > -1) {
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
		for (int i = 0; i < handleCount; i++) {
			final V candidate = articles[i];

			if (candidate.article().equals(key)) {
				return candidate;
			}
		}

		return null;
	}

	@Override
	public void clear() {
		for (int i = 0; i < handleCount; i++) {
			articles[i].zero();
		}
	}
}
