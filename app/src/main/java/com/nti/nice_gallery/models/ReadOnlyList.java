package com.nti.nice_gallery.models;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ReadOnlyList<T> implements Iterable<T> {

    private final ArrayList<T> list = new ArrayList<>();

    public ReadOnlyList(Collection<T> source) {
        if (source != null) {
            list.addAll(source);
        }
    }

    public T get(int index) {
        return list.get(index);
    }

    public int indexOf(T item) {
        return list.indexOf(item);
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<T> it = list.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                return it.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}