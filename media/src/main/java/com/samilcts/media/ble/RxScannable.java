package com.samilcts.media.ble;

import rx.Observable;

/**
 * Created by mskim on 2016-07-11.
 * mskim@31cts.com
 */
public interface RxScannable {

    Observable<ScanResult> scan();
}
