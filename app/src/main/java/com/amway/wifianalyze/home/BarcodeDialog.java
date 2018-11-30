package com.amway.wifianalyze.home;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.bean.DeviceInfo;
import com.autofit.widget.ScreenParameter;
import com.uuzuche.lib_zxing.activity.CodeUtils;

/**
 * Created by big on 2018/10/30.
 */

public class BarcodeDialog extends Dialog {
    public BarcodeDialog(@NonNull Context context) {
        super(context, R.style.transparent_dialog);
        init();
    }

    private ImageView mBarcodeImageView;
    private View mWarn;

    private void init() {
        View contentView = View.inflate(getContext(), R.layout.dialog_barcode, null);
        setContentView(contentView, new WindowManager.LayoutParams(-1, -1));
        getWindow().setLayout(-1, -1);
        mBarcodeImageView = (ImageView) findViewById(R.id.barcode_img);
        mWarn = findViewById(R.id.barcode_text);

        WifiManager wm = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wm.getConnectionInfo();
        DeviceInfo deviceInfo = HomeBiz.getInstance(getContext()).getDeviceInfo();
        if (wifiInfo != null && deviceInfo != null && !TextUtils.isEmpty(deviceInfo.ap)) {
            String content = deviceInfo.toJson().toString();
            int size = ScreenParameter.getFitSize(getContext(), 600);
            Bitmap bitmap = CodeUtils.createImage(content, size, size, null);
            mWarn.setVisibility(View.GONE);
            mBarcodeImageView.setImageBitmap(bitmap);
        } else {
            mWarn.setVisibility(View.VISIBLE);
            mBarcodeImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

}

