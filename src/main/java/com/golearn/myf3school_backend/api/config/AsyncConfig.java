package com.golearn.myf3school_backend.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * FIX: Khai báo @EnableAsync để các method @Async thực sự chạy bất đồng bộ.
 *      Nếu thiếu class này, @Async bị bỏ qua — method vẫn chạy synchronous.
 *
 * Thêm AsyncUncaughtExceptionHandler để các exception trong thread async
 * không bị "nuốt" mà được log ra đầy đủ.
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Thread pool riêng cho notification — tách biệt với request threads.
     * core-size và max-size lấy từ application.properties
     * (spring.task.execution.pool.*) nhưng ở đây ta define tường minh
     * để có thêm threadNamePrefix giúp debug dễ hơn trong log.
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        // FIX: Đặt tên thread rõ ràng → log sẽ hiện "notif-1", "notif-2", ...
        executor.setThreadNamePrefix("notif-");
        executor.initialize();
        return executor;
    }

    /**
     * FIX: Bắt exception từ @Async methods để không bị "nuốt" im lặng.
     * Trước đây nếu sendEmail() ném exception bên trong thread async,
     * không có gì được log → không biết tại sao mail không gửi được.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable ex, Method method, Object... params) {
                log.error("[ASYNC ERROR] Method: {}.{}() | Error: {}",
                        method.getDeclaringClass().getSimpleName(),
                        method.getName(),
                        ex.getMessage(),
                        ex);
            }
        };
    }
}