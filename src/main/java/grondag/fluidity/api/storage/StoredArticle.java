package grondag.fluidity.api.storage;

public interface StoredArticle<T> {
    default int slot() {
        return 0;
    }
    
	T article();
	
	ArticleProvider<T> provider();
}
