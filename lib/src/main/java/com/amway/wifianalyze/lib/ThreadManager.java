package com.amway.wifianalyze.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by big on 2018/11/8.
 */

public class ThreadManager {
    private static ExecutorService mExecutor; // 会自动回收的无界限线程池

    /**
     * @param @param run 设定文件
     * @return void 返回类型
     * @throws
     * @Description: TODO(耗时任务的执行入口函數)
     */
    public static void execute(Runnable task) {
        if (task == null) {
            return;
        }
        getExecutor().execute(task);
    }

    /**
     * 获取一个单例会自动回收的无界线程池
     */
    private static synchronized ExecutorService getExecutor() {
        if (mExecutor == null) {
            mExecutor = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue(), createThreadFactory(10, "ThreadManager-"));
        }
        return mExecutor;
    }

    private static ThreadFactory createThreadFactory(int threadPriority, String threadNamePrefix) {
        return new DefaultThreadFactory(threadPriority, threadNamePrefix);
    }

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final int threadPriority;

        DefaultThreadFactory(int threadPriority, String threadNamePrefix) {
            this.threadPriority = threadPriority;
            this.group = Thread.currentThread().getThreadGroup();
            this.namePrefix = threadNamePrefix + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }

            t.setPriority(this.threadPriority);
            return t;
        }
    }
}
