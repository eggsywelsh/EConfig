package com.eggsy.config;

import com.eggsy.config.annotation.ConfigProperty;
import com.eggsy.config.assist.ConfigPropertyAssist;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class ConfigProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<>();
        annotataions.add(ConfigProperty.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        HashMap<String, ArrayList<ConfigPropertyAssist>> classMaps = new HashMap<>();

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(ConfigProperty.class)) {
            if (annotatedElement.getKind() == ElementKind.FIELD) {

                VariableElement te = (VariableElement) annotatedElement;

                note("variable type : " + te.asType().toString());

                String className = annotatedElement.getEnclosingElement().getSimpleName().toString();
                ArrayList<ConfigPropertyAssist> list = classMaps.get(className);
                if (list == null) {
                    list = new ArrayList<>();
                    classMaps.put(className, list);
                }
                ConfigPropertyAssist assist = new ConfigPropertyAssist();
                assist.setConfigAnno(annotatedElement.getAnnotation(ConfigProperty.class));
                assist.setElement(te);
                assist.setEnclosingClassName(className);
                assist.setFieldName(te.getSimpleName().toString());
                assist.setDataType(getDataType(te));
                list.add(assist);
            } else {
                error(annotatedElement, "Only field can be annotated with @%s",
                        ConfigProperty.class.getSimpleName());
                return true; // Exit processing
            }
        }
        return generate(classMaps);
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    private void error(String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args));
    }

    private String getDataType(VariableElement variableElement) {
        String typeString = "";
        if (isBasicType(variableElement)) {
            switch (variableElement.asType().getKind()) {
                case BOOLEAN:
                    typeString = "java.lang.Boolean";
                    break;
                case BYTE:
                    typeString = "java.lang.Byte";
                    break;
                case SHORT:
                    typeString = "java.lang.Short";
                    break;
                case INT:
                    typeString = "java.lang.Integer";
                    break;
                case LONG:
                    typeString = "java.lang.Long";
                    break;
                case CHAR:
                    typeString = "java.lang.Char";
                    break;
                case FLOAT:
                    typeString = "java.lang.Float";
                    break;
                case DOUBLE:
                    typeString = "java.lang.Double";
                    break;
                default:
                    break;
            }
        } else {
            typeString = variableElement.asType().toString();
        }
        return typeString;
    }

    private boolean isBasicType(Element element) {
        return element.asType().getKind().isPrimitive();
    }

    private boolean generate(HashMap<String, ArrayList<ConfigPropertyAssist>> classMaps) {
        if (classMaps.size() > 0) {
            for (Map.Entry<String, ArrayList<ConfigPropertyAssist>> entry : classMaps.entrySet()) {
                String simpleClassName = entry.getKey();
                ArrayList<ConfigPropertyAssist> configPropertyAssists = entry.getValue();

                if (simpleClassName != null && configPropertyAssists != null && configPropertyAssists.size() > 0) {
                    String packageName = configPropertyAssists.get(0).getElement().getEnclosingElement().getEnclosingElement().toString();
                    String binderClassName = simpleClassName + "_" + "Binder";
                    try {
                        /*
                         * 生成类
                         */
                        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(binderClassName);

                        /**
                         * 生成bind方法
                         */
                        MethodSpec bindMs = generateBindMethod(configPropertyAssists);

                        /*
                         * 生成parseValue方法
                         */
                        MethodSpec parseValueMs = generateParseValueMethod();

                        /*
                         * 构建类
                         */
                        TypeSpec typeSpec = classBuilder  //类名
                                .addSuperinterface(ParameterizedTypeName.get(ClassName.bestGuess("com.eggsy.config.internal.ConfigBinder"), TypeVariableName.get("T")))
                                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                                .addMethod(bindMs)  //在类中添加方法
                                .addMethod(parseValueMs)
                                .addTypeVariable(TypeVariableName.get("T extends " + simpleClassName))
                                .build();
                        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
                        javaFile.writeTo(filer);
                        return true;
                    } catch (IOException e) {
                        error(e.getMessage());
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private MethodSpec generateBindMethod(ArrayList<ConfigPropertyAssist> configPropertyAssists) {
        final String bindMethodPsName1 = "config";
        final String bindMethodPsName2 = "prop";
        // 参数一：config配置类
        ParameterSpec ps1 = ParameterSpec.builder(TypeVariableName.get("T"), bindMethodPsName1).build();
        ParameterSpec ps2 = ParameterSpec.builder(Properties.class, bindMethodPsName2).build();
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class).addParameter(ps1)
                .addParameter(ps2);
        if (configPropertyAssists != null && configPropertyAssists.size() > 0) {
            for (ConfigPropertyAssist assist : configPropertyAssists) {
                ConfigProperty cp = assist.getConfigAnno();
                String propSrcName = cp.name();
                String propFormat = cp.format();
                String propDefaultValue = cp.defaultValue();
                String dateType = assist.getDataType();
                builder.addStatement(bindMethodPsName1 + "." + assist.getFieldName()
                        + "=parseValue(" + dateType + ".class,prop.getProperty(\"" + propSrcName + "\",\"" + propDefaultValue + "\")," + ("".equals(propFormat) ? "\"\"" : ("\"" + propFormat + "\"")) + ")");
            }
        }

        return builder.build();
    }

    private MethodSpec generateParseValueMethod() {
        final String parseValuePsName1 = "typeClazz";
        final String parseValuePsName2 = "requestCode";
        final String parseValuePsName3 = "format";
        ParameterSpec pvps1 = ParameterSpec.builder(TypeVariableName.get("Class<V>"), parseValuePsName1).build();
        ParameterSpec pvps2 = ParameterSpec.builder(String.class, parseValuePsName2).build();
        ParameterSpec pvps3 = ParameterSpec.builder(String.class, parseValuePsName3).build();
        MethodSpec.Builder pvBuilder = MethodSpec.methodBuilder("parseValue")
                .returns(TypeVariableName.get("<V> V"))
                .addModifiers(Modifier.PRIVATE)
                .addParameter(pvps1).addParameter(pvps2).addParameter(pvps3)

                .beginControlFlow("if (typeClazz == null && requestCode == null)")
                .addStatement("return (V)\"\"")
                .endControlFlow()

                .beginControlFlow("if (typeClazz == String.class)")
                .addStatement("return (V) requestCode")
                .endControlFlow()

                .beginControlFlow("else if (typeClazz == Integer.class)")
                .addStatement("return (V) Integer.valueOf(requestCode)")
                .endControlFlow()

                .beginControlFlow("else if (typeClazz == Long.class)")
                .addStatement("return (V) Long.valueOf(requestCode)")
                .endControlFlow()

                .beginControlFlow("else if (typeClazz == Boolean.class)")
                .addStatement("return (V) Boolean.valueOf(requestCode)")
                .endControlFlow()

                .beginControlFlow("else if (typeClazz == Float.class)")
                .addStatement("return (V) Float.valueOf(requestCode)")
                .endControlFlow()

                .beginControlFlow("else if (typeClazz == Double.class)")
                .addStatement("return (V) Double.valueOf(requestCode)")
                .endControlFlow()

                .beginControlFlow("else if (typeClazz == Short.class)")
                .addStatement("return (V) Short.valueOf(requestCode)")
                .endControlFlow()

                .beginControlFlow("else if (typeClazz == Byte.class)")
                .addStatement("return (V) Byte.valueOf(requestCode)")
                .endControlFlow()

                .beginControlFlow("else if (typeClazz == Character.class)")
                .addStatement("return (V) requestCode")
                .endControlFlow()

                .beginControlFlow("else if (typeClazz == java.util.Date.class)")
                .beginControlFlow("if (format != null)")
                .addStatement(
                        "try {\n" +
                                "    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(format);\n" +
                                "    java.util.Date finalDate = df.parse(requestCode);\n" +
                                "    return (V) finalDate;\n" +
                                "} catch (Exception e) {\n" +
                                "    e.printStackTrace();" +
                                "\n}")
                .endControlFlow()
                .endControlFlow()

                .addStatement("return (V) requestCode");
        return pvBuilder.build();
    }

    private void note(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

}
