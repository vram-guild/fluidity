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
package grondag.fluidity.base.synch;

import java.util.Comparator;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;
import grondag.fluidity.api.article.Article;

/**
 * Client-side representation of server inventory that supports
 * very large quantities and slotless/virtual containers via handles.
 */
@Experimental
public interface DisplayDelegate {
	/**
	 * Uniquely identifies this resource within the server-side container.
	 */
	int handle();

	Article article();

	long getCount();

	default long numerator() {
		return 0;
	}

	default long divisor() {
		return 1;
	}

	boolean isEmpty();

	String localizedName();

	String lowerCaseLocalizedName();

	/////////////////////////////////////////
	// SORTING UTILITIES
	/////////////////////////////////////////

	Comparator<DisplayDelegate> SORT_BY_NAME_ASC = new Comparator<DisplayDelegate>() {
		@Override
		public int compare(@Nullable DisplayDelegate o1, @Nullable DisplayDelegate o2) {
			if (o1 == null) {
				if (o2 == null) {
					return 0;
				}
				return 1;
			} else if (o2 == null) {
				return -1;
			}

			final String s1 = I18n.get(o1.localizedName());
			final String s2 = I18n.get(o2.localizedName());
			return s1.compareTo(s2);
		}
	};

	Comparator<DisplayDelegate> SORT_BY_NAME_DESC = new Comparator<DisplayDelegate>() {
		@Override
		public int compare(@Nullable DisplayDelegate o1, @Nullable DisplayDelegate o2) {
			return SORT_BY_NAME_ASC.compare(o2, o1);
		}
	};

	Comparator<DisplayDelegate> SORT_BY_QTY_ASC = new Comparator<DisplayDelegate>() {
		@Override
		public int compare(@Nullable DisplayDelegate o1, @Nullable DisplayDelegate o2) {
			if (o1 == null) {
				if (o2 == null) {
					return 0;
				}
				return 1;
			} else if (o2 == null) {
				return -1;
			}
			int result = Long.compare(o1.getCount(), o2.getCount());

			if(result == 0) {
				result =  Long.compare(o1.numerator() * o2.divisor(), o2.numerator() * o1.divisor());
			}

			return result == 0 ? SORT_BY_NAME_ASC.compare(o1, o2) : result;
		}
	};

	Comparator<DisplayDelegate> SORT_BY_QTY_DESC = new Comparator<DisplayDelegate>() {
		@Override
		public int compare(@Nullable DisplayDelegate o1, @Nullable DisplayDelegate o2) {
			return SORT_BY_QTY_ASC.compare(o2, o1);
		}
	};

	static Comparator<DisplayDelegate> getSort(int sortIndex) {
		switch(sortIndex % SORT_COUNT) {
			case 0:
			default:
				return SORT_BY_NAME_ASC;

			case 1:
				return SORT_BY_NAME_DESC;

			case 2:
				return SORT_BY_QTY_ASC;

			case 3:
				return SORT_BY_QTY_DESC;
		}
	}

	int SORT_COUNT = 4;

	static String getSortTranslactionKey(int sortIndex) {
		switch(sortIndex % SORT_COUNT) {
			case 0:
			default:
				return "label.fluidity.sort_by_name_asc";

			case 1:
				return "label.fluidity.sort_by_name_desc";

			case 2:
				return "label.fluidity.sort_by_qty_asc";

			case 3:
				return "label.fluidity.sort_by_qty_desc";
		}
	}

	@Deprecated
	static String getSortLabel(int sortIndex) {
		return I18n.get(getSortTranslactionKey(sortIndex));
	}

	static Component getSortText(int sortIndex) {
		return new TranslatableComponent(getSortTranslactionKey(sortIndex));
	}
}
