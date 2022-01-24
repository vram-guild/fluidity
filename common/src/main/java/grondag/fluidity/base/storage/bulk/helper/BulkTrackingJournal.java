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

package grondag.fluidity.base.storage.bulk.helper;

import java.util.concurrent.ArrayBlockingQueue;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.BlockPos;

import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.MutableFraction;

@Experimental
public class BulkTrackingJournal {
	private BulkTrackingJournal() { }

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
