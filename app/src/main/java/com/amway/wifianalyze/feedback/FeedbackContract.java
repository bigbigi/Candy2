package com.amway.wifianalyze.feedback;

import android.graphics.Bitmap;

import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BasePresenterImpl;

import java.util.List;

/**
 * Created by big on 2018/10/26.
 */

public class FeedbackContract implements BaseContract {

    interface FeedbackView extends BaseView {
    }

    abstract class FeedbackPresenter extends BasePresenterImpl<FeedbackView> {

        public FeedbackPresenter(FeedbackView view) {
            super(view);
        }

        public abstract void submint(List<Bitmap> list, String content);

        public abstract void addPicture();
    }

}
