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
package grondag.fluidity.base.multiblock;

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.api.multiblock.MultiBlock;
import grondag.fluidity.api.multiblock.MultiBlockMember;

@API(status = Status.EXPERIMENTAL)
public abstract class AbstractMultiBlock<T extends MultiBlockMember<T, U, V>, U extends AbstractMultiBlock<T, U, V>, V> implements MultiBlock<T, U, V> {

	protected final ObjectOpenHashSet<T> members = new ObjectOpenHashSet<>();

	@Override
	public void add(T member) {
		members.add(member);
		afterMemberAddition(member);
	}

	protected abstract void afterMemberAddition(T member);

	@Override
	public void remove(T member) {
		beforeMemberRemoval(member);
		members.remove(member);
	}

	protected abstract void beforeMemberRemoval(T member);

	@Override
	public int memberCount() {
		return members.size();
	}

	@Override
	public void removalAllAndClose(Consumer<T> closeAction) {
		members.forEach(m -> {
			beforeMemberRemoval(m);
			closeAction.accept(m);
		});

		members.clear();
		close();
	}
}
