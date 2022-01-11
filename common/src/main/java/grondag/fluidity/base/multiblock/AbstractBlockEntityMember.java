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

import java.util.function.Function;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus.Experimental;
import grondag.fluidity.api.multiblock.MultiBlock;

@Experimental
public abstract class AbstractBlockEntityMember<T extends AbstractBlockEntityMember<T, U, V, B>, U extends MultiBlock<T, U, V>, V, B extends BlockEntity> extends AbstractMember<T, U, V> {
	protected final B blockEntity;
	protected final Function<B, V> componentFunction;

	public AbstractBlockEntityMember(B blockEntity, Function<B, V> componentFunction) {
		this.blockEntity = blockEntity;
		this.componentFunction = componentFunction;
	}

	@Override
	public long getPackedPos() {
		return blockEntity.getBlockPos().asLong();
	}

	@Override
	public Level getWorld() {
		return blockEntity.getLevel();
	}

	@Override
	public V getMemberComponent() {
		return componentFunction.apply(blockEntity);
	}
}
