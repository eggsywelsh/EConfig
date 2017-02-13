package com.eggsy.econfig;


import com.eggsy.config.EggsyConfig;
import com.eggsy.config.annotation.ConfigProperty;
import com.eggsy.econfig.context.Env;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/**
 * Created by eggsy on 16-12-1.
 */
public final class RuntimeConfig {

    private static final String TAG = RuntimeConfig.class.getName();

    @ConfigProperty(name = "min_free_sdcard_size", defaultValue = "20")
    public int minFreeSdcardSize;

    @ConfigProperty(name = "version")
    public int version;

    @ConfigProperty(name = "log_level")
    public int logLevel;

    @ConfigProperty(name = "date_time",format = "yyyy-MM-dd HH:mm")
    public Date dateTime;

    @ConfigProperty(name = "base_sdcard_dir")
    public String baseSdcardDir;

    @ConfigProperty(name = "db_name")
    public String dbName;

    protected void init(Properties prop) {
        EggsyConfig.bindConfig(this, prop);
    }

    private RuntimeConfig() {
    }

    private static RuntimeConfig mConfig;

    public static RuntimeConfig obtain() {
        if (mConfig == null) {
            synchronized (RuntimeConfig.class) {
                if (mConfig == null) {
                    mConfig = new RuntimeConfig();

                    Properties prop = new Properties();
                    try {
                        prop.load(Env.sApplication.getResources().getAssets().open("runtime_config.properties"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (prop.size() > 0) {
                        mConfig.init(prop);
                    }
                }
            }
        }
        return mConfig;
    }
}
