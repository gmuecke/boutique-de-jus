package io.bdj.web;

import static java.util.logging.Logger.getLogger;
import static java.util.stream.Collectors.toList;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * File watcher than can be used to monitor a single file. Handlers can be registered to react on specific watch
 * events. The method provides a convenient access to the nio.files Watch API
 */
public class FileWatcher implements AutoCloseable {

    private static final Logger LOG = getLogger(FileWatcher.class.getName());

    private static final byte[] EMPTY_MD5 = new byte[0];

    private final Path containingDir;
    private final ScheduledExecutorService executor;
    private final WatchService watchService;
    private final CompletableFuture<Object> done;

    private final List<WatchKey> watchKeys = new CopyOnWriteArrayList<>();
    private final Map<WatchEvent.Kind<?>, List<Consumer<Path>>> handlers = new ConcurrentHashMap<>();

    public FileWatcher(final Path watchedFile) throws IOException {

        this.containingDir = watchedFile.getParent();
        this.executor = Executors.newScheduledThreadPool(1);
        this.watchService = FileSystems.getDefault().newWatchService();
        this.done = new CompletableFuture<>();

        this.executor.scheduleAtFixedRate(() -> {
            watchKeys.forEach(watchKey -> processWatchKey(watchKey, watchedFile));
            watchKeys.retainAll(watchKeys.stream().filter(WatchKey::reset).collect(toList()));
        }, 250, 100, TimeUnit.MILLISECONDS);

    }

    private void processWatchKey(final WatchKey watchKey, final Path watchedFile) {

        watchKey.pollEvents()
                .stream()
                .map(event -> new Tuple<>(event.kind(), containingDir.resolve((Path) event.context())))
                .filter(t -> watchedFile.equals(t.second) && filesize(t.second) > 0)
                .findFirst()
                .ifPresent(t -> handlers.get(t.first).forEach(h -> {
                    try {
                        h.accept(t.second);
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Error processing watch event for file " + watchedFile, e);
                    }
                }));
    }

    /**
     * Calculates the size of the file without throwing an exception. If the file size can not be determined due
     * to an exception, the method returns -1
     * @param p
     *  the path to the file
     * @return
     *  either the filesize or -1 if the size can not be determined
     */
    static long filesize(Path p) {

        try {
            return Files.size(p);
        } catch (IOException e) {
            return -1;
        }
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
            final MessageDigest md5 = MessageDigest.getInstance("MD5");
            try (DigestInputStream dis = new DigestInputStream(new BufferedInputStream(Files.newInputStream(file)), md5)) {
                while (dis.read() != -1) {
                    //noop
                }
                return md5.digest();
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Can not calculate MD5 of " + file, e);
        }
        return EMPTY_MD5;
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

    /**
     * Registers a handler to be notified upon events of the specified kind.
     * @param kind
     *  kind of event to notified the handler
     * @param changeHandler
     *  the action to perform on the file upon the occurrence of events of the specified kind.
     * @return
     *  this watcher
     * @throws IOException
     */
    public FileWatcher on(WatchEvent.Kind<?> kind, Consumer<Path> changeHandler) throws IOException {

        synchronized (this) {
            final WatchKey watchKey = this.containingDir.register(this.watchService, kind);
            this.handlers.putIfAbsent(kind, new CopyOnWriteArrayList<>());
            this.handlers.get(kind).add(changeHandler);
            this.watchKeys.add(watchKey);
        }
        return this;
    }

    @Override
    public void close() throws Exception {

        this.done.complete(null);
        this.executor.shutdownNow();
    }

    /**
     * Tries to obtain a write lock on the file. This can be used to verify that no other processes concurrently
     * writes to the file, for example to check that writing to a file, i.e. on a MODIFY_ENTRY event, is completed
     * @param path
     *  the path to the file
     * @return
     *  true, if the lock could be obtained. False, if not. Please not that the lock is released once the method
     *  returns!
     */
    public static boolean tryWriteLock(final Path path) {
        try(FileChannel ch = FileChannel.open(path, StandardOpenOption.WRITE);
            FileLock lock = ch.lock()){

            if(lock == null) {
                LOG.log(Level.INFO, "Could not obtain lock on file, maybe still writing");
                return false;
            } else {
                return true;
            }
        } catch (IOException e) {
            LOG.log(Level.INFO, "File not open for write access", e);
            return false;
        }
    }

    private static class Tuple<T1, T2> {

        private final T1 first;
        private final T2 second;

        Tuple(final T1 first, final T2 second) {

            this.first = first;
            this.second = second;
        }
    }
}
