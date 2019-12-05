package com.samilcts.sdk.mpaio.authentication;

import com.samilcts.sdk.mpaio.MpaioManager;
import com.samilcts.sdk.mpaio.callback.ResultCallback;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.crypto.Crypto;
import com.samilcts.sdk.mpaio.error.ResponseError;
import com.samilcts.sdk.mpaio.log.LogTool;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.util.android.BytesBuilder;
import com.samilcts.util.android.Logger;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import rx.Subscriber;

/**
 * do not retry authentication.
 * all process must transactional.
 * if something fail. must initiate all process.
 */
public class DefaultAuthenticationManager implements AuthenticationManager {

    private static final String TAG = "DefaultAuthenticationManager";

    private final Crypto mHashCrypto;
    private ResultCallback mAuthenticationCallback;

    private byte[] initKey = new byte[16];

    private final byte[] RND_A = new byte[16];
    private final byte[] RND_B = new byte[16];

    private final byte[] kbek = new byte[16];
    private final byte[] kbmk = new byte[16];

    private final MpaioManager mConnectionManager;
    private final Crypto mDataCrypto;

    private final int keyNumber;

    private static final long TIMEOUT = 1000; //fix. do not change
    private final Logger logger = LogTool.getLogger();
    private String swModel = "################";

    /**
     *
     * @param deviceManager mpaio manager
     * @param dataCrypto crypto method for data block
     * @param hashCrypto crypto method for hash block
     * @param initialKey initial key
     * @param keyNumber key number
     */
    public DefaultAuthenticationManager(MpaioManager deviceManager, Crypto dataCrypto, Crypto hashCrypto, byte[] initialKey, int keyNumber) {

        mDataCrypto = dataCrypto;
        mHashCrypto = hashCrypto;

        initKey = initialKey;

        this.keyNumber = keyNumber;
        mConnectionManager = deviceManager;
    }

    /**
     * set sw model
     * @param model 16 length sw model
     */
    public void setModel(String model) {

        if ( null != model && model.length() == 16) {
            swModel = model;
        } else {
            logger.w(TAG, "model must 16 bytes length.");
        }
    }

    @Override
    final public void authenticate(ResultCallback authenticationCallback) {

        this.mAuthenticationCallback = authenticationCallback;

        startAuthentication();
    }

    private void startAuthentication() {

       new SecureRandom().nextBytes(RND_A);

        mConnectionManager.rxSyncRequest(mConnectionManager.getNextAid(), new MpaioCommand(MpaioCommand.START_MUTUAL_AUTHENTICATION).getCode()
               ,BytesBuilder.merge(RND_A, new byte[]{(byte) keyNumber }))
               .timeout(TIMEOUT, TimeUnit.MILLISECONDS)

        .subscribe(new Subscriber<MpaioMessage>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {

                logger.d(TAG, "Error : " + e.toString());
                onResult(false);
            }

            @Override
            public void onNext(MpaioMessage mpaioMessage) {

                byte[] data = mpaioMessage.getData();

                boolean isOk = ResponseError.NO_ERROR.equals(ResponseError.fromCode(data[0]));

                if ( isOk ) {
                    System.arraycopy(data,1,RND_B, 0, RND_B.length);
                    verifyAuthentication();
                } else {
                    logger.d(TAG, "startAuthentication fail : response not ok ");
                    onResult(false);
                }


            }
        })
        ;

    }


    private void verifyAuthentication() {


        //generate keys
        byte[] kbpk = TR31.exclusiveOr(RND_A, RND_B, initKey);

        TR31 tr31 = null;
        try {
            tr31 = new TR31(kbpk);
            tr31.generateKeys();
            System.arraycopy(tr31.getKbek(), 0, kbek, 0, kbek.length);
            System.arraycopy(tr31.getKbmk(), 0, kbmk, 0, kbmk.length);

        } catch (InvalidKeyException e) {

            e.printStackTrace();
            onResult(false);

            return;

        } finally {
            if (tr31 != null) {
                tr31.removeKeys();
            }
            BytesBuilder.clear(kbpk);

        }

        //hash rand.

        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.reset();
            final byte[] hash = sha.digest(RND_B);

            if (LogTool.needKtcLog())
                logger.i(TAG, "init crypto with kbek");

             mDataCrypto.initKey(kbek);

            if (LogTool.needKtcLog())
                logger.i(TAG, "init crypto with kbmk");

            mHashCrypto.initKey(kbmk);

            byte[] encrypted = mDataCrypto.encrypt(hash);

            BytesBuilder.clear(hash);

            mConnectionManager.rxSyncRequest(mConnectionManager.getNextAid(), new MpaioCommand(MpaioCommand.VERIFY_MUTUAL_AUTHENTICATION).getCode(), encrypted)
                    .timeout(TIMEOUT, TimeUnit.MILLISECONDS)

                    .subscribe(new Subscriber<MpaioMessage>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                            logger.d(TAG, "Error " + e.toString());
                            onResult(false);
                        }

                        @Override
                        public void onNext(MpaioMessage mpaioMessage) {

                            byte[] data = mpaioMessage.getData();

                            boolean isOk = ResponseError.NO_ERROR.equals(ResponseError.fromCode(data[0]));

                            if ( isOk ) {

                                byte[] encryptedData = Arrays.copyOfRange(data,1, data.length);

                                if ( isEqualToRndA(encryptedData)) {

                                    confirmAuthentication();
                                } else {

                                    logger.d(TAG, "verifyAuthentication fail : not equal to sent ");
                                    onResult(false);
                                }

                            } else {

                                logger.d(TAG, "verifyAuthentication fail : response not ok ");
                                onResult(false);
                            }
                        }
                    });

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            onResult(false);
        }

    }

    private void confirmAuthentication() {

        byte[] data = ("OK"+swModel).getBytes();

        int tailLen = data.length % 16;

        if ( tailLen > 0)
            data = BytesBuilder.merge(data, new byte[16-tailLen]);

        byte[] encryptedData = mDataCrypto.encrypt(data);

        MessageDigest sha;
        byte[] hash = null;

        try {
            sha = MessageDigest.getInstance("SHA-256");
            hash = sha.digest(encryptedData);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] encryptedHash = mHashCrypto.encrypt(hash);

        BytesBuilder.clear(data);
        BytesBuilder.clear(hash);

        data = new byte[encryptedData.length+encryptedHash.length];
        System.arraycopy(encryptedData, 0, data, 0, encryptedData.length);
        System.arraycopy(encryptedHash, 0, data, encryptedData.length, encryptedHash.length);

        mConnectionManager.rxSyncRequest(mConnectionManager.getNextAid(), new MpaioCommand(MpaioCommand.CONFIRM_MUTUAL_AUTHENTICATION).getCode(), data)
                .timeout(TIMEOUT, TimeUnit.MILLISECONDS)

                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        logger.d(TAG, "Error : " + e.toString());
                        onResult(false);
                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {

                        byte[] data = mpaioMessage.getData();

                        boolean isOk = ResponseError.NO_ERROR.equals(ResponseError.fromCode(data[0]));

                        if ( !isOk)
                            logger.d(TAG, "verifyAuthentication fail : response not ok ");

                        onResult(isOk);
                    }
                });

        BytesBuilder.clear(hash);
        BytesBuilder.clear(encryptedHash);
        BytesBuilder.clear(data);

    }

    private boolean isEqualToRndA(byte[] encryptedData) {

        byte[] hashR = mDataCrypto.decrypt(encryptedData);

        MessageDigest sha;
        byte[] hash = null;
        try {
            sha = MessageDigest.getInstance("SHA-256");
            sha.reset();
            hash = sha.digest(RND_A);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try {
            return Arrays.equals(hash, hashR);
        } finally {
            BytesBuilder.clear(hash);
            BytesBuilder.clear(hashR);
            BytesBuilder.clear(RND_A);

        }
    }


    private void onResult(boolean isOk) {

        BytesBuilder.clear(kbek);
        BytesBuilder.clear(kbmk);
        BytesBuilder.clear(RND_A);
        BytesBuilder.clear(RND_B);

        if ( mAuthenticationCallback != null)
            mAuthenticationCallback.onCompleted(isOk);

        mAuthenticationCallback = null;

    }
}

