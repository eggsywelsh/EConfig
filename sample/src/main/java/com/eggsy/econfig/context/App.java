package com.eggsy.econfig.context;

import android.app.Application;

/**
 * Created by eggsy on 17-2-13.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Env.sApplication = this;
    }
}
