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

import grondag.fluidity.api.bulk.BulkVolumeView;
import grondag.fluidity.api.transact.Transactor;
import net.minecraft.util.Identifier;

public interface Storage<P extends Port, R extends StoredResourceView> extends Transactor {
    Identifier ANONYMOUS_ID = new Identifier("fluidity:anon");
    int NO_SLOT = -1;
    
    P voidPort();

    R emptyResource();
    
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
     * @see BulkVolumeView#slot()
     */
    default int slotFromId(Identifier id) {
        return NO_SLOT;
    }

    default Identifier idForSlot(int slot) {
        return ANONYMOUS_ID;
    }

    Iterable<R> contents(PortFilter portFilter, Predicate<R> resourceFilter);

    default Iterable<R> contents(PortFilter portFilter) {
        return contents(portFilter, Predicates.alwaysTrue());
    }

    default Iterable<R> contents(Predicate<R> resourceFilter) {
        return contents(PortFilter.ALL, resourceFilter);
    }

    default Iterable<R> contents() {
        return contents(PortFilter.ALL, Predicates.alwaysTrue());
    }
    
    R resourceForSlot(int slot);

    default R resourceForId(Identifier id) {
        final int slot = slotFromId(id);
        return slot >= 0 ? resourceForSlot(slot) : emptyResource();
    }
    
    default R firstResource(PortFilter portFilter, Predicate<R> resourceFilter) {
        Iterator<R> it = contents(portFilter, resourceFilter).iterator();
        return it.hasNext() ? it.next() : null;
    }

    default R firstResource(PortFilter portFilter) {
        return firstResource(portFilter, Predicates.alwaysTrue());
    }

    default R firstResouce(Predicate<R> resourceFilter) {
        return firstResource(PortFilter.ALL, resourceFilter);
    }

    default R firstResource() {
        return firstResource(PortFilter.ALL, Predicates.alwaysTrue());
    }
    
    StopNotifier startListening(StorageListener<R> listener, PortFilter portFilter, Predicate<R> resourceFilter);
}
