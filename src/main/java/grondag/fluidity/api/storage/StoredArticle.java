package grondag.fluidity.api.storage;

public interface StoredArticle {
    default int slot() {
        return 0;
    }
    
	Article article();
}
