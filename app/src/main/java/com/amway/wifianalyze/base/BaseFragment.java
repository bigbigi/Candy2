package com.amway.wifianalyze.base;

import android.app.Activity;
import android.support.v4.app.Fragment;

/**
 * Created by big on 2018/10/17.
 */

public abstract class BaseFragment extends Fragment {

    public boolean isFinishing() {
        Activity activity = getActivity();
        return activity == null || activity.isFinishing();
    }
}
