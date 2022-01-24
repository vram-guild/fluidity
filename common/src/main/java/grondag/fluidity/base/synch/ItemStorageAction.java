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

package grondag.fluidity.base.synch;

import org.jetbrains.annotations.ApiStatus.Experimental;

/**
 * Possible interactions with remote storage. Distinct from vanilla container interactions.
 */
@Experimental
public enum ItemStorageAction {
	/** move targeted stack to player's inventory. */
	QUICK_MOVE_STACK,

	/** move half of targeted item, up to half a stack, to player's inventory. */
	QUICK_MOVE_HALF,

	/** move one of targeted item to player's inventory. */
	QUICK_MOVE_ONE,

	/** if player has an empty hand or holds the target item, add one to held. */
	TAKE_ONE,

	/**
	 * if player has an empty hand, take half of targeted item, up to half a stack.
	 */
	TAKE_HALF,

	/** if player has an empty hand, take full stack of targeted item. */
	TAKE_STACK,

	/**
	 * if player holds a stack, deposit one of it into storage. target is
	 * ignored/can be null
	 */
	PUT_ONE_HELD,

	/**
	 * if player holds a stack, deposit all of it into storage. target is
	 * ignored/can be null
	 */
	PUT_ALL_HELD;
}
