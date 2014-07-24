package com.cesi.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;

import android.util.Log;

public class CasClient
{
    private static final String CAS_LOGIN_URL_PART = "tickets";
    private static final String CAS_LOGOUT_URL_PART = "tickets";

    private static final int REQUEST_TIMEOUT = 5*1000;
    private static final int SO_TIMEOUT = 10*1000;  

    private static DefaultHttpClient httpClient;

    private String casBaseURL;
    
    private static CasClient instance;
    
    private DefaultHttpClient getHttpClient()
    {
        BasicHttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
        DefaultHttpClient client = new DefaultHttpClient(httpParams);
        return client;
    }

    private CasClient (String casBaseUrl)
    {
        this.httpClient = getHttpClient();
        this.casBaseURL = casBaseUrl;
    }
    
    
   public static CasClient getInstance(){
    	if(instance==null){ 
    		synchronized(CasClient.class){
    			if(instance==null){
    				instance = new CasClient("http://www.cseicms.com/inspectCas/v1/");
//    				instance = new CasClient("http://www.cseicms.com/cas/v1/");
    			}
    		}
    	}
    	return instance;
    }

    public boolean login(String username,String password,String sessionGenerateService){
        String tgt = getTGT(username, password);
        if(tgt==null){
            return false;
        }

        String st = getST( sessionGenerateService, tgt);
        if(st==null){
            return false;
        }

        String sessionId = generateSession(sessionGenerateService,st);
        if(sessionId==null){
   
            return false;
        }
        return true;
    }


    private String getResponseBody(HttpResponse response){
        StringBuilder sb = new StringBuilder();
        try {
            InputStream instream = response.getEntity().getContent();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(instream), 65728);
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            instream.close();
        }
        catch (IOException e) { e.printStackTrace(); }
        catch (Exception e) { e.printStackTrace(); }
        return sb.toString();

    }

    /**
     *��ȡticket granting ticket
     */
    public String getTGT(String username, String password)
    {
        String tgt = null;
        HttpPost httpPost = new HttpPost (casBaseURL + CAS_LOGIN_URL_PART);
        try
        {
            List <NameValuePair> nvps = new ArrayList <NameValuePair> ();
            nvps.add(new BasicNameValuePair ("username", username));
            nvps.add(new BasicNameValuePair ("password", password));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            HttpResponse response = httpClient.execute(httpPost);
            String responseBody = getResponseBody(response);
            switch (response.getStatusLine().getStatusCode())
            {
                case 201:
                {
                    Matcher matcher = Pattern.compile(".*action='.*/tickets/(TGT-.*\\.whut\\.org).*")
                            .matcher(responseBody.replaceAll("\"", "'"));
                    if (matcher.matches()){
                        tgt = matcher.group(1);
                    }
                    break;
                }
                default:
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return tgt;
    }

    public String getST(String service,String tgt){
        if (tgt == null) return null;
        String st = null;
        HttpPost httpPost = new HttpPost (casBaseURL + CAS_LOGIN_URL_PART+"/" + tgt);
        try
        {
            List <NameValuePair> nvps = new ArrayList <NameValuePair> ();
            nvps.add(new BasicNameValuePair ("service", service));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));

            HttpResponse response = httpClient.execute(httpPost);
            String responseBody = getResponseBody(response);

            switch (response.getStatusLine().getStatusCode())
            {
                case 200:
                {
                    st = responseBody;
                    break;
                }
                default:
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return st;
    }


    public String generateSession(String service,String st){
        String sessionId = "";
        HttpGet httpGet = new HttpGet (service+"?ticket="+st);
        try
        {
            HttpResponse response = httpClient.execute(httpGet);
            EntityUtils.toString(response.getEntity() ,"utf-8");
            switch (response.getStatusLine().getStatusCode())
            {
                case 200:
                {
                    List<Cookie> cookies = httpClient.getCookieStore().getCookies();
                    if (! cookies.isEmpty()){
                        for (Cookie cookie : cookies){
                            if(cookie.getName().equals("JSESSIONID")){
                                sessionId = cookie.getValue();
                                break;
                            }
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return sessionId;
    }

  //�汾��
    @SuppressWarnings("deprecation")
	public String doSendFile2(String ServicePath,String FilePath) throws ClientProtocolException, IOException {
    	httpClient.getParams().setParameter(  
                CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);  
        HttpPost httppost = new HttpPost(ServicePath);  
        File file = new File(FilePath);
        MultipartEntity entity = new MultipartEntity();  
        FileBody fileBody = new FileBody(file);  
        entity.addPart("filename", fileBody);  
        httppost.setEntity(entity);  
        HttpResponse response = httpClient.execute(httppost);
        HttpEntity resEntity = response.getEntity();  
//        if (resEntity != null) {  
//            Log.i("sendfile", EntityUtils.toString(resEntity));  
//        }  
    	return EntityUtils.toString(resEntity);
	}
    //����GET����
    public String doGet(String service){
        Log.i("cas client doGet url:", service);
        HttpGet httpGet = new HttpGet (service);
        try
        {
            HttpResponse response = httpClient.execute(httpGet);
            String responseBody = getResponseBody(response);
            switch (response.getStatusLine().getStatusCode())
            {
                case 200:
                {
                    Log.i("cas client doGet response:", responseBody);
                    return responseBody;
                }
                default:
                    break;
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public InputStream DoGetFile(String service)
    {
    	HttpGet httpGet = new HttpGet (service);
        try
        {
            HttpResponse response = httpClient.execute(httpGet);
            switch (response.getStatusLine().getStatusCode())
            {
                case 200:
                {
                    Log.i("cas client doGetfile", "success");
                    return  response.getEntity().getContent();
                }
                default:
                    break;
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    	
    }
    
    //����POST����
    synchronized public String doPost(String service,HashMap<String,Object> params){
        Log.i("cas client doPost url:", service);
        HttpPost httpPost = new HttpPost (service);
        try
        {
            List <NameValuePair> nvps = new ArrayList <NameValuePair> ();
            for(String key:params.keySet()){
                nvps.add(new BasicNameValuePair (key, (String) params.get(key)));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps,HTTP.UTF_8));
            

            HttpResponse response = httpClient.execute(httpPost);
            String responseBody = getResponseBody(response);
            switch (response.getStatusLine().getStatusCode())
            {
                case 200:
                {
                    Log.i("cas client doPost response:", responseBody);
                    return responseBody;
                }
                default:
                    break;
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
    
  //����POSTû�д���������
   synchronized public String doPostNoParams(String service){
        Log.i("cas client doPost url:", service);
        HttpPost httpPost = new HttpPost (service);
        try
        {
            
//            httpPost.setEntity(new UrlEncodedFormEntity(null,HTTP.UTF_8));
            HttpResponse response = httpClient.execute(httpPost);
            String responseBody = getResponseBody(response);
            switch (response.getStatusLine().getStatusCode())
            {
                case 200:
                {
                    Log.i("cas client doPost response:", responseBody);
                    return responseBody;
                }
                default:
                    break;
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }





    public boolean logout ()
    {
        boolean logoutSuccess = false;
        HttpDelete httpDelete = new HttpDelete(casBaseURL + CAS_LOGOUT_URL_PART);
        try
        {
            HttpResponse response = httpClient.execute(httpDelete);
            logoutSuccess = (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        }
        catch (Exception e)
        {
            logoutSuccess = false;
        }
        return logoutSuccess;
    }


    public boolean reset(){
    	instance = null;
    	boolean result = false;
    	result = logout();
    	return result;
    }

}