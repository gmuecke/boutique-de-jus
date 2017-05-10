package io.bdj.web;

import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class FileWatcher implements AutoCloseable {

    private static final Logger LOG = getLogger(FileWatcher.class.getName());

    private static final byte[] EMPTY_MD5 = new byte[0];

    private final Path watchedFile;
    private final Path containingDir;
    private final ExecutorService executor;
    private final WatchService watchService;
    private final CompletableFuture<Object> done;

    private final List<WatchKey> watchKeys = new CopyOnWriteArrayList<>();
    private final Map<WatchEvent.Kind<?>, List<Consumer<Path>>> handlers = new ConcurrentHashMap<>();

    public FileWatcher(final Path watchedFile) throws IOException {

        this.watchedFile = watchedFile;
        this.containingDir = watchedFile.getParent();
        this.executor = Executors.newFixedThreadPool(1);
        this.watchService = FileSystems.getDefault().newWatchService();
        this.done = new CompletableFuture<>();

        this.executor.submit(() -> {
            while (!done.isDone()) {
                watchKeys.forEach(watchKey -> watchKey.pollEvents()
                                                      .stream()
                                                      .map(event -> (WatchEvent<Path>) event)
                                                      .map(event -> new Tuple<>(event.kind(),
                                                                                containingDir.resolve(event.context())))
                                                      .filter(t -> watchedFile.equals(t.second))
                                                      .filter(t -> filesize(t.second) > 0)
                                                      .findFirst()
                                                      .ifPresent(t -> handlers.get(t.first)
                                                                              .forEach(h -> h.accept(t.second))));

                //purge watchkeys all watchkeys that are no longer valid
                watchKeys.retainAll(watchKeys.stream().filter(WatchKey::reset).collect(toList()));

                if (!sleep(500)) {
                    done.complete(null);
                }
            }
        });

    }

    public static long filesize(Path p) {

        try {
            return Files.size(p);
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Puts the current thread to sleep for the specified amount of milliseconds. If the sleep gets
     * interrupted, the method return false, otherwise true if the sleep is not interrupted
     *
     * @param time
     *         milliseconds for the sleep duration
     *
     * @return true if the sleep completed without being interrupted
     */
    private boolean sleep(long time) {

        try {
            Thread.sleep(time);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public FileWatcher on(WatchEvent.Kind<?> event, Consumer<Path> changeHandler) throws IOException {
        synchronized (this) {
            final WatchKey watchKey = this.containingDir.register(this.watchService, event);
            this.handlers.putIfAbsent(event, new CopyOnWriteArrayList<>());
            this.handlers.get(event).add(changeHandler);
            this.watchKeys.add(watchKey);
        }
        return this;
    }

    /**
     * Calculates the MD5 hash of a file
     *
     * @param file
     *         the path to the file. The method does not chech if it's a real file or path
     *
     * @return the byte sequence representing the MD5 hash or an empty byte array if the hash could not be computed
     */
    public static byte[] md5(Path file) {

        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            try (DigestInputStream dis = new DigestInputStream(Files.newInputStream(file), md)) {
                while (dis.read() != -1) {
                    //noop
                }
                return md.digest();
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Can not calculate MD5 of " + file, e);
        }
        return EMPTY_MD5;
    }

    @Override
    public void close() throws Exception {

        this.done.complete(null);
        this.executor.shutdownNow();
    }

    private static class Tuple<T1, T2> {

        private final T1 first;
        private final T2 second;

        public Tuple(final T1 first, final T2 second) {

            this.first = first;
            this.second = second;
        }
    }
}
