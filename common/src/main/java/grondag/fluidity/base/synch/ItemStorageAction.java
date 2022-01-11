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
package grondag.fluidity.base.synch;

import org.jetbrains.annotations.ApiStatus.Experimental;

/**
 * Possible interactions with remote storage. Distinct from vanilla container interactions.
 */
@Experimental
public enum ItemStorageAction {
	/** move targeted stack to player's inventory */
	QUICK_MOVE_STACK,

	/** move half of targeted item, up to half a stack, to player's inventory */
	QUICK_MOVE_HALF,

	/** move one of targeted item to player's inventory */
	QUICK_MOVE_ONE,

	/** if player has an empty hand or holds the target item, add one to held */
	TAKE_ONE,

	/**
	 * if player has an empty hand, take half of targeted item, up to half a stack
	 */
	TAKE_HALF,

	/** if player has an empty hand, take full stack of targeted item */
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
