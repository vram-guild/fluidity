/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.fluidity.impl;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.IdentityHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import grondag.fluidity.api.transact.Transaction;
import grondag.fluidity.api.transact.TransactionContext;
import grondag.fluidity.api.transact.Transactor;
import net.minecraft.server.MinecraftServer;

public final class TransactionImpl implements Transaction {

	public final class ContextImpl implements TransactionContext {
		private ContextImpl() {
		}

		@Override
		public <T> void setState(T state) {
			stateStorage.put(contextContainer, state);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getState() {
			return (T) stateStorage.get(contextContainer);
		}

		@Override
		public boolean isCommited() {
			return isCommited;
		}
	}

	private final ContextImpl context = new ContextImpl();
	private boolean isOpen = true;
	private boolean isCommited = false;
	private final IdentityHashMap<Transactor, Consumer<TransactionContext>> participants = new IdentityHashMap<>();
	private final IdentityHashMap<Transactor, Object> stateStorage = new IdentityHashMap<>();
	private Transactor contextContainer;

	private TransactionImpl() {
	}

	@Override
	public void close() {
		if (isOpen) {
			rollback();
		}
	}

	private void clear() {
		participants.clear();
		stateStorage.clear();
		contextContainer = null;
		isOpen = false;
		isCommited = false;
	}

	private void validate() {
		// TODO: consider custom exceptions or at least revisit these
		if (!isOpen) {
			throw new IllegalStateException("Encountered transaction operation for closed transaction.");
		}
		if (STACK.get(stackPointer) != this) {
			throw new IndexOutOfBoundsException("Transaction operations must apply to most recent open transaction.");
		}
		if (!innerLock.isHeldByCurrentThread()) {
			throw new ConcurrentModificationException("Attempt to modify transaction status from foreign thread");
		}
	}

	@Override
	public void rollback() {
		close(false);
	}

	@Override
	public void commit() {
		close(true);
	}

	private void close(boolean isCommited) {
		validate();
		this.isCommited = isCommited;
		participants.forEach((c, r) -> {
			contextContainer = c;
			r.accept(context);
		});
		clear();

		final boolean root = --stackPointer == -1;

		innerLock.unlock();

		// non-server threads have an additional lock we must release.
		if (Thread.currentThread() != serverThread) {
			outerLock.unlock();
			if (root) {
				// Give other threads (like the server thread) that want to do transactions a
				// chance to jump in.
				Thread.yield();
			}
		}
	}

	@Override
	public <T extends Transactor> T enlist(T container) {
		validate();
		if (!participants.containsKey(container)) {
			contextContainer = container;
			participants.put(container, defaultRollback(container.prepareRollback(context)));
			contextContainer = null;
		}
		return container;
	}

	private Consumer<TransactionContext> defaultRollback(Consumer<TransactionContext> consumer) {
		return consumer;
	}

	///// STATIC MEMBERS FOLLOW /////
	public static void setServerThread(MinecraftServer server) {
		serverThread = Thread.currentThread();
	}

	private static Thread serverThread;

	/** Held by thread of active transaction, regardless of thread */
	static final ReentrantLock innerLock = new ReentrantLock();

	/**
	 * Must be held if thread is not the server thread - ensures non-server threads
	 * contend with each other before contending with server thread itself.
	 */
	static final ReentrantLock outerLock = new ReentrantLock();

	private static final ArrayList<TransactionImpl> STACK = new ArrayList<>();
	private static int stackPointer = -1;

	public static TransactionImpl open() {
		// non-server threads must take turns
		if (Thread.currentThread() != serverThread) {
			outerLock.lock();
		}
		innerLock.lock();

		final TransactionImpl result;
		if (STACK.size() > ++stackPointer) {
			result = STACK.get(stackPointer);
			result.isOpen = true;
		} else {
			assert STACK.size() == stackPointer;
			result = new TransactionImpl();
			STACK.add(result);
		}
		return result;
	}
}
