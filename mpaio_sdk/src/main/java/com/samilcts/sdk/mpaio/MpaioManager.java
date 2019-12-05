package com.samilcts.sdk.mpaio;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Looper;

import com.felhr.usbserial.UsbSerialInterface;
import com.samilcts.media.RxConnectable;
import com.samilcts.media.State;
import com.samilcts.media.ble.BleState;
import com.samilcts.media.ble.RxBle;
import com.samilcts.media.ble.ScanResult;
import com.samilcts.media.exception.BleException;
import com.samilcts.media.exception.UsbException;
import com.samilcts.media.serial.RxSerial;
import com.samilcts.media.serial.SerialState;
import com.samilcts.media.socket.RxSocket;
import com.samilcts.media.usb.RxUsb;
import com.samilcts.media.usb.UsbState;
import com.samilcts.sdk.mpaio.authentication.AuthenticationManager;
import com.samilcts.sdk.mpaio.authentication.DefaultAuthenticationManager;
import com.samilcts.sdk.mpaio.callback.ResultCallback;
import com.samilcts.sdk.mpaio.command.CommandID;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.crypto.Crypto;
import com.samilcts.sdk.mpaio.crypto.DefaultAES128;
import com.samilcts.sdk.mpaio.crypto.DefaultPacker;
import com.samilcts.sdk.mpaio.crypto.Packer;
import com.samilcts.sdk.mpaio.crypto.StubPacker;
import com.samilcts.sdk.mpaio.error.ResponseError;
import com.samilcts.sdk.mpaio.exception.ManagerException;
import com.samilcts.sdk.mpaio.log.LogTool;
import com.samilcts.sdk.mpaio.message.DefaultMessage;
import com.samilcts.sdk.mpaio.message.DefaultMessageAssembler;
import com.samilcts.sdk.mpaio.message.MessageAssembler;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.sdk.mpaio.packet.MpaioPacketAssembler;
import com.samilcts.sdk.mpaio.packet.Packet;
import com.samilcts.sdk.mpaio.packet.PacketAssembler;
import com.samilcts.sdk.mpaio.stream.MessageInputStream;
import com.samilcts.util.android.BytesBuilder;
import com.samilcts.util.android.Converter;
import com.samilcts.util.android.Logger;

import java.io.IOException;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by mskim on 2016-07-11.
 * mskim@31cts.com
 */
public class MpaioManager implements ConnectionManager {

    private static final UUID UUID_SERVICE_RECV = UUID // other -> mpaio
            .fromString("0000ffb0-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHARACTERISTIC_RECV = UUID    // other -> mpaio
            .fromString("0000ffb2-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_SERVICE_SEND = UUID    // mpaio -> other..
            .fromString("0000ffa0-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHARACTERISTIC_SEND = UUID // mpaio -> other..
            .fromString("0000ffa3-0000-1000-8000-00805f9b34fb");

    private static final String TAG = "MpaioManager";
    private final Looper mLooper;
    private final SubscriptionList subscriptionList = new SubscriptionList();

    private final Context mContext;

    private RxUsb rxUsb;
    private RxBle rxBle;
    private RxSerial rxSerial;

    private short mAid = 0;
    private PublishSubject<MpaioMessage> messageReceiveSubject = PublishSubject.create();

    private PublishSubject<byte[]> bleSentSubject = PublishSubject.create();
    private PublishSubject<byte[]> bleReceiveSubject = PublishSubject.create();
    private final PublishSubject<State> stateSubject = PublishSubject.create();

    private PublishSubject<byte[]> usbReadSubject = PublishSubject.create();
    private PublishSubject<byte[]> usbSentSubject = PublishSubject.create();

    private PublishSubject<byte[]> serialReceiveSubject = PublishSubject.create();
    private PublishSubject<byte[]> serialSentSubject = PublishSubject.create();

    private PacketAssembler packetAssembler;
    protected MessageAssembler mMessageAssembler;
    private final Logger logger = LogTool.getLogger();
    protected int maximumPacketLength = DefaultMessage.MAX_PACKET_LENGTH;

    private boolean isReady = false;
    private final HashMap<String, RxSocket> socketMap = new HashMap<>();


    private byte socketId = 0;

    private final int vid = 0x29C8;
    private final int mainPid = 0x0002;

    private int targetVid = -1;
    private int targetPid = -1;

    private final int USB_READ_TIMEOUT = 100; //if too short some device fail to read data.

    private Subscription usbReadSubscription;

    private AuthenticationManager authenticationManager;
    private final Crypto mDataCrypto = new DefaultAES128();
    private final Crypto mHashCrypto = new DefaultAES128();


    final private Object rwLock = new Object();

    protected Packer mPacker = new StubPacker();

    protected void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * vId & pId is -1 then use usb-serial device.
     * @param vendorId
     * @param productId
     */
    public void setTargetId(int vendorId, int productId) {

        this.targetVid = vendorId;
        this.targetPid = productId;
    }

    private String serialDeviceName = "UART0";

    /**
     * Set before connect. defulat is "UART0"
     * @param name name of serial device ex)UART0
     */
    public void setSerialDeviceName(String name)
    {
        serialDeviceName = name;
        if ( rxSerial != null)
        {
            rxSerial.setPortName(name);
        }
    }

    public MpaioManager(Context context) {

        mContext = context;

        initUSB();
        initBLE();
        initSerial();

        packetAssembler = new MpaioPacketAssembler();
        mMessageAssembler = new DefaultMessageAssembler();

        HandlerThread handlerThread = new HandlerThread("MpaioManager thread");
        handlerThread.start();
        mLooper = handlerThread.getLooper();

        this.setTargetId(0x0403, 0x6001); // FTDI USB2Serial
        setAuthenticationManager(new DefaultAuthenticationManager(this, mDataCrypto, mHashCrypto, new byte[16], 0));

        Subscription s = listenTcpCommand();
        subscriptionList.add(s);

        Observable<State> observable = rxUsb != null ? rxUsb.onStateChanged() : Observable.<State>empty();

        if (rxBle != null)
            observable = observable.mergeWith(rxBle.onStateChanged());

        if (rxSerial != null)
            observable = observable.mergeWith(rxSerial.onStateChanged());

        observable.observeOn(Schedulers.computation())
                .subscribe(new Subscriber<State>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(State state) {

                        if (State.CONNECTED == state.getValue() && !isReady) {
                            return;
                        }

                        stateSubject.onNext(state);

                        if (state.getValue() == State.DISCONNECTED && !isConnected()) {
                            clearSession();
                        }
                    }
                });
    }

    /**
     * clear all session data
     * called when disconnected
     */
    protected void clearSession() {

        isReady = false;
        logger.i(TAG, "clearSession");

        packetAssembler = new MpaioPacketAssembler();
        mMessageAssembler = new DefaultMessageAssembler();
        mPacker = new StubPacker();

        if (null != usbReadSubscription)
            usbReadSubscription.unsubscribe();

        closeAllSocket();
    }

    private Observable<Void> enableNotification() {

        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {

                logger.i(TAG, "enableNotification");
                rxBle.enableNotification(true, getSendDataCharacteristic())
                        .observeOn(Schedulers.computation())
                        .subscribe(subscriber);
            }
        });
    }

    @Override
    public Observable<Void> connect(final String address, final boolean autoConnect) {

        rxBle.setSetting(address, autoConnect);

        return getConnectable(rxBle)
                .delay(100, TimeUnit.MICROSECONDS)
                .concatWith(rxBle.discoverServices())
                .delay(100, TimeUnit.MICROSECONDS)
                .concatWith(enableNotification())
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        rxBle.disconnect();
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        isReady = true;

                        stateSubject.onNext(new BleState(State.CONNECTED));
                    }
                });
    }

    private boolean isThingsDevice(Context context) {
        final PackageManager pm = context.getPackageManager();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pm.hasSystemFeature(PackageManager.FEATURE_EMBEDDED);
    }

    @Override
    public Observable<Void> connect() {


        Observable<Void> obs = getUsbConnectObservable();

        if (isThingsDevice(mContext))
            obs = obs.onErrorResumeNext(getSerialConnectObservable());

        return obs;

     /*   if (isThingsDevice(mContext))
        {
            return getSerialConnectObservable();

        } else {

            return getUsbConnectObservable();
        }*/

    }


    private Observable<Void> getSerialConnectObservable() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {

                rxSerial.setSerialConfig(57600, 8, 1, 0, 0);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .concatWith(getConnectable(rxSerial))
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
/*
                        logger.d(TAG, "rx serial read repeat run");
                        rxSerial.read(200)
                                .onBackpressureBuffer()
                                .delay(1, TimeUnit.MILLISECONDS)
                                .repeat()
                                .observeOn(Schedulers.trampoline())
                                .subscribe(new Subscriber<byte[]>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onNext(byte[] bytes) {
                                        logger.d(TAG, "rx serial read bytes");

                                        serialReceiveSubject.onNext(bytes);
                                        handleReceivedBytes(bytes);
                                    }
                                });*/
                    }

                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                        rxSerial.disconnect();
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        isReady = true;
                        stateSubject.onNext(new SerialState(State.CONNECTED));
                    }
                });
    }




    private Observable<Void> getUsbConnectObservable() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {




                logger.d(TAG, "check usb devices");

                rxUsb.setTargetId(vid, mainPid);                //main firmware, hid
                List<UsbDevice> devices = rxUsb.findUsbDevice();

                if (!devices.isEmpty()) {
                  //  usbClass[0] = UsbConstants.USB_CLASS_HID;
                            rxUsb.setInterfaceType(UsbConstants.USB_CLASS_VENDOR_SPEC, UsbConstants.USB_ENDPOINT_XFER_BULK);
                            rxUsb.setDevice(devices.get(0));
                } else {

                    logger.d(TAG, "not found mpaio main fw");
                    rxUsb.setTargetId(-1, -1); //all
                    rxUsb.setSerialConfig(57600, UsbSerialInterface.DATA_BITS_8, UsbSerialInterface.STOP_BITS_1,
                            UsbSerialInterface.PARITY_NONE, UsbSerialInterface.FLOW_CONTROL_OFF);

                    //rxUsb.setTargetId(vid, bootPid);
                    devices = rxUsb.findUsbDevice();

                    if (!devices.isEmpty()) { //other devices
                       // usbClass[0] = UsbConstants.USB_CLASS_CDC_DATA;
                        for(UsbDevice device: devices) {
                            if(device.getVendorId() == targetVid && device.getProductId() == targetPid) {
                                rxUsb.setInterfaceType(UsbConstants.USB_CLASS_VENDOR_SPEC, UsbConstants.USB_ENDPOINT_XFER_BULK);
                                rxUsb.setDevice(device);
                            }
                        }
                    } else {
                        subscriber.onError(new UsbException("can not find MPAIO"));
                        return;
                    }
                }



                //p 8363, v 1659 //pl2303

                if (!rxUsb.hasPermission(devices.get(0))) {

                    logger.d(TAG, "no permission try to get permission");

                    Subscription subscription = rxUsb.requestPermission("request string")
                            //.observeOn(Schedulers.computation())
                            .subscribe(new Subscriber<Boolean>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {

                                    subscriber.onError(e);
                                }

                                @Override
                                public void onNext(Boolean aBoolean) {

                                    if (aBoolean)
                                        subscriber.onCompleted();
                                    else
                                        subscriber.onError(new UsbException("permission denied"));
                                }
                            });

                    subscriber.add(subscription);
                } else {

                    subscriber.onCompleted();
                }

            }
        })
                .subscribeOn(Schedulers.io())
                .concatWith(getConnectable(rxUsb))
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {

                        if (null != usbReadSubscription)
                            usbReadSubscription.unsubscribe();

                        usbReadSubscription = rxUsb.read(USB_READ_TIMEOUT)
                                .onBackpressureBuffer()
                                .delay(1, TimeUnit.MILLISECONDS)
                                .repeat()
                                .observeOn(Schedulers.trampoline())
                                .subscribe(new Subscriber<byte[]>() {
                                    @Override
                                    public void onCompleted() {
                                        logger.i(TAG, "read repeat completed");
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onNext(byte[] bytes) {

                                        usbReadSubject.onNext(bytes);
                                        handleReceivedBytes(bytes);

                                    }
                                });

                        subscriptionList.add(usbReadSubscription);

                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                        rxUsb.disconnect();
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        isReady = true;
                        stateSubject.onNext(new UsbState(State.CONNECTED));
                    }
                });
    }


    /**
     * set usb-serial baudrate
     * @param baudRate default 57600
     * @param dataBits default 8
     * @param stopBits default 1
     * @param parity default 0 (none)
     * @param flowControl default 0 (off)
     */
    public void setSerialConfig(int baudRate, int dataBits, int stopBits, int parity, int flowControl){

            rxUsb.setSerialConfig(baudRate, dataBits, stopBits,
                parity, flowControl);
    }


    /**
     * @param rxRxConnectable rxRxConnectable to use
     * @return observable
     */

    private Observable<Void> getConnectable(final RxConnectable rxRxConnectable) {

        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {

                if (isConnected()) {

                    subscriber.onError(new ManagerException("already connected with MPAIO"));
                    return;
                }

                Subscription subscription = rxRxConnectable.connect().subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        subscriber.onError(e);
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });
                subscriber.add(subscription);
            }
        });


    }

    /**
     * mpaio device name prefix
     */

    private static final String PREFIX_MPAIO_NAME = "Samil";

    /**
     * start scan.
     *
     * @return observable.
     */
    public Observable<ScanResult> scan() {

        return rxBle.scan()
                .observeOn(Schedulers.computation())
                .filter(new Func1<ScanResult, Boolean>() {
                    @Override
                    public Boolean call(ScanResult scanResult) {

                        BluetoothDevice device = scanResult.getBleDevice();
                        String name = device.getName();

                        return null != name && name.startsWith(PREFIX_MPAIO_NAME);
                    }
                });
    }

    @Override
    public Observable<State> onStateChanged() {

        return stateSubject.onBackpressureBuffer();


    }


    /**
     * initialize serial
     */
    private void initSerial() {

        if (isThingsDevice(mContext)) {

            rxSerial = new RxSerial(serialDeviceName);
            rxSerial.onReceived()
                    .observeOn(Schedulers.trampoline())
                    .subscribe(new Subscriber<byte[]>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(byte[] bytes) {
                            serialReceiveSubject.onNext(bytes);
                            handleReceivedBytes(bytes);
                        }
                    });
        }
    }


    /**
     * initialize usb
     */
    private void initUSB() {

        rxUsb = new RxUsb(mContext, vid, mainPid);
    }

    /**
     * initialize Ble. if has no ble, do nothing
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initBLE() {

        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            try {
                rxBle = new RxBle(mContext);
                rxBle.onCharacteristicChanged()
                        .observeOn(Schedulers.trampoline())
                        .filter(new Func1<BluetoothGattCharacteristic, Boolean>() {
                            @Override
                            public Boolean call(BluetoothGattCharacteristic characteristic) {
                                return characteristic.getUuid().equals(UUID_CHARACTERISTIC_SEND);
                            }
                        })
                        .subscribe(new Subscriber<BluetoothGattCharacteristic>() {
                            @Override
                            public void onCompleted() {

                                logger.i(TAG, "onCharacteristicChanged completed");
                            }

                            @Override
                            public void onError(Throwable e) {

                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(BluetoothGattCharacteristic characteristic) {
                                byte[] data = characteristic.getValue();

                                bleReceiveSubject.onNext(data);
                                handleReceivedBytes(data);

                            }
                        });

            } catch (BleException e) {

                e.printStackTrace();
            }

        }

    }

    /**
     * release all resources. must call before app finish
     */
    public void close() {

        if (null != subscriptionList) {
            subscriptionList.unsubscribe();
            subscriptionList.clear();
        }

        disconnect();

        if (rxUsb != null) rxUsb.close();
        if (rxBle != null) rxBle.close();
        if (rxSerial != null) rxSerial.close();

        rxBle = null;
        rxUsb = null;
        rxSerial = null;
        mLooper.quit();

        resetSubjects();
    }

    private void resetSubjects() {

        if (null != messageReceiveSubject)
            messageReceiveSubject.onCompleted();

        if (null != bleReceiveSubject)
            bleReceiveSubject.onCompleted();

        if (null != bleSentSubject)
            bleSentSubject.onCompleted();

        if (null != usbReadSubject)
            usbReadSubject.onCompleted();

        if (null != usbSentSubject)
            usbSentSubject.onCompleted();

        if (null != serialReceiveSubject)
            serialReceiveSubject.onCompleted();

        if (null != serialSentSubject)
            serialSentSubject.onCompleted();


        if (null != stateSubject)
            stateSubject.onCompleted();

        messageReceiveSubject = PublishSubject.create();
        bleReceiveSubject = PublishSubject.create();
        bleSentSubject = PublishSubject.create();
        usbReadSubject = PublishSubject.create();
        usbSentSubject = PublishSubject.create();
        serialReceiveSubject = PublishSubject.create();
        serialSentSubject = PublishSubject.create();



    }


    /**
     * Check command code format
     *
     * @param code command code
     * @return validity
     */
    private boolean isValidCommandFormat(byte[] code) {
        return code != null && 2 == code.length;
    }

    /**
     * Send command and receive response
     *
     * @param aid         aid of message
     * @param commandCode command code
     * @param data        parameters of command
     * @return observable
     */
    public Observable<MpaioMessage> rxSyncRequest(final short aid, byte[] commandCode, byte[] data) {

        final byte[] savedData = null == data ? new byte[0] : data.clone();
        final byte[] savedCommandCode = null == commandCode ? new byte[2] : commandCode.clone();

        return Observable.create(new Observable.OnSubscribe<MpaioMessage>() {
            @Override
            public void call(final Subscriber<? super MpaioMessage> subscriber) {

                final Subscription subscription = onMessageReceived()
                        .filter(new Func1<MpaioMessage, Boolean>() {
                            @Override
                            public Boolean call(MpaioMessage mpaioMessage) {

                                return isResponseOf(mpaioMessage, aid, savedCommandCode);
                            }
                        })
                        .subscribe(new Subscriber<MpaioMessage>() {
                            @Override
                            public void onCompleted() {
                                subscriber.onCompleted();

                            }

                            @Override
                            public void onError(Throwable e) {
                                subscriber.onError(e);
                            }

                            @Override
                            public void onNext(MpaioMessage mpaioMessage) {

                                subscriber.onNext(mpaioMessage);
                                subscriber.onCompleted();
                            }
                        });

                subscriber.add(subscription);

                final Subscription subscription2 = rxSendMessage(aid, savedCommandCode, savedData)
                        .observeOn(Schedulers.computation())
                        .subscribe(new Subscriber<byte[]>() {
                            @Override
                            public void onCompleted() {

                                //    logger.d(TAG, "Send  Command : [" + Converter.toHexString(savedCommandCode) + "] AID : " + Converter.toHexString(Converter.toBytes(AID)));
                            }

                            @Override
                            public void onError(Throwable e) {
                                subscriber.onError(e);
                            }

                            @Override
                            public void onNext(byte[] bytes) {
                            }
                        });

                subscriber.add(subscription2);

            }
        }).subscribeOn(AndroidSchedulers.from(mLooper));
    }


    /**
     * make mpaio message
     *
     * @param aid         aid
     * @param commandCode commandCode
     * @param data        data
     * @return observable
     */
    protected MpaioMessage makeMessage(byte[] aid, final byte[] commandCode, final byte[] data) {

        DefaultMessage message = new DefaultMessage(aid, null == commandCode ? new byte[2] : commandCode.clone(), null == data ? null : data.clone());

        if ( message.needDataIndexing())
            message.setMaxPacketLength(maximumPacketLength);

        return message;
    }


    /**
     * Send mpaio message.
     *
     * @param aid         aid of message
     * @param commandCode command code
     * @param data        parameters of command
     * @return observable
     */
    public Observable<byte[]> rxSendMessage(final short aid, final byte[] commandCode, final byte[] data) {

        synchronized (rwLock) {

            final MpaioMessage mpaioMessage = makeMessage(Converter.toBytes(aid), commandCode, data);

            return Observable.create(new Observable.OnSubscribe<byte[]>() {
                @Override
                public void call(final Subscriber<? super byte[]> subscriber) {

                    if (!isValidCommandFormat(commandCode)) {
                        subscriber.onError(new ManagerException("Invalid command code format"));
                        return;
                    }

                    MessageInputStream inputStream;

                    inputStream = new MessageInputStream(mpaioMessage, CommandID.OUT);

                    //app->mpaio command
                    if ((aid & 0xFF) < 0x40) mAid = aid;


                    if (!isConnected()) {

                        subscriber.onError(new ManagerException("not connected with MPAIO"));
                        return;
                    }

                    byte[] buf = new byte[inputStream.available()];

                    try {
                        int count = inputStream.read(buf);
                        buf = Arrays.copyOf(buf, count);
                    } catch (IOException e) {
                        subscriber.onError(e);
                        return;
                    }


                    MpaioPacketAssembler assembler = new MpaioPacketAssembler();
                    assembler.add(buf);

                    if (rxSerial != null && isSerialConnected()) {

                        while (assembler.hasPacket()) onPacketSend(assembler.pop());
                        writeToSerial(subscriber, buf);

                    } else if (rxUsb != null && isUsbConnected()) {

                        while (assembler.hasPacket()) onPacketSend(assembler.pop());
                        writeToUsb(subscriber, buf);

                    } else if (rxBle != null && isBleConnected()) {

                        while (assembler.hasPacket()) onPacketSend(assembler.pop());
                        writeToBle(subscriber, buf);
                    }

                }
            }).subscribeOn(Schedulers.trampoline());
        }
    }


    /**
     * Send bytes.
     *
     * @param rawData   data to send
     * @return observable
     */
    public Observable<byte[]> rxSendBytes(final byte[] rawData) {

        synchronized (rwLock) {

            return Observable.create(new Observable.OnSubscribe<byte[]>() {
                @Override
                public void call(final Subscriber<? super byte[]> subscriber) {

                    if (!isConnected()) {

                        subscriber.onError(new ManagerException("not connected with MPAIO"));
                        return;
                    }

                    if (rxUsb != null && isUsbConnected()) {
                        writeToUsb(subscriber, rawData);
                    } else if (rxBle != null && isBleConnected()) {
                        writeToBle(subscriber, rawData);
                    }

                }
            }).subscribeOn(Schedulers.trampoline());
        }
    }


    protected void onPacketSend(Packet packet) {

        if (logger.getLevel() < Logger.NONE) {

            MpaioCommand command = new MpaioCommand(packet.getCommandCode());
            logger.d(TAG, "Packet to write(" + command.getName() + ") : [" + Converter.toHexString(packet.getBytes()) + "]");
        }
    }

    /**
     * subscribe write buf to ble observable
     *
     * @param subscriber subscriber to observe
     * @param buf        data to write
     */
    private void writeToBle(Subscriber<? super byte[]> subscriber, byte[] buf) {

        BluetoothGattService service = rxBle.getService(UUID_SERVICE_RECV);

        if (null != service)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHARACTERISTIC_RECV);

                if (null != characteristic) {

                    rxBle.write(characteristic, buf)
                            .observeOn(Schedulers.trampoline())
                            .onBackpressureBuffer()
                            .map(new Func1<BluetoothGattCharacteristic, byte[]>() {
                                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                                @Override
                                public byte[] call(BluetoothGattCharacteristic characteristic) {
                                    byte[] data = characteristic.getValue().clone();
                                    bleSentSubject.onNext(data);
                                    return data;
                                }
                            })
                            .subscribe(subscriber);
                }
            }
    }

    /**
     * subscribe write usb to ble observable
     *
     * @param subscriber subscriber to observe
     * @param buf        data to write
     */
    private void writeToUsb(Subscriber<? super byte[]> subscriber, byte[] buf) {

        rxUsb.write(buf)
                .observeOn(Schedulers.trampoline())
                .onBackpressureBuffer()
                .doOnNext(new Action1<byte[]>() {
                    @Override
                    public void call(byte[] bytes) {
                        usbSentSubject.onNext(bytes);
                    }
                })
                .subscribe(subscriber);
    }


    /**
     * subscribe write serial to serial observable
     *
     * @param subscriber subscriber to observe
     * @param buf        data to write
     */
    private void writeToSerial(Subscriber<? super byte[]> subscriber, byte[] buf) {

        rxSerial.write(buf)
                .observeOn(Schedulers.trampoline())
                .onBackpressureBuffer()
                .doOnNext(new Action1<byte[]>() {
                    @Override
                    public void call(byte[] bytes) {
                        serialSentSubject.onNext(bytes);
                    }
                })
                .subscribe(subscriber);
    }


    /**
     * set notification to mpaio write service
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothGattCharacteristic getSendDataCharacteristic() {

        BluetoothGattService bgsSend = rxBle.getService(UUID_SERVICE_SEND);

        if (bgsSend == null) {
            logger.e(TAG, "UUID_SERVICE_SEND service not found!");

            return null;
        }

        BluetoothGattCharacteristic bgcSendData = bgsSend.getCharacteristic(UUID_CHARACTERISTIC_SEND);

        if (bgcSendData == null) {
            logger.e(TAG, "UUID_CHARACTERISTIC_SEND not found!");

            return null;
        }

        return bgcSendData;
    }


    /**
     * @param message target
     * @param aid     request aid
     * @param cmd     request cmd
     * @return true, if message has same aid, cmd
     */
    private boolean isResponseOf(MpaioMessage message, short aid, byte[] cmd) {

        return Arrays.equals(message.getCommandCode(), cmd)
                && Arrays.equals(Converter.toBytes(aid), message.getAID());

    }

    /**
     * Check connection
     *
     * @return return true, if ble or usb connected.
     */

    public boolean isConnected() {

        return isBleConnected() || isUsbConnected() || isSerialConnected();

    }

    /**
     * onNext called when message received
     *
     * @return observable
     */
    protected Observable<MpaioMessage> onMessageReceived() {

        return messageReceiveSubject.onBackpressureBuffer().observeOn(Schedulers.trampoline());
    }


    /**
     * onNext called when bytes received
     *
     * @return observable
     */
    public Observable<byte[]> onReceived() {

        return bleReceiveSubject.onBackpressureBuffer()
                .mergeWith(usbReadSubject.onBackpressureBuffer())
                .mergeWith(serialReceiveSubject.onBackpressureBuffer())
                .observeOn(Schedulers.trampoline());
    }


    /**
     * onNext called when bytes sent
     *
     * @return observable
     */
    public Observable<byte[]> onSent() {

        return bleSentSubject.onBackpressureBuffer()
                .mergeWith(usbSentSubject.onBackpressureBuffer())
                .mergeWith(serialSentSubject.onBackpressureBuffer())
                .observeOn(Schedulers.trampoline());
    }


    /**
     * disconnect
     */

    public void disconnect() {
        logger.i(TAG, "disconnect");

        if (isUsbConnected()) rxUsb.disconnect();
        if (isBleConnected()) rxBle.disconnect();
        if (isSerialConnected()) rxSerial.disconnect();

    }

    /**
     * Check ble is connected
     *
     * @return true, if connected
     */

    public boolean isBleConnected() {

        return rxBle != null && rxBle.isConnected() && isReady;
    }

    /**
     * Check usb is connected
     *
     * @return true, if connected
     */
    public boolean isUsbConnected() {

        return rxUsb != null && rxUsb.isConnected() && isReady;
    }

    /**
     * Check serial is connected
     *
     * @return true, if connected
     */
    public boolean isSerialConnected() {

        return rxSerial != null && rxSerial.isConnected() && isReady;
    }


    /**
     * assemble bytes to packets, messages
     *
     * @param bytes received bytes
     */

    private void handleReceivedBytes(byte[] bytes) {

        synchronized (rwLock) {

            packetAssembler.add(bytes);

            while (packetAssembler.hasPacket()) {

                Packet assembledPacket = packetAssembler.pop();

                onPacketReceived(assembledPacket);


                if (assembledPacket.validate()) {

                    if (mMessageAssembler.add(assembledPacket)) {

                        MpaioMessage mpaioMessage = mMessageAssembler.pop();
                        messageReceiveSubject.onNext(mpaioMessage);
                    }

                } else {

                    messageReceiveSubject.onError(new ManagerException("invalid checksum packet received"));
                    messageReceiveSubject = PublishSubject.create();
                    logger.w(TAG, "Checksum error");
                }
            }
        }

    }

    /**
     * called on packet received
     *
     * @param packet received packet
     */
    protected void onPacketReceived(Packet packet) {

        if (logger.getLevel() < Logger.NONE) {

            MpaioCommand command = new MpaioCommand(packet.getCommandCode());
            logger.d(TAG, "Received packet(" + command.getName() + ") : [" + Converter.toHexString(packet.getBytes()) + "]");
        }
    }

    /**
     * get next aid
     *
     * @return previous aid + 1, except 0
     */


    public synchronized short getNextAid() {

        short temp = mAid;

        if (++temp == 0x00) {
            temp++;
        }

        return temp;
    }

    /**
     * listen specific command received
     *
     * @param command command to wait
     * @return observable
     */
    protected Observable<MpaioMessage> listenCommand(final byte[] command) {

        return Observable.create(new Observable.OnSubscribe<MpaioMessage>() {
            @Override
            public void call(final Subscriber<? super MpaioMessage> subscriber) {

                Subscription subscription = onMessageReceived()
                        .filter(new Func1<MpaioMessage, Boolean>() {
                            @Override
                            public Boolean call(MpaioMessage mpaioMessage) {
                                return Arrays.equals(command, mpaioMessage.getCommandCode());
                            }
                        })
                        .subscribe(subscriber);

                subscriber.add(subscription);

            }
        }).subscribeOn(Schedulers.computation());
    }


    /**
     * barcode notify
     *
     * @return observable
     */
    public Observable<MpaioMessage> onBarcodeRead() {

        return listenCommand(getCommandCode(MpaioCommand.NOTIFY_READ_BARCODE));
    }

    /**
     * rfid wait notify
     *
     * @return observable
     */
    public Observable<MpaioMessage> onRfidNotify() {

        return listenCommand(getCommandCode(MpaioCommand.NOTIFY_WAIT_RFID_CARD));
    }

    /**
     * on read MS card
     *
     * @return observable
     */
    public Observable<MpaioMessage> onReadMsCard() {

        return listenCommand(getCommandCode(MpaioCommand.NOTIFY_READ_MS_CARD));
    }

    /**
     * on read EMV card
     *
     * @return observable
     */
    public Observable<MpaioMessage> onReadEmvCard() {

        return listenCommand(getCommandCode(MpaioCommand.NOTIFY_READ_EMV_CARD));
    }

    /**
     * on read RFID card
     *
     * @return observable
     */
    public Observable<MpaioMessage> onReadRfidCard() {

        return listenCommand(getCommandCode(MpaioCommand.NOTIFY_READ_RFID_CARD));
    }

    /**
     * on press pin pad
     *
     * @return observable
     */
    public Observable<MpaioMessage> onPressPinPad() {

        return listenCommand(getCommandCode(MpaioCommand.NOTIFY_READ_PIN_PAD));
    }

    /**
     * on notify read/recharge/refund prepaid card completed
     *
     * @return observable
     */
    public Observable<MpaioMessage> onNotifyPrepaidTransaction() {

        return listenCommand(getCommandCode(MpaioCommand.NOTIFY_PREPAID_TRANSACTION));
    }

    /**
     * on notify read log
     *
     * @return observable
     */
    public Observable<MpaioMessage> onReadPrepaidTransactionLog() {

        return listenCommand(getCommandCode(MpaioCommand.NOTIFY_READ_PREPAID_TRANSACTION_LOG));
    }

    /**
     * get command code value
     *
     * @param code code
     * @return command code value
     */
    private byte[] getCommandCode(int code) {

        return new MpaioCommand(code).getCode();
    }

    /**
     * default is {@link DefaultMessage#MAX_PACKET_LENGTH}
     *
     * @param length the maximum length of each multi packet to write
     */
    public void setMaximumPacketLength(int length) {

        maximumPacketLength = length;
    }


    /**
     * set minimum interval of ble write ( every 20 bytes)
     *
     * @param millis interval time (millisecond)
     * @return if set, return true
     */
    public boolean setBleWriteInterval(long millis) {

        if ( rxBle != null)
        {
            rxBle.setWriteInterval(millis);
            return true;
        }
        return false;
    }


    /**
     * on read MS card
     *
     * @return observable
     */
    private Subscription listenTcpCommand() {

        return listenCommand(getCommandCode(MpaioCommand.OPEN_TCP_SOCKET))
                .mergeWith(listenCommand(getCommandCode(MpaioCommand.CLOSE_TCP_SOCKET)))
                .mergeWith(listenCommand(getCommandCode(MpaioCommand.WRITE_TCP_SEGMENT)))
                .mergeWith(listenCommand(getCommandCode(MpaioCommand.READ_TCP_SEGMENT)))
                .observeOn(Schedulers.computation())
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(final MpaioMessage mpaioMessage) {

                        MpaioCommand command = new MpaioCommand(mpaioMessage.getCommandCode());

                        if (command.equals(MpaioCommand.OPEN_TCP_SOCKET)) {
                            logger.i(TAG, "OPEN_TCP_SOCKET received");
                            openTcp(mpaioMessage);

                        } else if (command.equals(MpaioCommand.CLOSE_TCP_SOCKET)) {
                            logger.i(TAG, "CLOSE_TCP_SOCKET received");
                            closeTcp(mpaioMessage);

                        } else if (command.equals(MpaioCommand.WRITE_TCP_SEGMENT)) {
                            logger.i(TAG, "WRITE_TCP_SEGMENT received");
                            writeTcp(mpaioMessage);

                        } else if (command.equals(MpaioCommand.READ_TCP_SEGMENT)) {
                            logger.i(TAG, "READ_TCP_SEGMENT received");
                            readTcp(mpaioMessage);
                        }

                    }
                });
    }

    /**
     * handle READ_TCP_SEGMENT command
     *
     * @param mpaioMessage mpaio message
     */
    private void readTcp(final MpaioMessage mpaioMessage) {

        byte[] data = mpaioMessage.getData();

        if (null == data) {
            sendNack(mpaioMessage, ResponseError.INVALID_PARAMETER_ERROR);
            return;
        }

        String[] param = new String(data).split("\\r");

        if (3 != param.length) {
            sendNack(mpaioMessage, ResponseError.INVALID_PARAMETER_ERROR);
            return;
        }

        String id = param[0];


        int maxRead;
        int timeout;

        try {
            maxRead = Integer.parseInt(param[1]);
            timeout = (int) Double.parseDouble(param[2]) * 1000;
        } catch (NumberFormatException e) {
            sendNack(mpaioMessage, ResponseError.INVALID_PARAMETER_ERROR);
            return;
        }

        final RxSocket socket = socketMap.get(id);

        if (null == socket || !socket.isConnected()) {
            sendNack(mpaioMessage, ResponseError.SOCKET_NOT_CONNECTED_ERROR);
            return;
        }

        try {
            socket.getSocket().setSoTimeout(timeout * 1000);
        } catch (SocketException e) {
            sendNack(mpaioMessage, ResponseError.UNKNOWN_ERROR);
            e.printStackTrace();
            return;
        }

        socket.read(maxRead)
                .subscribe(new Subscriber<byte[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        logger.i(TAG, "read tcp error: " + e.getMessage());

                        if (e instanceof SocketException)
                            sendResponse(mpaioMessage, ResponseError.SOCKET_NOT_CONNECTED_ERROR);
                        else
                            sendResponse(mpaioMessage, ResponseError.SOCKET_READ_ERROR);
                    }

                    @Override
                    public void onNext(byte[] bytes) {

                        sendResponse(mpaioMessage, BytesBuilder.merge(new byte[]{ResponseError.NO_ERROR.getCode()}, bytes));

                    }
                });
    }

    private void writeTcp(final MpaioMessage mpaioMessage) {

        byte[] data = mpaioMessage.getData();

        if (null == data || data.length < 2) {
            sendNack(mpaioMessage, ResponseError.INVALID_PARAMETER_ERROR);
            return;
        }

        String id = new String(Arrays.copyOf(data, 1));

        byte[] segment = Arrays.copyOfRange(data, 1, data.length);
        final RxSocket socket = socketMap.get(id);

        if (null == socket || !socket.isConnected()) {
            sendNack(mpaioMessage, ResponseError.SOCKET_NOT_CONNECTED_ERROR);
            return;
        }

        socket.write(segment)
                .subscribe(new Subscriber<byte[]>() {
                    @Override
                    public void onCompleted() {

                        sendAck(mpaioMessage);
                    }

                    @Override
                    public void onError(Throwable e) {
                        logger.i(TAG, "write tcp error: " + e.getMessage());

                        if (e instanceof SocketException)
                            sendResponse(mpaioMessage, ResponseError.SOCKET_NOT_CONNECTED_ERROR);
                        else
                            sendResponse(mpaioMessage, ResponseError.SOCKET_WRITE_ERROR);
                    }

                    @Override
                    public void onNext(byte[] bytes) {

                    }
                });

    }

    private void closeTcp(MpaioMessage mpaioMessage) {

        byte[] data = mpaioMessage.getData();
        if (null == data || 1 != data.length) {
            sendNack(mpaioMessage, ResponseError.INVALID_PARAMETER_ERROR);
            return;
        }

        String id = new String(data);

        if (" ".equals(id)) {
            closeAllSocket();
        } else {
            RxSocket socket = socketMap.get(id);

            if (socket != null)
                socket.close();
        }

        sendAck(mpaioMessage);
    }

    private void closeAllSocket() {
        for (RxSocket socket : socketMap.values()) {
            socket.close();
        }
        socketMap.clear();
    }

    private void openTcp(final MpaioMessage mpaioMessage) {

        byte[] data = mpaioMessage.getData();
        if (null == data || 0 == data.length) {
            sendNack(mpaioMessage, ResponseError.INVALID_PARAMETER_ERROR);
            return;
        }

        String[] param = new String(data).split("\\r");

        if (3 != param.length) {
            sendNack(mpaioMessage, ResponseError.INVALID_PARAMETER_ERROR);
            return;
        }

        String ip = param[0];
        int port, timeout;
        try {
            port = Integer.parseInt(param[1]);
            timeout = (int) Double.parseDouble(param[2]) * 1000;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            sendNack(mpaioMessage, ResponseError.INVALID_PARAMETER_ERROR);
            return;
        }

        RxSocket socket = new RxSocket(ip, port);
        socket.setConnectionTimeout(timeout);

        final String id = String.format(Locale.US, "%d", socketId++ % 10);
        socketMap.put(id, socket);

        socket.connect().subscribe(new Subscriber<Void>() {
            @Override
            public void onCompleted() {

                sendResponse(mpaioMessage, BytesBuilder.merge(
                        new byte[]{ResponseError.NO_ERROR.getCode()}, id.getBytes()));
            }

            @Override
            public void onError(Throwable e) {
                logger.w(TAG, e.getMessage());
                sendResponse(mpaioMessage, ResponseError.SOCKET_CONNECTION_ERROR);
            }

            @Override
            public void onNext(Void aVoid) {

            }
        });
    }

    private void sendNack(MpaioMessage mpaioMessage, ResponseError error) {

        sendResponse(mpaioMessage, new byte[]{error.getCode()});
    }

    private void sendAck(MpaioMessage mpaioMessage) {

        sendResponse(mpaioMessage, new byte[]{ResponseError.NO_ERROR.getCode()});
    }

    private void sendResponse(MpaioMessage mpaioMessage, ResponseError error) {

        sendResponse(mpaioMessage, new byte[]{error.getCode()});
    }

    private void sendResponse(MpaioMessage mpaioMessage, byte[] data) {

        short replyAid = (short) Converter.toInt(mpaioMessage.getAID());
        rxSendMessage(replyAid, mpaioMessage.getCommandCode(), data)
                .subscribe(new Subscriber<byte[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(byte[] bytes) {

                        //logger.i(TAG, "response : " + Converter.toHexString(bytes));
                    }
                });
    }

    /**
     * authenticate session
     *
     * @param authenticationCallback authenticationCallback
     */

    public void authenticate(final ResultCallback authenticationCallback) {

        logger.i(TAG, "authentication started");
        authenticationManager.authenticate(new ResultCallback() {
            @Override
            public void onCompleted(boolean isSuccess) {

                logger.i(TAG, "authentication result : " + isSuccess);

                if (isSuccess) {

                    try {
                        mPacker = new DefaultPacker(mDataCrypto, mHashCrypto, MessageDigest.getInstance("SHA-256"));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        isSuccess = false;
                    }
                }

                authenticationCallback.onCompleted(isSuccess);
            }
        });
    }


    /**
     * set company registration number
     *
     * @param crn company registration number
     * @return observable
     */
    public Observable<Boolean> setCrn(final String crn) {

        byte[] data = crn.getBytes();
        return rxSyncRequest(MpaioCommand.SET_CRN,  data);
    }


    /**
     * must use after authentication
     * set prepaid key
     *
     * @param defaultKey        default key
     * @param authenticationKey card auth key (hex)
     * @param placeId           installed place id
     * @param deviceId          device id
     * @param rechargeLimit     limit value of total balance
     * @return observable
     */
    public Observable<Boolean> injectPrepaidCardKey(final String defaultKey, final String authenticationKey, final String placeId, final String deviceId, final String rechargeLimit) {

        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {

                byte[] authKey = Converter.toByteArray(authenticationKey);

                byte[] data = BytesBuilder.merge(defaultKey.getBytes(), authKey, placeId.getBytes()
                        , deviceId.getBytes(), rechargeLimit.getBytes());

                rxSyncRequest(MpaioCommand.INJECT_PREPAID_CARD_KEY,  mPacker.pack(data)).subscribe(subscriber);
            }
        });


    }


    /**
     * Set managing server information
     * @param ip ip of server
     * @param port port number of server
     * @return observable
     */
    public Observable<Boolean> setManagingServerInfo(String ip, String port) {

        byte[] data = BytesBuilder.merge( ip.getBytes(),new byte[]{0x0d},port.getBytes()        );
        return rxSyncRequest(MpaioCommand.SET_MANAGING_SERVER_INFO, data);
    }

    /**
     * Set update server information
     * @param ip ip of server
     * @param port port number of server
     * @return observable
     */
    public Observable<Boolean> setUpdateServerInfo(String ip, String port) {

        byte[] data = BytesBuilder.merge( ip.getBytes(),new byte[]{0x0d},port.getBytes()        );
        return rxSyncRequest(MpaioCommand.SET_UPDATE_SERVER_INFO, data);
    }

    public Observable<Boolean> setCashInPulse(String cycle, String lowDuration, String intervalTime, String pulseValue, String[] pulsePerBillType) {

        BytesBuilder builder = new BytesBuilder();

        for (String str :
                pulsePerBillType) {
            builder.add(new byte[]{Byte.parseByte(str)});
        }

        byte[] data = (cycle +'\r' + lowDuration +'\r' +intervalTime +'\r' +pulseValue +'\r').getBytes();

        data = BytesBuilder.merge(data, builder.pop());

        return rxSyncRequest(MpaioCommand.SET_CASH_IN_PULSE_, data);
    }


    public Observable<Boolean> setChargeFactors(String taxIncluded, String serviceCharge, String unitPrice, String chargeStep, String defaultStepNumber, String[] stepMultipliers) {

        //1 0 500 20 1 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20

        BytesBuilder builder = new BytesBuilder();

        for (String str : stepMultipliers) {
            builder.add(new byte[]{Byte.parseByte(str)});
        }

        byte[] step = new byte[] { Byte.parseByte(chargeStep), 0x0d };
        byte[] defaultNum = new byte[] { Byte.parseByte(defaultStepNumber), 0x0d };

        byte[] data = (taxIncluded +'\r' + serviceCharge +'\r' +unitPrice +'\r').getBytes();

        data = BytesBuilder.merge(data, step, defaultNum, builder.pop());

        return rxSyncRequest(MpaioCommand.SET_CHARGE_FACTORS, data);
    }

    /**
     * set network information
     * @param ssid SSID of AP
     * @param password AP password
     * @param ip ip of terminal
     * @param subnet subnet mask
     * @param gateway gateway ip
     * @param dns DNS ip
     * @param dhcpEnable dhcpEnalbe
     * @param timeout AP connection timeout
     * @param moduleType network module type
     * @return observable
     */
    public Observable<Boolean> setNetworkInfo(String ssid, String password, String ip, String subnet, String gateway, String dns, String dhcpEnable, byte timeout, String moduleType) {

        byte[] data = BytesBuilder.merge( ssid.getBytes(),new byte[]{0x0d},password.getBytes()
                ,new byte[]{0x0d}, ip.getBytes(),new byte[]{0x0d}, subnet.getBytes(),new byte[]{0x0d}, gateway.getBytes(), new byte[]{0x0d}, dns.getBytes()
                , new byte[]{0x0d}, dhcpEnable.getBytes(), new byte[]{0x0d, timeout}, new byte[]{0x0d}, moduleType.getBytes());

        return rxSyncRequest(MpaioCommand.SET_NETWORK_INFO, data);
    }

    /**
     * set run mode     *
     * @param mode company registration number
     * @return observable
     */
    public Observable<Boolean> setRunMode(final byte mode) {

       return rxSyncRequest(MpaioCommand.SET_RUN_MODE, new byte[] { mode});
    }


    /**
     * set hardware revision
     * @param mainRevision GPOS : EMV
     * @param subRevision1 GPOS : Antena
     * @param subRevision2 GPOS : SIM
     * @param subRevision3 GPOS : Connector
     * @return observable
     */
    public Observable<Boolean> setHardwareRevision(String mainRevision, String subRevision1, String subRevision2, String subRevision3) {

        return rxSyncRequest(MpaioCommand.SET_HARDWARE_REVISION, (mainRevision+"\r"+subRevision1+"\r"+subRevision2+"\r"+subRevision3).getBytes());
    }

    /**
     * Set serial number of device
     * @param serialNumber number
     * @return observable
     */
    public Observable<Boolean> setSerialNumber(String serialNumber) {

        return rxSyncRequest(MpaioCommand.SET_SERIAL_NUMBER, serialNumber.getBytes());
    }


    protected Observable<Boolean> rxSyncRequest(final int command, final byte[] data)
    {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {

                Subscription subscription = rxSyncRequest(getNextAid(), getCommandCode(command), data)
                        .subscribe(new Subscriber<MpaioMessage>() {
                            @Override
                            public void onCompleted() {
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onError(Throwable e) {

                                subscriber.onError(e);
                            }

                            @Override
                            public void onNext(MpaioMessage mpaioMessage) {

                                byte[] data = mpaioMessage.getData();

                                boolean result = false;
                                if (data != null && data.length > 0) {
                                    ResponseError error = ResponseError.fromCode(data[0]);

                                    if (ResponseError.NO_ERROR.equals(error)) {
                                        result = true;
                                    }
                                }

                                subscriber.onNext(result);
                            }

                        });

                subscriber.add(subscription);

                BytesBuilder.clear(data);

            }
        });
    }




    /**
     * change initial key
     * @param key key to use
     * @param keyNumber key number of key
     * @param swModel 16 length sw model
     */
    public void setAuthenticationParameter(byte[] key, int keyNumber, String swModel) {
        DefaultAuthenticationManager authenticationManager = new DefaultAuthenticationManager(this, mDataCrypto, mHashCrypto, key, keyNumber);
        authenticationManager.setModel(swModel);
        setAuthenticationManager(authenticationManager);
    }



}
