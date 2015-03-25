package ru.ifmo.ctddev.belonogov;

import java.util.List;
import java.util.function.Function;

/**
 * Created by vanya on 24.03.15.
 */

public interface ParallelMapper extends AutoCloseable {
    <T, R> List<R> run( Function<? super T, ? extends R> f,  List<? extends T> args ) throws InterruptedException;
    @Override
    void close() throws InterruptedException;
}
