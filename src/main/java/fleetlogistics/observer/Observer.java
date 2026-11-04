
package fleetlogistics.observer;

@FunctionalInterface
public interface Observer<T> {
    void onEvent(T event);
}
