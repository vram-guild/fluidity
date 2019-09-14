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

package grondag.fluidity.api.bulk;

import grondag.fluidity.api.fraction.Fraction;
import grondag.fluidity.api.fraction.FractionView;
import grondag.fluidity.api.storage.StoredResourceView;

/**
 * For container views and queries. Volumes outside containers should use
 * concrete types.
 */
public interface BulkVolumeView extends StoredResourceView {

    BulkVolumeView EMPTY = new BulkVolumeView() {
    };

    default BulkResource resource() {
        return BulkResource.EMPTY;
    }

    default FractionView volume() {
        return Fraction.ZERO;
    }

    default FractionView capacity() {
        return volume();
    }

    default ImmutableBulkVolume toImmutable() {
        return ImmutableBulkVolume.of(resource(), volume());
    }

    default MutableBulkVolume mutableCopy() {
        return MutableBulkVolume.of(resource(), volume());
    }
}
