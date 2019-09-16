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
    <T> FractionView accept(T article, FractionView volume, int flags);

    default <T> long accept(T article, long volume, long units, int flags) {
        return accept(article, Fraction.of(volume, units), flags).toLong(units);
    }

    default <T> BulkArticle<T> accept(BulkArticleView<T> volume, int flags) {
        return BulkArticle.of(volume.article(), accept(volume.article(), volume.volume(), flags));
    }

    default <T> boolean acceptFrom(T article, FractionView volume, int flags, MutableBulkArticle<T> target) {
        if (target.article().equals(article) || target.volume().isZero()) {
            final FractionView result = accept(article, volume, flags);
            if (result.isZero()) {
                return false;
            } else {
                target.volume().subtract(result);
                target.setArticle(article);
                return true;
            }
        } else {
            return false;
        }
    }

    default <T> boolean acceptFrom(MutableBulkArticle<T> target, int flags) {
        if (target.volume().isZero()) {
            return false;
        } else {
            final FractionView result = accept(target.article(), target.volume(), flags);
            if (result.isZero()) {
                return false;
            } else {
                target.volume().subtract(result);
                return true;
            }
        }
    }

    <T> FractionView supply(T article, FractionView volume, int flags);

    default <T> long supply(T article, long volume, long units, int flags) {
        return supply(article, Fraction.of(volume, units), flags).toLong(units);
    }

    default <T> BulkArticle<T> supply(BulkArticleView<T> volume, int flags) {
        return BulkArticle.of(volume.article(), supply(volume.article(), volume.volume(), flags));
    }

    default <T> boolean supplyTo(T article, FractionView volume, int flags, MutableBulkArticle<T> target) {
        if (target.article().equals(article) || target.volume().isZero()) {
            final FractionView result = supply(article, volume, flags);
            if (result.isZero()) {
                return false;
            } else {
                target.volume().add(result);
                target.setArticle(article);
                return true;
            }
        } else {
            return false;
        }
    }

    static BulkPort VOID = new BulkPort() {
        @Override
        public <T> FractionView accept(T resource, FractionView volume, int flags) {
            return Fraction.ZERO;
        }

        @Override
        public <T> FractionView supply(T resource, FractionView volume, int flags) {
            return Fraction.ZERO;
        }
    };
}
