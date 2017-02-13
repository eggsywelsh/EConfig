package com.eggsy.config.internal;

import java.util.Properties;

/**
 * Created by eggsy on 16-12-3.
 */

public interface ConfigBinder<T> {

    void bind(T config, Properties prop) ;

}
