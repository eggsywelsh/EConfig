package com.eggsy.config;


import com.eggsy.config.internal.ConfigBinder;

import java.util.Properties;


/**
 * Created by eggsy on 16-12-3.
 */

public class EggsyConfig {

    public static void bindConfig(Object target, Properties prop){
        Class<?> targetClass = target.getClass();
        String clsName = targetClass.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
            throw new IllegalArgumentException("target class can't start with android or java");
        }
        try {
            Class<?> bindingClass = Class.forName(clsName + "_Binder");
            ConfigBinder viewBinder = (ConfigBinder<Object>) bindingClass.newInstance();
            viewBinder.bind(target,prop);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to find binder class for " + clsName, e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Unable to create binder for " + clsName, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to create binder for " + clsName, e);
        }
    }
}
