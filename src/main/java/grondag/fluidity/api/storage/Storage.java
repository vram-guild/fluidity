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
/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grondag.fluidity.api.storage;

import java.util.Iterator;
import java.util.function.Predicate;

import com.google.common.base.Predicates;

import grondag.fluidity.api.bulk.BulkArticleView;
import grondag.fluidity.api.transact.Transactor;
import net.minecraft.util.Identifier;

public interface Storage<P extends Port, V extends StoredArticle<T>, T> extends Transactor {
    Identifier ANONYMOUS_ID = new Identifier("fluidity:anon");
    int NO_SLOT = -1;
    
    P voidPort();

    V emptyView();
    
    boolean isEmpty();

    /**
     * True when can contain more than one fluid.
     */
    default boolean isCompound() {
        return false;
    }

    /**
     * True when has ports that can only be accessed from certain sides.
     */
    default boolean isSided() {
        return false;
    }

    /**
     * True when container is a view of other containers. This means the contents of
     * this container could be visible in other containers.
     */
    default boolean isVirtual() {
        return false;
    }

    Iterable<P> ports(PortFilter portFilter);

    default Iterable<P> ports() {
        return ports(PortFilter.ALL);
    }

    default P firstPort(PortFilter portFilter) {
		Iterator<P> it = ports(portFilter).iterator();
        return it.hasNext() ? it.next() : voidPort();
    }

    default P firstPort() {
        return firstPort(PortFilter.ALL);
    }

    /**
     * For containers with named slots, finds slot using named-spaced identifier.
     * 
     * @param id
     * @return integer identifier of first matching slot found or {@code NO_SLOT} if
     *         no match
     * @implNote Should be overridden if container has named slots
     * @see BulkArticleView#slot()
     */
    default int slotFromId(Identifier slotId) {
        return NO_SLOT;
    }

    default Identifier idForSlot(int slot) {
        return ANONYMOUS_ID;
    }

    boolean canStore(T article);
    
    boolean contains(T article);
    
    Iterable<V> articles(PortFilter portFilter, Predicate<T> articleFilter);

    default Iterable<V> articles(PortFilter portFilter) {
        return articles(portFilter, Predicates.alwaysTrue());
    }

    default Iterable<V> articles(Predicate<T> articleFilter) {
        return articles(PortFilter.ALL, articleFilter);
    }

    default Iterable<V> articles() {
        return articles(PortFilter.ALL, Predicates.alwaysTrue());
    }
    
    V articleForSlot(int slot);

    default V articleForId(Identifier id) {
        final int slot = slotFromId(id);
        return slot >= 0 ? articleForSlot(slot) : emptyView();
    }
    
    default V firstArticle(PortFilter portFilter, Predicate<T> articleFilter) {
        Iterator<V> it = articles(portFilter, articleFilter).iterator();
        return it.hasNext() ? it.next() : null;
    }

    default V firstArticle(PortFilter portFilter) {
        return firstArticle(portFilter, Predicates.alwaysTrue());
    }

    default V firstArticle(Predicate<T> articleFilter) {
        return firstArticle(PortFilter.ALL, articleFilter);
    }

    default V firstArticle() {
        return firstArticle(PortFilter.ALL, Predicates.alwaysTrue());
    }
    
    StopNotifier startListening(StorageListener<V, T> listener, PortFilter portFilter, Predicate<T> articleFilter);
}
