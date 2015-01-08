package com.frames.managers;

import android.os.Bundle;
import android.util.Log;

import com.frames.items.FrameItem;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiManager {

    private static final String TAG = ApiManager.class.getCanonicalName();

    DefaultHttpClient httpClient;
    HttpContext localContext;

    private static final ApiManager instance = new ApiManager();

    private static String BASE_URL = "http://saynomoreapp.com/api/";

    public static ApiManager getInstance() {
        return instance;
    }

    private ApiManager() {
        HttpParams myParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(myParams, 10000);
        HttpConnectionParams.setSoTimeout(myParams, 10000);
        httpClient = new DefaultHttpClient(myParams);
        localContext = new BasicHttpContext();
    }

    private synchronized byte[] sendPost(String url, Map<String, String> data) throws IOException {
        byte[] ret = null;

        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);

        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.15 (KHTML, like Gecko) Chrome/24.0.1295.0 Safari/537.15");
        httpPost.setHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        httpPost.setHeader("Accept-Charset", "utf-8");

        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> pair : data.entrySet()) {
            pairs.add(new BasicNameValuePair(pair.getKey(), pair.getValue()));
        }

        httpPost.setEntity(new UrlEncodedFormEntity(pairs, "utf-8"));
        HttpResponse response = httpClient.execute(httpPost, localContext);
        if (response != null)
            ret = EntityUtils.toByteArray(response.getEntity());
        return ret;
    }

    private synchronized byte[] sendGet(String url) throws IOException {
        byte[] ret = null;
        httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
        HttpGet httpGet = new HttpGet(url);

        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.15 (KHTML, like Gecko) Chrome/24.0.1295.0 Safari/537.15");
        httpGet.setHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        httpGet.setHeader("Accept-Charset", "utf-8");

        HttpResponse response = httpClient.execute(httpGet, localContext);
        if (response != null)
            ret = EntityUtils.toByteArray(response.getEntity());
        return ret;
    }

    protected <T> T getResponsePost(String urlStr, Map<String, String> data, TypeReference typeReference) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            byte[] result = sendPost(urlStr, data);
            String data_ = new String(result, "UTF-8");
            System.out.print(data_);
            if (data_.equals("[]")) {
                return null;
            }
            return mapper.readValue(result, typeReference);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Log.i(TAG, e.getMessage(), e);
        }
        return null;
    }

    protected <T> T getResponseGet(String urlStr, TypeReference typeReference) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            byte[] result = sendGet(urlStr);
            String data = new String(result, "UTF-8");
            System.out.print(data);
            if (data.equals("[]")) {
                return null;
            }
            return mapper.readValue(result, typeReference);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Log.i(TAG, e.getMessage(), e);
        }
        return null;
    }

    public List<FrameItem> getFrames(int categoryId) {
        String URL = BASE_URL + "?id="+categoryId;
        return getResponseGet(URL, new TypeReference<List<FrameItem>>() {});
    }
}

