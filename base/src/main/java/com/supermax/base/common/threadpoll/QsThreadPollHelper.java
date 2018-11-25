package com.supermax.base.common.threadpoll;

import android.os.Looper;

import com.supermax.base.common.log.L;
import com.supermax.base.mvp.model.QsConstants;

/**
 * @Author yinzh
 * @Date   2018/10/16 15:24
 * @Description:线程池管理类
 */
public class QsThreadPollHelper {
    private WorkThreadPoll workThreadPoll;
    private HttpThreadPoll httpThreadPoll;
    private SingleThreadPoll singleThreadPoll;

    private static QsThreadPollHelper instance;

    private QsThreadPollHelper() {
    }

    public static QsThreadPollHelper getInstance() {
        if (instance == null) {
            synchronized (QsThreadPollHelper.class) {
                if (instance == null) instance = new QsThreadPollHelper();
            }
        }
        return instance;
    }

    public boolean isMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    public boolean isWorkThread() {
        return QsConstants.NAME_WORK_THREAD.equals(Thread.currentThread().getName());
    }

    public boolean isHttpThread() {
        return QsConstants.NAME_HTTP_THREAD.equals(Thread.currentThread().getName());
    }

    public boolean isSingleThread() {
        return QsConstants.NAME_SINGLE_THREAD.equals(Thread.currentThread().getName());
    }

    public MainExecutor getMainThread() {
        return MainExecutor.getInstance();
    }

    public WorkThreadPoll getWorkThreadPoll() {
        if(workThreadPoll == null){
            synchronized (this){
                if(workThreadPoll == null) workThreadPoll = new WorkThreadPoll(10);
            }
        }
        return workThreadPoll;
    }

    public HttpThreadPoll getHttpThreadPoll() {
        if (httpThreadPoll == null) {
            synchronized (this) {
                if (httpThreadPoll == null) httpThreadPoll = new HttpThreadPoll(10);
            }

        }
        return httpThreadPoll;
    }

    public SingleThreadPoll getSingleThreadPoll() {
        if (singleThreadPoll == null) {
            synchronized (this) {
                if (singleThreadPoll == null) singleThreadPoll = new SingleThreadPoll();
            }
        }
        return singleThreadPoll;
    }

    public synchronized void shutdown() {
        L.i("QsThreadPollHelper", "shutdown()");
        if (workThreadPoll != null) {
            workThreadPoll.shutdown();
            workThreadPoll = null;
        }
        if (httpThreadPoll != null) {
            httpThreadPoll.shutdown();
            httpThreadPoll = null;
        }
        if (singleThreadPoll != null) {
            singleThreadPoll.shutdown();
            singleThreadPoll = null;
        }
    }

}

