/*******************************************************************************
 * Copyright 2019, 2020 grondag
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
package grondag.fluidity.base.transact;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.transact.TransactionContext;

@API(status = Status.EXPERIMENTAL)
public class LazyRollbackHandler {
	public  final Consumer<TransactionContext> externalHandler = this::handleExternal;
	protected final Supplier<Object> rollbackSupplier;
	protected final RollbackHandler rollbackHandler;

	private Object rollbackState = NO_TRANSACTION;

	public LazyRollbackHandler(Supplier<Object> rollbackSupplier, RollbackHandler rollbackHandler) {
		this.rollbackSupplier = rollbackSupplier;
		this.rollbackHandler = rollbackHandler;
	}

	public void prepareIfNeeded() {
		if (rollbackState == NOT_PREPARED) {
			rollbackState = rollbackSupplier.get();
		}
	}

	private void handleExternal(TransactionContext context) {
		if(rollbackState != NOT_PREPARED) {
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
