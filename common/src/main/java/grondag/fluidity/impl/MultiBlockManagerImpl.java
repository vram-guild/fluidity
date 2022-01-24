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

package grondag.fluidity.impl;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import grondag.fluidity.api.multiblock.MultiBlock;
import grondag.fluidity.api.multiblock.MultiBlockManager;
import grondag.fluidity.api.multiblock.MultiBlockMember;

@Experimental
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MultiBlockManagerImpl<T extends MultiBlockMember<T, U, V>, U extends MultiBlock<T, U, V>, V> implements MultiBlockManager<T, U, V> {
	@SuppressWarnings("serial")
	private class WorldHandler extends Long2ObjectOpenHashMap<T> {
		private final Object2LongOpenHashMap<T> reverseMap = new Object2LongOpenHashMap<>();

		private final Object2BooleanOpenHashMap<T> requests = new Object2BooleanOpenHashMap<>();

		boolean didRequestTick = false;

		private void firstTick() {
			clear();
			reverseMap.clear();
		}

		private void process() {
			didRequestTick = false;

			for (final it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry<T> e : requests.object2BooleanEntrySet()) {
				if (e.getBooleanValue()) {
					doConnect(e.getKey());
				} else {
					doDisconnect(e.getKey());
				}
			}

			requests.clear();
		}

		private void request(T device, boolean status) {
			requests.put(device, status);

			if (!didRequestTick) {
				didRequestTick = true;
				TICK_REQUESTS.add(this);
			}
		}

		private void doConnect(T device) {
			if (FluidityConfig.TRACE_DEVICE_CONNECTIONS) {
				Fluidity.trace("Device connection request from %s @ %s", device.toString(), device.getBlockPos().toString());
			}

			final long pos = device.getPackedPos();
			final T prior = put(pos, device);

			if (prior != null) {
				// TODO handle pathological case
				Fluidity.LOG.warn("Device already exists on connect.");
			}

			reverseMap.put(device, pos);

			tryPairing(device, get(BlockPos.offset(pos, 1, 0, 0)));
			tryPairing(device, get(BlockPos.offset(pos, -1, 0, 0)));
			tryPairing(device, get(BlockPos.offset(pos, 0, 1, 0)));
			tryPairing(device, get(BlockPos.offset(pos, 0, -1, 0)));
			tryPairing(device, get(BlockPos.offset(pos, 0, 0, 1)));
			tryPairing(device, get(BlockPos.offset(pos, 0, 0, -1)));
		}

		private void tryPairing(T fromDevice, @Nullable T toDevice) {
			if (toDevice == null) {
				return;
			}

			if (!connectionTest.test(fromDevice, toDevice)) {
				return;
			}

			final U fromOwner = fromDevice.getMultiblock();
			final U toOwner = toDevice.getMultiblock();

			if (fromOwner == null) {
				if (toOwner == null) {
					// form new compound device
					final U newOwner = compoundSupplier.get();

					if (FluidityConfig.TRACE_DEVICE_CONNECTIONS) {
						Fluidity.trace("New compound device %s from %s @ %s and %s @ %s", newOwner.toString(), fromDevice.toString(), fromDevice.getBlockPos().toString(), toDevice.toString(), toDevice.getBlockPos().toString());
					}

					fromDevice.setMultiblock(newOwner);
					newOwner.add(fromDevice);
					toDevice.setMultiblock(newOwner);
					newOwner.add(toDevice);
				} else {
					// join to device compound
					if (FluidityConfig.TRACE_DEVICE_CONNECTIONS) {
						Fluidity.trace("Compound device %s added %s @ %s", toOwner.toString(), fromDevice.toString(), fromDevice.getBlockPos().toString());
					}

					fromDevice.setMultiblock(toOwner);
					toOwner.add(fromDevice);
				}
			} else if (fromOwner == toOwner) {
				// already joined
				if (FluidityConfig.TRACE_DEVICE_CONNECTIONS) {
					Fluidity.trace("Device connection ignored: %s already associated with %s @ %s", fromOwner.toString(), fromDevice.toString(), fromDevice.getBlockPos().toString());
				}
			} else if (toOwner == null) {
				// to device joins from device compound
				if (FluidityConfig.TRACE_DEVICE_CONNECTIONS) {
					Fluidity.trace("Compound device %s added %s @ %s", fromOwner.toString(), toDevice.toString(), toDevice.getBlockPos().toString());
				}

				toDevice.setMultiblock(fromOwner);
				fromOwner.add(toDevice);
			} else {
				// two different non-null compound devices - must merge one of them
				if (fromOwner.memberCount() > toOwner.memberCount()) {
					if (FluidityConfig.TRACE_DEVICE_CONNECTIONS) {
						Fluidity.trace("Merging compound device %s from device %s @ %s into comound device %s from device %s @ %s",
							toOwner, toDevice.toString(), toDevice.getBlockPos().toString(), fromOwner, fromDevice.toString(), fromDevice.getBlockPos().toString());
					}

					handleMerge(toOwner, fromOwner);
				} else {
					if (FluidityConfig.TRACE_DEVICE_CONNECTIONS) {
						Fluidity.trace("Merging compound device %s from device %s @ %s into comound device %s from device %s @ %s",
							fromOwner, fromDevice.toString(), fromDevice.getBlockPos().toString(), toOwner, toDevice.toString(), toDevice.getBlockPos().toString());
					}

					handleMerge(fromOwner, toOwner);
				}
			}
		}

		private void handleMerge(U victim, U survivor) {
			victim.removalAllAndClose(d -> {
				d.setMultiblock(survivor);
				survivor.add(d);
			});
		}

		private void doDisconnect(T device) {
			if (FluidityConfig.TRACE_DEVICE_CONNECTIONS) {
				Fluidity.trace("Device disconnect request from %s @ %s", device.toString(), device.getBlockPos().toString());
			}

			final long pos = reverseMap.removeLong(device);
			final T prior = remove(pos);

			if (prior != device) {
				// TODO handle pathological case
				Fluidity.LOG.warn("Device not found on disconnect.");
				return;
			}

			final U owner = device.getMultiblock();

			if (owner == null) {
				// not connected to anything
				return;
			}

			// look for neighboring connections
			neighbors.clear();
			addNeighbor(owner, get(BlockPos.offset(pos, 1, 0, 0)));
			addNeighbor(owner, get(BlockPos.offset(pos, -1, 0, 0)));
			addNeighbor(owner, get(BlockPos.offset(pos, 0, 1, 0)));
			addNeighbor(owner, get(BlockPos.offset(pos, 0, -1, 0)));
			addNeighbor(owner, get(BlockPos.offset(pos, 0, 0, 1)));
			addNeighbor(owner, get(BlockPos.offset(pos, 0, 0, -1)));

			if (FluidityConfig.TRACE_DEVICE_CONNECTIONS) {
				Fluidity.trace("Device %s @ %s removed from compound device %s", device.toString(), device.getBlockPos().toString(), owner.toString());
			}

			owner.remove(device);
			device.setMultiblock(null);

			if (neighbors.size() > 1) {
				// if part of a compound device and has more than one neighbor,
				// then any neighbors that are not connected via a different path must be split
				handleComplicatedSplit(owner, pos);
			}

			// if we are next to last one out, close up shop
			if (owner.memberCount() == 1) {
				owner.removalAllAndClose(d -> d.setMultiblock(null));
			} else if (owner.memberCount() == 0) {
				owner.close();
			}
		}

		private void addNeighbor(U owner, @Nullable T neighbor) {
			if (neighbor != null && neighbor.getMultiblock() == owner) {
				neighbors.add(neighbor);
			}
		}

		private void handleComplicatedSplit(U owner, long pos) {
			if (FluidityConfig.TRACE_DEVICE_CONNECTIONS) {
				Fluidity.trace("Compound Device %s requires complicated split due to removal of device @ %s", owner.toString(), BlockPos.of(pos).toString());
			}

			splitIndex = 0;
			Arrays.fill(splits, UNDETERMINED);
			visited.clear();

			for (int i = 0; i < 6; i++) {
				splitDevices[i].clear();
			}

			final int limit = neighbors.size();

			for (int i = 0; i < limit; i++) {
				if (splits[i] == UNDETERMINED) {
					splits[i] = splitIndex;
					visitFrom(owner, neighbors.get(i));

					// mark any subsequent siblings that were visited as part of the search that just happened
					if (i < limit - 1) {
						for (int j = i + 1; j < limit; j++) {
							final long nPos = neighbors.get(j).getPackedPos();

							if (splits[j] == UNDETERMINED && visited.contains(nPos)) {
								splits[j] = splitIndex;
							}
						}
					}

					splitIndex++;
				}
			}

			if (splitIndex > 1) {
				// at least two disconnected groups now, so must split off new compound devices
				if (FluidityConfig.TRACE_DEVICE_CONNECTIONS) {
					Fluidity.trace("Compound Device %s was split into %d devices, including original", owner.toString(), splitIndex);
				}

				// keep the largest one (first if tied) as existing compound device
				int winningSize = splitDevices[0].size();

				for (int i = 1; i < splitIndex; i++) {
					winningSize = Math.max(winningSize, splitDevices[i].size());
				}

				for (int i = 0; i < splitIndex; i++) {
					if (splitDevices[i].size() == winningSize) {
						winningSize = -1; // breaks ties - first wins
						continue;
					}

					handleSplit(owner, i);
				}
			}
		}

		private void handleSplit(U owner, int index) {
			final int count = splitDevices[index].size();

			if (count == 1) {
				final MultiBlockMember d = splitDevices[index].get(0);
				owner.remove((T) d);
				d.setMultiblock(null);
			} else if (count > 1) {
				final U newOwner = compoundSupplier.get();

				for (final MultiBlockMember d : splitDevices[index]) {
					owner.remove((T) d);
					d.setMultiblock(newOwner);
					newOwner.add((T) d);
				}
			}
		}

		private void visitFrom(U owner, MultiBlockMember sibling) {
			final long pos = sibling.getPackedPos();
			visited.add(pos);
			splitDevices[splitIndex].add(sibling);
			searchStack.add(pos);

			while (!searchStack.isEmpty()) {
				final long p = searchStack.popLong();
				visit(owner, BlockPos.offset(p, 1, 0, 0));
				visit(owner, BlockPos.offset(p, -1, 0, 0));
				visit(owner, BlockPos.offset(p, 0, 1, 0));
				visit(owner, BlockPos.offset(p, 0, -1, 0));
				visit(owner, BlockPos.offset(p, 0, 0, 1));
				visit(owner, BlockPos.offset(p, 0, 0, -1));
			}
		}

		private void visit(U owner, long pos) {
			if (!visited.contains(pos)) {
				final T d = get(pos);

				if (d != null && d.getMultiblock() == owner) {
					splitDevices[splitIndex].add(d);
					searchStack.add(pos);
				}

				visited.add(pos);
			}
		}
	}

	private final IdentityHashMap<Level, WorldHandler> worlds = new IdentityHashMap<>();

	private final Supplier<U> compoundSupplier;
	private final BiPredicate<T, T> connectionTest;

	public static <T extends MultiBlockMember<T, U, V>, U extends MultiBlock<T, U, V>, V> MultiBlockManager<T, U, V> create(Supplier<U> compoundSupplier, BiPredicate<T, T> connectionTest) {
		return new MultiBlockManagerImpl(compoundSupplier, connectionTest);
	}

	private MultiBlockManagerImpl(Supplier<U> compoundSupplier, BiPredicate<T, T> connectionTest) {
		this.compoundSupplier = compoundSupplier;
		this.connectionTest = connectionTest;
		MANAGERS.add(new WeakReference(this));
	}

	private WorldHandler worldHandler(Level world) {
		return worlds.computeIfAbsent(world, d -> new WorldHandler());
	}

	@Override
	public void connect(T device) {
		worldHandler(device.getWorld()).request(device, true);
	}

	@Override
	public void disconnect(T device) {
		worldHandler(device.getWorld()).request(device, false);
	}

	private static final ObjectArrayList<MultiBlockMember> neighbors = new ObjectArrayList<>();

	private static int splitIndex = 0;
	private static final int[] splits = new int[6];
	private static final ObjectArrayList<MultiBlockMember>[] splitDevices = new ObjectArrayList[6];

	static {
		for (int i = 0; i < 6; i++) {
			splitDevices[i] = new ObjectArrayList<>();
		}
	}

	private static final int UNDETERMINED = -1;

	private static final LongOpenHashSet visited = new LongOpenHashSet();

	private static final LongArrayList searchStack = new LongArrayList();

	// PERF: if instances are to be held as static final by their creator, why do we need weak references?
	private static final ObjectArrayList<WeakReference<MultiBlockManagerImpl>> MANAGERS = new ObjectArrayList<>();

	private static final ObjectArrayList<MultiBlockManagerImpl.WorldHandler> TICK_REQUESTS = new ObjectArrayList<>();

	public static void tick(MinecraftServer server) {
		if (TICK_REQUESTS.isEmpty()) {
			return;
		}

		// TODO: make this properly concurrent or simplify it
		final Object[] elements = TICK_REQUESTS.elements();
		final int limit = Math.min(elements.length, TICK_REQUESTS.size());

		for (int i = 0; i < limit; i++) {
			final MultiBlockManagerImpl.WorldHandler e = (MultiBlockManagerImpl.WorldHandler) elements[i];

			if (e == null) {
				break;
			}

			e.process();
		}

		TICK_REQUESTS.clear();
	}

	public static void start(MinecraftServer server) {
		final Iterator<WeakReference<MultiBlockManagerImpl>> it = MANAGERS.iterator();

		while (it.hasNext()) {
			final MultiBlockManagerImpl m = it.next().get();

			if (m == null) {
				it.remove();
			} else {
				m.worlds.values().forEach(w -> ((MultiBlockManagerImpl.WorldHandler) w).firstTick());
			}
		}
	}
}
