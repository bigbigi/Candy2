package com.amway.wifianalyze.feedback;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BasePresenterImpl;

import java.util.List;

/**
 * Created by big on 2018/10/26.
 */

public interface FeedbackContract extends BaseContract {

    interface FeedbackView extends BaseView {
        void updateTime(String s);

        void onRecordStart();

        void onRecordStop();

    }

    abstract class FeedbackPresenter extends BasePresenterImpl<FeedbackView> {


        public FeedbackPresenter(FeedbackView view) {
            super(view);
        }

        public abstract void submit(final Context context, final List<String> list, final String content);

        public abstract void startRecord(final Activity context);

        public abstract void stopRecord();

    }

}
