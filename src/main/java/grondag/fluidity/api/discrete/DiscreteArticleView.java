package grondag.fluidity.api.discrete;

import grondag.fluidity.api.storage.Article;
import grondag.fluidity.api.storage.StoredArticle;
import grondag.fluidity.impl.ArticleImpl;

public interface DiscreteArticleView extends StoredArticle {
	DiscreteArticleView EMPTY = new DiscreteArticleView() {
		@Override
		public Article article() {
			return ArticleImpl.EMPTY;
		}
    };

    default long count() {
        return 0;
    }

    default long capacity() {
        return count();
    }
    
    default DiscreteArticle toImmutable() {
        return DiscreteArticle.of(article(), count());
    }

    default MutableDiscreteArticle mutableCopy() {
        return MutableDiscreteArticle.of(article(), count());
    }
}
