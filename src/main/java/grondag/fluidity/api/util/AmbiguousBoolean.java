package grondag.fluidity.api.util;

public enum AmbiguousBoolean {
	YES(true, false),
	NO(false, true),
	MAYBE(true, true);

	public final boolean mayBeTrue;
	public final boolean mayBeFalse;

	private AmbiguousBoolean(boolean mayBeTrue, boolean mayBeFalse) {
		this.mayBeTrue = mayBeTrue;
		this.mayBeFalse = mayBeFalse;
	}
}
