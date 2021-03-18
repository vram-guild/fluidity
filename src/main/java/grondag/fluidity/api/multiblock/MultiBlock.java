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

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;

/**
 * Represents a collection of member blocks in a world.
 * See sub-types in base package for prototype implementations.
 *
 * @param <T> member type
 * @param <U> multi block type
 * @param <V> member component type (arbitrary)
 *
 * @see <a href="https://github.com/grondag/fluidity#multiblocks">https://github.com/grondag/fluidity#multiblocks</a>
 */
@Experimental
public interface MultiBlock<T extends MultiBlockMember<T, U, V>, U extends MultiBlock<T, U, V>, V> {
	/**
	 * Called when the multi block is destroyed because no members remain.
	 */
	default void close() {

	}

	/**
	 * Called when a member is added to this instance.
	 * The member will already be notified that it belongs to
	 * this instance before this method is called.
	 *
	 * @param member the member to be added to this instance
	 */
	void add(T member);

	/**
	 * Called when a member is removed from this instance.
	 * The member will be notified that it no longer belongs to
	 * this instance after this method returns.
	 *
	 * @param member the member being removed from this instance
	 */
	void remove(T member);

	/**
	 * Get the number of members currently in this instance.
	 *
	 * @return the number of members currently in this instance
	 */
	int memberCount();

	/**
	 * Applies the close action to all members, removes them from
	 * this instance and executes {@link #close()}. <p>
	 *
	 * Called when this instance is merged into another and this instance
	 * does not survive. May be called in other circumstances also.
	 *
	 * @param closeAction action to be applied to all members before removal
	 */
	void removalAllAndClose(Consumer<T> closeAction);
}
