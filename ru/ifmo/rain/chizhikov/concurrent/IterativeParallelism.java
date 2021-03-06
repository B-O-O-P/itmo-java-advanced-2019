package ru.ifmo.rain.chizhikov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of ScalarIP interface for iterative parallelism.
 */
public class IterativeParallelism implements ListIP {

    //java -cp . -p . -m info.kgeorgiy.java.advanced.concurrent scalar ru.ifmo.rain.chizhikov.concurrent.IterativeParallelism

    /**
     * Returns maximum of list.
     *
     * @param i number of concurrent threads.
     * @param list list of values to get maximum of.
     * @param comparator values comparator
     * @param <T> value type
     *
     * @return maximum of given list.
     *
     * @throws InterruptedException if one of executing threads was interrupted.
     */
    @Override
    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return multiThreading(i, list,
                stream -> stream.max(comparator).orElseThrow(),
                stream -> stream.max(comparator).orElseThrow());
    }

    /**
     * Returns minimum of list.
     *
     * @param i number of concurrent threads.
     * @param list list of values to get minimum of.
     * @param comparator values comparator
     * @param <T> value type
     *
     * @return minimum of given list.
     *
     * @throws InterruptedException if one of executing threads was interrupted.
     */
    @Override
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(i, list, Collections.reverseOrder(comparator));
    }

    /**
     * Returns whether all values satisfies predicate.
     *
     * @param i number of concurrent threads.
     * @param list list of values to check.
     * @param predicate testing predicate.
     * @param <T> value type.
     *
     * @return whether all values satisfies predicate.
     *
     * @throws InterruptedException  if one of executing threads was interrupted.
     */
    @Override
    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return multiThreading(i, list,
                stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(item -> item));
    }

    /**
     * Returns whether any value satisfies predicate.
     *
     * @param i number of concurrent threads.
     * @param list list of values to check.
     * @param predicate testing predicate.
     * @param <T> value type.
     *
     * @return whether any value satisfies predicate.
     *
     * @throws InterruptedException  if one of executing threads was interrupted.
     */
    @Override
    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return !all(i, list, predicate.negate());
    }

    /**
     * Join values to string.
     *
     * @param i number of concurrent threads.
     * @param list values to join.
     *
     * @return list of joined result of {@link #toString()} call on each value.
     *
     * @throws InterruptedException if one of executing thread was interrupted.
     */
    @Override
    public String join(int i, List<?> list) throws InterruptedException {
        return multiThreading(i,list,
                stream -> stream.map(Object::toString).collect(Collectors.joining()),
                stream -> stream.collect(Collectors.joining()));
    }

    /**
     * Filters values by predicate.
     *
     * @param i number of concurrent threads.
     * @param list values to filter.
     * @param predicate filter predicate.
     * @param <T> value type.
     *
     * @return list of values satisfying given predicate.
     *
     * @throws InterruptedException if one of executing thread was interrupted.
     */
    @Override
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return multiThreading(i,list,
                stream -> stream.filter(predicate).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    /**
     * Map values.
     *
     * @param i number of concurrent threads.
     * @param list values to filter.
     * @param function mapper function.
     * @param <T> value type.
     *
     * @return list of values mapped by given function.
     *
     * @throws InterruptedException if one of executing thread was interrupted.
     */
    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return multiThreading(i,list,
                stream -> stream.map(function).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    /**
     * Default constructor.
     */
    public IterativeParallelism(){}

    /**
     *  Splits the execution of this function in a given number of threads.
     *
     * @param numberOfThreads number of concurrent threads.
     * @param list list of values to perform the function.
     * @param task function to perform.
     * @param answerCollector function to collect resulting values.
     * @param <T> value type.
     * @param <R> resulting type.
     *
     * @return collected by given function result of task function perform.
     *
     * @throws InterruptedException if one of executing threads was interrupted.
     * @throws IllegalArgumentException if number of threads less than 1 or list of values is null.
     */
    private <T, R> R multiThreading(int numberOfThreads,
                                    List<? extends T> list,
                                    Function<Stream<? extends T>, R> task,
                                    Function<Stream<? extends R>, R> answerCollector) throws InterruptedException {
        if (numberOfThreads < 1) {
            throw new IllegalArgumentException("ERROR: Number of threads must be at least one.");
        }

        if (list == null){
            throw new IllegalArgumentException("ERROR: list of values must be non-null.");
        }

        numberOfThreads = Math.max(1, Math.min(numberOfThreads, list.size()));
        List<Thread> threads = new ArrayList<>();
        int partSize = list.size() / numberOfThreads;
        int rest = list.size() % numberOfThreads;

        List<R> answerThreads = new ArrayList<>(Collections.nCopies(numberOfThreads, null));

        for (int i = 0, r = 0; i < numberOfThreads; ++i) {
            final int l = r;
            r = l + partSize + (rest-- > 0 ? 1 : 0);

            final int index = i;
            final int fR = r;
            Thread partThread = new Thread(() -> answerThreads.set(index,
                    task.apply(list.subList(l, fR).stream())));
            partThread.start();
            threads.add(partThread);

        }

        for (Thread thread : threads) {
            thread.join();
        }

        return answerCollector.apply(answerThreads.stream());
    }
}
