package com.appstax;

abstract class AppstaxPaths {

    public static String collection(String collection) {
        return "objects/" + collection;
    }

    public static String object(String collection, String id) {
        return AppstaxPaths.collection(collection) + "/" + id;
    }

}
