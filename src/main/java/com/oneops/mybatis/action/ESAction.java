package com.oneops.mybatis.action;

import com.oneops.mybatis.Stats;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

/**
 * Created by o0k000m on 5/2/17.
 */
public class ESAction extends JSONStringAction{
    private Log logger = LogFactory.getLog(ESAction.class);

    @Override
    public void setProperties(Properties properties) {
        properties.getProperty("esMetricIndexURL", "localhost:9200/metrics/mybatis");
    }

    @Override
    void process(String jsonString) {
        HttpClient httpClient = new DefaultHttpClient();
        try {
            ResponseHandler<String> handler = new BasicResponseHandler();

            HttpPost httppost = new HttpPost();
            StringEntity entity = new StringEntity(jsonString);
            entity.setContentType("application/json");
            httppost.setEntity(entity);

            HttpResponse response = httpClient.execute(httppost);
            if (response.getStatusLine().getStatusCode() != 200) {
                logger.warn("Post to ES Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }
            handler.handleResponse(response);

        } catch (IOException e) {
            logger.warn("Error while posting to ES", e);
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
}
