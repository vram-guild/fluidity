package grondag.fluidity.impl;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.CompoundTag;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.article.ArticleType;

public class ArticleCache {
	static class ArticleKey {
		private ArticleType<?> type;
		private Object resource;
		private int hashCode;

		ArticleKey() {
			this(ArticleType.NOTHING, new Object());
		}

		ArticleKey(ArticleType<?> type, Object resource) {
			set(type, resource);
		}

		ArticleKey set (ArticleType<?> type, Object resource) {
			this.type = type;
			this.resource = resource;
			hashCode = type.hashCode() ^ resource.hashCode();
			return this;

		}
		static ArticleKey of(ArticleType<?> type, Object resource) {
			return new ArticleKey(type, resource);
		}

		@Override
		protected ArticleKey clone() {
			return of(type, resource);
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

	private static final ThreadLocal<ArticleKey> KEYS = ThreadLocal.withInitial(ArticleKey::new);
	private static final ConcurrentHashMap<ArticleKey, ArticleImpl<?>> UNIQUES = new ConcurrentHashMap<>();

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
			//TODO: cache these (don't intern)
			return new ArticleImpl(type, resource, tag);
		}
	}
}
