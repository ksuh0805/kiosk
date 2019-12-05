package com.samilcts.util.android.text;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;


/**
 * text formatter class
 * default format is  ###,###.##
 */

public class FormattingTextWatcher implements TextWatcher {
    private final TextView mTextView;
    private String mFormat = "###,###.##";
    private boolean mUseSymbol = false;
    private Locale mLocale = Locale.US;
    private int mMaxPointLength = 0;

    public FormattingTextWatcher(TextView textView) {
        mTextView = textView;

        mTextView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setMaxPointLength();
    }

    public FormattingTextWatcher(TextView textView, String format) {
        mTextView = textView;
        mFormat = format;
        mTextView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        setMaxPointLength();
    }

    public void enableSymbol(Locale locale) {

        mLocale = locale;
        mUseSymbol = true;
    }
    public void disableSymbol() {
        mUseSymbol = false;
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {    //텍스트가 변경될때마다 실행
        format(s);

    }



    private void setMaxPointLength() {

        if ( mFormat.contains(".")) {

            String temp[] = mFormat.split("\\.");

            if ( temp.length > 1)
                mMaxPointLength = temp[1].length();
        }



    }

    private void format(CharSequence s)
    {
        try {

            if (s.toString().length() >= 0)
            {
                String convertedStr = s.toString();

                String symbol = Currency.getInstance(mLocale).getSymbol(mLocale);

                convertedStr = convertedStr.replaceAll("[^0-9.]+", "");


                if ( convertedStr.equals("") ) convertedStr = "0";

                if (convertedStr.contains("."))
                {
                    if ( convertedStr.lastIndexOf(".") != convertedStr.indexOf(".") ) {

                        convertedStr = convertedStr.substring(0, convertedStr.length()-1);
                    }


                    String temp[] = convertedStr.split("\\.");

                    if ( temp.length > 1 && temp[1].length() > mMaxPointLength ) {

                        convertedStr = convertedStr.substring(0, convertedStr.length()-1);
                    }

                    if ( mMaxPointLength == 0) {
                        convertedStr = convertedStr.substring(0, convertedStr.length()-1);
                    }



                    if (chkConvert(convertedStr))
                        //convertedStr = customFormat(mFormat,Double.parseDouble(convertedStr.replace(",","")));
                        convertedStr = customFormat(mFormat,new BigDecimal(convertedStr.replace(",","")));
                }
                else
                {
                    //convertedStr = customFormat(mFormat, Double.parseDouble(convertedStr.replace(",","")));
                    convertedStr = customFormat(mFormat,new BigDecimal(convertedStr.replace(",","")));
                }

                if ( mUseSymbol) {
                    convertedStr = symbol + convertedStr;
                }

                if (!mTextView.getText().toString().equals(convertedStr) && convertedStr.length() > 0) {


                    if (mTextView instanceof EditText) {
                        mTextView.removeTextChangedListener(this);
                        mTextView.getEditableText().clear();
                        mTextView.append(convertedStr);
                        mTextView.addTextChangedListener(this);

                    }
                    else
                        mTextView.setText(convertedStr);

                }

                //mTextView.setSelection(mTextView.getText().length());

                if (mTextView instanceof EditText) {
                    ((EditText)mTextView).setSelection(mTextView.getText().length());
                }
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    public String customFormat(String pattern, BigDecimal value) {

        NumberFormat numberFormat = NumberFormat.getNumberInstance(mLocale);
        DecimalFormat myFormatter = (DecimalFormat) numberFormat;
        myFormatter.applyPattern(pattern);
        myFormatter.setRoundingMode(RoundingMode.UNNECESSARY);


        return myFormatter.format(value);
    }

    public boolean chkConvert(String s) {

        String tempArray[] = s.split("\\.");
        return tempArray.length > 1 && Integer.parseInt(tempArray[1]) > 0;
    }

}
