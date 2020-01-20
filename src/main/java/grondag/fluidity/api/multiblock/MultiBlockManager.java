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
package grondag.fluidity.api.multiblock;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.impl.MultiBlockManagerImpl;

/**
 * Service interface and factory for Fluidity's multi-block manager implementation.
 * Do not implement this interface.
 *
 * @param <T> member type
 * @param <U> multi block type
 * @param <V> member component type (arbitrary)
 *
 * @see <a href="https://github.com/grondag/fluidity#multiblocks">https://github.com/grondag/fluidity#multiblocks</a>
 */
@API(status = Status.EXPERIMENTAL)
public interface MultiBlockManager<T extends MultiBlockMember<T, U, V>, U extends MultiBlock<T, U, V>, V> {
	/**
	 * Call to notify the manager when a member is added to the world.
	 * The member will be automatically joined to adjacent members according to the predicate
	 * function provided when the manager was created. Call only from the main server thread.
	 *
	 * @param member the member that was added to the world
	 */
	void connect(T member);

	/**
	 * Call to notify the manager when a member is removed from the world.
	 * The member will be automatically removed from the multiblock it belongs to, if any
	 * and the multiblock will be split if needed. Call only from the main server thread.
	 *
	 * @param member the member being removed from the world
	 */
	void disconnect(T member);

	/**
	 * Call to construct a new multi-block manager. The instance should be retained as {@code static final.}
	 * The manager will handle multi-blocks in all server worlds but methods must only be invoked from the main server thread.
	 *
	 * @param <T> member type
	 * @param <U> multi block type
	 * @param <V> member component type (arbitrary)
	 * @param multiBlockFactory supplier for new multiblock instances
	 * @param connectionTest function to determine when two adjacent members in the same world should connect to form a multiblock
	 * @return a new multiblock manager instance
	 */
	static <T extends MultiBlockMember<T, U, V>, U extends MultiBlock<T, U, V>, V> MultiBlockManager<T, U, V> create(Supplier<U> multiBlockFactory, BiPredicate<T, T> connectionTest) {
		return MultiBlockManagerImpl.create(multiBlockFactory, connectionTest);
	}
}
