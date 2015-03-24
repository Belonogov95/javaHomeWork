package ru.ifmo.ctddev.belonogov.arrayset;
 import java.util.*;

/**
 * Created by vanya on 24.02.15.
 */
public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {


    public void print() {
        System.out.println("l r: " + left + " " + right);
        for (int i = left; i < right; i++)
            System.out.print(data.get(i) + " ");
        System.out.println();
    }

    private class MyComparator implements Comparator<T> {
        Comparator<T> comparator;

        public MyComparator(Comparator<T> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(T a, T b) {
            if (comparator != null)
                return comparator.compare(a, b);
            Comparable<? super T> x = (Comparable<? super T>) a;
            return x.compareTo(b);
        }
    }

    private boolean checkPos(int pos) {
        return left <= pos && pos < right;
    }

    private class MyIterator implements Iterator<T> {
        private int dir;
        private int pos;

        private MyIterator(int pos, int dir) {
            this.pos = pos;
            this.dir = dir;
        }

        @Override
        public boolean hasNext() {
            return checkPos(pos);
        }

        @Override
        public T next() {
            T value = data.get(pos);
            pos += dir;
            return value;
        }
    }

    private ArrayList<T> data;
    private MyComparator comparator;
    int left, right;
    boolean reverseFlag;
    private Comparator < T > comparatorOuter;


    private ArraySet(ArrayList<T> data, MyComparator comparator, int left, int right, boolean reverseFlag) {
        this.data = data;
        this.comparator = comparator;
        this.left = left;
        this.right = right;
        this.reverseFlag = reverseFlag;
        this.comparatorOuter = comparator;
    }

    public ArraySet() {
        data = new ArrayList<>();
        left = right = 0;
        reverseFlag = false;
    }

    public ArraySet(Comparator<T> comparator) {
        this();
        this.comparator = new MyComparator(comparator);
    }

    public ArraySet(Collection<? extends T> c, Comparator<T> comparatorOuter) {
        this();
        //System.out.println("constructor");
        //System.out.println("sz: " + c.size());
//        if (c.size() < 20)
//            for (T x: c)
//                System.out.print(x + " ");
//        System.out.println();

        this.comparatorOuter = comparatorOuter;
        this.comparator = new MyComparator(comparatorOuter);
        for (T item : c) {
            //System.out.println(item);
            data.add(item);
        }
        Collections.sort(data, comparator);
        int cur = 0;
        for (int i = 0; i < data.size(); i++) {
            //System.out.println(comparator);
            if (cur == 0 || (comparator.compare(data.get(cur - 1), data.get(i)) != 0)) {
                data.set(cur, data.get(i));
                cur++;
            }
        }
//        System.out.println("cur: " + cur);

        data.subList(cur, data.size()).clear();
        //.removeRange(cur, data.size());
        //System.out.println("")
        //print();
        left = 0;
        right = data.size();
        reverseFlag = false;
    }

    public ArraySet(Collection<? extends T> c) {
        this(c, null);
    }

    private int binSearch(T key, boolean flagEqual) {
        int l = left - 1;
        int r = right;
        //System.out.println("bin search key: " + key);
        while (r - l > 1) {
            int m = (l + r) / 2;
            int cmp = comparator.compare(key, data.get(m));
            if (cmp < 0 || (flagEqual && cmp == 0))
                r = m;
            else
                l = m;
        }
        //System.out.println("pos: " + r);
        return r;
    }

    private T safelyGet(int pos) {
        if (left <= pos && pos < right)
            return data.get(pos);
        return null;
    }

    @Override
    public T lower(T t) {
        return safelyGet(lowerInt(t));
    }

    public int lowerInt(T t) {
        int pos = Collections.binarySearch(data, t, comparator);
        if (pos >= 0)
            return pos - 1;
        return -pos - 2;
    }

    @Override
    public T floor(T t) {
        return safelyGet(floorInt(t));
    }

    private int floorInt(T t) {
        int pos = Collections.binarySearch(data, t, comparator);
        if (pos >= 0)
            return pos;
        return -pos - 2;
    }

    @Override
    public T ceiling(T t) {
        return safelyGet(ceilingInt(t));
    }

    private int ceilingInt(T t) {
        int pos = Collections.binarySearch(data, t, comparator);
        if (pos >= 0)
            return pos;
        return -pos - 1;
    }

    private int higherInt(T t) {
        int pos = Collections.binarySearch(data, t, comparator);
        if (pos >= 0)
            return pos + 1;
        return -pos - 1;
    }

    @Override
    public T higher(T t) {
        return safelyGet(higherInt(t));
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
        return new MyIterator((reverseFlag)? right - 1: left, (reverseFlag) ? -1 : 1);
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(data, comparator, left, right, !reverseFlag);
    }

    @Override
    public Iterator<T> descendingIterator() {
        return new MyIterator((!reverseFlag)? right - 1: left, (!reverseFlag) ? -1 : 1);
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {

//        if (fromElement.equals(toElement)) return new ArraySet<>(data, comparator, 0, 0, reverseFlag);

        int l = ceilingInt(fromElement);
        //int r = higherInt(toElement);
        int r = floorInt(toElement);
//        System.out.println("l r: " + l + " " + r);

        if (fromElement != null && safelyGet(l) != null && !fromInclusive && comparator.compare(fromElement, safelyGet(l)) == 0)
            l++;
        if (toElement != null && safelyGet(r) != null && !toInclusive && comparator.compare(toElement, safelyGet(r)) == 0)
            r--;
//        System.out.println("l r: " + l + " " + r);
        r++;
        if (l > r)
            l = r;
        return new ArraySet<>(data, comparator, l, r, reverseFlag);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        return subSet(firstSafe(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        return subSet(fromElement, inclusive, lastSafe(), true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparatorOuter;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return subSet(firstSafe(), true, toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return subSet(fromElement, true, lastSafe(), true);
    }

    @Override
    public T first() {
        if (size() == 0)
            throw new NoSuchElementException();
        return safelyGet(left);
    }

    @Override
    public T last() {
        if (size() == 0) throw new NoSuchElementException();
        return safelyGet(right - 1);
    }

    public T firstSafe() {
        return safelyGet(left);
    }

    public T lastSafe() {
        return safelyGet(right - 1);
    }

    @Override
    public int size() {
        return right - left;
    }

    @Override
    public boolean contains(Object o) {
        T a = (T)o;
        T res = floor(a);
        if (res == null || a == null) return false;
        return comparator.compare(res, a) == 0;
    }

}


