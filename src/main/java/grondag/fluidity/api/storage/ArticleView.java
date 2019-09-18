package grondag.fluidity.api.storage;

public interface ArticleView {
	/**
	 * For stores with fixed slots, this represents a specific location within the store.
	 * In other cases, it is an abstract handle to a quantity of a specific article instance that will
	 * retain the slot:article mapping even if all of the article is removed, for as long as there is
	 * any listener.  This means listeners can always use slots to maintain a replicate of contents
	 * and reliably identify articles that have changed.
	 */
	int slot();
}
