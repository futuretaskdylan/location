package com.sfmap.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Dylan
 */
public class SPUtils {

    /**
     * sp 文件名
     */
    public static String FILE_NAME_LOC="file_name_loc.config";
    public static String FILE_NAME_MAP="file_name_map.config";
    public static String FILE_NAME_SEARCH="file_name_search.config";
    public static String FILE_NAME_NAVI="file_name_navi.config";
    public static String FILE_NAME;

    public static void setSPFileName(String fileName) {
        FILE_NAME = fileName;
    }

    public static void put(Context context, String key, Object object){

        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (object instanceof String){

            editor.putString(key, (String) object);
        } else if (object instanceof Integer){

            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean){

            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float){

            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long){

            editor.putLong(key, (Long) object);
        } else{

            editor.putString(key, object.toString());
        }
        SharedPreferencesCompat.apply(editor);
    }

    public static Object get(Context context, String key, Object defaultObject){

        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);

        if (defaultObject instanceof String){

            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer){

            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean){

            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float){

            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long){

            return sp.getLong(key, (Long) defaultObject);
        }
        return null;
    }

    /**
     * 移除某个key值已经对应的值
     * @param context   上下文环境
     * @param key
     */
    public static void remove(Context context, String key){
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 清除所有数据
     * @param context   上下文环境
     */
    public static void clear(Context context){

        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 查询某个key是否已经存在
     * @return 如果存在返回true
     */
    public static boolean contains(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    /**
     * 返回所有的键值对
     * @param context 上下文
     * @return 所有的键值
     */
    public static Map<String, ?> getAll(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        return sp.getAll();
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     * @author zeng
     */
    private static class SharedPreferencesCompat{

        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private static Method findApplyMethod() {
            try{
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         */
        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
            editor.commit();
        }
    }

}
