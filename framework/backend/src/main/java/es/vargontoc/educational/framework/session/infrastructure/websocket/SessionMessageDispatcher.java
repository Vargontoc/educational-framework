package es.vargontoc.educational.framework.session.infrastructure.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SessionMessageDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionMessageDispatcher.class);

    private final ExecutorService executor;
    private final ConcurrentHashMap<Long, SessionQueue> queues = new ConcurrentHashMap<>();
    private final int pendingLimit;
    private final WebSocketMetrics metrics;

    public SessionMessageDispatcher(int pendingLimit, WebSocketMetrics metrics) {
        this.pendingLimit = pendingLimit;
        this.metrics = metrics;
        this.executor = Executors.newThreadPerTaskExecutor(
            Thread.ofVirtual().name("ws-session-", 0).factory()
        );
        metrics.registerGauge("ws.dispatcher.queues", "dispatcher", this::queueCount);
        metrics.registerGauge("ws.dispatcher.pending", "dispatcher", this::totalPending);
    }

    public boolean dispatch(Long childSessionId, Runnable task) {
        SessionQueue queue = queues.computeIfAbsent(childSessionId, k -> new SessionQueue(pendingLimit));
        if (!queue.offer(task)) {
            LOGGER.warn("Pending limit exceeded for childSessionId={}", childSessionId);
            return false;
        }
        queue.tryProcess(executor);
        return true;
    }

    public void dispatchHeartbeat(Long childSessionId, Runnable task) {
        executor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.error("Error in heartbeat task for childSessionId={}: {}", childSessionId, e.getMessage());
            }
        });
    }

    public void dispatchOrClose(Long childSessionId, Runnable task, Runnable onLimitExceeded) {
        SessionQueue queue = queues.computeIfAbsent(childSessionId, k -> new SessionQueue(pendingLimit));
        if (!queue.offer(task)) {
            LOGGER.warn("Pending limit exceeded for childSessionId={}, closing", childSessionId);
            queues.remove(childSessionId, queue);
            queue.discard();
            onLimitExceeded.run();
        } else {
            queue.tryProcess(executor);
        }
    }

    public void closeSession(Long childSessionId) {
        SessionQueue queue = queues.remove(childSessionId);
        if (queue != null) {
            int discarded = queue.discard();
            if (discarded > 0) {
                LOGGER.info("Discarded {} pending messages for closed childSessionId={}", discarded, childSessionId);
            }
        }
    }

    public int queueCount() {
        return queues.size();
    }

    public int totalPending() {
        int total = 0;
        for (SessionQueue q : queues.values()) {
            total += q.pendingCount();
        }
        return total;
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    public boolean awaitQuiescence(long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (queues.isEmpty()) return true;
            boolean allIdle = true;
            for (SessionQueue q : queues.values()) {
                if (q.pendingCount() > 0 || q.isRunning()) {
                    allIdle = false;
                    break;
                }
            }
            if (allIdle) return true;
            Thread.sleep(5);
        }
        return totalPending() == 0;
    }

    static class SessionQueue {
        private static final Logger LOGGER = LoggerFactory.getLogger(SessionQueue.class);

        private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
        private final AtomicInteger count = new AtomicInteger(0);
        private final AtomicBoolean running = new AtomicBoolean(false);
        private final int limit;
        private volatile boolean closed = false;

        SessionQueue(int limit) {
            this.limit = limit;
        }

        boolean offer(Runnable task) {
            if (closed) return false;
            int current = count.incrementAndGet();
            if (current > limit) {
                count.decrementAndGet();
                return false;
            }
            queue.add(task);
            return true;
        }

        void tryProcess(ExecutorService executor) {
            if (running.compareAndSet(false, true)) {
                executor.submit(() -> runAll(executor));
            }
        }

        private void runAll(ExecutorService executor) {
            try {
                Runnable task;
                while ((task = queue.poll()) != null) {
                    try {
                        task.run();
                    } catch (Exception e) {
                        LOGGER.error("Error in session queue task: {}", e.getMessage(), e);
                    } finally {
                        count.decrementAndGet();
                    }
                }
            } finally {
                running.set(false);
                if (!queue.isEmpty()) {
                    tryProcess(executor);
                }
            }
        }

        int discard() {
            closed = true;
            int discarded = 0;
            while (queue.poll() != null) {
                count.decrementAndGet();
                discarded++;
            }
            return discarded;
        }

        int pendingCount() {
            return count.get();
        }

        boolean isRunning() {
            return running.get();
        }
    }
}
