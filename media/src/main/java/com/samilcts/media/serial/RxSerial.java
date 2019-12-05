package com.samilcts.media.serial;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.felhr.usbserial.UsbSerialInterface;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;
import com.samilcts.media.AbstractRxMedia;
import com.samilcts.media.LogTool;
import com.samilcts.media.State;
import com.samilcts.media.exception.MediaException;
import com.samilcts.media.exception.SerialException;
import com.samilcts.util.android.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import rx.Observable;
import rx.Subscriber;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by mskim on 2018-02-08.
 * mskim@31cts.com
 */
@TargetApi(Build.VERSION_CODES.O)
public class RxSerial extends AbstractRxMedia {

    private static final String TAG = "RxSerial";
    private final SubscriptionList mSubscriptionList;
    private final Logger logger = LogTool.getLogger();

    private PeripheralManager mService;
    private UartDevice serialDevice;

    private Lock reentrantLock = new ReentrantLock();

    private int baudRate = 57600;
    private int dataBits = UsbSerialInterface.DATA_BITS_8;
    private int stopBits = UsbSerialInterface.STOP_BITS_1;
    private int parity = UsbSerialInterface.PARITY_NONE;
    private int flowControl = UsbSerialInterface.FLOW_CONTROL_OFF;


    private boolean useSynchronousRead = false;

    private String portName = "";

    private HandlerThread mInputThread;
    private Handler mInputHandler;

    private final PublishSubject<byte[]> onReceiveSubject = PublishSubject.create();


    /**
     * Callback invoked when UART receives new incoming data.
     */
    private UartDeviceCallback mCallback;


    private  Runnable readRun = new Runnable() {
        @Override
        public void run() {
            transferUartData(serialDevice);
        }
    };

    public RxSerial(String name) throws SerialException {

        mService = PeripheralManager.getInstance();
        portName= name;
        mSubscriptionList = new SubscriptionList();

        // Create a background looper thread for I/O
        mInputThread = new HandlerThread("read thread");
        mInputThread.start();
        mInputHandler = new Handler(mInputThread.getLooper());

        mCallback = new UartDeviceCallback() {
            @Override
            public boolean onUartDeviceDataAvailable(UartDevice uart) {

                logger.d(TAG, "onUartDeviceDataAvailable start.");

                // Queue up a data transfer
                //transferUartData(uart);
                mInputHandler.post(readRun);
                logger.d(TAG, "onUartDeviceDataAvailable end.");
                //Continue listening for more interrupts
                return true;
            }

            @Override
            public void onUartDeviceError(UartDevice uart, int error) {
                Log.w(TAG, uart + ": Error event " + error);
            }
        };

    }

    public void setPortName(String name)
    {
        portName = name;
    }

    /**
     * set serial config
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

        if ( null != serialDevice ) {

            try {
                serialDevice.setBaudrate(baudRate);
                serialDevice.setDataSize(dataBits);
                serialDevice.setStopBits(stopBits);
                serialDevice.setParity(parity);
                serialDevice.setHardwareFlowControl(flowControl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public Observable<Void> connect() {

        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {

                if (State.CONNECTING == mState || State.CONNECTED == mState) {
                    logger.i(TAG, "serial connecting already tried. return.");
                    subscriber.onError(new MediaException("already connected or connecting"));
                    return;
                }

                logger.d(TAG, "Trying to create a new connection.");
                notifyState(new SerialState(State.CONNECTING));

                try {
                    //disconnect();
                    serialDevice = mService.openUartDevice(portName);

                    setSerialConfig(baudRate, dataBits, stopBits, parity, flowControl);
                    serialDevice.registerUartDeviceCallback(mCallback, mInputHandler);

                    subscriber.onCompleted();
                    notifyState(new SerialState(State.CONNECTED));

                    logger.d(TAG, "uart connected");

                    // Read any initially buffered data
                    mInputHandler.post(readRun);


                } catch ( IOException ex){
                    logger.e(TAG, ex.getMessage());
                    subscriber.onError(new SerialException("fail to open : " + portName));
                    notifyState(new SerialState(State.DISCONNECTED));
                }

            }
        });

    }





    private static final int CHUNK_SIZE = 512;
    /**
     * Loop over the contents of the UART RX buffer, transferring each
     * one back to the TX buffer to create a loopback service.
     *
     * Potentially long-running operation. Call from a worker thread.
     */
    private void transferUartData(UartDevice uartDevice) {

        if (uartDevice != null && !useSynchronousRead) {
            // Loop until there is no more data in the RX buffer.
            logger.d(TAG, "transferUartData 1.");

            try {
                byte[] buffer = new byte[CHUNK_SIZE];
                int read;
                logger.d(TAG, "transferUartData 2.");

                while ((read = serialDevice.read(buffer, buffer.length)) > 0) {

                    logger.d(TAG, "transferUartData 3.");
                    onReceiveSubject.onNext(Arrays.copyOf(buffer,read));

                    logger.d(TAG, "transferUartData 4.");
                    //serialDevice.write(buffer, read);
                    //logger.d(TAG, "serial read : " +Converter.toHexString(buffer));
                }
            } catch (IOException e) {
                Log.w(TAG, "Unable to transfer data over UART", e);
                onReceiveSubject.onError(e);
            }
        }
    }


    /**
     * synchronous read
     * @param timeout read timeout
     * @return observable
     */
    public Observable<byte[]> read(final int timeout) {

        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(final Subscriber<? super byte[]> subscriber) {

                    if (null == serialDevice) {
                        subscriber.onError(new SerialException("not connected"));
                        return;
                    }

                useSynchronousRead = true;

                try {
                    logger.d(TAG, "sync read");
                    byte[] buffer = new byte[CHUNK_SIZE];
                    int read;
                    if ( (read = serialDevice.read(buffer, buffer.length)) > 0)
                    {
                        //logger.d(TAG, "serial read : " +Converter.toHexString(buffer));
                        subscriber.onNext(Arrays.copyOf(buffer, read));
                    }

                    subscriber.onCompleted();

                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }


            }
        }).subscribeOn(Schedulers.io());

    }

    public boolean isConnected() {

        return mState == State.CONNECTED;
        //return (null != connection);
    }

    public Observable<byte[]> write( final byte[] data) {
        logger.d(TAG, "write");
            final int maxLength = 1024;

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
            }).repeat(repeat).subscribeOn(Schedulers.trampoline())
                ;

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

                    boolean isSuccess = false;

                    if (null != serialDevice) {

                        int wrote = serialDevice.write(data, data.length);
                        isSuccess = data.length == wrote;
                    }

                    if (isSuccess) {
                        subscriber.onNext(data);
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(new SerialException("write fail"));
                    }

                }
                catch (IOException ex)
                {
                    subscriber.onError(ex);
                }
                finally {
                    reentrantLock.unlock();
                }
            }
        });

    }

    /**
     * 연결을 종료한다.
     */
    public void disconnect() {

        if (serialDevice != null) {
            serialDevice.unregisterUartDeviceCallback(mCallback);
            try {
                serialDevice.close();

            } catch (IOException ex){
                logger.e(TAG, ex.getMessage());
            }
            finally {
                serialDevice = null;
            }
        }

        notifyState(new SerialState(State.DISCONNECTED));

    }




    /**
     * close. unregisterReceiver
     */

    public void close() {


        if (isConnected())
            disconnect();

        // Terminate the worker thread
        if (mInputThread != null) {
            mInputThread.quitSafely();
        }

        notifyState(new SerialState(State.DISCONNECTED));

        logger.d(TAG, "close");

        mSubscriptionList.unsubscribe();
    }

    public Observable<byte[]> onReceived() {

        useSynchronousRead = false;
        return onReceiveSubject.onBackpressureBuffer().observeOn(Schedulers.computation());
    }
}
