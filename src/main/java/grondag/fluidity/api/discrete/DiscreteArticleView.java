package grondag.fluidity.api.discrete;

import grondag.fluidity.api.article.StoredArticle;

public interface DiscreteArticleView<T, V extends DiscreteArticleView<T, V>> extends StoredArticle<T> {
    default long count() {
        return 0;
    }

    default long capacity() {
        return count();
    }
    
    default DiscreteArticle<T, V> toImmutable() {
        return DiscreteArticle.of(article(), count());
    }

    default MutableDiscreteArticle<T, V> mutableCopy() {
        return MutableDiscreteArticle.of(article(), count());
    }
}
