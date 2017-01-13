import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Decorator for underlying iterator.
 * This does not allow modifying underlying data (i.e. remove() throws UnsupportedOperationException).
 * This restriction is due to the fact that we need to look ahead and remove item first before applying the filter.
 */
public class FilteringIterator<T> implements Iterator<T> {
    private Iterator<T> iterator;
    private IObjectTest<T> filter;

    // use extra boolean to indicate end rather
    // than using null since null may be valid item in the underlying
    // collection (refer to testGetNumberFilterAllowingNull())
    private boolean hasNextElement;
    private T nextElement;

    // hold on to exception and throw it on
    // "next" call after fetching last valid item
    private FilterFailedException filterFailedException;

    /**
     * Creates an iterator with filter
     *
     * @param iterator underlying iterator to wrap
     * @param filter   filter condition to apply
     */
    public FilteringIterator(Iterator<T> iterator, IObjectTest<T> filter) {
        this.iterator = iterator;
        this.filter = filter;

        if (iterator == null || filter == null) {
            throw new IllegalArgumentException("iterator and filter required!");
        }

        // increment to next element
        fetchNext();
    }

    private void fetchNext() {

        hasNextElement = false;
        nextElement = null;

        try {
            while (iterator.hasNext()) {
                T item = iterator.next();

                if (isMatches(item)) {
                    hasNextElement = true;
                    nextElement = item;
                    break;
                }
            }
        } catch (FilterFailedException e) {
            // store so we can throw next time hasNext() or next() is called
            filterFailedException = e;
        }
    }

    private boolean isMatches(T item) {
        try {
            return filter.test(item);
        } catch (RuntimeException e) {
            throw new FilterFailedException("Filter threw exception while matching " + item, e);
        }
    }

    @Override
    public boolean hasNext() {
        if (filterFailedException != null) {
            throw filterFailedException;
        }
        return hasNextElement;
    }

    @Override
    public T next() {
        if (filterFailedException != null) {
            throw filterFailedException;
        }

        if (!hasNextElement) {
            throw new NoSuchElementException();
        }

        // return current next and fetch next one
        T element = nextElement;
        fetchNext();

        return element;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Does not support remove operation!");
    }

    public static class FilterFailedException extends RuntimeException {
        public FilterFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
