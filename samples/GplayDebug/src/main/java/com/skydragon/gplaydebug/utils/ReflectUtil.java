package com.skydragon.gplaydebug.utils;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * package : com.skydragon.gplaydebug.utils
 * <p/>
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016.6.8 14:01.s
 */
public class ReflectUtil {

    private static final String TAG = "ReflectUtil";

    private static Method getMethod(Class clazz, String methodName, final Class[] paramTypes) throws Exception {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ex) {
                if (clazz.getSuperclass() == null) {
                    return method;
                } else {
                    method = getMethod(clazz.getSuperclass(), methodName, paramTypes);
                }
            }
        }
        return method;
    }

    public static Object newInstance(String objectName, final Class[] paramTypes, Object[] params) {
        Object instances = null;
        try {
            Class<?> clazz = Class.forName(objectName);
            Constructor con = clazz.getConstructor(paramTypes);
            instances = con.newInstance(params);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return instances;
    }

    public static Field getField(Class clazz, String fieldName){
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return field;
    }

    public static Object getFieldValue(final Object obj, String fieldName){
        Field field = getField(obj.getClass(), fieldName);
        Object value = null;
        try {
            value = field.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static Field setField(final Object obj, String fieldName, Object arg){
        Field field = null;
        try {
            field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, arg);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return field;
    }

    public static Object invokeMethod(final Object obj, final String methodName, final Class[] paramTypes, final Object[] args) {
        try {
            Method method = getMethod(obj.getClass(), methodName, paramTypes);
            method.setAccessible(true);
            Object objret = method.invoke(obj, args);
            return objret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeMethod(final Object obj, final String methodName) {
        return invokeMethod(obj, methodName, null, null);
    }
}
