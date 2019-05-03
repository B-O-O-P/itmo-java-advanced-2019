package ru.ifmo.rain.chizhikov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Basic implementation of {@link info.kgeorgiy.java.advanced.crawler.Crawler}.
 */
public class WebCrawler implements Crawler {

    /**
     * Main function, which performs crawling of specified url, using specified
     * number of thread for downloading and extracting.
     * <p>
     * Usage: WebCrawler url [downloaders [extractors [perHost]]]
     *
     * @param args array of string arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length < 1 || args.length > 4) {
            System.err.println("Usage: WebCrawler url [downloaders [extractors [perHost]]]");
            return;
        }

        int downloaders = getArgument(args, 1, 16);
        int extractors = getArgument(args, 2, 16);
        int perHost = getArgument(args, 3, 4);
        int depth = getArgument(args, 4, 2);
        String url = args[0];

        try {
            Downloader downloader = new CachingDownloader();
            WebCrawler webCrawler = new WebCrawler(downloader, downloaders, extractors, perHost);
            webCrawler.download(url, depth);
        } catch (IOException e) {
            System.out.println("Unable to create of CachingDownloader: " + e.getMessage());
        }
    }

    /**
     * Gets list of all URLs, that were visited by crawler, starting from {@code url}
     * and lifting by {@code depth} down as most.
     *
     * @param url   url, specifying starting position of crawler
     * @param depth maximal depth of web-pages, which will be visited by crawler
     * @return {@link info.kgeorgiy.java.advanced.crawler.Result}, containing list of all
     * downloaded links and all errors, which happened during execution
     */
    @Override
    public Result download(String url, int depth) {
        Set<String> result = ConcurrentHashMap.newKeySet();
        Set<String> usedLinks = ConcurrentHashMap.newKeySet();
        usedLinks.add(url);
        ConcurrentMap<String, IOException> exceptions = new ConcurrentHashMap<>();

        Phaser sync = new Phaser(1);
        recursiveDownload(url, result, exceptions, usedLinks, sync, depth);
        sync.arriveAndAwaitAdvance();

        return new Result(new ArrayList<>(result), exceptions);
    }

    /**
     * Shutdowns all threads, created by crawler. All invocations of {@link
     * #download(String, int)}, that didn't finish yet, will return {@code null}
     * as result.
     */
    @Override
    public void close() {
        downloaders.shutdownNow();
        extractors.shutdownNow();
    }

    /**
     * Class constructor, specifying what {@link Downloader} to use, number of threads,
     * which download, number of threads, which extract and maximal number of threads,
     * which can download from the same host simultaneously ({@code perHost}).
     *
     * @param downloader  downloader, which will be used to get web-page
     * @param downloaders number of threads for downloading
     * @param extractors  number of threads for extracting links
     * @param perHost     maximal number of threads, which can download from the same
     *                    host simultaneously
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.hosts = new ConcurrentHashMap<>();
    }

    private static int getArgument(String[] args, int index, int defaultValue) {
        return args.length > index ? Integer.parseInt(args[index]) : defaultValue;
    }

    private class Data {
        final Queue<Runnable> tasksQueue;
        int loaded = 0;

        private Data() {
            tasksQueue = new ArrayDeque<>();
        }

        private synchronized void addTask(Runnable task) {
            if (loaded < perHost) {
                loaded++;
                downloaders.submit(task);
            } else {
                tasksQueue.add(task);
            }
        }
    }

    private void recursiveDownload(String url,
                                   Set<String> result,
                                   ConcurrentMap<String, IOException> exceptions,
                                   Set<String> usedLinks, Phaser sync, int depth) {
        try {
            String host = URLUtils.getHost(url);
            Data data = hosts.computeIfAbsent(host, s -> new Data());

            sync.register();
            data.addTask(() -> {
                try {
                    Document downloaded = downloader.download(url);
                    result.add(url);

                    if (depth != 1) {
                        sync.register();

                        Runnable extractorTask = () -> {
                            try {
                                for (String link : downloaded.extractLinks()) {
                                    if (usedLinks.add(link)) {
                                        recursiveDownload(link, result, exceptions, usedLinks, sync, depth - 1);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                sync.arrive();
                            }
                        };

                        extractors.submit(extractorTask);
                    }
                } catch (IOException e) {
                    exceptions.put(url, e);
                } finally {
                    sync.arrive();

                    synchronized (data) {
                        Runnable other = data.tasksQueue.poll();

                        if (other != null) {
                            downloaders.submit(other);
                        } else {
                            --data.loaded;
                        }
                    }
                }
            });
        } catch (MalformedURLException e) {
            exceptions.put(url, e);
        }
    }

    private Downloader downloader;
    private int perHost;
    private ExecutorService downloaders;
    private ExecutorService extractors;
    private ConcurrentMap<String, Data> hosts;
}
