package com.samilcts.media.ble;


import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.samilcts.media.AbstractRxMedia;
import com.samilcts.media.LogTool;
import com.samilcts.media.State;
import com.samilcts.media.exception.BleException;
import com.samilcts.media.exception.ConnectException;
import com.samilcts.media.exception.ScanException;
import com.samilcts.util.android.Logger;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 *
 *  android version >= android 4.3
 * @author mskim
 *
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class RxBle extends AbstractRxMedia implements RxScannable {

    private final PublishSubject<BluetoothGattCharacteristic> characteristicChangeSubject = PublishSubject.create();
    private PublishSubject<BluetoothGattCharacteristic> characteristicWriteSubject = PublishSubject.create();
    private PublishSubject<BluetoothGattCharacteristic> characteristicReadSubject = PublishSubject.create();
    private PublishSubject<Void> serviceDiscoverSubject = PublishSubject.create();
    private PublishSubject<BluetoothGattDescriptor> descriptorWriteSubject = PublishSubject.create();

    private final Logger logger = LogTool.getLogger();

    private final static String TAG = "RxBle";

    private final BluetoothAdapter mBluetoothAdapter;

    private PublishSubject<ScanResult> scanResultSubject  = PublishSubject.create();

    private final Context mContext;
	private BluetoothGatt bluetoothGatt;

    private LeScanCallback leScanCallback;
    private ScanCallback scanCallback;
    private String address;
    private boolean autoConnect;
    private BluetoothLeScanner leScanner;
    private long minInterval = 0;

    //final private Object rwLock = new Object();

    public RxBle(Context context) throws BleException {

        mContext = context;

        mBluetoothAdapter = getAdapter();
	}

    /**
     * set interval of write ( every 20 bytes)
     * @param millis interval time
     */
    public void setWriteInterval(long millis) {
        minInterval = millis;
    }

    /**
     * Set connection settings
     * @param address address to connect
     * @param autoConnect Whether to directly connect to the remote device (false) or
     *                    to automatically connect as soon as the remote device becomes available (true).
     */
    public void setSetting(String address, boolean autoConnect) {

        this.address = address;
        this.autoConnect = autoConnect;
    }

    private static final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");  // Client Characteristic Configuration

	/**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public synchronized void close() {

        try {

            if (bluetoothGatt != null) {

                logger.i(TAG, "close");
                bluetoothGatt.close();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * BLE Callbacks
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        synchronized public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            bluetoothGatt = gatt;

            if ( BluetoothGatt.GATT_SUCCESS == status ) {
                descriptorWriteSubject.onNext(descriptor);
                descriptorWriteSubject.onCompleted();
            } else {
                BleException ex = new BleException("fail write descriptor. gatt status="+status);
                ex.status = status;
                descriptorWriteSubject.onError(ex);
            }
        }

        @Override
        synchronized public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            bluetoothGatt = gatt;

            if ( BluetoothGatt.GATT_SUCCESS == status ) {
        	   
        		  if (newState == BluetoothProfile.STATE_CONNECTED) {

                      logger.i(TAG, "Connected to GATT server.");

                      notifyState(new BleState(State.CONNECTED));


                  } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                      logger.i(TAG, "Disconnected from GATT server.");

                      notifyState(new BleState(State.DISCONNECTED));
                      close();

                  }

        	} else {

                logger.e(TAG, "GATT_SUCCESS NOT");

               if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                   logger.i(TAG, "Disconnected from GATT server.");

                   notifyState(new BleState(State.DISCONNECTED));
                   close();
               }
        	}
                	
          
        }

        @Override
        synchronized public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            bluetoothGatt = gatt;

            if ( BluetoothGatt.GATT_SUCCESS == status ) {
                serviceDiscoverSubject.onNext(null);
                serviceDiscoverSubject.onCompleted();
            } else {
                BleException ex = new BleException("fail discover services. gatt status="+status);
                ex.status = status;
                serviceDiscoverSubject.onError(ex);
            }

        }


        @Override
        synchronized public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            bluetoothGatt = gatt;

            //this needed for prevent overwrite value.
            BluetoothGattCharacteristic copied = new BluetoothGattCharacteristic(characteristic.getUuid(), characteristic.getProperties(), characteristic.getPermissions());
            copied.setValue(characteristic.getValue() == null ? new byte[0] : characteristic.getValue().clone());
            copied.setWriteType(characteristic.getWriteType());
            characteristicChangeSubject.onNext(copied);

        }

        @Override
        synchronized public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {


                //logger.i(TAG,"onWrite : " + Converter.toHexString(characteristic.getValue()));
                bluetoothGatt = gatt;

                boolean isGattSuccess = (BluetoothGatt.GATT_SUCCESS == status);

                if (isGattSuccess) {

                    //this needed for prevent overwrite value.
                    BluetoothGattCharacteristic copied = new BluetoothGattCharacteristic(characteristic.getUuid(), characteristic.getProperties(), characteristic.getPermissions());
                    copied.setValue(characteristic.getValue() == null ? new byte[0] : characteristic.getValue().clone());
                    copied.setWriteType(characteristic.getWriteType());
                    characteristicWriteSubject.onNext(copied);
                    characteristicWriteSubject.onCompleted();

                } else {
                    BleException ex = new BleException("fail write characteristic. gatt status=" + status);
                    ex.status = status;
                    characteristicWriteSubject.onError(ex);
                }

        }

        @Override
        synchronized public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            bluetoothGatt = gatt;

            boolean isGattSuccess =  (BluetoothGatt.GATT_SUCCESS == status);

            if ( isGattSuccess) {
                characteristicReadSubject.onNext(characteristic);
            } else {
                BleException ex = new BleException("fail read gatt status="+status);
                ex.status = status;
                characteristicReadSubject.onError(ex);
            }
        }
    };

    /**
     *
     * @param uuid uuid to find
     * @return BluetoothGattService if supported, or null if the requested service is not offered by the remote device.
     */

    public BluetoothGattService getService(UUID uuid) {

        if ( null != bluetoothGatt) {
            return bluetoothGatt.getService(uuid);
        }

        return null;
    }


    /**
     * start discover services and wait complete
     * @return observable
     */
    public Observable<Void> discoverServices() {

        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                logger.i(TAG, "discoverServices");

                serviceDiscoverSubject = PublishSubject.create();
                Subscription s = serviceDiscoverSubject.onBackpressureBuffer()
                        .observeOn(Schedulers.computation())
                        .subscribe(subscriber);

                subscriber.add(s);

                if (!bluetoothGatt.discoverServices() ) {
                    subscriber.onError(new BleException("fail to start discover services"));
                }
            }
        });
    }

    /**
     * read data from characteristic
     * @param characteristic characteristic to read
     * @return observable
     */
    public Observable<BluetoothGattCharacteristic> read(final BluetoothGattCharacteristic characteristic) {

        return Observable.create(new Observable.OnSubscribe<BluetoothGattCharacteristic>() {
            @Override
            public void call(final Subscriber<? super BluetoothGattCharacteristic> subscriber) {

                characteristicReadSubject = PublishSubject.create();
                Subscription s = characteristicReadSubject.onBackpressureBuffer()
                        .observeOn(Schedulers.computation())
                        .subscribe(subscriber);

                subscriber.add(s);

                if (!bluetoothGatt.readCharacteristic(characteristic)) {
                    subscriber.onError(new BleException("fail initiate read operation"));
                }

            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * write data to characteristic and wait callback until all data wrote
     * @param characteristic characteristic to write
     * @param data data to write
     * @return observable
     */
     public synchronized Observable<BluetoothGattCharacteristic> write(final BluetoothGattCharacteristic characteristic, final byte[] data) {

        final ByteBuffer byteBuffer = ByteBuffer.wrap(data.clone());
        int repeat = (byteBuffer.remaining() / 20);
        repeat += byteBuffer.remaining() % 20 > 0 ? 1 : 0;

         return Observable.create(new Observable.OnSubscribe<BluetoothGattCharacteristic>() {
            @Override
            public void call(final Subscriber<? super BluetoothGattCharacteristic> subscriber) {

                int count = byteBuffer.remaining() >= 20 ? 20 : byteBuffer.remaining();
                byte[] bytes = new byte[count];
                byteBuffer.get(bytes);

                writeBlock(characteristic, bytes)
                        .observeOn(Schedulers.trampoline())
                        .onBackpressureBuffer()
                        .subscribe(new Subscriber<BluetoothGattCharacteristic>() {
                            @Override
                            public void onCompleted() {

                                if ( minInterval > 0) {
                                    try {
                                        Thread.sleep(minInterval);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                subscriber.onCompleted();
                            }

                            @Override
                            public void onError(Throwable e) {
                                subscriber.onError(e);
                            }

                            @Override
                            public void onNext(BluetoothGattCharacteristic characteristic) {
                                //logger.i(TAG, "write1 : " + Converter.toHexString(characteristic.getValue()));
                                subscriber.onNext(characteristic);
                            }
                        });

            }
         }).repeat(repeat).subscribeOn(Schedulers.io());
    }

    /**
     * write data to characteristic and wait callback
     * @param characteristic characteristic to write
     * @param data data to write (max 20 bytes)
     * @return observable
     */
     private synchronized Observable<BluetoothGattCharacteristic> writeBlock(final BluetoothGattCharacteristic characteristic, final byte[] data) {

        return Observable.create(new Observable.OnSubscribe<BluetoothGattCharacteristic>() {
            @Override
            public void call(final Subscriber<? super BluetoothGattCharacteristic> subscriber) {

                characteristic.setValue(data);

                characteristicWriteSubject = PublishSubject.create();
                Subscription s = characteristicWriteSubject.onBackpressureBuffer()
                        .observeOn(Schedulers.trampoline())
                        .subscribe(subscriber);

                subscriber.add(s);


                    if (!bluetoothGatt.writeCharacteristic(characteristic) )  {
                        subscriber.onError(new BleException("fail initiate write operation"));
                    }



            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * write data to descriptor
     * @param descriptor descriptor to write
     * @param data data to write
     * @return observable
     */
    public Observable<BluetoothGattDescriptor> write(final BluetoothGattDescriptor descriptor, final byte[] data) {

        return Observable.create(new Observable.OnSubscribe<BluetoothGattDescriptor>() {
            @Override
            public void call(Subscriber<? super BluetoothGattDescriptor> subscriber) {

                descriptorWriteSubject = PublishSubject.create();
                Subscription s = descriptorWriteSubject.onBackpressureBuffer()
                        .observeOn(Schedulers.computation())
                        .subscribe(subscriber);

                subscriber.add(s);

                descriptor.setValue(data);


                boolean isSuccess = bluetoothGatt.writeDescriptor(descriptor);

                if (!isSuccess) {
                    subscriber.onError(new BleException("fail initiate writeDescriptor operation"));
                }


            }
        }).subscribeOn(Schedulers.io());
    }


    /**
     * enable of disable notification of characteristic
     * @param enabled enable
     * @param characteristic characteristic to change
     * @return observable
     */
	public Observable<Void> enableNotification(final boolean enabled,
                                      final BluetoothGattCharacteristic characteristic) {

        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {


                boolean isSuccess = bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

                if (!isSuccess) {

                    subscriber.onError(new BleException("fail set notification status"));
                    return;
                }


                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CCC);
                write(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        .map(new Func1<BluetoothGattDescriptor, Void>() {
                            @Override
                            public Void call(BluetoothGattDescriptor descriptor) {
                                return null;
                            }
                        })
                .subscribe(subscriber);

            }
        }).subscribeOn(Schedulers.io());

	}

    private LeScanCallback getLeScanCallback() {

        if ( null == leScanCallback ) {
            leScanCallback = new LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

                    scanResultSubject.onNext(new ScanResult(device, rssi, scanRecord));
                }
            };
        }

        return leScanCallback;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback getScanCallback() {

        if ( null == scanCallback ) {
            scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, android.bluetooth.le.ScanResult scanResult) {

                    byte[] record = scanResult.getScanRecord() == null ? new byte[0] : scanResult.getScanRecord().getBytes();
                    logger.i(TAG, "onScanResult : " + scanResult.getDevice().getAddress());
                    scanResultSubject.onNext(new ScanResult(scanResult.getDevice(),
                            scanResult.getRssi(),record));
                }

                @Override
                public void onBatchScanResults(List<android.bluetooth.le.ScanResult> results) {
                    logger.i(TAG, "onBatchScanResults : " );

                    Observable.from(results)
                            .map(new Func1<android.bluetooth.le.ScanResult, ScanResult>() {
                                @Override
                                public ScanResult call(android.bluetooth.le.ScanResult scanResult) {
                                    byte[] record = scanResult.getScanRecord() == null ? new byte[0]
                                            : scanResult.getScanRecord().getBytes();
                                    logger.i(TAG, "onBatchScanResults : " + scanResult.getDevice().getAddress());
                                    return new ScanResult(scanResult.getDevice(), scanResult.getRssi(),
                                            record);
                                }
                            })
                            .subscribe(scanResultSubject);

                }

                @Override
                public void onScanFailed(int errorCode) {
                    ScanException e = new ScanException("scan fail errorCode="+errorCode);
                    e.status = errorCode;
                    scanResultSubject.onError(e);

                }
            };
        }

        return scanCallback;
    }

    @Override
    public Observable<ScanResult> scan() {

        return Observable.create(new Observable.OnSubscribe<ScanResult>() {
            @Override
            public void call(final Subscriber<? super ScanResult> subscriber) {

                scanResultSubject = PublishSubject.create();

                subscriber.onStart();

                if ( !hasPermissionForScan() ) {

                    subscriber.onError(new ScanException("need permission 'ACCESS_COARSE_LOCATION' or 'ACCESS_FINE_LOCATION'"));

                } else if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {

                    leScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    leScanner.startScan(getScanCallback());
                    scanResultSubject.onBackpressureBuffer().subscribe(subscriber);

                } else if ( mBluetoothAdapter.startLeScan(getLeScanCallback())) {
                    scanResultSubject.onBackpressureBuffer().subscribe(subscriber);
                } else {
                    subscriber.onError(new ScanException("scan not started"));
                }

            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {

                scanResultSubject.onCompleted();

                try {

                    if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && null != leScanner) {

                        leScanner.stopScan(scanCallback);
                        logger.i(TAG, "ble scan stopped");

                    } else if ( mBluetoothAdapter != null) {
                        mBluetoothAdapter.stopLeScan(leScanCallback);
                        logger.i(TAG, "ble scan stopped");
                    }

                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }

            }
        }).subscribeOn(Schedulers.computation());


    }


    /**
     * check whether required permissions are granted.
     * @return true, if granted.
     */
    public boolean hasPermissionForScan() {

        if ( Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {

            int permissionCheck = mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

            int permissionCheck2 = mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

            if (  PackageManager.PERMISSION_GRANTED != permissionCheck &&
                    PackageManager.PERMISSION_GRANTED != permissionCheck2) {

                return false;
            }

        }

        return true;
    }

    /**
     * disconnect with device
     */
    synchronized public void disconnect() {

        logger.i(TAG, "disconnecting");

        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }

	}


    /**
     * get bluetooth adapter
     * @return bluetoothManager
     * @throws BleException
     */
    private BluetoothAdapter getAdapter() throws BleException {

        BluetoothManager bluetoothManager =  (BluetoothManager) mContext
                .getSystemService(Context.BLUETOOTH_SERVICE);

        if (null == bluetoothManager ) {

            throw new BleException("can not get bluetooth manager");
        }

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {

            throw new BleException("can not get bluetooth adapter");
        }

        return bluetoothAdapter;
    }

    @Override
    public Observable<Void> connect() {

        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {

                if ( State.CONNECTING == mState || State.CONNECTED == mState) {
                    subscriber.onError(new BleException("already connected or connecting"));
                    return;
                }

                try {

                    final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    bluetoothGatt = device.connectGatt(mContext, autoConnect, mGattCallback);

                    Subscription subscription = onStateChanged()
                            .observeOn(Schedulers.computation())
                            .subscribe(new Subscriber<State>() {
                                @Override
                                public void onCompleted() {

                                    subscriber.onError(new ConnectException("connection state listening is completed"));
                                }

                                @Override
                                public void onError(Throwable e) {

                                    subscriber.onError(e);
                                }

                                @Override
                                public void onNext(State state) {

                                    if (State.CONNECTED == state.getValue()) {
                                        subscriber.onCompleted();
                                    } else if (State.DISCONNECTED == state.getValue()) {
                                        subscriber.onError(new ConnectException("fail to connect"));
                                    }

                                }
                            });

                    notifyState(new BleState(State.CONNECTING));
                    subscriber.add(subscription);


                } catch (Exception e) {

                    subscriber.onError(e);

                }

            }
        }).subscribeOn(Schedulers.trampoline());

    }


    /**
     * check connection mState
     * @return true, if connected
     */
    public boolean isConnected() {

        return State.CONNECTED == mState;
    }

    public Observable<BluetoothGattCharacteristic> onCharacteristicChanged() {

        return characteristicChangeSubject.onBackpressureBuffer().observeOn(Schedulers.computation());
    }


}
