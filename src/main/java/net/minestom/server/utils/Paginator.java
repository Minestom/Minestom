package net.minestom.server.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Paginator<T> {

    public static <T> @Nullable List<T> get(@NotNull List<T> values, int pageIndex, int pageSize) {
        if (!pageExists(values, pageIndex, pageSize))
            return null;
        final int fromIndex = pageIndex * pageSize;
        final int toIndex = Math.min(fromIndex + pageSize, values.size());
        return values.subList(fromIndex, toIndex);
    }
    public static <T> @Nullable T get(@NotNull List<T> values, int pageIndex, int pageSize, int valueIndexInPage) {
        if (!valueExists(values, pageIndex, pageSize, valueIndexInPage))
            return null;
        final int idx = getValueIndex(pageIndex, pageSize, valueIndexInPage);
        return values.get(idx);
    }

    public static <T> boolean set(@NotNull List<T> values, T value, int pageIndex, int pageSize, int valueIndexInPage) {
        if (!valueExists(values, pageIndex, pageSize, valueIndexInPage))
            return false;
        final int idx = getValueIndex(pageIndex, pageSize, valueIndexInPage);
        values.set(idx, value);
        return true;
    }

    public static <T> boolean pageExists(@NotNull List<T> values, int pageIndex, int pageSize) {
        return valueExists(values, pageIndex, pageSize, 0);
    }
    public static <T> boolean valueExists(@NotNull List<T> values, int pageIndex, int pageSize, int valueIndexInPage) {
        final int idx = getValueIndex(pageIndex, pageSize, valueIndexInPage);
        return idx >= 0 && idx < values.size();
    }

    public static int getValueIndex(int pageIndex, int pageSize, int valueIndexInPage) {
        return pageIndex * pageSize + valueIndexInPage;
    }

    private List<T> values;
    private int pageSize;

    public Paginator(List<T> values, int pageSize) {
        this.values = values;
        this.pageSize = pageSize;
    }
    public Paginator(int pageSize) {
        this(new ArrayList<>(), pageSize);
    }
    public Paginator() {
        this(0);
    }

    public @Nullable List<T> get(int pageIndex) {
        return get(values, pageIndex, pageSize);
    }
    public @Nullable T get(int pageIndex, int valueIndexInPage) {
        return get(values, pageIndex, pageSize, valueIndexInPage);
    }

    public boolean set(T value, int pageIndex, int valueIndexInPage) {
        return set(values, value, pageIndex, pageSize, valueIndexInPage);
    }

    public boolean add(T value) {
        return values.add(value);
    }
    public void add(int index, T value) {
        values.add(index, value);
    }
    public boolean addAll(Collection<? extends T> c) {
        return values.addAll(c);
    }
    public boolean addAll(int index, Collection<? extends T> c) {
        return values.addAll(index, c);
    }

    public boolean remove(T value) {
        return values.remove(value);
    }
    public T remove(int index) {
        return values.remove(index);
    }

    public boolean pageExists(int pageIndex) {
        return pageExists(values, pageIndex, pageSize);
    }
    public boolean valueExists(int pageIndex, int valueIndexInPage) {
        return valueExists(values, pageIndex, pageSize, valueIndexInPage);
    }

    public List<T> getValues() {
        return values;
    }
    public void setValues(List<T> values) {
        this.values = values;
    }
    public int getPageSize() {
        return pageSize;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
