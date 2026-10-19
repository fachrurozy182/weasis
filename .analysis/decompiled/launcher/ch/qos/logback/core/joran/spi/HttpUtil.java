/*
 * Decompiled with CFR 0.152.
 */
package ch.qos.logback.core.joran.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpUtil {
    URL url;
    HttpURLConnection conn;
    RequestMethod requestMethod;
    Map<String, String> headerMap = new HashMap<String, String>(2);

    public HttpUtil(RequestMethod requestMethod, URL url) {
        this.requestMethod = requestMethod;
        this.url = url;
    }

    public HttpUtil(RequestMethod requestMethod, String urlStr) throws MalformedURLException {
        this(requestMethod, new URL(urlStr));
    }

    Map<String, String> getHeaderMap() {
        return this.headerMap;
    }

    public HttpURLConnection connectTextTxt() {
        return this.connectType("text/txt;charset=utf-8");
    }

    public HttpURLConnection connectTextPlain() {
        return this.connectType("text/plain; charset=utf-8");
    }

    public HttpURLConnection connectType(String acceptType) {
        try {
            HttpURLConnection conn = (HttpURLConnection)this.url.openConnection();
            conn.setRequestMethod(this.requestMethod.name());
            this.headerMap.forEach((h, v) -> conn.setRequestProperty((String)h, (String)v));
            conn.setRequestProperty("Accept", acceptType);
            if (this.requestMethod == RequestMethod.POST) {
                conn.setDoOutput(true);
            }
            conn.connect();
            return conn;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String readResponse(HttpURLConnection conn) {
        if (conn == null) {
            return null;
        }
        try {
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                return this.innerReadResponse(conn);
            }
            System.out.println("status=" + responseCode + " Failed response");
            return null;
        }
        catch (IOException e) {
            System.out.println("url=" + this.url.toString() + " failed to read status");
            e.printStackTrace();
            return null;
        }
    }

    private String innerReadResponse(HttpURLConnection conn) {
        String string;
        block9: {
            InputStream is = conn.getInputStream();
            try {
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuffer buffer = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    buffer.append(inputLine);
                }
                string = buffer.toString();
                if (is == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (is != null) {
                        try {
                            is.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            is.close();
        }
        return string;
    }

    public boolean post(HttpURLConnection conn, String str) {
        boolean bl;
        block10: {
            if (conn == null) {
                System.out.println("null HttpURLConnection object");
                return false;
            }
            if (this.requestMethod != RequestMethod.POST) {
                System.out.println("Incorrect request method " + this.requestMethod.name());
                return false;
            }
            OutputStream os = conn.getOutputStream();
            try {
                OutputStreamWriter wr = new OutputStreamWriter(os);
                wr.write(str);
                wr.flush();
                bl = true;
                if (os == null) break block10;
            }
            catch (Throwable throwable) {
                try {
                    if (os != null) {
                        try {
                            os.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            os.close();
        }
        return bl;
    }

    public static enum RequestMethod {
        GET,
        POST;

    }
}

