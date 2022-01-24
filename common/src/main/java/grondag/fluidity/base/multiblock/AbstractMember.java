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

package grondag.fluidity.base.multiblock;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.multiblock.MultiBlock;
import grondag.fluidity.api.multiblock.MultiBlockMember;

@Experimental
public abstract class AbstractMember<T extends AbstractMember<T, U, V>, U extends MultiBlock<T, U, V>, V> implements MultiBlockMember<T, U, V> {
	protected U owner;

	protected abstract void beforeOwnerRemoval();

	protected abstract void afterOwnerAddition();

	@Override
	public U getMultiblock() {
		return owner;
	}

	@Override
	public void setMultiblock(U owner) {
		if (owner == this.owner) {
			return;
		}

		if (this.owner != null) {
			beforeOwnerRemoval();
		}

		this.owner = owner;

		if (owner != null) {
			afterOwnerAddition();
		}
	}
}
