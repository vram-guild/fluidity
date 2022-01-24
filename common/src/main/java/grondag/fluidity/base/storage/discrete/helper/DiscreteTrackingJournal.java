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

package grondag.fluidity.base.storage.discrete.helper;

import java.util.concurrent.ArrayBlockingQueue;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.BlockPos;

import grondag.fluidity.api.article.Article;

@Experimental
public class DiscreteTrackingJournal {
	private DiscreteTrackingJournal() { }

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
