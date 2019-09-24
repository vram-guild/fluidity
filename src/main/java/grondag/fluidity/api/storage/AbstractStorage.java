package grondag.fluidity.api.storage;

import java.util.function.Consumer;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class AbstractStorage<T, U, V extends ArticleView<T>> implements Storage<T, U, V> {
	protected ObjectArrayList<Consumer<V>> listeners;
	
	@Override
	public void startListening(Consumer<V> listener, U connection, Predicate<V> articleFilter) {
		if (listeners == null) {
			listeners = new ObjectArrayList<>();
		}
		listeners.add(listener);
		this.forEach(v -> {
			listener.accept(v);
			return true;
		});
	}

	@Override
	public void stopListening(Consumer<V> listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	protected void notifyListeners(V article) {
		if (this.listeners != null) {
			final int limit = listeners.size();
			for (int i = 0; i < limit; i++) {
				listeners.get(i).accept(article);
			}
		}
	}
}
