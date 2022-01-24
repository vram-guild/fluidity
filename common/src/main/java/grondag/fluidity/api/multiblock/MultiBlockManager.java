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

package grondag.fluidity.api.multiblock;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.Experimental;

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
@Experimental
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
