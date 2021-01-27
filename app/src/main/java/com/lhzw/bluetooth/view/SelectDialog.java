package com.lhzw.bluetooth.view;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.lhzw.bluetooth.R;

/**
 * Created by heCunCun on 2019/11/28
 */
public class SelectDialog extends BottomSheetDialog implements View.OnClickListener {

    private final TextView mTvCancel;
    private final TextView mTvCamera;
    private final TextView mTvPhotos;

    public SelectDialog(Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_bottom, null);
        setContentView(view);
        mTvPhotos = view.findViewById(R.id.tv_photos);
        mTvCamera = view.findViewById(R.id.tv_camera);
        mTvCancel = view.findViewById(R.id.tv_cancel);
        mTvPhotos.setOnClickListener(this);
        mTvCamera.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);
    }

    private OnChoseListener mOnChoseListener;

    @Override
    public void onClick(View view) {
        if (mOnChoseListener!=null){
            mOnChoseListener.select(view.getId());
        }

        dismiss();

    }

   public interface OnChoseListener {
        void select(int resID);
    }

    public void setOnChoseListener(OnChoseListener onChoseListener) {
        mOnChoseListener = onChoseListener;
    }
}
