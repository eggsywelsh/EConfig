package com.eggsy.econfig.context;

import android.app.Application;
import android.support.annotation.LayoutRes;
import android.view.View;

/**
 * Created by eggsy on 16-11-30.
 * 系统环境类
 */

public class Env {

    public static Application sApplication;

    public static View inflate(@LayoutRes int resId) {
        View view = null;

        if (sApplication != null && resId != 0) {
            view = View.inflate(sApplication, resId, null);
        }

        return view;
    }

}
