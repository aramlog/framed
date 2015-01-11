package com.frames.managers;

import android.os.Environment;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadManager {

    private static final int TIMEOUT_CONNECTION = 500;
    private static final int TIMEOUT_SOCKET = 500;

    private static DownloadManager instance;

    private DownloadManager() {
    }

    public static DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
        }

        return instance;
    }

    public boolean downloadFile(String fileUrl) {
        boolean result = false;
        try {
            File root = Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath());
            if (dir.exists() == false) {
                dir.mkdirs();
            }
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/"));
            URL url = new URL(fileUrl);
            File file = new File(dir, fileName);

            URLConnection uconn = url.openConnection();
            uconn.setReadTimeout(TIMEOUT_CONNECTION);
            uconn.setConnectTimeout(TIMEOUT_SOCKET);

            InputStream is = uconn.getInputStream();
            BufferedInputStream bufferIS = new BufferedInputStream(is);

            ByteArrayBuffer baf = new ByteArrayBuffer(5000);
            int current = 0;
            while ((current = bufferIS.read()) != -1) {
                baf.append((byte) current);
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baf.toByteArray());
            fos.flush();
            fos.close();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
