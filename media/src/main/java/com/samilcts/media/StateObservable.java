package com.samilcts.media;

import rx.Observable;

/**
 * Created by mskim on 2016-07-11.
 * mskim@31cts.com
 */
public interface StateObservable {

    /**
     * when state changed, notify state.
     * @return state observable
     */
    Observable<State> onStateChanged();
}
