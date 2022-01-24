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

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.fluidity.api.multiblock.MultiBlock;
import grondag.fluidity.api.multiblock.MultiBlockMember;

@Experimental
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
