package grondag.fluidity.test;

import java.util.function.Consumer;

import grondag.fluidity.api.transact.Transaction;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.TransactionParticipant;
import grondag.fluidity.api.transact.TransactionParticipant.TransactionDelegate;

class TestParticipant implements TransactionParticipant, TransactionDelegate {
	private String value;

	TestParticipant(String value) {
		this.value = value;
	}

	void value(String val) {
		Transaction.selfEnlistIfOpen(this);
		this.value = val;
	}

	String value() {
		return value;
	}

	@Override
	public boolean isSelfEnlisting() {
		return true;
	}

	@Override
	public TransactionDelegate getTransactionDelegate() {
		return this;
	}

	@Override
	public Consumer<TransactionContext> prepareRollback(TransactionContext context) {
		context.setState(value);
		return this::handleClose;
	}

	private void handleClose(TransactionContext context) {
		if (!context.isCommited()) {
			value = context.getState();
		}
	}
}
