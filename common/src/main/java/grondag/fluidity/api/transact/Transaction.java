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

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import grondag.fluidity.impl.TransactionImpl;

/**
 * Represents a aggregate operation involving one or
 * more participants in which participants guarantee the
 * entire operation will be atomic.
 *
 * <p>Changes in participant state are immediately effective and
 * visible as they happen, but all participants will roll back
 * all changes that happened after this transaction started if the
 * transaction is closed without calling {@link #commit()}.
 *
 * @see <a href="https://github.com/grondag/fluidity#transactions">https://github.com/grondag/fluidity#transactions</a>
 */
@Experimental
public interface Transaction extends AutoCloseable {
	/**
	 * Close the transaction and notify all participants to reverse all state changes
	 * that happened after this transaction was opened.
	 */
	void rollback();

	/**
	 * Close the transaction and notify all participants to retain all state changes
	 * that happened after this transaction was opened.  If the transaction is nested
	 * then participants without a checkpoint on the outer transaction will not receive
	 * a commit notification until the outer transaction is committed.
	 */
	void commit();

	/**
	 * Add the participant to this transaction if it is not already enlisted and
	 * is not self-enrolling.
	 *
	 * @param <T> identifies the specific type of the participant
	 * @param participant the participant to be enrolled if appropriate
	 * @return the participant
	 */
	default <T extends TransactionParticipant> T enlist(T participant) {
		if (!participant.isSelfEnlisting()) {
			return enlistSelf(participant);
		} else {
			return participant;
		}
	}

	/**
	 * Add the participant to this transaction if it is not already enlisted.
	 *
	 * <p>Should only be called by participants that are self-enrolling!
	 *
	 * @param <T> identifies the specific type of the participant
	 * @param participant  the participant to be enrolled
	 * @return the participant to be enrolled if appropriate
	 */
	<T extends TransactionParticipant> T enlistSelf(T participant);

	/**
	 * Closes this transaction as if {@link #rollback()} were called,
	 * unless {@link #commit()} was called first. Has no effect if the
	 * transaction is already closed.
	 */
	@Override
	void close();

	/**
	 * Creates a new transaction.  Should always be called from a try-with-resource block.
	 *
	 * <p>If a transaction is already open, the behavior of this call varies depending on the thread:
	 *
	 * <p>If called from the same thread that opened the current transaction, the new transaction
	 * will be "nested" or enclosed in the existing transaction.  A nested transaction will
	 * be rolled back if the enclosing transaction is rolled back, even if the nested transaction
	 * was successfully committed.
	 *
	 * <p>When a nested transaction is closed, the enclosing transaction will again become the "current"
	 * transaction and if the nested transaction was rolled back the state of the enclosing
	 * transaction will be the same as it was prior to opening the nested transaction.
	 *
	 * <p>If called from a different thread than the current transaction, this method will block
	 * until all current transactions on the other thread are closed, and then return a new,
	 * root-level transaction.
	 *
	 * @return a new transaction
	 */
	static Transaction open() {
		return TransactionImpl.open();
	}

	/**
	 * Retrieves the current open transaction at the deepest level of nesting, or null if
	 * no transaction is currently open.
	 *
	 * @return the current open transaction
	 */
	static @Nullable Transaction current() {
		return TransactionImpl.current();
	}

	/**
	 * Enlists the given participant in the current open transaction if there is one,
	 * or does nothing if no transaction is open.  If the participant is already
	 * enlisted or is self-enlisting, this has no effect even when a transaction is open.
	 *
	 * @param participant the participant to be enrolled
	 */
	static void enlistIfOpen(TransactionParticipant participant) {
		final Transaction tx = current();

		if (tx != null) {
			tx.enlist(participant);
		}
	}

	/**
	 * Self-enlists the given participant in the current open transaction if there is one,
	 * or does nothing if no transaction is open.  If the participant is already
	 * enlisted, this has no effect even when a transaction is open. Use for self-enlisting implementations.
	 *
	 * @param participant the participant to be enrolled
	 */
	static void selfEnlistIfOpen(TransactionParticipant participant) {
		final Transaction tx = current();

		if (tx != null) {
			tx.enlistSelf(participant);
		}
	}
}
