package grondag.fluidity.api.storage;

@FunctionalInterface
public interface StopNotifier {
    void stopListening();
}