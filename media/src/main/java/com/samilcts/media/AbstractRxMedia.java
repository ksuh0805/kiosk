package com.samilcts.media;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by mskim on 2016-07-13.
 * mskim@31cts.com
 */
public abstract class AbstractRxMedia implements StateObservable, RxConnectable {

    protected int mState = State.DISCONNECTED;

    private final PublishSubject<State> stateSubject = PublishSubject.create();
    protected void notifyState(State state) {

        mState = state.getValue();
        stateSubject.onNext(state);

    }

    @Override
    public Observable<State> onStateChanged() {

        return stateSubject.onBackpressureBuffer().distinctUntilChanged(new Func1<State, Integer>() {
            @Override
            public Integer call(State state) {
                return state.getValue();
            }
        }).subscribeOn(Schedulers.trampoline());
    }

}
