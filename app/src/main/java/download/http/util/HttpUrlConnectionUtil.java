package download.http.util;

import android.webkit.URLUtil;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import download.http.exception.AppException;
import download.http.request.Request;

/**
 * Created by lizhiyun on 16/6/3.
 */
public class HttpUrlConnectionUtil {
    public static HttpURLConnection execute(Request request) throws AppException {
        if (!URLUtil.isNetworkUrl(request.url)){
            throw new AppException(AppException.ErrorType.IO,"the url :"+request.url + "is not valid");
        }
        switch (request.method){
            case GET:
            case DELETE:
                return get(request);
            case POST:
            case PUT:
                return post(request);

        }
        return null;

    }
    private static HttpURLConnection get(Request request) throws AppException {
        try {
            request.checkIfCanceled();
            HttpURLConnection connection = (HttpURLConnection) new URL(request.url+"?"+request.content).openConnection();
            connection.setRequestMethod(request.method.name());
            connection.setConnectTimeout(15 * 1000);
            connection.setReadTimeout(10 * 1000);
            addHeader(connection, request.headers);
            request.checkIfCanceled();
            return connection;

        }
        catch(InterruptedIOException e) {
            throw new AppException(AppException.ErrorType.TIMEOUT,e.getMessage());
        }
        catch(IOException e){
            throw new AppException(AppException.ErrorType.IO,e.getMessage());
        }

    }

    private static HttpURLConnection post(Request request) throws AppException {
        try {
            request.checkIfCanceled();
            HttpURLConnection connection = (HttpURLConnection) new URL(request.url).openConnection();
            connection.setRequestMethod(request.method.name());
            connection.setConnectTimeout(15 * 1000);
            connection.setReadTimeout(10 * 1000);
            connection.setDoOutput(true);
            addHeader(connection, request.headers);
            request.checkIfCanceled();
            OutputStream os = connection.getOutputStream();
            os.write(request.content.getBytes());
            request.checkIfCanceled();
            return connection;
        }
        catch(InterruptedIOException e) {
            throw new AppException(AppException.ErrorType.TIMEOUT,e.getMessage());
        }catch (IOException e) {
            throw new AppException(AppException.ErrorType.IO,e.getMessage());
        }
    }
    private static void addHeader(HttpURLConnection connection, Map<String, String> headers) {
        if (headers == null || headers.size() == 0)
            return;
        for (Map.Entry<String ,String> entry: headers.entrySet()){
            connection.addRequestProperty(entry.getKey(),entry.getValue());
        }
    }
}