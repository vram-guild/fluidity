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
package grondag.fluidity.base.multiblock;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.multiblock.MultiBlock;
import grondag.fluidity.api.multiblock.MultiBlockMember;

@API(status = Status.EXPERIMENTAL)
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
		if(owner == this.owner) {
			return;
		}

		if(this.owner != null) {
			beforeOwnerRemoval();
		}

		this.owner = owner;

		if(owner != null) {
			afterOwnerAddition();
		}
	}
}
