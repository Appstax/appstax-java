package com.appstax;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

abstract class AppstaxPaths {

    public static String collection(String collection) {
        return "objects/" + collection;
    }

    public static String filter(String collection, String filter) {
        try {
            return AppstaxPaths.collection(collection) + "?filter=" + URLEncoder.encode(filter, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AppstaxException(e.getMessage(), e);
        }
    }

    public static String object(String collection, String id) {
        return AppstaxPaths.collection(collection) + "/" + id;
    }

}
