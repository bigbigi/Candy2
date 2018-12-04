package com.amway.wifianalyze.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.FileUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.lib.util.Utils;
import com.autofit.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by big on 2018/12/4.
 */

public class UpdateBiz {
    private static volatile UpdateBiz mInstance;

    public synchronized static UpdateBiz getInstance() {
        if (mInstance == null) {
            synchronized (UpdateBiz.class) {
                if (mInstance == null) {
                    mInstance = new UpdateBiz();
                }
            }
        }
        return mInstance;
    }

    private String mUpdateUrl;

    public void request(final Context context, final Callback<String> callback) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                String updateUrl = "";
                boolean must = false;
                String ret = HttpHelper.getInstance().get(String.format(Server.UPDATE, Server.HOST));
                if (!TextUtils.isEmpty(ret)) {
                    try {
                        JSONObject obj = new JSONObject(ret);
                        if (100 == obj.getInt("code")) {
                            JSONObject data = obj.getJSONObject("data");
                            int version = Utils.parseInt(data.optString("version"));
                            must = data.optInt("must") == 1;
                            if (Utils.getVersion(context) < version) {
                                updateUrl = data.optString("url");
                                mUpdateUrl = updateUrl;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (callback != null) {
                    callback.onCallBack(must, updateUrl);
                }
            }
        });

    }

    public void download(final ProgressBar progressBar) {
//        mUpdateUrl = "https://up.cp33.ott.cibntv.net/apk/cibnvst/merchant/J9G2VMCS8NTLBSRFFVNQ.apk";
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/amway/";
                String path = dir + "update.apk";
                FileUtils.mkdirs(dir);
                InputStream input = null;
                FileOutputStream output = null;
                try {
                    HttpURLConnection conn = httpConnection(mUpdateUrl);
                    conn.setReadTimeout(60000);
                    conn.connect();
                    byte[] buf = new byte[4096];
                    input = conn.getInputStream();
                    output = new FileOutputStream(path);
                    int length = conn.getContentLength();
                    if (length == -1)
                        length = 1;
                    final int range = length / 100;
                    int read = -1, progress = 0, increase = 0;
                    while ((read = input.read(buf)) != -1) {
                        output.write(buf, 0, read);
                        increase += read;
                        if (increase >= range || increase > length) {
                            progress += increase;
                            increase = 0;
                            progressBar.setProgress(progress * 100 / length);
                        }
                    }
                    progressBar.setProgress(100);
                    progressBar.getContext().startActivity(getInstallIntent(progressBar.getContext(), path));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    FileUtils.closeIO(input);
                    FileUtils.closeIO(output);
                }
            }
        });
    }

    @SuppressLint("InlinedApi")
    public static Intent getInstallIntent(Context context, String path) {
        Intent intent;
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME,
                    context.getPackageName());
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
        }
        intent.setDataAndType(Uri.fromFile(new File(path)), MimeTypeMap
                .getSingleton().getMimeTypeFromExtension("apk"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private static final TrustManager[] trustManager = new TrustManager[]{new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }};
    private static final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public static HttpURLConnection httpConnection(String uri) {
        try {
            java.net.URL url = new URL(uri.trim());
            String protocol = url.getProtocol();
            if (protocol.equalsIgnoreCase("https")) {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init((KeyManager[]) null, trustManager, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(hostnameVerifier);
                return https;
            } else {
                return (HttpURLConnection) url.openConnection();
            }
        } catch (Throwable var5) {
            return null;
        }
    }
}
