package org.softeg.slartus.forpda;

import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.softeg.slartus.forpda.classes.AppHttpStatus;
import org.softeg.slartus.forpda.classes.Exceptions.NotReportException;
import org.softeg.slartus.forpda.classes.SerializableCookie;
import org.softeg.slartus.forpda.classes.common.FileUtils;
import org.softeg.slartus.forpda.classes.common.Translit;
import org.softeg.slartus.forpda.common.Log;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 16.09.11
 * Time: 19:10
 * To change this template use File | Settings | File Templates.
 */
public class HttpHelper {
    private static final String TAG = "HttpHelper";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final int POST_TYPE = 1;
    private static final int GET_TYPE = 2;
    private static final int DOWNLOAD_TYPE = 3;
    public static final String GZIP = "gzip";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";

    public static String HTTP_CONTENT_CHARSET = "windows-1251";
    public static String USER_AGENT = "Android";
    public static final String MIME_FORM_ENCODED = "application/x-www-form-urlencoded";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String HTTP_RESPONSE = "HTTP_RESPONSE";
    public static final String HTTP_RESPONSE_ERROR = "HTTP_RESPONSE_ERROR";

    // Establish client once, as static field with static setup block.
    // (This is a best practice in HttpClient docs - but will leave reference until *process* stopped on Android.)
    private final DefaultHttpClient client;


    public void writeExternalCookies() throws Exception {
        String cookiesFile = PreferencesActivity.getCookieFilePath(MyApp.getContext());
        if (!FileUtils.mkDirs(cookiesFile))
            throw new Exception("Не могу создать директорию '" + cookiesFile + "' для cookies");

        new File(cookiesFile).createNewFile();
        FileOutputStream fw = new FileOutputStream(cookiesFile, false);

        ObjectOutput out = new ObjectOutputStream(fw);
        final List<Cookie> cookies = client.getCookieStore().getCookies();


        for (Cookie cookie : cookies) {
            new SerializableCookie(cookie).writeExternal(out);
        }
        out.close();
        fw.close();
    }

    private RuntimeException mLeakedException = new IllegalStateException(
            "AndroidHttpClient created and never closed");

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mLeakedException != null) {
            Log.e(null, mLeakedException);
            mLeakedException = null;
        }
    }

    /**
     * Release resources associated with this client.  You must call this,
     * or significant resources (sockets and memory) may be leaked.
     */
    public void close() {
        if (mLeakedException != null) {
            getConnectionManager().shutdown();

            mLeakedException = null;
        }
    }

    public ClientConnectionManager getConnectionManager() {
        return client.getConnectionManager();
    }

    public void clearCookies() {
        client.getCookieStore().clear();
    }


    public static void readExternalCookies(CookieStore cookieStore) throws IOException {
        try {
            FileInputStream fw = new FileInputStream(PreferencesActivity.getCookieFilePath(MyApp.getContext()));
            ObjectInput input = new ObjectInputStream(fw);

            while (true) {
                try {
                    SerializableCookie serializableCookie = new SerializableCookie();
                    serializableCookie.readExternal(input);
                    cookieStore.addCookie(serializableCookie);
                } catch (Exception ex) {
                    break;
                }

            }
            input.close();
            fw.close();
        } catch (FileNotFoundException ex) {
            Log.e("Файл с печеньками не найден");
        } catch (IOException ex) {
            throw ex;
        }
    }

    public List<Cookie> getCookies() {
        return client.getCookieStore().getCookies();
    }


    public HttpHelper() {
        responseHandler = new ResponseHandler<String>() {
            public String handleResponse(HttpResponse httpResponse) throws IOException {
                StatusLine status = httpResponse.getStatusLine();
                int statusCode = status.getStatusCode();
                if (statusCode < 200 || statusCode >= 300) {
                    if (statusCode >= 500 && statusCode < 600)
                        throw new NotReportException("Сайт не отвечает: " + statusCode + " " + AppHttpStatus.getReasonPhrase(statusCode, status.getReasonPhrase()));
                    else
                        throw new NotReportException(statusCode + " " + AppHttpStatus.getReasonPhrase(statusCode, status.getReasonPhrase()));
                }

                return EntityUtils.toString(httpResponse.getEntity(), HTTP_CONTENT_CHARSET);
            }
        };
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP_CONTENT_CHARSET);
        params.setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
        params.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);


        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        client = new DefaultHttpClient(cm, params);

        client.setCookieStore(new CookieStore() {
            private List<Cookie> m_Cookies = null;

            public void addCookie(Cookie cookie) {
                for (int i = 0; i < m_Cookies.size(); i++) {
                    if (m_Cookies.get(i).getName().equals(cookie.getName())) {
                        m_Cookies.remove(i);
                        break;
                    }
                }
                m_Cookies.add(cookie);
            }

            public List<Cookie> getCookies() {
                if (m_Cookies == null) {
                    m_Cookies = new ArrayList<Cookie>();
                    try {
                        readExternalCookies(this);
                    } catch (IOException ignoreEx) {
                        // e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
                return m_Cookies;
            }

            public boolean clearExpired(Date date) {
                for (int i = m_Cookies.size() - 1; i >= 0; i--) {
                    if (m_Cookies.get(i).getExpiryDate() != null && date != null && date.after(m_Cookies.get(i).getExpiryDate())) {
                        m_Cookies.remove(i);
                        break;
                    }
                }
                return true;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void clear() {
                if (m_Cookies != null)
                    m_Cookies.clear();
            }
        });
        // add gzip decompressor to handle gzipped content in responses
        // (default we *do* always send accept encoding gzip cat_name in request)
        client.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                HttpEntity entity = response.getEntity();
                Header contentEncodingHeader = entity.getContentEncoding();

                if (contentEncodingHeader != null) {
                    HeaderElement[] codecs = contentEncodingHeader.getElements();
                    for (int i = 0; i < codecs.length; i++) {
                        if (codecs[i].getName().equalsIgnoreCase(HttpHelper.GZIP)) {
                            response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                            return;
                        }
                    }
                }
            }
        });

        client.setRedirectHandler(new DefaultRedirectHandler() {
            private static final String REDIRECT_LOCATIONS = "http.protocol.redirect-locations";

            public URI getLocationURI(HttpResponse response, HttpContext context) {
                if (response == null) {
                    throw new IllegalArgumentException("HTTP response may not be null");
                }
                //get the location header to find out where to redirect to
                Header locationHeader = response.getFirstHeader("location");
                if (locationHeader == null) {

                }

                String location = locationHeader.getValue();

                Matcher matcher = Pattern.compile("(http://sdl\\d+.4pda.ru/\\d+/)(.*?)(\\?.*)").matcher(location);
                if (matcher.find()) {
                    location = matcher.group(1) + URLEncoder.encode(matcher.group(2)) + matcher.group(3);
                }else
                    location = location.replaceAll(" ", "%20");

                if (location.indexOf("/#") != -1)
                    location = location.substring(0, location.indexOf("#"));
                URI uri = null;
                try {
                    uri = new URI(location);
                } catch (URISyntaxException ex) {

                }


                HttpParams params = response.getParams();
                // rfc2616 demands the location value be a complete URI
                // Location       = "Location" ":" absoluteURI
                if (!uri.isAbsolute()) {
                    if (params.isParameterTrue(ClientPNames.REJECT_RELATIVE_REDIRECT)) {

                    }
                    // Adjust location URI
                    HttpHost target = (HttpHost) context.getAttribute(
                            ExecutionContext.HTTP_TARGET_HOST);
                    if (target == null) {
                        throw new IllegalStateException("Target host not available " +
                                "in the HTTP context");
                    }

                    HttpRequest request = (HttpRequest) context.getAttribute(
                            ExecutionContext.HTTP_REQUEST);

                    try {
                        URI requestURI = new URI(request.getRequestLine().getUri());
                        URI absoluteRequestURI = URIUtils.rewriteURI(requestURI, target, true);
                        uri = URIUtils.resolve(absoluteRequestURI, uri);
                    } catch (URISyntaxException ex) {

                    }
                }

                if (params.isParameterFalse(ClientPNames.ALLOW_CIRCULAR_REDIRECTS)) {

                    RedirectLocations redirectLocations = (RedirectLocations) context.getAttribute(
                            REDIRECT_LOCATIONS);

                    if (redirectLocations == null) {
                        redirectLocations = new RedirectLocations();
                        context.setAttribute(REDIRECT_LOCATIONS, redirectLocations);
                    }

                    URI redirectURI = null;
                    if (uri.getFragment() != null) {
                        try {
                            HttpHost target = new HttpHost(
                                    uri.getHost(),
                                    uri.getPort(),
                                    uri.getScheme());
                            redirectURI = URIUtils.rewriteURI(uri, target, true);
                        } catch (URISyntaxException ex) {

                        }
                    } else {
                        redirectURI = uri;
                    }

                    if (redirectLocations.contains(redirectURI)) {

                    } else {
                        redirectLocations.add(redirectURI);
                    }
                }
                m_RedirectUri = uri;
                return uri;
            }
        });
    }


    private final ResponseHandler<String> responseHandler;


    /**
     * Perform a simple HTTP GET operation.
     */
    public String performGet(final String url, final String acceptEncoding) throws IOException {
        return performRequest(null, url, null, null, null, null, HttpHelper.GET_TYPE, acceptEncoding, HTTP_CONTENT_CHARSET);
    }

    public String performGet(final String url) throws IOException {
        return performRequest(null, url, null, null, null, null, HttpHelper.GET_TYPE, HttpHelper.GZIP, HTTP_CONTENT_CHARSET);
    }

    /**
     * Perform an HTTP GET operation with user/pass and headers.
     */
    public String performGet(final String url, final String user, final String pass,
                             final Map<String, String> additionalHeaders) throws IOException {
        return performRequest(null, url, user, pass, additionalHeaders, null, HttpHelper.GET_TYPE, HttpHelper.GZIP, HTTP_CONTENT_CHARSET);
    }

    /**
     * Perform a simplified HTTP POST operation.
     */
    public String performPost(final String url, final Map<String, String> params) throws IOException {
        return performRequest(HttpHelper.MIME_FORM_ENCODED, url, null, null, null, params, HttpHelper.POST_TYPE, HttpHelper.GZIP, HTTP_CONTENT_CHARSET);
    }

    public String performPost(final String url, final Map<String, String> params, String encoding) throws IOException {
        return performRequest(HttpHelper.MIME_FORM_ENCODED, url, null, null, null, params, HttpHelper.POST_TYPE, HttpHelper.GZIP, encoding);
    }

    /**
     * Perform an HTTP POST operation with user/pass, headers, request
     * parameters,
     * and a default content-type of "application/x-www-form-urlencoded."
     */
    public String performPost(final String url, final String user, final String pass,
                              final Map<String, String> additionalHeaders, final Map<String, String> params) throws IOException {
        return performRequest(HttpHelper.MIME_FORM_ENCODED, url, user, pass, additionalHeaders, params,
                HttpHelper.POST_TYPE, HttpHelper.GZIP, HTTP_CONTENT_CHARSET);
    }

    /**
     * Perform an HTTP POST operation with flexible parameters (the
     * complicated/flexible version of the method).
     */
    public String performPost(final String contentType, final String url, final String user, final String pass,
                              final Map<String, String> additionalHeaders, final Map<String, String> params) throws IOException {
        return performRequest(contentType, url, user, pass, additionalHeaders, params, HttpHelper.POST_TYPE, HttpHelper.GZIP, HTTP_CONTENT_CHARSET);
    }

    private static URI m_RedirectUri;

    public static URI getRedirectUri() {
        return m_RedirectUri;
    }

    //
    // private methods
    //
    private String performRequest(final String contentType, final String url, final String user, final String pass,
                                  final Map<String, String> headers, final Map<String, String> params, final int requestType,
                                  final String acceptEncoding, String encoding) throws IOException {

        // add user and pass to client credentials if present
        if ((user != null) && (pass != null)) {
            client.getCredentialsProvider().setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(user, pass));
        }

        // process headers using request interceptor
        final Map<String, String> sendHeaders = new HashMap<String, String>();
        // add encoding cat_name for gzip if not present

        sendHeaders.put(HttpHelper.ACCEPT_ENCODING, HttpHelper.GZIP);

        if ((headers != null) && (headers.size() > 0)) {
            sendHeaders.putAll(headers);
        }
        if (requestType == HttpHelper.POST_TYPE) {
            sendHeaders.put(HttpHelper.CONTENT_TYPE, contentType);
        }
        if (sendHeaders.size() > 0) {
            client.addRequestInterceptor(new HttpRequestInterceptor() {
                public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                    for (String key : sendHeaders.keySet()) {
                        if (!request.containsHeader(key)) {
                            request.addHeader(key, sendHeaders.get(key));
                        }
                    }
                }
            });
        }


        m_RedirectUri = null;

        // handle POST or GET request respectively
        HttpRequestBase method = null;
        if (requestType == HttpHelper.POST_TYPE) {
            method = new HttpPost(url);
            // data - name/value params
            List<NameValuePair> nvps = null;
            if ((params != null) && (params.size() > 0)) {
                nvps = new ArrayList<NameValuePair>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
            }
            if (nvps != null) {
                try {
                    HttpPost methodPost = (HttpPost) method;
                    methodPost.setEntity(new UrlEncodedFormEntity(nvps, encoding));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Error peforming HTTP request: " + e.getMessage(), e);
                }
            }
        } else if (requestType == HttpHelper.GET_TYPE) {
            method = new HttpGet(url);
        }
        // execute request
        return execute(method);
    }

    private synchronized String execute(final HttpRequestBase method) throws IOException {
        String response = null;
        // execute method returns?!? (rather than async) - do it here sync, and wrap async elsewhere

        try {
            response = client.execute(method, responseHandler);
        } catch (ConnectTimeoutException ex) {
            throw new NotReportException("Превышен таймаут ожидания: " + ex.getMessage());
        } catch (ClientProtocolException ex) {
            throw new NotReportException("Ошибка соединения: " + ex.getMessage());
        } catch (HttpHostConnectException ex) {
            throw new NotReportException("Ошибка соединения с сервером: " + ex.getMessage());
        } catch (UnknownHostException ex) {
            throw new NotReportException("Сервер не найден: " + ex.getMessage());
        } catch (Exception ex) {
            throw new NotReportException("ошипка: " + ex.getMessage());
        }


        return response;
    }

    public String uploadFile(String url, String filePath, Map<String, String> additionalHeaders) throws Exception {

        // process headers using request interceptor
        final Map<String, String> sendHeaders = new HashMap<String, String>();
        sendHeaders.put(HttpHelper.CONTENT_TYPE, "multipart/form-data;");
        // sendHeaders.put(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP_CONTENT_CHARSET);
        // add encoding cat_name for gzip if not present
        if (!sendHeaders.containsKey(HttpHelper.ACCEPT_ENCODING)) {
            sendHeaders.put(HttpHelper.ACCEPT_ENCODING, HttpHelper.GZIP);
        }

        if (sendHeaders.size() > 0) {
            client.addRequestInterceptor(new HttpRequestInterceptor() {
                public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                    for (String key : sendHeaders.keySet()) {
                        if (!request.containsHeader(key)) {
                            request.addHeader(key, sendHeaders.get(key));
                        }
                    }
                }
            });
        }

        File file = new File(filePath);

        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file, Translit.translit(FileUtils.getFileNameFromUrl(filePath)).replace(' ', '_'), "text/plain", "UTF-8");
        FormBodyPart formBodyPart = new FormBodyPart("FILE_UPLOAD", cbFile);

        mpEntity.addPart(formBodyPart);
        m_RedirectUri = null;

        HttpPost httppost = new HttpPost(url);
        for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
            mpEntity.addPart(entry.getKey(), new StringBody(entry.getValue()));
        }

        httppost.setEntity(mpEntity);
        HttpResponse response = client.execute(httppost);
        StatusLine line = response.getStatusLine();


        // return code indicates upload failed so throw exception
        if (line.getStatusCode() < 200 || line.getStatusCode() >= 300) {
            throw new NotReportException("Ошибка загрузки файла:" + line.getReasonPhrase());
        }
        String res = EntityUtils.toString(response.getEntity());
        return res;
    }

//    public void downloadFile(String dirPath, DownloadTask downloadTask) {
//        try {
//            String url = downloadTask.getUrl();
//            url = "http://4pda.ru/forum/dl/post/944795/PolarisOffice_3.0.3047Q_SGS.apk"; //9.5Mb
//
//            // process headers using request interceptor
//            final Map<String, String> sendHeaders = new HashMap<String, String>();
//            // add encoding cat_name for gzip if not present
//            if (!sendHeaders.containsKey(HttpHelper.ACCEPT_ENCODING)) {
//                sendHeaders.put(HttpHelper.ACCEPT_ENCODING, HttpHelper.GZIP);
//            }
//
//            if (sendHeaders.size() > 0) {
//                client.addRequestInterceptor(new HttpRequestInterceptor() {
//                    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
//                        for (String key : sendHeaders.keySet()) {
//                            if (!request.containsHeader(key)) {
//                                request.addHeader(key, sendHeaders.get(key));
//                            }
//                        }
//                    }
//                });
//            }
//
//            HttpGet request = new HttpGet(url);
//            HttpResponse response = client.execute(request);
//            // Check if server response is valid
//            StatusLine status = response.getStatusLine();
//            if (status.getStatusCode() != HttpStatus.SC_OK) {
//                downloadTask.setEx(new Exception(Integer.toString(status.getStatusCode())));
//                return;
//            }
//
//
//            String fileName = downloadTask.getFileName();
//            String saveDir = dirPath;
//
//            String filePath = FileUtils.getUniqueFilePath(saveDir, fileName);
//            String downloadingFilePath = filePath + "_download";
//
//            FileUtils.mkDirs(downloadingFilePath);
//            new File(downloadingFilePath).createNewFile();
//
//            downloadTask.setOutputFile(downloadingFilePath);
//
//
//            long contentLength = response.getEntity().getContentLength();
//            downloadTask.setProgressState(0, contentLength);
//
//
//            long total  = 0;
//            int count;
//            int percent = 0;
//            int prevPercent = 0;
//
//            Date lastUpdateTime = new Date();
//            Boolean first = true;
//
//            InputStream in = new BufferedInputStream(response.getEntity().getContent());
//            OutputStream output = new FileOutputStream(downloadingFilePath, true);
//
//            byte data[] = new byte[1024];
//            try {
//                while ((count = in.read(data)) != -1) {
//                    if (downloadTask.getState() == DownloadTask.STATE_CANCELED)
//                        return;
//                    output.write(data, 0, count);
//                    total += count;
//
//                    percent = (int) ((float) total / contentLength * 100);
//
//                    long diffInMs = new Date().getTime() - lastUpdateTime.getTime();
//                    long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
//
//                    if ((percent != prevPercent && diffInSec > 1) || first) {
//                        lastUpdateTime = new Date();
//                        downloadTask.setProgressState(total, contentLength);
//                        first = false;
//                    }
//                    prevPercent = percent;
//                    if (downloadTask.getState() == DownloadTask.STATE_CANCELED)
//                        return;
//                }
//                downloadTask.setProgressState(contentLength, contentLength);
//            } finally {
//                output.flush();
//                output.close();
//                in.close();
//            }
//            File downloadingFile = new File(downloadingFilePath);
//            File downloadedFile = new File(filePath);
//            if (!downloadingFile.renameTo(downloadedFile)) {
//                throw new NotReportException("Не могу переименовать файл: " + downloadingFilePath + " в " + filePath);
//            }
//            downloadTask.setState(downloadTask.STATE_SUCCESSFULL);
//        } catch (Exception ex) {
//            downloadTask.setEx(ex);
//        }
//
//
//    }

    public HttpEntity getDownloadResponse(String url, long range) throws Exception {

        // String url = downloadTask.getUrl();
        //url = "http://4pda.ru/forum/dl/post/944795/PolarisOffice_3.0.3047Q_SGS.apk"; //9.5Mb

        // process headers using request interceptor
        final Map<String, String> sendHeaders = new HashMap<String, String>();
        sendHeaders.put(HttpHelper.ACCEPT_ENCODING, HttpHelper.GZIP);
        if (range != 0)
            sendHeaders.put("Range", "bytes=" + range + "-");

        if (sendHeaders.size() > 0) {
            client.addRequestInterceptor(new HttpRequestInterceptor() {
                public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                    for (String key : sendHeaders.keySet()) {
                        if (!request.containsHeader(key)) {
                            request.addHeader(key, sendHeaders.get(key));
                        }
                    }
                }
            });
        }

        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        // Check if server response is valid
        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() != HttpStatus.SC_OK && status.getStatusCode() != HttpStatus.SC_PARTIAL_CONTENT) {
            throw new Exception(Integer.toString(status.getStatusCode()));
        }


        return response.getEntity();
    }

    public InputStream getImageStream(String url) throws IOException {
        // process headers using request interceptor
        final Map<String, String> sendHeaders = new HashMap<String, String>();
        // add encoding cat_name for gzip if not present
        if (!sendHeaders.containsKey(HttpHelper.ACCEPT_ENCODING)) {
            sendHeaders.put(HttpHelper.ACCEPT_ENCODING, HttpHelper.GZIP);
        }

        if (sendHeaders.size() > 0) {
            client.addRequestInterceptor(new HttpRequestInterceptor() {
                public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                    for (String key : sendHeaders.keySet()) {
                        if (!request.containsHeader(key)) {
                            request.addHeader(key, sendHeaders.get(key));
                        }
                    }
                }
            });
        }

        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        // Check if server response is valid
        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() != HttpStatus.SC_OK) {

            int statusCode = status.getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                if (statusCode >= 500 && statusCode < 600)
                    throw new NotReportException("Сайт не отвечает: " + statusCode + " " + AppHttpStatus.getReasonPhrase(statusCode, status.getReasonPhrase()));
                else
                    throw new NotReportException(statusCode + " " + AppHttpStatus.getReasonPhrase(statusCode, status.getReasonPhrase()));
            }

        }

        return response.getEntity().getContent();

    }


    static class GzipDecompressingEntity extends HttpEntityWrapper {
        public GzipDecompressingEntity(final HttpEntity entity) {
            super(entity);
        }

        @Override
        public InputStream getContent() throws IOException, IllegalStateException {
            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();
            return new GZIPInputStream(wrappedin);
        }

        @Override
        public long getContentLength() {
            // length of ungzipped content is not known
            return -1;
        }
    }
}
