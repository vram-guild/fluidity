/*
 * This file is part of Fluidity and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.fluidity.base.transact;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.transact.Transaction;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.TransactionParticipant;

@Experimental
public class LazyRollbackHandler {
	public final Consumer<TransactionContext> externalHandler = this::handleExternal;
	protected final Supplier<Object> rollbackSupplier;
	protected final RollbackHandler rollbackHandler;
	protected final TransactionParticipant participant;

	private Object rollbackState = NO_TRANSACTION;

	public LazyRollbackHandler(Supplier<Object> rollbackSupplier, RollbackHandler rollbackHandler, TransactionParticipant participant) {
		this.rollbackSupplier = rollbackSupplier;
		this.rollbackHandler = rollbackHandler;
		this.participant = participant;
	}

	public void prepareIfNeeded() {
		Transaction.selfEnlistIfOpen(participant);

		if (rollbackState == NOT_PREPARED) {
			rollbackState = rollbackSupplier.get();
		}
	}

	private void handleExternal(TransactionContext context) {
		if (rollbackState != NOT_PREPARED) {
			rollbackHandler.accept(rollbackState, context.isCommited());
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

	@FunctionalInterface
	public interface RollbackHandler {
		void accept(Object rollbackState, boolean isCommitted);
	}
}
