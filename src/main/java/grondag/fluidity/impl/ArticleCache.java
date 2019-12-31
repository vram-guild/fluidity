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
package grondag.fluidity.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.Fluidity;
import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;

@API(status = Status.INTERNAL)
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
			if(obj instanceof ArticleKey ) {
				final ArticleKey other = (ArticleKey) obj;
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
			if(obj instanceof FatArticleKey ) {
				final FatArticleKey other = (FatArticleKey) obj;
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
		if(type == ArticleType.NOTHING) {
			return Article.NOTHING;
		}

		if(tag == null) {
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
				if(warnOnTaggedFailure) {
					Fluidity.LOG.warn("Cache load failure for tagged article.  Subsequent warnings are suppressed.", e);
					warnOnTaggedFailure = false;
				}

				return new ArticleImpl(type, resource, tag);
			}
		}
	}
}
