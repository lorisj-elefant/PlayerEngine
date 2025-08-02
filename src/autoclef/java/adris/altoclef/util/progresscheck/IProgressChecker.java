package adris.altoclef.util.progresscheck;

public interface IProgressChecker<T> {
    void setProgress(T paramT);

    boolean failed();

    void reset();
}
