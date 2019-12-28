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
package grondag.fluidity.wip;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

@API(status = Status.EXPERIMENTAL)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CompoundDeviceManager<T extends CompoundDeviceMember<T, U>, U extends CompoundDevice<T, U>> {

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

			for(final it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry<T> e : requests.object2BooleanEntrySet()) {
				if(e.getBooleanValue() ) {
					doConnect(e.getKey());
				} else {
					doDisconnect(e.getKey());
				}
			}

			requests.clear();
		}

		private void request(T device, boolean status) {
			requests.put(device, status);

			if(!didRequestTick) {
				didRequestTick = true;
				TICK_REQUESTS.add(this);
			}
		}

		private void doConnect(T device) {
			final long pos = device.packedPos();
			final T prior = put(pos, device);

			if(prior != null) {
				// TODO handle pathological case
				System.out.println("u did a bad");
			}

			reverseMap.put(device, pos);

			tryPairing(device, get(BlockPos.add(pos, 1, 0, 0)));
			tryPairing(device, get(BlockPos.add(pos, -1, 0, 0)));
			tryPairing(device, get(BlockPos.add(pos, 0, 1, 0)));
			tryPairing(device, get(BlockPos.add(pos, 0, -1, 0)));
			tryPairing(device, get(BlockPos.add(pos, 0, 0, 1)));
			tryPairing(device, get(BlockPos.add(pos, 0, 0, -1)));
		}

		private void tryPairing(T fromDevice, @Nullable T toDevice) {
			if(toDevice == null) {
				return;
			}

			if(!connectionTest.test(fromDevice, toDevice)) {
				return;
			}

			final U fromOwner = fromDevice.getCompoundDevice();
			final U toOwner = toDevice.getCompoundDevice();

			if(fromOwner == null) {
				if(toOwner == null) {
					// form new compound device
					final U newOwner = compoundSupplier.get();
					fromDevice.setCompoundDevice(newOwner);
					newOwner.add(fromDevice);
					toDevice.setCompoundDevice(newOwner);
					newOwner.add(toDevice);
				} else {
					// join to device compound
					fromDevice.setCompoundDevice(toOwner);
					toOwner.add(fromDevice);
				}
			} else if (fromOwner == toOwner) {
				// already joined
				return;
			} else if (toOwner == null) {
				// to device joins from device compound
				toDevice.setCompoundDevice(fromOwner);
				fromOwner.add(toDevice);
			} else {
				// two different non-null compound devices - must merge one of them
				if(fromOwner.deviceCount() > toOwner.deviceCount()) {
					handleMerge(toOwner, fromOwner);
				} else {
					handleMerge(fromOwner, toOwner);
				}
			}
		}

		private void handleMerge(U victim, U survivor) {
			victim.removalAllAndClose(d -> {
				d.setCompoundDevice(survivor);
				survivor.add(d);
			});
		}

		private void doDisconnect(T device) {
			final long pos = reverseMap.removeLong(device);
			final T prior = remove(pos);

			if(prior != device) {
				// TODO handle pathological case
				System.out.println("u did a bad");
				return;
			}

			final U owner = device.getCompoundDevice();
			if(owner == null) {
				// not connected to anything
				return;
			}

			// look for neighboring connections
			neighbors.clear();
			addNeighbor(owner, get(BlockPos.add(pos, 1, 0, 0)));
			addNeighbor(owner, get(BlockPos.add(pos, -1, 0, 0)));
			addNeighbor(owner, get(BlockPos.add(pos, 0, 1, 0)));
			addNeighbor(owner, get(BlockPos.add(pos, 0, -1, 0)));
			addNeighbor(owner, get(BlockPos.add(pos, 0, 0, 1)));
			addNeighbor(owner, get(BlockPos.add(pos, 0, 0, -1)));

			owner.remove(device);
			device.setCompoundDevice(null);

			if (neighbors.size() > 1) {
				// if part of a compound device and has more than one neighbor,
				// then any neighbors that are not connected via a different path must be split
				handleComplicatedSplit(device, owner, pos);
			}

			// if we are next to last one out, close up shop
			if(owner.deviceCount() == 1) {
				owner.removalAllAndClose(d -> d.setCompoundDevice(null));
			} else if (owner.deviceCount() == 0) {
				owner.close();
			}
		}

		private void addNeighbor(U owner, @Nullable T neighbor) {
			if(neighbor != null && neighbor.getCompoundDevice() == owner) {
				neighbors.add(neighbor);
			}
		}

		private void handleComplicatedSplit(T device, U owner, long pos) {
			splitIndex = 0;
			Arrays.fill(splits, UNDETERMINED);
			visited.clear();

			for (int i = 0; i < 6; i++) {
				splitDevices[i].clear();
			}

			final int limit = neighbors.size();
			for(int i = 0; i < limit; i++) {
				if(splits[i] == UNDETERMINED) {
					splits[i] = splitIndex;
					visitFrom(owner, neighbors.get(i));

					// mark any subsequent siblings that were visited as part of the search that just happened
					if (i < limit - 1) {
						for(int j = i + 1; j < limit; j++) {
							final long nPos = neighbors.get(j).packedPos();
							if(splits[j] == UNDETERMINED && visited.contains(nPos)) {
								splits[j] = splitIndex;
							}
						}
					}

					splitIndex++;
				}
			}

			if(splitIndex > 1) {
				// at least two disconnected groups now, so must split off new compound devices

				// keep the largest one (first if tied) as existing compound device
				int winningSize = splitDevices[0].size();

				for(int i = 1; i < splitIndex; i++) {
					winningSize = Math.max(winningSize, splitDevices[i].size());
				}

				for(int i = 0; i < splitIndex; i++) {
					if(splitDevices[i].size() == winningSize) {
						winningSize = -1; // breaks ties - first wins
						continue;
					}

					handleSplit(owner, i);
				}
			}
		}

		private void handleSplit(U owner, int index) {
			final int count = splitDevices[index].size();

			if(count== 1) {
				final CompoundDeviceMember d  = splitDevices[index].get(0);
				owner.remove((T) d);
				d.setCompoundDevice(null);
			} else if(count > 1) {
				final U newOwner = compoundSupplier.get();

				for(final CompoundDeviceMember d : splitDevices[index]) {
					owner.remove((T) d);
					d.setCompoundDevice(newOwner);
					newOwner.add((T) d);
				}
			}
		}

		private void visitFrom(U owner, CompoundDeviceMember sibling) {
			final long pos = sibling.packedPos();
			visited.add(pos);
			splitDevices[splitIndex].add(sibling);
			searchStack.add(pos);

			while(!searchStack.isEmpty()) {
				final long p = searchStack.popLong();
				visit(owner, BlockPos.add(p, 1, 0, 0));
				visit(owner, BlockPos.add(p, -1, 0, 0));
				visit(owner, BlockPos.add(p, 0, 1, 0));
				visit(owner, BlockPos.add(p, 0, -1, 0));
				visit(owner, BlockPos.add(p, 0, 0, 1));
				visit(owner, BlockPos.add(p, 0, 0, -1));
			}
		}

		private void visit(U owner, long pos) {
			if(!visited.contains(pos)) {
				final T d = get(pos);

				if(d != null && d.getCompoundDevice() == owner) {
					visited.add(pos);
					splitDevices[splitIndex].add(d);
					searchStack.add(pos);
				} else {
					visited.add(pos);
				}
			}
		}
	}

	private final Int2ObjectOpenHashMap<WorldHandler> worlds = new Int2ObjectOpenHashMap<>();

	private final Supplier<U> compoundSupplier;
	private final BiPredicate<T, T> connectionTest;

	public CompoundDeviceManager(Supplier<U> compoundSupplier, BiPredicate<T, T> connectionTest) {
		this.compoundSupplier = compoundSupplier;
		this.connectionTest = connectionTest;
		MANAGERS.add(new WeakReference(this));
	}

	private WorldHandler world(int dimensionId) {
		return worlds.computeIfAbsent(dimensionId, d -> new WorldHandler());
	}

	public void connect(T device) {
		world(device.dimensionId()).request(device, true);
	}

	public void disconnect(T device) {
		world(device.dimensionId()).request(device, false);
	}

	private static final ObjectArrayList<CompoundDeviceMember> neighbors = new ObjectArrayList<>();

	private static int splitIndex = 0;
	private static final int[] splits = new int[6];
	private static final ObjectArrayList<CompoundDeviceMember>[] splitDevices = new ObjectArrayList[6];
	static {
		for (int i = 0; i < 6; i++) {
			splitDevices[i] = new ObjectArrayList<>();
		}
	}
	private static final int UNDETERMINED = -1;

	private static final LongOpenHashSet visited = new LongOpenHashSet();

	private static final LongArrayList searchStack = new LongArrayList();

	private static final ObjectArrayList<WeakReference<CompoundDeviceManager>> MANAGERS = new ObjectArrayList<>();

	private static final ObjectArrayList<CompoundDeviceManager.WorldHandler> TICK_REQUESTS = new ObjectArrayList<>();

	public static void tick(MinecraftServer server) {
		if(TICK_REQUESTS.isEmpty()) {
			return;
		}

		// TODO: make this properly concurrent or simplify it
		final Object[] elements = TICK_REQUESTS.elements();
		final int limit = Math.min(elements.length, TICK_REQUESTS.size());

		for(int i = 0; i < limit; i++) {
			final CompoundDeviceManager.WorldHandler e = (CompoundDeviceManager.WorldHandler) elements[i];

			if(e == null) {
				break;
			}

			e.process();
		}

		TICK_REQUESTS.clear();
	}

	public static void start(MinecraftServer server) {
		final Iterator<WeakReference<CompoundDeviceManager>> it = MANAGERS.iterator();

		while(it.hasNext()) {
			final CompoundDeviceManager m = it.next().get();

			if(m == null) {
				it.remove();
			} else {
				m.worlds.values().forEach(w -> ((CompoundDeviceManager.WorldHandler)w).firstTick());
			}
		}

	}
}
