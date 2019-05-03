package ru.ifmo.rain.chizhikov.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final List<T> data;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        data = Collections.emptyList();
        comparator = null;
    }

    private ArraySet(Comparator<? super T> comparator) {
        this.data = Collections.emptyList();
        this.comparator = comparator;
    }


    public ArraySet(Collection<? extends T> collection) {
        data = new ArrayList<>(new TreeSet<>(collection));
        comparator = null;
    }


    public ArraySet(Collection<? extends T> collection, Comparator<? super T> comparator) {
        SortedSet<T> newSet = new TreeSet<>(comparator);
        this.comparator = comparator;
        newSet.addAll(collection);
        data = new ArrayList<>(newSet);
    }


    private ArraySet(List<T> data, Comparator<? super T> comparator) {
        this.data = data;
        this.comparator = comparator;

        if (data instanceof ReversedList) {
            ((ReversedList) data).reverse();
        }
    }

    @Override
    public T lower(T t) {
        return getElementInSet(t, -1, -1);
    }

    @Override
    public T floor(T t) {
        return getElementInSet(t, 0, -1);
    }

    @Override
    public T ceiling(T t) {
        return getElementInSet(t, 0, 0);
    }

    @Override
    public T higher(T t) {
        return getElementInSet(t, 1, 0);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new ReversedList<>(data), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int fromIndex = fromInclusive ? indexOf(fromElement, 0, 0) : indexOf(fromElement, 1, 0);
        int toIndex = toInclusive ? indexOf(toElement, 0, -1) : indexOf(toElement, -1, -1);
        if (fromIndex > toIndex || fromIndex == -1 || toIndex == -1) {
            return new ArraySet<>(comparator);
        }
        return new ArraySet<>(data.subList(fromIndex, toIndex + 1), comparator);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        return (data.isEmpty()) ? new ArraySet<>(comparator) : subSet(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        return (data.isEmpty()) ? new ArraySet<>(comparator) : subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (comparator != null) {
            if (comparator.compare(fromElement, toElement) > 0) {
                throw new IllegalArgumentException("ERROR: Invalid arguments.");
            }
        } else {
            if (fromElement instanceof Comparable && toElement instanceof Comparable) {
                if (((Comparable) fromElement).compareTo(toElement) > 0) {
                    throw new IllegalArgumentException("ERROR: Invalid arguments.");
                }
            }
        }
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        checkNotEmpty();
        return data.get(0);
    }

    @Override
    public T last() {
        checkNotEmpty();
        return data.get(size() - 1);
    }

    @Override
    public int size() {
        return data.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (T) Objects.requireNonNull(o), comparator) >= 0;
    }

    private boolean indexValidation(int i) {
        return (i >= 0 && i < size());
    }

    private void checkNotEmpty() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        }
    }

    private int indexOf(T element, int includeFound, int includeNotFound) {
        int i = Collections.binarySearch(data, Objects.requireNonNull(element), comparator);
        if (i < 0) {
            i = -(i + 1);
            return indexValidation(i + includeNotFound) ? (i + includeNotFound) : -1;
        }
        return indexValidation(i + includeFound) ? (i + includeFound) : -1;
    }

    private T getElementInSet(T element, int includeFound, int includeNotFound) {
        int i = indexOf(element, includeFound, includeNotFound);
        return indexValidation(i) ? data.get(i) : null;
    }

    class ReversedList<E> extends AbstractList<E> implements RandomAccess {
        private List<E> data;
        private boolean isReversed;

        private ReversedList(List<E> data) {
            this.data = data;
        }

        @Override
        public E get(int index) {
            return isReversed ? data.get(size() - 1 - index) : data.get(index);
        }

        @Override
        public int size() {
            return data.size();
        }

        void reverse() {
            isReversed = !isReversed;
        }
    }
}
