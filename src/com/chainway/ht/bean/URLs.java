package com.chainway.ht.bean;

import java.io.Serializable;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import com.chainway.ht.utils.StringUtils;


public class URLs implements Serializable {

    public final static String HOST = "116.204.106.81:8032";// "192.168.100.174";
    public final static String HTTP = "http://";
    public final static String HTTPS = "https://";

    private final static String URL_SPLITTER = "/";
    private final static String URL_UNDERLINE = "_";

    private final static String URL_API_HOST = HTTP + HOST + URL_SPLITTER;
    public final static String UPDATE_VERSION = URL_API_HOST
            + "suitup/check_version.ashx";

    private final static String URL_HOST = "116.204.106.81:8032";// "192.168.100.174";

    public final static String URL_UP_FILE = URL_API_HOST + "Test/Upload.aspx";

    public final static String URL_GET_FILE_LIST = URL_API_HOST
            + "Test/filelist.txt";

    private int objId;
    private String objKey = "";
    private int objType;

    public int getObjId() {
        return objId;
    }

    public void setObjId(int objId) {
        this.objId = objId;
    }

    public String getObjKey() {
        return objKey;
    }

    public void setObjKey(String objKey) {
        this.objKey = objKey;
    }

    public int getObjType() {
        return objType;
    }

    public void setObjType(int objType) {
        this.objType = objType;
    }

    public final static URLs parseURL(String path) {
        if (StringUtils.isEmpty(path))
            return null;
        path = formatURL(path);
        URLs urls = null;
        String objId = "";
        try {
            URL url = new URL(path);
            // 站内链接
            if (url.getHost().contains(URL_HOST)) {
                urls = new URLs();

                // if (path.contains(URL_TYPE_NEWS)) {
                // objId = parseObjId(path, URL_TYPE_NEWS);
                // urls.setObjId(StringUtils.toInt(objId));
                // urls.setObjType(URL_OBJ_TYPE_NEWS);
                // }

            }
        } catch (Exception e) {
            e.printStackTrace();
            urls = null;
        }
        return urls;
    }

    private final static String parseObjId(String path, String url_type) {
        String objId = "";
        int p = 0;
        String str = "";
        String[] tmp = null;
        p = path.indexOf(url_type) + url_type.length();
        str = path.substring(p);
        if (str.contains(URL_SPLITTER)) {
            tmp = str.split(URL_SPLITTER);
            objId = tmp[0];
        } else {
            objId = str;
        }
        return objId;
    }

    private final static String parseObjKey(String path, String url_type) {
        path = URLDecoder.decode(path);
        String objKey = "";
        int p = 0;
        String str = "";
        String[] tmp = null;
        p = path.indexOf(url_type) + url_type.length();
        str = path.substring(p);
        if (str.contains("?")) {
            tmp = str.split("?");
            objKey = tmp[0];
        } else {
            objKey = str;
        }
        return objKey;
    }

    private final static String formatURL(String path) {
        if (path.startsWith("http://") || path.startsWith("https://"))
            return path;
        return "http://" + URLEncoder.encode(path);
    }
}