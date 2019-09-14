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

package grondag.fluidity.api.bulk;

import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.Port;

/**
 * The thing that will be used to take fluid in or out of another thing.
 */
public interface BulkPort extends Port {
    FractionView accept(BulkResource resource, FractionView volume, int flags);

    default long accept(BulkResource resource, long volume, long units, int flags) {
        return accept(resource, Fraction.of(volume, units), flags).toLong(units);
    }

    default ImmutableBulkVolume accept(BulkVolume volume, int flags) {
        return ImmutableBulkVolume.of(volume.resource(), accept(volume.resource(), volume.volume(), flags));
    }

    default boolean acceptFrom(BulkResource resource, FractionView volume, int flags, MutableBulkVolume target) {
        if (target.resource().equals(resource) || target.volume().isZero()) {
            final FractionView result = accept(resource, volume, flags);
            if (result.isZero()) {
                return false;
            } else {
                target.volume().subtract(result);
                target.setResource(resource);
                return true;
            }
        } else {
            return false;
        }
    }

    default boolean acceptFrom(MutableBulkVolume target, int flags) {
        if (target.volume().isZero()) {
            return false;
        } else {
            final FractionView result = accept(target.resource(), target.volume(), flags);
            if (result.isZero()) {
                return false;
            } else {
                target.volume().subtract(result);
                return true;
            }
        }
    }

    FractionView supply(BulkResource resource, FractionView volume, int flags);

    default long supply(BulkResource resource, long volume, long units, int flags) {
        return supply(resource, Fraction.of(volume, units), flags).toLong(units);
    }

    default ImmutableBulkVolume supply(BulkVolume volume, int flags) {
        return ImmutableBulkVolume.of(volume.resource(), supply(volume.resource(), volume.volume(), flags));
    }

    default boolean supplyTo(BulkResource resource, FractionView volume, int flags, MutableBulkVolume target) {
        if (target.resource().equals(resource) || target.volume().isZero()) {
            final FractionView result = supply(resource, volume, flags);
            if (result.isZero()) {
                return false;
            } else {
                target.volume().add(result);
                target.setResource(resource);
                return true;
            }
        } else {
            return false;
        }
    }

    static BulkPort VOID = new BulkPort() {
        @Override
        public FractionView accept(BulkResource resource, FractionView volume, int flags) {
            return Fraction.ZERO;
        }

        @Override
        public FractionView supply(BulkResource resource, FractionView volume, int flags) {
            return Fraction.ZERO;
        }
    };
}
