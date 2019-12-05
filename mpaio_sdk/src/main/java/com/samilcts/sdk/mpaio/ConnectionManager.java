package com.samilcts.sdk.mpaio;



import com.samilcts.media.StateObservable;
import com.samilcts.media.ble.RxScannable;

import rx.Observable;

/**
 * Created by mskim on 2016-07-11.
 * mskim@31cts.com
 */
public interface ConnectionManager extends RxScannable, StateObservable {

    /**
     * connect with bluetooth
     * @param address ble address ("XX:XX:XX:XX:XX:XX")
     * @param autoConnect auto connect or not
     * @return observable
     */
     Observable<Void> connect(String address, boolean autoConnect);

    /**
     * connect with cable
     * @return observable
     */
    Observable<Void> connect();
}


