package com.core.framework.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述：json解析
 * <p>
 * <p>
 * 作者：Created by tgl on 2019/7/8.
 * <p>
 * 修改人：
 * 修改描述：
 * 修改日期
 */
public class JsonUtils {
    public static <T> List<T> parseList(String jsonString, Type type) {
//        Type type = new TypeToken<List<T>>() {}.getType();
        List<T> list = new ArrayList<T>();
        try {
            Gson gson = new Gson();
            list = gson.fromJson(jsonString, type);
        } catch (Exception e) {
        }
        return list;
    }

    public static <T> T parseObject(String json, Type type) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T parseObject(String json, Class<T> typeCls) {
        try {
            Gson gson = new Gson();
            return (T) gson.fromJson(json, typeCls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toJsonString(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }
}
