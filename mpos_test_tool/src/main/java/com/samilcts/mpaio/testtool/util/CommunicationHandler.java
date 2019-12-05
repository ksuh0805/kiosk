package com.samilcts.mpaio.testtool.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.samilcts.sdk.mpaio.MpaioManager;
import com.samilcts.sdk.mpaio.ext.nice.message.MpaioNiceMessageAssembler;
import com.samilcts.sdk.mpaio.message.MessageAssembler;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.sdk.mpaio.packet.MpaioPacketAssembler;
import com.samilcts.sdk.mpaio.packet.Packet;
import com.samilcts.sdk.mpaio.packet.PacketAssembler;
import com.samilcts.util.android.Converter;
import com.samilcts.util.android.Logger;

import rx.Subscriber;
import rx.Subscription;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;

/**
 * Created by mskim on 2016-07-28.
 * mskim@31cts.com
 */
public class CommunicationHandler {

    private final MpaioManager mConnectionManager;
    private final RecyclerView mView;
    private final Context mContext;
    private SubscriptionList mSubscription ;
    private final String TAG = "CommunicationHandler";
    private final Logger logger = AppTool.getLogger();

    public CommunicationHandler(Context context, MpaioManager connectionManager, RecyclerView view) {


        mContext = context;
        mConnectionManager = connectionManager;
        mView = view;
    }

    public void stopHandle() {

        if(null != mSubscription ) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

    public void startHandle() {

        if(null != mSubscription ) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }

        mSubscription = new SubscriptionList();

        Subscription s1 =  mConnectionManager.onSent()
                .observeOn(Schedulers.trampoline())
                .subscribe(new Subscriber<byte[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Output.printError(mView,"sent", e);
                    }

                    @Override
                    public void onNext(byte[] bytes) {
                       // logger.i(TAG, "write3 : " + Converter.toHexString(bytes));
                        handle(mContext, mView, bytes, true);

                    }
                });

        Subscription s2 = mConnectionManager.onReceived()
                .observeOn(Schedulers.trampoline())
                .subscribe(new Subscriber<byte[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Output.printError(mView, "receive", e);
                    }

                    @Override
                    public void onNext(byte[] bytes) {
                        handle(mContext, mView, bytes, false);
                    }
                });

        mSubscription.add(s1);
        mSubscription.add(s2);


    }

    private PacketAssembler sendPacketAssembler = new MpaioPacketAssembler();
    private PacketAssembler recvPacketAssembler = new MpaioPacketAssembler();
    private MessageAssembler messageAssembler = new MpaioNiceMessageAssembler();

    private synchronized void handle(Context context, RecyclerView view, byte[] bytes, boolean isOut ) {

        String direction = isOut ? "SEND" : "RECV";

        //logger.i(TAG, direction + " handle : " + Converter.toHexString(bytes));

        if (Output.isEnableLogType(context, "Bytes"))
            Output.printText(view, direction+"-bytes", bytes, null);

        if ( isOut)
            sendPacketAssembler.add(bytes);
        else
            recvPacketAssembler.add(bytes);

        while (sendPacketAssembler.hasPacket() || recvPacketAssembler.hasPacket() ){

            Packet packet = sendPacketAssembler.pop();
            direction = "SEND ";
            if (packet == null){
                packet = recvPacketAssembler.pop();
                direction = "RECV ";
            }


            if ( !packet.validate()) {

                sendPacketAssembler = new MpaioPacketAssembler();
                recvPacketAssembler = new MpaioPacketAssembler();
                Output.printText(view, direction+"-packet", packet.getBytes(), "packet is not valid");
                logger.e(TAG, direction + " packet is not valid : " + Converter.toHexString( packet.getBytes()));
                return;
            }

            if ( Output.isEnableLogType(context, "Packet"))
                Output.printText(view, direction+"-packet", packet.getBytes(), null);


            if (  messageAssembler.add(packet)) {

                MpaioMessage message = messageAssembler.pop();

                if (Output.isEnableLogType(context, "Message") )
                    Output.printMessage(view, message, isOut);
            }

        }

        if ( !mConnectionManager.isConnected()) {
            sendPacketAssembler = new MpaioPacketAssembler();
            recvPacketAssembler = new MpaioPacketAssembler();
            messageAssembler = new MpaioNiceMessageAssembler();
        }

    }
}
