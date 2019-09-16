package grondag.fluidity.api.discrete;

import grondag.fluidity.api.storage.StoredArticle;

public interface DiscreteArticleView<V> extends StoredArticle<V> {
    default long count() {
        return 0;
    }

    default long capacity() {
        return count();
    }
    
    default DiscreteArticle<V> toImmutable() {
        return DiscreteArticle.of(article(), count());
    }

    default MutableDiscreteArticle<V> mutableCopy() {
        return MutableDiscreteArticle.of(article(), count());
    }
}
