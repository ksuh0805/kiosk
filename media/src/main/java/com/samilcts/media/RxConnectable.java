package com.samilcts.media;

import rx.Observable;

/**
 * Created by mskim on 2016-09-01.
 * mskim@31cts.com
 */
public interface RxConnectable {
    Observable<Void> connect();
}
