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

/**
 * Defines the object used to share data between a transaction and a specific participant.
 *
 * <p>Participants should never retain a reference - this instance may be reused for other participants.
 *
 * @see <a href="https://github.com/grondag/fluidity#transactions">https://github.com/grondag/fluidity#transactions</a>
 */
@Experimental
public interface TransactionContext {
	/**
	 * Use during {@link TransactionParticipant.TransactionDelegate#prepareRollback(TransactionContext)} to
	 * save rollback state in the transaction.  The state can be any type of instance or {@code null}. It
	 * will never be inspected or altered by the transaction.
	 *
	 * @param <T> Class of the state instance
	 * @param state the rollback state
	 */
	<T> void setState(T state);

	/**
	 * Use during when the consumer returned by {@link TransactionParticipant.TransactionDelegate#prepareRollback(TransactionContext)}
	 * is called to retrieve the rollback state that was earlier passed to {@link #setState(Object)}.
	 *
	 * @param <T> Class of the state instance
	 * @return the rollback state previously passed to {@link #setState(Object)}
	 */
	<T> T getState();

	/**
	 * Use during when the consumer returned by {@link TransactionParticipant.TransactionDelegate#prepareRollback(TransactionContext)}
	 * to test if the transaction is rolled back or committed. If rolled back, the participant must undo
	 * all state changes that happened during the transaction.
	 *
	 * @return {@code true} if the transaction was committed, false if it was rolled back on request or because of an exception
	 */
	boolean isCommited();
}
