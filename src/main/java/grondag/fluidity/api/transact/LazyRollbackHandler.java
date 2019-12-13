package grondag.fluidity.api.transact;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LazyRollbackHandler {
	public  final Consumer<TransactionContext> externalHandler = this::handleExternal;
	protected final Supplier<Object> rollbackSupplier;
	protected final Consumer<Object> rollbackConsumer;

	private Object rollbackState = NO_TRANSACTION;

	public LazyRollbackHandler(Supplier<Object> rollbackSupplier, Consumer<Object> rollbackConsumer) {
		this.rollbackSupplier = rollbackSupplier;
		this.rollbackConsumer = rollbackConsumer;
	}

	public void prepareIfNeeded() {
		if (rollbackState == NOT_PREPARED) {
			rollbackState = rollbackSupplier.get();
		}
	}

	private void handleExternal(TransactionContext context) {
		if(!context.isCommited() && rollbackState != NOT_PREPARED) {
			rollbackConsumer.accept(rollbackState);
		}

		rollbackState = context.getState();
	}

	public Consumer<TransactionContext> prepareExternal(TransactionContext context) {
		context.setState(rollbackState);
		rollbackState = NOT_PREPARED;
		return externalHandler;
	}

	private static final Object NO_TRANSACTION = new Object();
	private static final Object NOT_PREPARED = new Object();
}
