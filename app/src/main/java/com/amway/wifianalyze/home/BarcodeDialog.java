package com.amway.wifianalyze.home;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.autofit.widget.EditText;
import com.autofit.widget.ScreenParameter;
import com.uuzuche.lib_zxing.activity.CodeUtils;

/**
 * Created by big on 2018/10/30.
 */

public class BarcodeDialog extends Dialog {
    public BarcodeDialog(@NonNull Context context) {
        super(context);
        init();
    }

    private ImageView mBarcodeImageView;

    private void init() {
        setContentView(R.layout.dialog_barcode);
        mBarcodeImageView = (ImageView) findViewById(R.id.barcode_img);

        String content = NetworkUtils.getMac(getContext());//todo
        int size = ScreenParameter.getFitSize(getContext(), 600);
        Bitmap bitmap = CodeUtils.createImage(content, size, size, null);
        mBarcodeImageView.setImageBitmap(bitmap);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

}

