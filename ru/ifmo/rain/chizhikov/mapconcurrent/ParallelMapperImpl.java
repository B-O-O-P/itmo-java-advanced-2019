package ru.ifmo.rain.chizhikov.mapconcurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * Implementation of {@link ParallelMapper} interface for parallel mapping.
 */
public class ParallelMapperImpl implements ParallelMapper {

    /**
     * Maps function {@code function} over specified {@code list}.
     * Mapping for each element performs in parallel.
     *
     * @param function function to map
     * @param list     arguments
     * @param <T>      value type
     * @param <R>      result type
     * @return mapped function
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list)
            throws InterruptedException {

        List<R> answerCollector = new ArrayList<>(Collections.nCopies(list.size(), null));
        final SynchronizedCounter counter = new SynchronizedCounter(list.size());

        for (int i = 0; i < list.size(); ++i) {
            final int finalI = i;
            addTask(new Task(
                    () -> answerCollector.set(finalI, function.apply(list.get(finalI))), counter));
        }

        synchronized (counter) {
            while (!counter.isCompleted()) {
                counter.wait();
            }
        }

        return answerCollector;
    }


    /**
     * Stops all threads. All unfinished mappings leave in undefined state.
     */
    @Override
    public void close() {
        threads.forEach(Thread::interrupt);

        threads.forEach(thread -> {
                    try {
                        thread.join();
                    } catch (InterruptedException ignored) {
                    }
                }
        );
    }

    /**
     * Construct threads of given number.
     *
     * @param numberOfThreads given number of threads
     */
    public ParallelMapperImpl(int numberOfThreads) {
        threads = new ArrayList<>();
        executionTasks = new ArrayDeque<>();

        for (int i = 0; i < numberOfThreads; ++i) {
            threads.add(new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        runTask();
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    Thread.currentThread().interrupt();
                }
            }));

            threads.get(i).start();
        }

    }

    private void addTask(final Task task) throws InterruptedException {
        synchronized (executionTasks) {
            while (executionTasks.size() == MAX_STACK_SIZE) {
                executionTasks.wait();
            }

            executionTasks.add(task);
            executionTasks.notifyAll();
        }
    }

    private void runTask() throws InterruptedException {
        Task task;
        synchronized (executionTasks) {
            while (executionTasks.isEmpty()) {
                executionTasks.wait();
            }

            task = executionTasks.poll();

        }

        task.runnable.run();

        synchronized (task.counter) {
            task.counter.count++;
            if (task.counter.isCompleted()) {
                task.counter.notify();
            }
        }
    }

    private class Task {
        Runnable runnable;
        final SynchronizedCounter counter;

        Task(Runnable runnable, SynchronizedCounter counter) {
            this.runnable = runnable;
            this.counter = counter;
        }
    }

    private class SynchronizedCounter {
        private int count;
        private int neededCount;

        SynchronizedCounter(int neededCount) {
            this.neededCount = neededCount;
        }

        boolean isCompleted() {
            return count >= neededCount;
        }
    }

    private final static int MAX_STACK_SIZE = 100000;
    private List<Thread> threads;
    private final Queue<Task> executionTasks;
}
