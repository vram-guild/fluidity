package grondag.fluidity.base.storage.bulk;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.storage.Storage;

public interface BulkStorage extends Storage {
	@Override
	default long accept(Article item, long count, boolean simulate) {
		return accept(item, count, 1, simulate);
	}

	@Override
	default long supply(Article item, long count, boolean simulate) {
		return supply(item, count, 1, simulate);
	}

	@Override
	default long count() {
		return amount().whole();
	}

	@Override
	default long capacity() {
		return volume().whole();
	}
}
