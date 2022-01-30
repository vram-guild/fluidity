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

package grondag.fluidity.api.transact;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;

/**
 * Implement on objects that can participate in transactions.
 *
 * @see <a href="https://github.com/grondag/fluidity#transactions">https://github.com/grondag/fluidity#transactions</a>
 */
@FunctionalInterface
@Experimental
public interface TransactionParticipant {
	/**
	 * Override for implementations that want to enlist lazily.
	 * When true, calls to {@link Transaction#enlist(TransactionParticipant)} will
	 * be ignored and implementation should instead call {@link Transaction#enlistSelf(TransactionParticipant)}
	 * when ready to enlist.
	 *
	 * @return {@code true} for implementations that will self-enlist.
	 */
	default boolean isSelfEnlisting() {
		return false;
	}

	/**
	 * Allows instances that share the same rollback state to share a delegate.
	 * If the same delegate is enlisted more than once, it will only be asked to prepare
	 * rollback the first time.
	 *
	 * <p>The delegate uniquely identifies a participant (or a group of participants that share
	 * rollback state). A participant (or group of them) must have exactly one delegate that is
	 * used in all transactions at any level of nesting.
	 *
	 * @return the transaction delegate for this participant
	 */
	TransactionDelegate getTransactionDelegate();

	@FunctionalInterface
	public interface TransactionDelegate {
		/**
		 * Called on enlisting to signal saving of rollback state or whatever
		 * preparation is appropriate for the participating implementation.
		 * Will be called only once per transaction (including nested transactions).
		 *
		 * <p>Consumer is called for both commit and rollback events just in case some
		 * implementations need to lock or store resources internally during a
		 * transaction and need notification when one ends.
		 *
		 * <p>If a nested transaction is commited and the participant owning this delegate
		 * does not already have a delegate in the outer transaction, then the participant
		 * and it's delegate will be moved to the outer tranaction and calls to the consumer
		 * will be deferred until there are no more unresolved outer transactions or another
		 * delegate is available in an outer transaction.
		 *
		 * @param The transaction context - use to store and retrieve transaction state and query status at close
		 * @return The action (as a {@code Consumer}) to be run when the transaction is closed
		 */
		Consumer<TransactionContext> prepareRollback(TransactionContext context);

		/**
		 * Specialized transaction delegate that does nothing. Use as the delegate
		 * for participants without any internal state to be rolled back.
		 */
		TransactionDelegate IGNORE = c0 -> c1 -> { };
	}
}
