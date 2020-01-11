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
package grondag.fluidity.base.storage.discrete.helper;

import java.util.concurrent.ArrayBlockingQueue;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.minecraft.util.math.BlockPos;

import grondag.fluidity.api.article.Article;

@API(status = Status.EXPERIMENTAL)
public class DiscreteTrackingJournal {
	private DiscreteTrackingJournal() {}

	private static final ArrayBlockingQueue<DiscreteTrackingJournal> POOL = new ArrayBlockingQueue<>(4096);

	public long capacityDelta;
	public final Object2LongOpenHashMap<Article> changes = new Object2LongOpenHashMap<>();

	public void clear() {
		capacityDelta = 0;
		changes.clear();
	}

	BlockPos pos;

	static DiscreteTrackingJournal claim() {
		final DiscreteTrackingJournal result = POOL.poll();
		return result == null ? new DiscreteTrackingJournal() : result;
	}

	static void release(DiscreteTrackingJournal journal) {
		journal.clear();
		POOL.offer(journal);
	}
}
