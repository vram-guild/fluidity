package grondag.fluidity.api.storage;

public interface StoredResourceView {
    default int slot() {
        return 0;
    }
}
