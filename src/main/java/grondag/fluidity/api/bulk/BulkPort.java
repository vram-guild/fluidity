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
import grondag.fluidity.api.storage.Article;
import grondag.fluidity.api.storage.Port;

/**
 * The thing that will be used to take fluid in or out of another thing.
 */
public interface BulkPort extends Port {
    FractionView accept(Article article, FractionView volume, int flags);

    default long accept(Article article, long volume, long units, int flags) {
        return accept(article, Fraction.of(volume, units), flags).toLong(units);
    }

    default BulkArticle accept(AbstractBulkArticle<?> volume, int flags) {
        return BulkArticle.of(volume.article(), accept(volume.article(), volume.volume(), flags));
    }

    default boolean acceptFrom(Article article, FractionView volume, int flags, MutableBulkArticle target) {
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

    default boolean acceptFrom(MutableBulkArticle target, int flags) {
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

    FractionView supply(Article article, FractionView volume, int flags);

    default long supply(Article article, long volume, long units, int flags) {
        return supply(article, Fraction.of(volume, units), flags).toLong(units);
    }

    default BulkArticle supply(AbstractBulkArticle<?> volume, int flags) {
        return BulkArticle.of(volume.article(), supply(volume.article(), volume.volume(), flags));
    }

    default boolean supplyTo(Article article, FractionView volume, int flags, MutableBulkArticle target) {
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
        public FractionView accept(Article resource, FractionView volume, int flags) {
            return Fraction.ZERO;
        }

        @Override
        public FractionView supply(Article resource, FractionView volume, int flags) {
            return Fraction.ZERO;
        }
    };
}
