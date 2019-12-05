package com.samilcts.media.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;


import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.google.android.things.pio.UartDevice;
import com.samilcts.media.AbstractRxMedia;
import com.samilcts.media.LogTool;
import com.samilcts.media.State;
import com.samilcts.media.exception.MediaException;
import com.samilcts.media.exception.UsbException;
import com.samilcts.util.android.Converter;
import com.samilcts.util.android.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;
/**
 * Created by mskim on 2016-07-13.
 * mskim@31cts.com
 */

public class RxUsb extends AbstractRxMedia {

    private static final String TAG = "RxUsb";
    private final UsbManager mUsbManager;
    private final Context mContext;
    private final SubscriptionList mSubscriptionList;
    private final Logger logger = LogTool.getLogger();

    private UsbDevice mUsbDevice;
    private UsbSerialDevice mUsbSerialDevice;
    private int mUseClassType;
    private int mEndPointType;
    private UsbEndpoint mEpIn = null;
    private UsbEndpoint mEpOut = null;
    private UsbDeviceConnection mConnection;
    private BroadcastReceiver usbStateReceiver;
    private UartDevice mLoopbackDevice;
//    private final Object rwLock = new Object();
    private int mVendorId;
    private int mProductId;

    private long requestNumber = 1;
    private UsbRequest request;
    private Lock reentrantLock = new ReentrantLock();
    private byte[] receiveBuffer;

    //usb-serial
    private UsbSerialInterface.UsbReadCallback usbSerialReadCallback = null;

    private int baudRate = 57600;
    private int dataBits = UsbSerialInterface.DATA_BITS_8;
    private int stopBits = UsbSerialInterface.STOP_BITS_1;
    private int parity = UsbSerialInterface.PARITY_NONE;
    private int flowControl = UsbSerialInterface.FLOW_CONTROL_OFF;

    public RxUsb(Context context,int vendorId, int productId) throws UsbException {

        mContext = context;
        mUsbManager = getManager();
        mSubscriptionList = new SubscriptionList();

        mVendorId = vendorId;
        mProductId = productId;

        listenUsbAttach();
    }

    private void listenUsbAttach() {

        // USB 접속 정보 리시버
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        usbStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                String action = intent.getAction();

                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {

                    if ( !findUsbDevice().isEmpty() )
                        notifyState(new UsbState(UsbState.STATE_ATTACHED));

                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                    if(  findUsbDevice().isEmpty() ) {
                        notifyState(new UsbState(UsbState.STATE_DETACHED));
                        notifyState(new UsbState(UsbState.DISCONNECTED));
                    }

                }
            }
        };

        mContext.registerReceiver(usbStateReceiver, filter);
    }

    private UsbManager getManager() throws UsbException {

        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        if ( null == usbManager )
            throw new UsbException("can not get usb manager");

        return usbManager;
    }

    public void setDevice(UsbDevice usbDevice) {

        mUsbDevice = usbDevice;
    }

    /**
     * vId & pId is -1 then use usb-serial device.
     * @param vendorId
     * @param productId
     */
    public void setTargetId(int vendorId, int productId) {

        this.mVendorId = vendorId;
        this.mProductId = productId;
    }

    public void setInterfaceType(int usbClassType, int endPointType) {

        mUseClassType = usbClassType;
        mEndPointType = endPointType;
    }


    /**
     * set usb-serial baudrate
     * @param baudRate default 57600
     * @param dataBits default 8
     * @param stopBits default 1
     * @param parity default 0 (none)
     * @param flowControl default 0 (off)
     */
    public void setSerialConfig(int baudRate, int dataBits, int stopBits, int parity, int flowControl)
    {
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
        this.flowControl = flowControl;

        if ( null != mUsbSerialDevice ) {

            mUsbSerialDevice.setBaudRate(baudRate);
            mUsbSerialDevice.setDataBits(dataBits);
            mUsbSerialDevice.setStopBits(stopBits);
            mUsbSerialDevice.setParity(parity);
            mUsbSerialDevice.setFlowControl(flowControl);
            mUsbSerialDevice.setDTR(true);
            mUsbSerialDevice.setRTS(true);
        }

    }


    @Override
    public Observable<Void> connect() {

        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {

                if (State.CONNECTING == mState || State.CONNECTED == mState) {
                    logger.i(TAG, "Usb connecting already tried. return.");
                    subscriber.onError(new MediaException("already connected or connecting"));
                    return;
                }

                logger.d(TAG, "Trying to create a new connection.");


                notifyState(new UsbState(State.CONNECTING));

                UsbInterface usbInterface;

                if (null == mUsbDevice) {
                    subscriber.onError(new UsbException("set device first"));
                    notifyState(new UsbState(State.DISCONNECTED));
                    return;
                } else if (!hasPermission(mUsbDevice)) {
                    subscriber.onError(new UsbException("has no permission for " + mUsbDevice.getDeviceName()));
                    notifyState(new UsbState(State.DISCONNECTED));
                    return;
                } else if (null == (usbInterface = findInterface(mUseClassType))) {
                    subscriber.onError(new UsbException("has no interface class " + mUseClassType));
                    notifyState(new UsbState(State.DISCONNECTED));
                    return;
                }

                if (!setEndpoint(usbInterface, mEndPointType)) {
                    subscriber.onError(new UsbException("has no in out end point " + mUsbDevice.getDeviceName()));
                    return;
                }

                boolean isConnected = false;

                mConnection = mUsbManager.openDevice(mUsbDevice);

                if (null != mConnection && mVendorId == -1 && mProductId == -1 && UsbSerialDevice.isSupported(mUsbDevice) ) {

                    //usb-serial device
                    mUsbSerialDevice = UsbSerialDevice.createUsbSerialDevice(mUsbDevice, mConnection);

                    //sync methods fail to read large data..
                    if ( isConnected = mUsbSerialDevice.open() ) {
                        setSerialConfig(baudRate, dataBits, stopBits, parity, flowControl);
                    }

                } else if (null != mConnection && mConnection.claimInterface(usbInterface, true)) {
                    request = new UsbRequest();
                    request.initialize(mConnection, mEpIn);
                    isConnected = true;
                }

                if (isConnected) {

                    subscriber.onCompleted();
                    notifyState(new UsbState(State.CONNECTED));
                } else {
                    subscriber.onError(new UsbException("fail to open device : " + mUsbDevice.getDeviceName()));
                    notifyState(new UsbState(State.DISCONNECTED));
                }
            }
        });

    }

    /**
     * check whether device has permission.
     * @param usbDevice device to check.
     * @return true, if permission granted.
     */
    public boolean hasPermission(UsbDevice usbDevice) {
        return mUsbManager.hasPermission(usbDevice);
    }

    private ByteBuffer usbSerialBuffer = null;

    private final Object serialLock = new Object();

    public Observable<byte[]> read(final int timeout) {

        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(final Subscriber<? super byte[]> subscriber) {

                    if (null == mConnection) {
                        subscriber.onError(new UsbException("not connected"));
                        return;
                    } else if (null == mEpIn) {
                        subscriber.onError(new UsbException("mEpIn null"));
                        return;
                    }

                    if ( null != mUsbSerialDevice ) {

                        if ( null == usbSerialReadCallback ) {

                            usbSerialBuffer = ByteBuffer.allocate(1024 * 16);
                            usbSerialReadCallback= new UsbSerialInterface.UsbReadCallback() {
                                @Override
                                public void onReceivedData(byte[] bytes)
                                {
                                    logger.d(TAG, "usb serial" + " len " + bytes.length + " / " + Converter.toHexString(bytes));

                                    synchronized (serialLock) {

                                        try {
                                            usbSerialBuffer.put(bytes);
                                        } catch (Exception ex) {
                                            subscriber.onError(ex);
                                        }
                                    }
                                }
                            };

                            mUsbSerialDevice.read(usbSerialReadCallback);
                        }

                        synchronized (serialLock) {

                            int pos = usbSerialBuffer.position();

                            if (pos > 0) {

                                byte[] temp = new byte[pos];
                                usbSerialBuffer.position(0);
                                usbSerialBuffer.get(temp);
                                usbSerialBuffer.clear();
                                subscriber.onNext(temp);
                            }

                            subscriber.onCompleted();
                        }

                    } else {

                        reentrantLock.lock();

                        try {

                            int length = mConnection.bulkTransfer(mEpIn, receiveBuffer, receiveBuffer.length, timeout); // 타임아웃0은 무한대기.

                            if (length > 0) {

                                byte result[] = new byte[length];
                                System.arraycopy(receiveBuffer, 0, result, 0, length);
                                subscriber.onNext(result);
                            }

                            subscriber.onCompleted();

                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                        Arrays.fill(receiveBuffer, (byte) 0x00);

                        reentrantLock.unlock();
                    }
            }
        }).subscribeOn(Schedulers.io());

    }

    /**
     * set in, out endpoint
     * @param usbInterface interface to set
     * @param endPointType type of endpoint
     * @return true, if in, out endpoint set successfully.
     */
    private boolean setEndpoint(UsbInterface usbInterface, int endPointType) {

        mEpIn = null;
        mEpOut = null;

        int count = usbInterface.getEndpointCount();
        logger.v(TAG, "count : "+count);
        for (int i = 0; i < count; i++) {
            UsbEndpoint endpoint = usbInterface.getEndpoint(i);

            logger.v(TAG, "endpoint type : "+endpoint.getType());
            if ( endpoint.getType() == endPointType){

                if ( null == mEpOut && endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    logger.v(TAG, "USB_DIR_OUT endpoint type : "+endpoint.getType());
                    mEpOut = endpoint;

                } else if  (null == mEpIn && endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                    logger.v(TAG, "USB_DIR_IN endpoint type : "+endpoint.getType());
                    mEpIn = endpoint;
                    receiveBuffer = new byte[mEpIn.getMaxPacketSize()];

                }
            }
        }

        return ( null != mEpIn) && (null != mEpOut);

    }

    /**
     * find interface of device
     * @param usbClassType find class
     * @return usb interface. if not exists, return null.
     */

    private UsbInterface findInterface(int usbClassType) {

        int count = mUsbDevice.getInterfaceCount();

        for (int i = 0; i < count; i++) {
            UsbInterface usbInterface = mUsbDevice.getInterface(i);
            logger.d(TAG, "usbInterface : "+usbInterface.getInterfaceClass());
            if ( usbClassType == usbInterface.getInterfaceClass())
                return usbInterface;
        }

        return null;
    }

    /**
     * find usb device
     * @return list of device. if not found, return empty list;
     */
    public List<UsbDevice> findUsbDevice() {

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();

        logger.d(TAG, ""+deviceList.size());

        for ( UsbDevice device:
             deviceList.values()) {

            logger.d(TAG, "getDeviceName : "+   device.getDeviceName());
            logger.d(TAG, "getProductId : "+   device.getProductId());
            logger.d(TAG, "getVendorId : "+   device.getVendorId());
            logger.d(TAG, "getDeviceClass : "+   device.getDeviceClass());
            logger.d(TAG, "getDeviceProtocol : "+   device.getDeviceProtocol());
            logger.d(TAG, "getDeviceSubclass"+   device.getDeviceSubclass());


            logger.d(TAG, "-----------");
        }

        //

       Observable<UsbDevice> observable = Observable.from(deviceList.values());

        if ( mVendorId != -1 || mProductId != -1 ) {

            observable = observable.filter(new Func1<UsbDevice, Boolean>() {
                @Override
                public Boolean call(UsbDevice usbDevice) {

                    return usbDevice.getVendorId() == mVendorId
                            && usbDevice.getProductId() == mProductId;
                }
            });
        }

       return observable.toList()
                .toBlocking()
                .firstOrDefault(new ArrayList<UsbDevice>());
    }

    /**
     * request permission
     * @param action request intent action
     * @return result observable
     */
    public Observable<Boolean> requestPermission(final String action) {

        final BroadcastReceiver[] receiver = {null};

        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {

                PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(action), 0);

                if ( mUsbDevice == null) {
                    subscriber.onError(new UsbException("device not set"));
                    return;
                }

                logger.i(TAG, "request permission");

                IntentFilter filter = new IntentFilter();
                filter. addAction(action);

                receiver[0] = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        if ( intent.getAction().equals(action)) {

                            boolean isGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);

                            logger.i(TAG, "permission :" + isGranted);
                            subscriber.onNext(isGranted);
                            subscriber.onCompleted();
                            if ( mContext != null) {
                                mContext.unregisterReceiver(receiver[0]);
                            }
                        }
                    }
                };

                mContext.registerReceiver(receiver[0], filter);
                mUsbManager.requestPermission(mUsbDevice, pendingIntent);

            }
        });


    }

    public boolean isConnected() {

        return mState == State.CONNECTED;
        //return (null != connection);
    }

    public Observable<byte[]> write(final byte[] data) {

            final int HID_MAX_LENGTH = 64;

            final int maxLength =  mUsbSerialDevice == null ? HID_MAX_LENGTH : 1024;

            final ByteBuffer byteBuffer = ByteBuffer.wrap(data.clone());
            int repeat = (byteBuffer.remaining() / maxLength);
            repeat += byteBuffer.remaining() % maxLength > 0 ? 1 : 0;


            return Observable.create(new Observable.OnSubscribe<byte[]>() {
                @Override
                public void call(final Subscriber<? super byte[]> subscriber) {

                    int count = byteBuffer.remaining() >= maxLength ? maxLength : byteBuffer.remaining();
                    byte[] bytes = new byte[count];
                    byteBuffer.get(bytes);

                    writeBlock(bytes).subscribe(subscriber);

                }
            }).repeat(repeat).subscribeOn(Schedulers.trampoline());
    }

    /**
     * write data to characteristic and wait callback
     * @param data data to write (max 64 bytes)
     * @return observable
     */
    private Observable<byte[]> writeBlock(final byte[] data) {

        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(final Subscriber<? super byte[]> subscriber) {

                reentrantLock.lock();

                try {

                    boolean isSuccess;

                    if (null != mUsbSerialDevice) {

                        mUsbSerialDevice.write(data);
                        isSuccess = true;

                    } else {

                        isSuccess = writeBulk(data);
                    }

                    if (isSuccess) {
                        subscriber.onNext(data);
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(new UsbException("write fail"));
                    }

                } finally {
                    reentrantLock.unlock();
                }

            }
        });

    }

    /**
     * 데이터를 전송한다.
     * @param data 전송할 데이터
     * @return 전송된 데이터
     */
    private boolean writeBulk(byte[] data) {

        if ( null == mConnection || null == mEpOut || null == data)
            return false;

        int transferred = mConnection.bulkTransfer(mEpOut, data, data.length, 1000);
        return transferred >= 0;
    }

    /**
     * 연결을 종료한다.
     */
    public void disconnect() {

        if ( request != null) request.close();

        if ( mConnection != null) {

            mConnection.close();
            mConnection = null;
        }

        if (mUsbSerialDevice != null) {

            mUsbSerialDevice.close();
            usbSerialBuffer = null;
            usbSerialReadCallback = null;
            mUsbSerialDevice = null;

        }

        notifyState(new UsbState(State.DISCONNECTED));

    }


    /**
     * close. unregisterReceiver
     */
    public void close() {


        if (isConnected())
            disconnect();

        notifyState(new UsbState(State.DISCONNECTED));
        mContext.unregisterReceiver(usbStateReceiver);

        logger.d(TAG, "close");
        if ( null != mSubscriptionList)
            mSubscriptionList.unsubscribe();
    }

}
