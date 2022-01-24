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

package grondag.fluidity.impl.article;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;
import grondag.fluidity.impl.Fluidity;

@Internal
public class ArticleCache {
	static class ArticleKey {
		private ArticleType<?> type;
		private Object resource;
		private int hashCode;

		ArticleKey set (ArticleType<?> type, Object resource) {
			this.type = type;
			this.resource = resource;
			hashCode = type.hashCode() ^ resource.hashCode();
			return this;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof final ArticleKey other) {
				return type == other.type && resource == other.resource;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}

	static class FatArticleKey {
		private ArticleType<?> type;
		private Object resource;
		private int hashCode;
		public CompoundTag tag;

		FatArticleKey set (ArticleType<?> type, Object resource, CompoundTag tag) {
			this.type = type;
			this.resource = resource;
			this.tag = tag.copy();
			hashCode = type.hashCode() ^ resource.hashCode() ^ tag.hashCode();
			return this;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof final FatArticleKey other) {
				return type == other.type && resource == other.resource && tag.equals(other.tag);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}

	private static final ThreadLocal<ArticleKey> KEYS = ThreadLocal.withInitial(ArticleKey::new);
	private static final ThreadLocal<FatArticleKey> FAT_KEYS = ThreadLocal.withInitial(FatArticleKey::new);
	private static final ConcurrentHashMap<ArticleKey, ArticleImpl<?>> UNIQUES = new ConcurrentHashMap<>();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final LoadingCache<FatArticleKey, ArticleImpl<?>> TAGGED = CacheBuilder
		.newBuilder()
		.maximumSize(0x10000)
		.build(CacheLoader.from(k -> {
			FAT_KEYS.set(new FatArticleKey());
			return new ArticleImpl(k.type, k.resource, k.tag);
		}));

	private static boolean warnOnTaggedFailure = true;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Article getArticle(ArticleType type, Object resource, CompoundTag tag) {
		if (type == ArticleType.NOTHING) {
			return Article.NOTHING;
		}

		if (tag == null) {
			final ArticleKey key = KEYS.get().set(type, resource);
			return UNIQUES.computeIfAbsent(key, k -> {
				KEYS.set(new ArticleKey());
				return new ArticleImpl(k.type, k.resource, null);
			});
		} else {
			final FatArticleKey key = FAT_KEYS.get().set(type, resource, tag);

			try {
				return TAGGED.get(key);
			} catch (final ExecutionException e) {
				if (warnOnTaggedFailure) {
					Fluidity.LOG.warn("Cache load failure for tagged article.  Subsequent warnings are suppressed.", e);
					warnOnTaggedFailure = false;
				}

				return new ArticleImpl(type, resource, tag);
			}
		}
	}
}
