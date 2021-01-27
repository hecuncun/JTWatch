package com.lhzw.bluetooth.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.lhzw.bluetooth.R;


public class LoadingView extends ProgressDialog {
    private TextView mTvLoadingMsg;
    private View mView;

    public LoadingView(Context context) {
        this(context, R.style.CustomProgressDialog);
    }

    public LoadingView(Context context, int theme) {
        super(context, theme);
        mView = LayoutInflater.from(context).inflate(R.layout.loading_view, null, false);
        mTvLoadingMsg = (TextView) mView.findViewById(R.id.loadingMsg);
    }

    @Override
    public void show() {
        super.show();
        setCanceledOnTouchOutside(false);
        setContentView(mView);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*if (!isShowing()){
            super.onBackPressed();
        }*/
    }

    public void setLoadingTitle(String loadingTitle) {
        mTvLoadingMsg.setText(loadingTitle);
    }
}
