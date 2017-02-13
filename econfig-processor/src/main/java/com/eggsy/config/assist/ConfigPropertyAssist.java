package com.eggsy.config.assist;


import com.eggsy.config.annotation.ConfigProperty;

import javax.lang.model.element.Element;

/**
 * Created by eggsy on 16-12-2.
 */

public class ConfigPropertyAssist {

    private ConfigProperty configAnno;

    private Element element;

    private String fieldName;

    private String enclosingClassName;

    private String dataType;

    public ConfigProperty getConfigAnno() {
        return configAnno;
    }

    public void setConfigAnno(ConfigProperty configAnno) {
        this.configAnno = configAnno;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public String getEnclosingClassName() {
        return enclosingClassName;
    }

    public void setEnclosingClassName(String enclosingClassName) {
        this.enclosingClassName = enclosingClassName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
