package com.thomas.rxpersistence;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.thomas.rxpersistence.CacheEntity;
import com.thomas.rxpersistence.CacheField;
import com.thomas.rxpersistence.CacheMode;
import com.thomas.rxpersistence.SPEntity;
import com.thomas.rxpersistence.SPField;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;


@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class RxPersistenceProcessor extends AbstractProcessor {

    private Elements elementUtils;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(SPEntity.class.getCanonicalName());
        annotations.add(CacheEntity.class.getCanonicalName());
        return annotations;
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(SPEntity.class);
        processSP(elements);
        elements = roundEnv.getElementsAnnotatedWith(CacheEntity.class);
        processCache(elements);
        return true;
    }

    private void processCache(Set<? extends Element> elements) {
        for (Element element : elements) {
            // 判断是否Class
            if (!(element instanceof TypeElement)) {
                continue;
            }
            TypeSpec typeSpec = null;
            TypeElement typeElement = (TypeElement) element;
            CacheEntity typeElementAnnotation = typeElement.getAnnotation(CacheEntity.class);
            if (typeElementAnnotation.mode() == CacheMode.MEMORY) {
                typeSpec = processMemoryCache("", typeElement, typeElementAnnotation.memoryMaxCount(), typeElementAnnotation.global());
            } else if (typeElementAnnotation.mode() == CacheMode.DISK) {
                typeSpec = processDiskCache("",typeElement, typeElementAnnotation.diskMaxCount(), typeElementAnnotation.diskMaxSize(), typeElementAnnotation.global());
            } else if (typeElementAnnotation.mode() == CacheMode.DOUBLE) {
                typeSpec = processDoubleCache(typeElement, typeElementAnnotation.memoryMaxCount(), typeElementAnnotation.diskMaxCount(), typeElementAnnotation.diskMaxSize(), typeElementAnnotation.global());
            }
            JavaFile javaFile = JavaFile.builder(getPackageName(typeElement), typeSpec).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private TypeSpec processDoubleCache(TypeElement typeElement, int memoryMaxCount, int diskMaxCount, long diskMaxSize, boolean global) {
        // 获取该类的全部成员，包括
        List<? extends Element> members = elementUtils.getAllMembers(typeElement);
        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (Element element : members) {
            // 忽略除了成员方法外的元素
            if (!(element instanceof ExecutableElement)) {
                continue;
            }
            //忽略final、static 方法
            if (element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }

            ExecutableElement executableElement = (ExecutableElement) element;
            //父类方法名称
            String name = element.getSimpleName().toString();
            // 忽略基类的一个get方法
            if (name.equals("getClass")) {
                continue;
            }

            // 忽略不是get、set、is 开头的方法
            boolean getter = false;
            if (name.startsWith("get") || name.startsWith("is")) {
                getter = true;
            } else if (name.startsWith("set")) {
                getter = false;
            } else {
                continue;
            }
            // 从方法名称提取成员变量的名称
            String fieldName = name.replaceFirst("get|is|set", "");
            fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);

            // 根据名称提取成员变量的元素
            Element fieldElement = getElement(members, fieldName);
            if (fieldElement == null) {
                continue;
            }
            // 检查是否有注解
            CacheField annotation = fieldElement.getAnnotation(CacheField.class);
            // 检查是否需要保存
            if (annotation != null && !annotation.save()) {
                continue;
            }
            boolean globalField = true;
            if (annotation != null && !annotation.global()) {
                globalField = false;
            }
            int saveTime = -1;
            if (annotation != null) {
                saveTime = annotation.saveTime();
            }

            String modName = "";
            boolean isBaseType = false;
            boolean isObject = false;
            TypeName type = TypeName.get(fieldElement.asType());
            if (type.equals(TypeName.get(String.class))) {
                if (getter) {
                    modName = "getString";
                }
            } else if (type.equals(ClassName.get("android.graphics.drawable", "Drawable"))) {
                if (getter) {
                    modName = "getDrawable";
                }
            } else if (type.equals(ClassName.get("android.graphics", "Bitmap"))) {
                if (getter) {
                    modName = "getBitmap";
                }
            } else if (type.equals(TypeName.BOOLEAN)
                    || type.equals(TypeName.INT)
                    || type.equals(TypeName.DOUBLE)
                    || type.equals(TypeName.FLOAT)
                    || type.equals(TypeName.LONG)) {
              continue;
            } else {
                if (getter) {
                    modName = "getString";
                }
                isObject = true;
            }
            ParameterizedTypeName rxfield = ParameterizedTypeName.get(ClassName.get("com.thomas.rxpersistence", "RxField"), type.box());
            ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get("io.reactivex", "Observable"), rxfield);
            if (name.startsWith("set")) {
                //setter
                String parameterName = executableElement.getParameters().get(0).getSimpleName().toString();
                MethodSpec setMethod;

                    setMethod = MethodSpec.overriding(executableElement)
                            .addStatement(String.format("mDiskCache.%s(%s)", name, parameterName))
                            .addStatement(String.format("mMemoryCache.%s(%s)", name, parameterName))
                            .build();

                MethodSpec setMethodRx = MethodSpec.methodBuilder(name + "Rx")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(type, parameterName, Modifier.FINAL)
                        .returns(typeName)
                        .addCode("    return Observable.create(new $T<$T>() {\n", ClassName.get("io.reactivex", "ObservableOnSubscribe"), rxfield)
                        .addCode("        public void subscribe($T<$T> emitter) throws Exception {\n", ClassName.get("io.reactivex", "ObservableEmitter"), rxfield)
                        .addCode(String.format(
                                "            try {\n" +
                                        "                %s(%s);\n" +
                                        "                emitter.onNext(new RxField(%s));\n" +
                                        "                emitter.onComplete();\n" +
                                        "            } catch (Exception e) {\n" +
                                        "                    emitter.onError(e);\n" +
                                        "            }\n" +
                                        "        }\n" +
                                        "    });\n", name, parameterName, parameterName))
                        .build();
                methodSpecs.add(setMethodRx);
                methodSpecs.add(setMethod);
            } else {
                //getter
                MethodSpec setMethod= MethodSpec.overriding(executableElement)
                            .addStatement(String.format("%s obj = mMemoryCache.%s()", type.box(), name))
                            .addStatement("if(obj!=null) return obj")
                            .addStatement(String.format("return mDiskCache.%s()", name))
                            .build();


                MethodSpec setMethodRx = MethodSpec.methodBuilder(name + "Rx")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(typeName)
                        .addCode("    return Observable.create(new $T<$T>() {\n", ClassName.get("io.reactivex", "ObservableOnSubscribe"), rxfield)
                        .addCode("        public void subscribe($T<$T> emitter) throws Exception {\n", ClassName.get("io.reactivex", "ObservableEmitter"), rxfield)
                        .addCode(String.format(
                                "            try {\n" +
                                        "                emitter.onNext(new RxField(%s()));\n" +
                                        "                emitter.onComplete();\n" +
                                        "            } catch (Exception e) {\n" +
                                        "                    emitter.onError(e);\n" +
                                        "            }\n" +
                                        "        }\n" +
                                        "    });\n", name))
                        .build();
                methodSpecs.add(setMethodRx);
                methodSpecs.add(setMethod);

            }

        }
        TypeName targetClassName = ClassName.get(getPackageName(typeElement), typeElement.getSimpleName() + "DoubleCache");
        MethodSpec getMethodSpec2 = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(targetClassName)
                .addCode("if (sInstance == null){\n" +
                        "   synchronized ($T.class){\n" +
                        "      if (sInstance == null){\n" +
                        "          sInstance = new $T();\n" +
                        "      }\n" +
                        "   }\n" +
                        "}\n" +
                        "return sInstance;\n", targetClassName, targetClassName)
                .build();

        MethodSpec clearMethodSpec = MethodSpec.methodBuilder("clear")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addStatement("mMemoryCache.clear()")
                .addStatement("mDiskCache.clear()")
                .build();

        MethodSpec getDiskSizeMethodSpec = MethodSpec.methodBuilder("getCacheDiskSize")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.LONG)
                .addStatement("return mDiskCache.getCacheSize()")
                .build();
        MethodSpec getDiskCountMethodSpec = MethodSpec.methodBuilder("getCacheDiskCount")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT)
                .addStatement("return mDiskCache.getCacheCount()")
                .build();
        MethodSpec getMemoryCountMethodSpec = MethodSpec.methodBuilder("getCacheMemoryCount")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT)
                .addStatement("return mMemoryCache.getCacheCount()")
                .build();
        ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get("io.reactivex", "Observable"), ClassName.get(Boolean.class));

        MethodSpec setMethodRx = MethodSpec.methodBuilder("clearRx")
                .addModifiers(Modifier.PUBLIC)
                .returns(typeName)
                .addCode("    return Observable.create(new $T<$T>() {\n", ClassName.get("io.reactivex", "ObservableOnSubscribe"), ClassName.get(Boolean.class))
                .addCode("        public void subscribe($T<$T> emitter) throws Exception {\n", ClassName.get("io.reactivex", "ObservableEmitter"), TypeName.BOOLEAN.box())
                .addCode(String.format(
                        "            try {\n" +
                                "                mMemoryCache.clear();\n" +
                                "                emitter.onNext(mDiskCache.clear());\n" +
                                "                emitter.onComplete();\n" +
                                "            } catch (Exception e) {\n" +
                                "                    emitter.onError(e);\n" +
                                "            }\n" +
                                "        }\n" +
                                "    });\n"))
                .build();

        MethodSpec resetMethodSpec = MethodSpec.methodBuilder("reset")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addStatement("sInstance = null")
                .build();
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement(String.format("mDiskCache = %s.get()",typeElement.getSimpleName() + "DiskCache"))
                .addStatement(String.format("mMemoryCache = %s.get()",typeElement.getSimpleName() + "MemoryCache"))
                .build();

        FieldSpec fieldSpec = FieldSpec.builder(targetClassName, "sInstance", Modifier.PRIVATE, Modifier.VOLATILE, Modifier.STATIC)
                .build();
        TypeSpec typeSpec = TypeSpec.classBuilder(typeElement.getSimpleName() + "DoubleCache")
                .superclass(TypeName.get(typeElement.asType()))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethods(methodSpecs)
                .addType(processMemoryCache(typeElement.getSimpleName() + "DoubleCache",typeElement,memoryMaxCount,global))
                .addType(processDiskCache(typeElement.getSimpleName() + "DoubleCache",typeElement,diskMaxCount,diskMaxSize,global))
                .addMethod(getMethodSpec2)
                .addMethod(constructor)
                .addMethod(clearMethodSpec)
                .addMethod(getDiskCountMethodSpec)
                .addMethod(getDiskSizeMethodSpec)
                .addMethod(getMemoryCountMethodSpec)
                .addMethod(resetMethodSpec)
                .addMethod(setMethodRx)
                .addField(ClassName.get(getPackageName(typeElement)+"."+typeElement.getSimpleName()+"DoubleCache", typeElement.getSimpleName()+"DiskCache"), "mDiskCache", Modifier.PRIVATE, Modifier.FINAL)
                .addField(ClassName.get(getPackageName(typeElement)+"."+typeElement.getSimpleName()+"DoubleCache", typeElement.getSimpleName()+"MemoryCache"), "mMemoryCache", Modifier.PRIVATE, Modifier.FINAL)
                .addField(fieldSpec)
                .build();
        return typeSpec;
    }

    private TypeSpec processDiskCache(String prefix, TypeElement typeElement, int diskMaxCout, long diskMaxSize, boolean global) {
        // 获取该类的全部成员，包括
        List<? extends Element> members = elementUtils.getAllMembers(typeElement);
        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (Element element : members) {
            // 忽略除了成员方法外的元素
            if (!(element instanceof ExecutableElement)) {
                continue;
            }
            //忽略final、static 方法
            if (element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }

            ExecutableElement executableElement = (ExecutableElement) element;
            //父类方法名称
            String name = element.getSimpleName().toString();
            // 忽略基类的一个get方法
            if (name.equals("getClass")) {
                continue;
            }

            // 忽略不是get、set、is 开头的方法
            boolean getter = false;
            if (name.startsWith("get") || name.startsWith("is")) {
                getter = true;
            } else if (name.startsWith("set")) {
                getter = false;
            } else {
                continue;
            }
            // 从方法名称提取成员变量的名称
            String fieldName = name.replaceFirst("get|is|set", "");
            fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);

            // 根据名称提取成员变量的元素
            Element fieldElement = getElement(members, fieldName);
            if (fieldElement == null) {
                continue;
            }
            // 检查是否有注解
            CacheField annotation = fieldElement.getAnnotation(CacheField.class);
            // 检查是否需要保存
            if (annotation != null && !annotation.save()) {
                continue;
            }
            boolean globalField = true;
            if (annotation != null && !annotation.global()) {
                globalField = false;
            }
            int saveTime = -1;
            if (annotation != null) {
                saveTime = annotation.saveTime();
            }

            String modName = "";
            boolean isBaseType = false;
            boolean isObject = false;
            TypeName type = TypeName.get(fieldElement.asType());
            if (type.equals(TypeName.get(String.class))) {
                if (getter) {
                    modName = "getString";
                }
            } else if (type.equals(ClassName.get("android.graphics.drawable", "Drawable"))) {
                if (getter) {
                    modName = "getDrawable";
                }
            } else if (type.equals(ClassName.get("android.graphics", "Bitmap"))) {
                if (getter) {
                    modName = "getBitmap";
                }
            } else if (type.equals(TypeName.BOOLEAN)
                    || type.equals(TypeName.INT)
                    || type.equals(TypeName.DOUBLE)
                    || type.equals(TypeName.FLOAT)
                    || type.equals(TypeName.LONG)) {
                continue;
            } else {
                if (getter) {
                    modName = "getString";
                }
                isObject = true;
            }
            ParameterizedTypeName rxfield = ParameterizedTypeName.get(ClassName.get("com.thomas.rxpersistence", "RxField"), type.box());
            ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get("io.reactivex", "Observable"), rxfield);
            if (name.startsWith("set")) {
                //setter
                String parameterName = executableElement.getParameters().get(0).getSimpleName().toString();
                MethodSpec setMethod;
                if (isObject) {
                    setMethod = MethodSpec.overriding(executableElement)
                            .addStatement(String.format("mDiskCache.put(getRealKey(\"%s\",%b), RxPersistence.getParser().serialize(%s),%d)", fieldName, globalField, parameterName, saveTime))
                            .build();
                } else if (!isBaseType) {
                    setMethod = MethodSpec.overriding(executableElement)
                            .addStatement(String.format("mDiskCache.put(getRealKey(\"%s\",%b), %s,%d)", fieldName, globalField, parameterName, saveTime))
                            .build();
                } else {
                    setMethod = MethodSpec.overriding(executableElement)
                            .addStatement(String.format("mDiskCache.put(getRealKey(\"%s\",%b),String.valueOf(%s),%d)", fieldName, globalField, parameterName, saveTime))
                            .build();
                }
                MethodSpec setMethodRx = MethodSpec.methodBuilder(name + "Rx")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(type, parameterName, Modifier.FINAL)
                        .returns(typeName)
                        .addCode("    return Observable.create(new $T<$T>() {\n", ClassName.get("io.reactivex", "ObservableOnSubscribe"), rxfield)
                        .addCode("        public void subscribe($T<$T> emitter) throws Exception {\n", ClassName.get("io.reactivex", "ObservableEmitter"), rxfield)
                        .addCode(String.format(
                                "            try {\n" +
                                        "                %s(%s);\n" +
                                        "                emitter.onNext(new RxField(%s));\n" +
                                        "                emitter.onComplete();\n" +
                                        "            } catch (Exception e) {\n" +
                                        "                    emitter.onError(e);\n" +
                                        "            }\n" +
                                        "        }\n" +
                                        "    });\n", name, parameterName, parameterName))
                        .build();
                methodSpecs.add(setMethodRx);
                methodSpecs.add(setMethod);
            } else {
                //getter
                MethodSpec setMethod;
                if (isObject) {
                    setMethod = MethodSpec.overriding(executableElement)
                            .addStatement(String.format("return ((%s)RxPersistence.getParser().deserialize(\n" +
                                    "new %s<%s>() {}.getType(),mDiskCache.%s(getRealKey(\"%s\",%b),\n" +
                                    "RxPersistence.getParser().serialize(super.%s()))))", type.box(), ClassName.get("com.google.gson.reflect", "TypeToken"), type.box(), modName, fieldName, globalField, name))
                            .build();
                } else if (!isBaseType) {
                    setMethod = MethodSpec.overriding(executableElement)
                            .addStatement(String.format("return mDiskCache.%s(getRealKey(\"%s\",%b), super.%s())", modName, fieldName, globalField, name))
                            .build();
                } else {
                    setMethod = MethodSpec.overriding(executableElement)
                            .addStatement(String.format("return %s.valueOf(mDiskCache.%s(getRealKey(\"%s\",%b),String.valueOf(super.%s())))", type.box(), modName, fieldName, globalField, name))
                            .build();
                }

                MethodSpec setMethodRx = MethodSpec.methodBuilder(name + "Rx")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(typeName)
                        .addCode("    return Observable.create(new $T<$T>() {\n", ClassName.get("io.reactivex", "ObservableOnSubscribe"), rxfield)
                        .addCode("        public void subscribe($T<$T> emitter) throws Exception {\n", ClassName.get("io.reactivex", "ObservableEmitter"), rxfield)
                        .addCode(String.format(
                                "            try {\n" +
                                        "                emitter.onNext(new RxField(%s()));\n" +
                                        "                emitter.onComplete();\n" +
                                        "            } catch (Exception e) {\n" +
                                        "                    emitter.onError(e);\n" +
                                        "            }\n" +
                                        "        }\n" +
                                        "    });\n", name))
                        .build();
                methodSpecs.add(setMethodRx);
                methodSpecs.add(setMethod);

            }

        }
        TypeName targetClassName;
        if(prefix.length()>0) {
             targetClassName = ClassName.get(getPackageName(typeElement) + "." + prefix, typeElement.getSimpleName() + "DiskCache");
        }else {
             targetClassName = ClassName.get(getPackageName(typeElement), typeElement.getSimpleName() + "DiskCache");

        }
        MethodSpec getMethodSpec2 = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(targetClassName)
                .addCode("if (sInstance == null){\n" +
                        "   synchronized ($T.class){\n" +
                        "      if (sInstance == null){\n" +
                        "          sInstance = new $T();\n" +
                        "      }\n" +
                        "   }\n" +
                        "}\n" +
                        "return sInstance;\n", targetClassName, targetClassName)
                .build();

        MethodSpec clearMethodSpec = MethodSpec.methodBuilder("clear")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addStatement("return mDiskCache.clear()")
                .build();
        MethodSpec getSizeMethodSpec = MethodSpec.methodBuilder("getCacheSize")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.LONG)
                .addStatement("return mDiskCache.getCacheSize()")
                .build();
        MethodSpec getCountMethodSpec = MethodSpec.methodBuilder("getCacheCount")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT)
                .addStatement("return mDiskCache.getCacheCount()")
                .build();
        ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get("io.reactivex", "Observable"), ClassName.get(Boolean.class));

        MethodSpec setMethodRx = MethodSpec.methodBuilder("clearRx")
                .addModifiers(Modifier.PUBLIC)
                .returns(typeName)
                .addCode("    return Observable.create(new $T<$T>() {\n", ClassName.get("io.reactivex", "ObservableOnSubscribe"), ClassName.get(Boolean.class))
                .addCode("        public void subscribe($T<$T> emitter) throws Exception {\n", ClassName.get("io.reactivex", "ObservableEmitter"), TypeName.BOOLEAN.box())
                .addCode(String.format(
                        "            try {\n" +
                                "                emitter.onNext(mDiskCache.clear());\n" +
                                "                emitter.onComplete();\n" +
                                "            } catch (Exception e) {\n" +
                                "                    emitter.onError(e);\n" +
                                "            }\n" +
                                "        }\n" +
                                "    });\n"))
                .build();
        MethodSpec getRealKeyMethodSpec = MethodSpec.methodBuilder("getRealKey")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addParameter(String.class, "key")
                .addParameter(TypeName.BOOLEAN, "global")
                .addStatement("return global ? key : $T.getUserToken() + key", ClassName.get("com.thomas.rxpersistence", "RxPersistence"))
                .build();

        MethodSpec getRealFileNameMethodSpec = MethodSpec.methodBuilder("getRealFileName")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addParameter(String.class, "key")
                .addParameter(TypeName.BOOLEAN, "global")
                .addStatement("return global ? key : $T.getGroupToken() + key", ClassName.get("com.thomas.rxpersistence", "RxPersistence"))
                .build();
        MethodSpec resetMethodSpec = MethodSpec.methodBuilder("reset")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addStatement("sInstance = null")
                .build();
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("mDiskCache = CacheDiskUtils.getInstance(" + String.format("getRealFileName(\"%s\",%b)", prefix+typeElement.getSimpleName(), global) + "," + diskMaxSize + "," + diskMaxCout + ")")
                .build();
        FieldSpec fieldSpec;
        if(prefix.length()>0){
            fieldSpec = FieldSpec.builder(ClassName.get(getPackageName(typeElement)+"."+prefix,typeElement.getSimpleName() + "DiskCache"), "sInstance", Modifier.PRIVATE, Modifier.VOLATILE, Modifier.STATIC)
                    .build();
        }else {
            fieldSpec = FieldSpec.builder(targetClassName, "sInstance", Modifier.PRIVATE, Modifier.VOLATILE, Modifier.STATIC)
                    .build();
        }

        TypeSpec.Builder builder = TypeSpec.classBuilder(typeElement.getSimpleName() + "DiskCache");
        builder.superclass(TypeName.get(typeElement.asType()));
        builder.addModifiers( Modifier.FINAL);
        builder.addMethods(methodSpecs);
        builder.addMethod(getMethodSpec2);
        builder.addMethod(constructor);
        builder.addMethod(clearMethodSpec);
        builder.addMethod(getCountMethodSpec);
        builder.addMethod(getSizeMethodSpec);
        builder.addMethod(resetMethodSpec);
        builder.addMethod(setMethodRx);
        builder.addMethod(getRealKeyMethodSpec);
        builder.addMethod(getRealFileNameMethodSpec);
        builder.addField(ClassName.get("com.thomas.rxpersistence", "CacheDiskUtils"), "mDiskCache", Modifier.PRIVATE, Modifier.FINAL);
        builder.addField(fieldSpec);
        if(prefix.length()>0){
            builder.addModifiers(Modifier.PRIVATE,Modifier.STATIC);
        }else {
            builder.addModifiers(Modifier.PUBLIC);
        }
        return  builder.build();
    }

    private TypeSpec processMemoryCache(String prefix, TypeElement typeElement, int memoryMaxCout, boolean global) {
        // 获取该类的全部成员，包括
        List<? extends Element> members = elementUtils.getAllMembers(typeElement);
        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (Element element : members) {
            // 忽略除了成员方法外的元素
            if (!(element instanceof ExecutableElement)) {
                continue;
            }
            //忽略final、static 方法
            if (element.getModifiers().contains(Modifier.FINAL) || element.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }

            ExecutableElement executableElement = (ExecutableElement) element;
            //父类方法名称
            String name = element.getSimpleName().toString();
            // 忽略基类的一个get方法
            if (name.equals("getClass")) {
                continue;
            }

            // 忽略不是get、set、is 开头的方法
            boolean getter = false;
            if (name.startsWith("get") || name.startsWith("is")) {
                getter = true;
            } else if (name.startsWith("set")) {
                getter = false;
            } else {
                continue;
            }
            // 从方法名称提取成员变量的名称
            String fieldName = name.replaceFirst("get|is|set", "");
            fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);

            // 根据名称提取成员变量的元素
            Element fieldElement = getElement(members, fieldName);
            if (fieldElement == null) {
                continue;
            }
            // 检查是否有注解
            CacheField annotation = fieldElement.getAnnotation(CacheField.class);
            // 检查是否需要保存
            if (annotation != null && !annotation.save()) {
                continue;
            }
            boolean globalField = true;
            if (annotation != null && !annotation.global()) {
                globalField = false;
            }
            int saveTime = -1;
            if (annotation != null) {
                saveTime = annotation.saveTime();
            }
            TypeName type = TypeName.get(fieldElement.asType());
            if (name.startsWith("set")) {
                //setter

                ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get("io.reactivex", "Observable"), type.box());
                String parameterName = executableElement.getParameters().get(0).getSimpleName().toString();

                MethodSpec setMethod = MethodSpec.overriding(executableElement)
                        .addStatement(String.format("mMemoryCache.put(getRealKey(\"%s\",%b), %s,%d)", fieldName, globalField, parameterName, saveTime))
                        .build();
                methodSpecs.add(setMethod);
            } else {
                //getter


                MethodSpec setMethod = MethodSpec.overriding(executableElement)
                        .addStatement(String.format("return mMemoryCache.get(getRealKey(\"%s\",%b), super.%s())", fieldName, globalField, name))
                        .build();
                methodSpecs.add(setMethod);

            }

        }
        TypeName targetClassName;
        if(prefix.length()>0) {
            targetClassName = ClassName.get(getPackageName(typeElement) + "." + prefix, typeElement.getSimpleName() + "MemoryCache");
        }else {
            targetClassName = ClassName.get(getPackageName(typeElement), typeElement.getSimpleName() + "MemoryCache");

        }
        MethodSpec getMethodSpec2 = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(targetClassName)
                .addCode("if (sInstance == null){\n" +
                        "   synchronized ($T.class){\n" +
                        "      if (sInstance == null){\n" +
                        "          sInstance = new $T();\n" +
                        "      }\n" +
                        "   }\n" +
                        "}\n" +
                        "return sInstance;\n", targetClassName, targetClassName)
                .build();

        MethodSpec clearMethodSpec = MethodSpec.methodBuilder("clear")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addStatement("mMemoryCache.clear()")
                .build();

        MethodSpec getCountMethodSpec = MethodSpec.methodBuilder("getCacheCount")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT)
                .addStatement("return mMemoryCache.getCacheCount()")
                .build();
        ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get("io.reactivex", "Observable"), ClassName.get(Boolean.class));

        MethodSpec setMethodRx = MethodSpec.methodBuilder("clearRx")
                .addModifiers(Modifier.PUBLIC)
                .returns(typeName)
                .addCode("    return Observable.create(new $T<$T>() {\n", ClassName.get("io.reactivex", "ObservableOnSubscribe"), ClassName.get(Boolean.class))
                .addCode("        public void subscribe($T<$T> emitter) throws Exception {\n", ClassName.get("io.reactivex", "ObservableEmitter"), TypeName.BOOLEAN.box())
                .addCode(String.format(
                        "            try {\n" +
                                "                emitter.onNext(mEdit.clear().commit());\n" +
                                "                emitter.onComplete();\n" +
                                "            } catch (Exception e) {\n" +
                                "                    emitter.onError(e);\n" +
                                "            }\n" +
                                "        }\n" +
                                "    });\n"))
                .build();
        MethodSpec getRealKeyMethodSpec = MethodSpec.methodBuilder("getRealKey")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addParameter(String.class, "key")
                .addParameter(TypeName.BOOLEAN, "global")
                .addStatement("return global ? key : $T.getUserToken() + key", ClassName.get("com.thomas.rxpersistence", "RxPersistence"))
                .build();

        MethodSpec getRealFileNameMethodSpec = MethodSpec.methodBuilder("getRealFileName")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addParameter(String.class, "key")
                .addParameter(TypeName.BOOLEAN, "global")
                .addStatement("return global ? key : $T.getGroupToken() + key", ClassName.get("com.thomas.rxpersistence", "RxPersistence"))
                .build();

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("mMemoryCache = CacheMemoryUtils.getInstance(" + String.format("getRealFileName(\"%s\",%b)", prefix+typeElement.getSimpleName(), global) + "," + memoryMaxCout + ")")
                .build();
        MethodSpec resetMethodSpec = MethodSpec.methodBuilder("reset")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addStatement("sInstance = null")
                .build();
        FieldSpec fieldSpec ;
        if(prefix.length()>0){
            fieldSpec = FieldSpec.builder(ClassName.get(getPackageName(typeElement)+"."+prefix,typeElement.getSimpleName() + "MemoryCache"), "sInstance", Modifier.PRIVATE, Modifier.VOLATILE, Modifier.STATIC)
                    .build();
        }else {
            fieldSpec = FieldSpec.builder(targetClassName, "sInstance", Modifier.PRIVATE, Modifier.VOLATILE, Modifier.STATIC)
                    .build();
        }
        TypeSpec.Builder builder = TypeSpec.classBuilder(typeElement.getSimpleName() + "MemoryCache");
        builder.superclass(TypeName.get(typeElement.asType()));
        builder.addModifiers(Modifier.FINAL);
        builder.addMethods(methodSpecs);
        builder.addMethod(getMethodSpec2);
        builder.addMethod(constructor);
        builder.addMethod(clearMethodSpec);
        builder.addMethod(resetMethodSpec);
        builder.addMethod(getCountMethodSpec);
        builder.addMethod(getRealKeyMethodSpec);
        builder.addMethod(getRealFileNameMethodSpec);
        builder.addField(ClassName.get("com.thomas.rxpersistence", "CacheMemoryUtils"), "mMemoryCache", Modifier.PRIVATE, Modifier.FINAL);
        builder.addField(fieldSpec);
        if(prefix.length()>0){
            builder.addModifiers(Modifier.PRIVATE,Modifier.STATIC);
        }else {
            builder.addModifiers(Modifier.PUBLIC);
        }

        return builder.build();
    }

    private void processSP(Set<? extends Element> elements) {
        for (Element element : elements) {
            // 判断是否Class
            if (!(element instanceof TypeElement)) {
                continue;
            }
            TypeElement typeElement = (TypeElement) element;
            SPEntity typeElementAnnotation = typeElement.getAnnotation(SPEntity.class);

            boolean globalEntity = true;
            if (typeElementAnnotation != null && !typeElementAnnotation.global()) {
                globalEntity = false;
            }
            // 获取该类的全部成员，包括
            List<? extends Element> members = elementUtils.getAllMembers(typeElement);
            List<MethodSpec> methodSpecs = new ArrayList<>();
            for (Element item : members) {
                // 忽略除了成员方法外的元素
                if (!(item instanceof ExecutableElement)) {
                    continue;
                }
                //忽略final、static 方法
                if (item.getModifiers().contains(Modifier.FINAL) || item.getModifiers().contains(Modifier.STATIC)) {
                    continue;
                }

                ExecutableElement executableElement = (ExecutableElement) item;
                //父类方法名称
                String name = item.getSimpleName().toString();
                // 忽略基类的一个get方法
                if (name.equals("getClass")) {
                    continue;
                }

                // 忽略不是get、set、is 开头的方法
                boolean getter = false;
                if (name.startsWith("get") || name.startsWith("is")) {
                    getter = true;
                } else if (name.startsWith("set")) {
                    getter = false;
                } else {
                    continue;
                }
                // 从方法名称提取成员变量的名称
                String fieldName = name.replaceFirst("get|is|set", "");
                fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);

                // 根据名称提取成员变量的元素
                Element fieldElement = getElement(members, fieldName);
                if (fieldElement == null) {
                    continue;
                }
                // 检查是否有注解
                SPField annotation = fieldElement.getAnnotation(SPField.class);
                // 检查是否需要保存
                if (annotation != null && !annotation.save()) {
                    continue;
                }

                // editor.xxx 方法名
                String modName;
                boolean isDouble = false;
                boolean isObject = false;
                TypeName type = TypeName.get(fieldElement.asType());
                if (type.equals(TypeName.BOOLEAN)) {
                    if (getter) {
                        modName = "getBoolean";
                    } else {
                        modName = "putBoolean";
                    }
                } else if (type.equals(TypeName.INT)) {
                    if (getter) {
                        modName = "getInt";
                    } else {
                        modName = "putInt";
                    }
                } else if (type.equals(TypeName.DOUBLE)) {
                    if (getter) {
                        modName = "getFloat";
                    } else {
                        modName = "putFloat";
                    }
                    isDouble = true;
                } else if (type.equals(TypeName.FLOAT)) {
                    if (getter) {
                        modName = "getFloat";
                    } else {
                        modName = "putFloat";
                    }
                } else if (type.equals(TypeName.LONG)) {
                    if (getter) {
                        modName = "getLong";
                    } else {
                        modName = "putLong";
                    }
                } else if (type.equals(TypeName.get(String.class))) {
                    if (getter) {
                        modName = "getString";
                    } else {
                        modName = "putString";
                    }
                } else {
                    if (getter) {
                        modName = "getString";
                    } else {
                        modName = "putString";
                    }

                    isObject = true;
                }
                boolean globalField = true;
                if (annotation != null && !annotation.global()) {
                    globalField = false;
                }


                if (name.startsWith("set")) {
                    //setter

                    ParameterizedTypeName rxfield = ParameterizedTypeName.get(ClassName.get("com.thomas.rxpersistence", "RxField"), type.box());
                    ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get("io.reactivex", "Observable"), rxfield);

                    String parameterName = executableElement.getParameters().get(0).getSimpleName().toString();
                    if (isObject) {
                        MethodSpec setMethod = MethodSpec.overriding(executableElement)
                                .addStatement(String.format("mEdit.putString(getRealKey(\"%s\",%b), RxPersistence.getParser().serialize(%s)).apply()", fieldName, globalField, parameterName))
                                .build();

                        MethodSpec setMethodRx = MethodSpec.methodBuilder(name + "Rx")
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(type, parameterName, Modifier.FINAL)
                                .returns(typeName)
                                .addCode("    return Observable.create(new $T<$T>() {\n", ClassName.get("io.reactivex", "ObservableOnSubscribe"), rxfield)
                                .addCode("        public void subscribe($T<$T> emitter) throws Exception {\n", ClassName.get("io.reactivex", "ObservableEmitter"), rxfield)
                                .addStatement(String.format("            boolean b = mEdit.putString(getRealKey(\"%s\",%b), RxPersistence.getParser().serialize(%s)).commit()", fieldName, globalField, parameterName))
                                .addCode(String.format(
                                        "            if (b) {\n"
                                                + "                emitter.onNext(new RxField(%s));\n"
                                                + "                emitter.onComplete();\n"
                                                + "            } " +
                                                "else emitter.onError(new RuntimeException(\"%s.%s(final %s %s) failed\"));\n", parameterName, element.getSimpleName() + "SP", name + "Rx", type.getClass().getSimpleName(), parameterName))
                                .addCode("        }\n" +
                                        "    });\n")
                                .build();
                        methodSpecs.add(setMethodRx);
                        methodSpecs.add(setMethod);
                        continue;
                    }
                    if (isDouble) {

                        MethodSpec setMethodRx = MethodSpec.methodBuilder(name + "Rx")
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(type, parameterName, Modifier.FINAL)
                                .returns(typeName)
                                .addCode("    return Observable.create(new $T<$T>() {\n", ClassName.get("io.reactivex", "ObservableOnSubscribe"), rxfield)
                                .addCode("        public void subscribe($T<$T> emitter) throws Exception {\n", ClassName.get("io.reactivex", "ObservableEmitter"), rxfield)
                                .addStatement(String.format("            boolean b = mEdit.%s(getRealKey(\"%s\",%b), (float)%s).commit()", modName, fieldName, globalField, parameterName))
                                .addCode(String.format(
                                        "            if (b) {\n"
                                                + "                emitter.onNext(new RxField(%s));\n"
                                                + "                emitter.onComplete();\n"
                                                + "            } " +
                                                "else emitter.onError(new RuntimeException(\"%s.%s(final %s %s) failed\"));\n", parameterName, element.getSimpleName() + "SP", name + "Rx", type.getClass().getSimpleName(), parameterName))
                                .addCode("        }\n" +
                                        "    });\n")
                                .build();
                        methodSpecs.add(setMethodRx);
                    } else {

                        MethodSpec setMethodRx = MethodSpec.methodBuilder(name + "Rx")
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(type, parameterName, Modifier.FINAL)
                                .returns(typeName)
                                .addCode("    return Observable.create(new $T<$T>() {\n", ClassName.get("io.reactivex", "ObservableOnSubscribe"), rxfield)
                                .addCode("        public void subscribe($T<$T> emitter) throws Exception {\n", ClassName.get("io.reactivex", "ObservableEmitter"), rxfield)
                                .addStatement(String.format("            boolean b =  mEdit.%s(getRealKey(\"%s\",%b), %s).commit()", modName, fieldName, globalField, parameterName))
                                .addCode(String.format(
                                        "            if (b) {\n"
                                                + "                emitter.onNext(new RxField(%s));\n"
                                                + "                emitter.onComplete();\n"
                                                + "            } " +
                                                "else emitter.onError(new RuntimeException(\"%s.%s(final %s %s) failed\"));\n", parameterName, element.getSimpleName() + "SP", name + "Rx", type.getClass().getSimpleName(), parameterName))
                                .addCode("        }\n" +
                                        "    });\n")
                                .build();
                        methodSpecs.add(setMethodRx);
                    }
                    MethodSpec setMethod;
                    if (annotation != null && annotation.commit()) {
                        if (isDouble) {
                            setMethod = MethodSpec.overriding(executableElement)
                                    .addStatement(String.format("mEdit.%s(getRealKey(\"%s\",%b), (float)%s).commit()", modName, fieldName, globalField, parameterName)).build();
                        } else {
                            setMethod = MethodSpec.overriding(executableElement)
                                    .addStatement(String.format("mEdit.%s(getRealKey(\"%s\",%b), %s).commit()", modName, fieldName, globalField, parameterName)).build();
                        }
                    } else {
                        if (isDouble) {
                            setMethod = MethodSpec.overriding(executableElement)
                                    .addStatement(String.format("mEdit.%s(getRealKey(\"%s\",%b), (float)%s).apply()", modName, fieldName, globalField, parameterName)).build();
                        } else {
                            setMethod = MethodSpec.overriding(executableElement)
                                    .addStatement(String.format("mEdit.%s(getRealKey(\"%s\",%b), %s).apply()", modName, fieldName, globalField, parameterName)).build();
                        }
                    }
                    methodSpecs.add(setMethod);
                } else {
                    //getter
                    ParameterizedTypeName rxfield = ParameterizedTypeName.get(ClassName.get("com.thomas.rxpersistence", "RxField"), type.box());
                    ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get("io.reactivex", "Observable"), rxfield);
                    MethodSpec setMethodRx = MethodSpec.methodBuilder(name + "Rx")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(typeName)
                            .addCode("    return Observable.create(new $T<$T>() {\n", ClassName.get("io.reactivex", "ObservableOnSubscribe"), rxfield)
                            .addCode("        public void subscribe($T<$T> emitter) throws Exception {\n", ClassName.get("io.reactivex", "ObservableEmitter"), rxfield)
                            .addCode(String.format(
                                    "            try {\n" +
                                            "                emitter.onNext(new RxField(%s()));\n" +
                                            "                emitter.onComplete();\n" +
                                            "            } catch (Exception e) {\n" +
                                            "                    emitter.onError(e);\n" +
                                            "            }\n" +
                                            "        }\n" +
                                            "    });\n", name))
                            .build();
                    methodSpecs.add(setMethodRx);

                    if (isObject) {
                        TypeName className = ClassName.get(fieldElement.asType());
                     MethodSpec setMethod = MethodSpec.overriding(executableElement)
                                .addStatement(String.format("String text = mPreferences.getString(getRealKey(\"%s\",%b), null)", fieldName, globalField))
                                .addStatement("Object object = null")
                                .addCode(String.format("if (text != null){\n" +
                                        "   object = RxPersistence.getParser().deserialize" +
                                        "(new %s<%s>() {}.getType(),text);\n" +
                                        "}\n" +
                                        "if (object != null){\n" +
                                        "   return (%s) object;\n" +
                                        "}\n", ClassName.get("com.google.gson.reflect", "TypeToken"), type.box(), className))
                                .addStatement(String.format("return super.%s()", executableElement.getSimpleName()))
                                .build();

                     /*   MethodSpec   setMethod = MethodSpec.overriding(executableElement)
                                .addStatement(String.format("return ((%s)RxPersistence.getParser().deserialize(\n" +
                                        "new %s<%s>() {}.getType(),mDiskCache.%s(getRealKey(\"%s\",%b),\n" +
                                        "RxPersistence.getParser().serialize(super.%s()))))", type.box(), ClassName.get("com.google.gson.reflect", "TypeToken"), type.box(), modName, fieldName, globalField, name))
                                .addStatement(String.format("return super.%s()", executableElement.getSimpleName()))
                                .build();*/
                        methodSpecs.add(setMethod);


                        continue;
                    }


                    if (isDouble) {
                        MethodSpec setMethod = MethodSpec.overriding(executableElement)
                                .addStatement(String.format("return mPreferences.%s(getRealKey(\"%s\",%b), (float)super.%s())", modName, fieldName, globalField, name))
                                .build();

                        methodSpecs.add(setMethod);
                    } else {
                        MethodSpec setMethod = MethodSpec.overriding(executableElement)
                                .addStatement(String.format("return mPreferences.%s(getRealKey(\"%s\",%b), super.%s())", modName, fieldName, globalField, name))
                                .build();

                        methodSpecs.add(setMethod);
                    }

                }
            }

            TypeName targetClassName = ClassName.get(getPackageName(typeElement), element.getSimpleName() + "SP");
            MethodSpec getMethodSpec2 = MethodSpec.methodBuilder("get")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(targetClassName)
                    .addCode("if (sInstance == null){\n" +
                            "   synchronized ($T.class){\n" +
                            "      if (sInstance == null){\n" +
                            "          sInstance = new $T();\n" +
                            "      }\n" +
                            "   }\n" +
                            "}\n" +
                            "return sInstance;\n", targetClassName, targetClassName)
                    .build();

            MethodSpec clearMethodSpec = MethodSpec.methodBuilder("clear")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.VOID)
                    .addStatement("mEdit.clear().commit()")
                    .build();
            ParameterizedTypeName typeName = ParameterizedTypeName.get(ClassName.get("io.reactivex", "Observable"), ClassName.get(Boolean.class));

            MethodSpec setMethodRx = MethodSpec.methodBuilder("clearRx")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(typeName)
                    .addCode("    return Observable.create(new $T<$T>() {\n", ClassName.get("io.reactivex", "ObservableOnSubscribe"), ClassName.get(Boolean.class))
                    .addCode("        public void subscribe($T<$T> emitter) throws Exception {\n", ClassName.get("io.reactivex", "ObservableEmitter"), TypeName.BOOLEAN.box())
                    .addCode(String.format(
                            "            try {\n" +
                                    "                emitter.onNext(mEdit.clear().commit());\n" +
                                    "                emitter.onComplete();\n" +
                                    "            } catch (Exception e) {\n" +
                                    "                    emitter.onError(e);\n" +
                                    "            }\n" +
                                    "        }\n" +
                                    "    });\n"))
                    .build();
            MethodSpec getRealKeyMethodSpec = MethodSpec.methodBuilder("getRealKey")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addParameter(String.class, "key")
                    .addParameter(TypeName.BOOLEAN, "global")
                    .addStatement("return global ? key : RxPersistence.getUserToken() + key")
                    .build();

            MethodSpec getRealFileNameMethodSpec = MethodSpec.methodBuilder("getRealFileName")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addParameter(String.class, "key")
                    .addParameter(TypeName.BOOLEAN, "global")
                    .addStatement("return global ? key : RxPersistence.getGroupToken() + key")
                    .build();

            MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .addStatement("mPreferences = $T.getContext().getSharedPreferences(" + String.format("getRealFileName(\"%s\",%b)", element.getSimpleName(), globalEntity) + ", 0)", ClassName.get("com.thomas.rxpersistence", "RxPersistence"))
                    .addStatement("mEdit = mPreferences.edit()");


            FieldSpec fieldSpec = FieldSpec.builder(targetClassName, "sInstance", Modifier.PRIVATE, Modifier.VOLATILE, Modifier.STATIC)
                    //  .initializer("new $T()", targetClassName)
                    .build();

            MethodSpec resetMethodSpec = MethodSpec.methodBuilder("reset")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.VOID)
                    .addStatement("sInstance = null")
                    .build();
            TypeSpec typeSpec = TypeSpec.classBuilder(element.getSimpleName() + "SP")
                    .superclass(TypeName.get(typeElement.asType()))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethods(methodSpecs)
                    .addMethod(getMethodSpec2)
                    .addMethod(constructor.build())
                    .addMethod(clearMethodSpec)
                    .addMethod(setMethodRx)
                    .addMethod(getRealKeyMethodSpec)
                    .addMethod(getRealFileNameMethodSpec)
                    .addMethod(resetMethodSpec)
                    .addField(ClassName.get("android.content", "SharedPreferences", "Editor"), "mEdit", Modifier.PRIVATE, Modifier.FINAL)
                    .addField(ClassName.get("android.content", "SharedPreferences"), "mPreferences", Modifier.PRIVATE, Modifier.FINAL)
                    .addField(fieldSpec)
                    .build();
            JavaFile javaFile = JavaFile.builder(getPackageName(typeElement), typeSpec).build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    private Element getElement(List<? extends Element> members, String fieldName) {
        for (Element item : members) {
            if (item.getSimpleName().toString().equals(fieldName)) {
                return item;
            }
        }
        return null;
    }


    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
    }

    /**
     * If the processor class is annotated with {@link
     * }, return the source version in the
     * annotation.  If the class is not so annotated, {@link
     * SourceVersion#RELEASE_6} is returned.
     *
     * @return the latest source version supported by this processor
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }
}