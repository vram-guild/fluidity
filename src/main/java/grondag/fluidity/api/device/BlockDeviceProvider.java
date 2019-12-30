package grondag.fluidity.api.device;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

@API(status = Status.EXPERIMENTAL)
@FunctionalInterface
public interface BlockDeviceProvider {
	Device getDevice(BlockState blockState, IWorld iWorld, BlockPos blockPos);

	static Device get(BlockState blockState, IWorld iWorld, BlockPos blockPos) {
		final Block block = blockState.getBlock();

		return block instanceof BlockDeviceProvider ? ((BlockDeviceProvider)block).getDevice(blockState, iWorld, blockPos) : Device.EMPTY;
	}
}
