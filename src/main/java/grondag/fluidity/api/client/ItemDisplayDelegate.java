package grondag.fluidity.api.client;

import java.util.Comparator;

import javax.annotation.Nullable;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;

import grondag.fluidity.impl.ItemDisplayDelegateImpl;

/**
 * Client-side representation of server inventory that supports
 * very large quantities and slotless/virtual containers via handles.
 */
public interface ItemDisplayDelegate {
	/**
	 * Uniquely identifies this resource within the server-side container.
	 */
	int handle();

	ItemStack displayStack();

	long count();

	String localizedName();

	String lowerCaseLocalizedName();

	ItemDisplayDelegate set(ItemStack stack, long count, int handle);

	default ItemDisplayDelegate set(ItemDisplayDelegate from) {
		return set(from.displayStack(), from.count(), from.handle());
	}

	ItemDisplayDelegate EMPTY = new ItemDisplayDelegateImpl(ItemStack.EMPTY, 0, -1);

	static ItemDisplayDelegate create(ItemStack stack, long count, int handle) {
		return new ItemDisplayDelegateImpl(stack, count, handle);
	}

	/////////////////////////////////////////
	// SORTING UTILITIES
	/////////////////////////////////////////

	Comparator<ItemDisplayDelegate> SORT_BY_NAME_ASC = new Comparator<ItemDisplayDelegate>() {
		@Override
		public int compare(@Nullable ItemDisplayDelegate o1, @Nullable ItemDisplayDelegate o2) {
			if (o1 == null) {
				if (o2 == null) {
					return 0;
				}
				return 1;
			} else if (o2 == null) {
				return -1;
			}

			final String s1 = I18n.translate(o1.localizedName());
			final String s2 = I18n.translate(o2.localizedName());
			return s1.compareTo(s2);
		}
	};

	Comparator<ItemDisplayDelegate> SORT_BY_NAME_DESC = new Comparator<ItemDisplayDelegate>() {
		@Override
		public int compare(@Nullable ItemDisplayDelegate o1, @Nullable ItemDisplayDelegate o2) {
			return SORT_BY_NAME_ASC.compare(o2, o1);
		}
	};

	Comparator<ItemDisplayDelegate> SORT_BY_QTY_ASC = new Comparator<ItemDisplayDelegate>() {
		@Override
		public int compare(@Nullable ItemDisplayDelegate o1, @Nullable ItemDisplayDelegate o2) {
			if (o1 == null) {
				if (o2 == null) {
					return 0;
				}
				return 1;
			} else if (o2 == null) {
				return -1;
			}
			final int result = Long.compare(o1.count(), o2.count());
			return result == 0 ? SORT_BY_NAME_ASC.compare(o1, o2) : result;
		}
	};

	Comparator<ItemDisplayDelegate> SORT_BY_QTY_DESC = new Comparator<ItemDisplayDelegate>() {
		@Override
		public int compare(@Nullable ItemDisplayDelegate o1, @Nullable ItemDisplayDelegate o2) {
			return SORT_BY_QTY_ASC.compare(o2, o1);
		}
	};

	int SORT_COUNT = 4;

	static Comparator<ItemDisplayDelegate> getSort(int sortIndex) {
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

	static String getSortLabel(int sortIndex) {
		return I18n.translate(getSortTranslactionKey(sortIndex));
	}
}
