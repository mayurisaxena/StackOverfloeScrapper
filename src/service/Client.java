package service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Random;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Client {

   private static final org.apache.log4j.Logger logger = Logger.getLogger(Client.class);
   
   public static final String PROXY_HOST = ""; // ENTER YOUR PROXY HOST
   public static final int PROXY_PORT = 8123;
   public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
   
   public String sessionId = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
   public CloseableHttpClient client;

   public Client(String country) {
      
      HttpHost proxy = new HttpHost(PROXY_HOST, PROXY_PORT);
      RequestConfig config = RequestConfig.custom().setConnectTimeout(10 * 1000).setConnectionRequestTimeout(10 * 1000)
           .setSocketTimeout(20 * 1000).setProxy(proxy).build();
      SocketConfig sc = SocketConfig.custom().setSoTimeout(20 * 1000).build();
      client = HttpClientBuilder.create().setDefaultRequestConfig(config).setDefaultSocketConfig(sc).build();
    
   }

   public String request(String url) throws IOException, InterruptedException {
      CloseableHttpResponse response = null;
      try {
         HttpGet request = new HttpGet(url);
         HttpHost target = new HttpHost("stackoverflow.com", 443, "http");
         request.setHeader("User-Agent", USER_AGENT);
         logger.info("Going to execute http method");
         response = client.execute(target, request);
         logger.info("http method execution ends");
         return EntityUtils.toString(response.getEntity());
      } finally {
         try {
            if (response!=null)
               response.close();
         } catch (Exception e) {
            logger.error("In finally of request for url : " + url, e);
         }
      }
   }

   public void close() throws IOException {
      client.close();
   }

   public static Document stackOverflowProxyCall(String url) throws Exception {
      Client client = new Client(null);
      try {
         if (null != client.client) {
            String doc = client.request(url);
            logger.info("doc retrived from StackOverflow:" + url);
            return Jsoup.parse(doc);
         } else {
            logger.info("In Client StackOverflow call blocked for url " + url);
            Thread.sleep(30000);
            return stackOverflowProxyCall(url);
         }
      } catch (SocketTimeoutException e) {
         logger.error("In SocketTimeoutException of client request call for url :" + url, e);
         Thread.sleep(3000);
         return stackOverflowProxyCall(url);
      } catch (ConnectTimeoutException e) {
         logger.error("In ConnectTimeoutException of client request call for url :" + url, e);
         Thread.sleep(3000);
         return stackOverflowProxyCall(url);
      } finally {
         try {
            if (client!=null)
               client.close();
         } catch (Exception e) {
            logger.error("In finally of stackOverflowProxyCall for url : " + url, e);
         }
      }
   }
}
