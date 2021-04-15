package com.dailyyoga.cn.media.example;

import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * @author: YougaKingWu@gmail.com
 * @created on: 4/15/21 2:26 PM
 * @description:
 */
public class RxScheduler {

    /** io线程 */
    @NonNull
    public static Scheduler io() {
        return Schedulers.io();
    }

    /** main线程 */
    @NonNull
    public static Scheduler main() {
        return AndroidSchedulers.mainThread();
    }

    /** 线程调度 通用 */
    @NonNull
    public static <T> ObservableTransformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(io())
                .unsubscribeOn(io())
                .observeOn(main());
    }

}
