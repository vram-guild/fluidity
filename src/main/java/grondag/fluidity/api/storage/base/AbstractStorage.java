package grondag.fluidity.api.storage.base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import grondag.fluidity.api.storage.Storage;
import grondag.fluidity.api.storage.view.ArticleView;
import grondag.fluidity.api.transact.TransactionContext;

public abstract class AbstractStorage implements Storage {
	protected final List<Consumer<? super ArticleView>> listeners = new ArrayList<>();
	protected final Consumer<TransactionContext> rollbackHandler = this::handleRollback;

	protected abstract void handleRollback(TransactionContext context);

	@Override
	public void startListening(Consumer<? super ArticleView> listener, Object connection, Predicate<? super ArticleView> articleFilter) {
		listeners.add(listener);

		this.forEach(v -> {
			listener.accept(v);
			return true;
		});
	}

	@Override
	public void stopListening(Consumer<? super ArticleView> listener) {
		listeners.remove(listener);
	}

	<T extends ArticleView> void notifyListeners(T article) {
		final List<Consumer<? super ArticleView>> listeners = this.listeners;

		final int limit = listeners.size();

		for (int i = 0; i < limit; i++) {
			listeners.get(i).accept(article);
		}
	}
}
