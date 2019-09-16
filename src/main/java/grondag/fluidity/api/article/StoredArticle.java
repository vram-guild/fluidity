package grondag.fluidity.api.article;

public interface StoredArticle<T> {
    default int slot() {
        return 0;
    }
    
	T article();
	
	ArticleProvider<T> provider();
	
	default boolean isArticleEqual(StoredArticle<T> other) {
		return provider().areEqual(this.article(), other.article());
	}
	
	default int articleHashCode() {
		return provider().hashCode(this.article());
	}
}
