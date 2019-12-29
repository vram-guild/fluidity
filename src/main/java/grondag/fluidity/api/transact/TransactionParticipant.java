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
/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grondag.fluidity.api.transact;

import java.util.function.Consumer;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Implement on objects that can participate in transactions.
 */
@FunctionalInterface
@API(status = Status.EXPERIMENTAL)

public interface TransactionParticipant {
	/**
	 * Allows instances that share the same rollback state to share a delegate.
	 * If the same delegate is enlisted more than once, will only be asked to prepare rollback once.
	 */
	TransactionDelegate getTransactionDelegate();


	@FunctionalInterface
	public interface TransactionDelegate {
		/**
		 * Consumer can save state in the context if it needs to and retrieve it on rollback.<p>
		 *
		 * Consumer is called for both commit and rollback events just in case some
		 * implementation need to lock or store resources internally during a
		 * transaction and need notification when one ends. <p>
		 *
		 * @param context
		 * @return
		 */
		Consumer<TransactionContext> prepareRollback(TransactionContext context);

		TransactionDelegate IGNORE = c0 -> c1 -> {};
	}
}
