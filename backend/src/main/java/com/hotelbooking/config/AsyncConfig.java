package com.hotelbooking.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * Enables {@code @Async} for notification emails (and other background work)
 * so payment/booking request threads are not blocked by I/O.
 * <p>
 * Implements {@link AsyncConfigurer} to supply a bounded pool instead of the
 * default unbounded {@code SimpleAsyncTaskExecutor}, which can exhaust threads under load.
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 8;
    private static final int QUEUE_CAPACITY = 100;
    private static final int AWAIT_TERMINATION_SECONDS = 30;

    /**
     * Executor used by bare {@code @Async} methods (e.g. {@code AsyncNotificationFacade}).
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("hotel-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS);
        executor.initialize();
        log.info(
                "Async executor ready core={}, max={}, queue={}, prefix=hotel-async-",
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                QUEUE_CAPACITY
        );
        return executor;
    }

    /**
     * Last-resort handler when an {@code @Async} method throws and the caller cannot catch it.
     * Façades should still try/catch internally; this covers unexpected failures.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error(
                "Uncaught async error method={} params={} message={}",
                method.getDeclaringClass().getSimpleName() + "#" + method.getName(),
                Arrays.toString(params),
                ex.getMessage(),
                ex
        );
    }
}
