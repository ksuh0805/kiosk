package com.samilcts.media.socket;

import com.samilcts.media.AbstractRxMedia;
import com.samilcts.media.State;
import com.samilcts.util.android.BytesBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by mskim on 2016-09-05.
 * mskim@31cts.com
 */
public class RxSocket extends AbstractRxMedia {

    private final Socket socket;

    private final String ip;
    private final int port;
    private int timeout;


    public Socket getSocket() {
        return socket;
    }

    public RxSocket(String ip, int port) {

        socket = new Socket();
        timeout = 100000;
        this.ip = ip;
        this.port = port;
    }

    /**
     * set socket connection timeout
     * @param timeMillis milliseconds
     */
    public void setConnectionTimeout(int timeMillis){
        timeout = timeMillis;
    }

    @Override
    public Observable<Void> connect()  {

        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {

                notifyState(new SocketState(State.CONNECTING));

                try {

                    socket.connect(new InetSocketAddress(ip, port), timeout);
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                    notifyState(new SocketState(State.CONNECTED));

                } catch (IOException e) {
                    subscriber.onError(e);
                    notifyState(new SocketState(State.DISCONNECTED));
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<byte[]> write(final byte[] data) {

        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(Subscriber<? super byte[]> subscriber) {

                try {
                    OutputStream out = socket.getOutputStream();
                    out.write(data);
                    out.flush();
                    subscriber.onNext(data);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        })
        .subscribeOn(Schedulers.io())
        ;
    }

    /**
     * read all data from stream while data available
     * @return onNext returns read bytes.
     */
    public Observable<byte[]> read() {

        return read(-1);
    }

    /**
     * read data of maximumLength from stream if available.
     * if maximumLength <= 0 read all available data.
     * @param maximumLength length to read
     * @return @return onNext returns read bytes.
     */
    public Observable<byte[]> read(final int maximumLength) {

        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(Subscriber<? super byte[]> subscriber) {

                try {
                    InputStream in = socket.getInputStream();
                    byte[] buffer = new byte[1024*4];

                    BytesBuilder builder = new BytesBuilder();

                    int maxCount = maximumLength > 0 ? maximumLength : buffer.length;
                    int count;
                    do {
                        count = in.read(buffer, 0, maxCount);
                        builder.add(Arrays.copyOf(buffer, count));
                        maxCount = maximumLength - count;


                    } while ( (maximumLength <= 0 || builder.getSize() < maximumLength) && in.available() > 0  );

                    BytesBuilder.clear(buffer);
                    subscriber.onNext(builder.pop());
                    subscriber.onCompleted();

                } catch (IOException e) {
                    subscriber.onError(e);
                }
            }
        })
        .subscribeOn(Schedulers.io())
        ;
    }

    public boolean isConnected(){

        return socket.isConnected();
    }

    public void close() {

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
