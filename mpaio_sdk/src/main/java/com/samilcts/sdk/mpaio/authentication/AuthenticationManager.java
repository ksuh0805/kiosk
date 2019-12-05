package com.samilcts.sdk.mpaio.authentication;

import com.samilcts.sdk.mpaio.callback.ResultCallback;

/**
 * Created by mskim on 2016-07-07.
 * mskim@31cts.com
 */
public interface AuthenticationManager {

    void authenticate(ResultCallback authenticationCallback);
}
