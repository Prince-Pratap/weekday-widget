package com.example.weekdaywidget;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FontDownloader {
    private static final String TAG = "FontDownloader";
    private static final Map<String, String> FONT_URLS = new HashMap<>();
    
    static {
        // Google Fonts URLs (these are example URLs - in production you'd use proper Google Fonts API)
        FONT_URLS.put("roboto", "https://fonts.gstatic.com/s/roboto/v30/KFOmCnqEu92Fr1Mu4mxK.woff2");
        FONT_URLS.put("roboto_slab", "https://fonts.gstatic.com/s/robotoslab/v25/BngbUXZYTXPIvIBgJJSb6s3BzlRRfKOFbvjojISWaG5iddG-1A.woff2");
        FONT_URLS.put("open_sans", "https://fonts.gstatic.com/s/opensans/v34/memSYaGs126MiZpBA-UvWbX2vVnXBbObj2OVZyOOSr4dVJWUgsjZ0B4gaVc.woff2");
        FONT_URLS.put("lato", "https://fonts.gstatic.com/s/lato/v23/S6uyw4BMUTPHjx4wXiWtFCc.woff2");
        FONT_URLS.put("montserrat", "https://fonts.gstatic.com/s/montserrat/v25/JTUSjIg1_i6t8kCHKm459Wlhyw.woff2");
        FONT_URLS.put("poppins", "https://fonts.gstatic.com/s/poppins/v20/pxiEyp8kv8JHgFVrJJfecg.woff2");
        FONT_URLS.put("nunito", "https://fonts.gstatic.com/s/nunito/v25/XRXV3I6Li01BKofINeaE.woff2");
        FONT_URLS.put("playfair_display", "https://fonts.gstatic.com/s/playfairdisplay/v30/nuFvD-vYSZviVYUb_rj3ij__anPXJzDwcbmjWBN2PKdFvXDXbtXK-F2qO0isEw.woff2");
        FONT_URLS.put("merriweather", "https://fonts.gstatic.com/s/merriweather/v30/u-440qyriQwlOrhSvowK_l5-fCZMdeX3rsHo.woff2");
        FONT_URLS.put("oswald", "https://fonts.gstatic.com/s/oswald/v49/TK3_WkUHHAIjg75cFRf3bXL8LICs1_FvsUZiZQ.woff2");
        FONT_URLS.put("raleway", "https://fonts.gstatic.com/s/raleway/v28/1Ptug8zYS_SKggPNyC0IT4ttDfA.woff2");
    }

    public interface FontDownloadListener {
        void onFontDownloaded(String fontName, boolean success);
        void onProgress(String fontName, int progress);
    }

    public static void downloadFont(Context context, FontStyle fontStyle, FontDownloadListener listener) {
        if (fontStyle.isPreInstalled()) {
            listener.onFontDownloaded(fontStyle.getName(), true);
            return;
        }

        String fontUrl = FONT_URLS.get(fontStyle.getFileName());
        if (fontUrl == null) {
            Log.e(TAG, "No URL found for font: " + fontStyle.getName());
            listener.onFontDownloaded(fontStyle.getName(), false);
            return;
        }

        new DownloadFontTask(context, fontStyle, listener).execute(fontUrl);
    }

    public static boolean isFontDownloaded(Context context, FontStyle fontStyle) {
        if (fontStyle.isPreInstalled()) {
            return true;
        }
        
        File fontFile = new File(context.getFilesDir(), "fonts/" + fontStyle.getFileName() + ".ttf");
        return fontFile.exists();
    }

    private static class DownloadFontTask extends AsyncTask<String, Integer, Boolean> {
        private final Context context;
        private final FontStyle fontStyle;
        private final FontDownloadListener listener;

        DownloadFontTask(Context context, FontStyle fontStyle, FontDownloadListener listener) {
            this.context = context;
            this.fontStyle = fontStyle;
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                String fontUrl = urls[0];
                URL url = new URL(fontUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return false;
                }

                int fileLength = connection.getContentLength();
                
                // Create fonts directory
                File fontsDir = new File(context.getFilesDir(), "fonts");
                if (!fontsDir.exists()) {
                    fontsDir.mkdirs();
                }

                // Create font file
                File fontFile = new File(fontsDir, fontStyle.getFileName() + ".ttf");
                
                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(fontFile);

                byte[] data = new byte[4096];
                long total = 0;
                int count;
                
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        output.close();
                        fontFile.delete();
                        return false;
                    }
                    
                    total += count;
                    if (fileLength > 0) {
                        publishProgress((int) (total * 100 / fileLength));
                    }
                    
                    output.write(data, 0, count);
                }

                output.close();
                input.close();
                connection.disconnect();
                
                return true;
                
            } catch (IOException e) {
                Log.e(TAG, "Error downloading font: " + fontStyle.getName(), e);
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            listener.onProgress(fontStyle.getName(), progress[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            listener.onFontDownloaded(fontStyle.getName(), success);
        }
    }
}
