package grondag.fluidity.api.article;

public interface StoredArticle<T> {
    default int slot() {
        return 0;
    }
    
	T article();
	
	ArticleProvider<T> provider();
}
