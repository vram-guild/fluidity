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
package grondag.fluidity.base.storage.bulk.helper;

import java.util.concurrent.ArrayBlockingQueue;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.util.math.BlockPos;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.MutableFraction;

@Experimental
public class BulkTrackingJournal {
	private BulkTrackingJournal() {}

	private static final ArrayBlockingQueue<BulkTrackingJournal> POOL = new ArrayBlockingQueue<>(4096);

	public final MutableFraction capacityDelta = new MutableFraction();
	public final Object2ObjectOpenHashMap<Article, MutableFraction> changes = new Object2ObjectOpenHashMap<>();

	public void clear() {
		capacityDelta.set(0);
		changes.clear();
	}

	BlockPos pos;

	static BulkTrackingJournal claim() {
		final BulkTrackingJournal result = POOL.poll();
		return result == null ? new BulkTrackingJournal() : result;
	}

	static void release(BulkTrackingJournal journal) {
		journal.clear();
		POOL.offer(journal);
	}
}
