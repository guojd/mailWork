package com.pt.myworkcenter.util;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pt.myworkcenter.pojo.ClientCredentials;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class Graph {

    @Value("${mw.clientId}")
    private String clientId;
    @Value("${mw.scope}")
    private String scope;
    @Value("${mw.clientSecret}")
    private String clientSecret;
    @Value("${mw.grantType}")
    private String grantType;
    @Value("${mw.tenant}")
    private String tenant;
    @Value("${mw.api:https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token}")
    private String api;

    private Logger logger= LoggerFactory.getLogger(Graph.class);
    private ClientCredentials clientCredentials;


    public ClientCredentials getClientCredentials() {
        if(!Objects.isNull(clientCredentials)&&System.currentTimeMillis()- clientCredentials.getLoadTime() < (clientCredentials.getExpiresIn()-10)*1000){
            return clientCredentials;
        }
        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("scope", scope);
        params.put("client_secret", clientSecret);
        params.put("grant_type", grantType);
        String token = HttpHelper.post(api.replace("{tenant}", tenant), params, 10000, 10000, "UTF-8");
        logger.info("token:{}",token);
        ClientCredentials clientCredentials= JSON.parseObject(token,ClientCredentials.class);
        clientCredentials.setLoadTime(System.currentTimeMillis());
        this.clientCredentials =clientCredentials;
        return clientCredentials;
    }

    public String call(String api,String method,Map<String, String> headers, Map<String,Object> params,String bodyJson) throws IOException {
        RequestBody requestBody=null;
        if(!"GET;get".contains(method)) {
            if (bodyJson != null && bodyJson.length() > 0) {
                requestBody = getRequestBody(bodyJson);
            } else if (params != null && params.size() > 0) {
                requestBody = getRequestBody(params);
            }
        }
        Request.Builder builder=new Request.Builder()
                .url(api)
                .method(method,requestBody);
        return httpEexc(headers, builder);
    }

    public String get(String api, Map<String, String> headers) throws Exception{
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request.Builder builder=new Request.Builder()
                .url(api)
                .get();
        return httpEexc(headers, builder);
    }
    public String post(String api, Map<String, String> headers, Map<String,Object> params) throws Exception{
        Request.Builder builder=new Request.Builder()
                .url(api)
                .post(getRequestBody(params));
        return httpEexc(headers, builder);
    }
    public String post(String api, Map<String, String> headers, String params) throws Exception{
        Request.Builder builder=new Request.Builder()
                .url(api)
                .post(getRequestBody(params));
        return httpEexc(headers, builder);
    }
    public String delete(String api, Map<String, String> headers) throws Exception{
        Request.Builder builder=new Request.Builder()
                .url(api)
                .delete();
        return httpEexc(headers,builder);
    }
    public String delete(String api, Map<String, String> headers, Map<String,Object> params) throws Exception{
        Request.Builder builder=new Request.Builder()
                .url(api)
                .delete(getRequestBody(params));
        return httpEexc(headers, builder);
    }
    public String delete(String api, Map<String, String> headers, String params) throws Exception{
        Request.Builder builder=new Request.Builder()
                .url(api)
                .delete(getRequestBody(params));
        return httpEexc(headers, builder);
    }

    public String patch(String api, Map<String, String> headers, Map<String,Object> params) throws Exception{
        Request.Builder builder=new Request.Builder()
                .url(api)
                .patch(getRequestBody(params));
        return httpEexc(headers, builder);
    }
    public String patch(String api, Map<String, String> headers, String params) throws Exception{
        Request.Builder builder=new Request.Builder()
                .url(api)
                .patch(getRequestBody(params));
        return httpEexc(headers, builder);
    }

    private RequestBody getRequestBody(Map<String,Object> params){
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for(String key: params.keySet()){
            formBodyBuilder.add(key, String.valueOf(params.get(key)));
        }
        return formBodyBuilder.build();
    }

    private RequestBody getRequestBody(String params){
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        return FormBody.create(mediaType, params);
    }

    private String httpEexc(Map<String, String> headers, Request.Builder builder) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        if(headers ==null){
            headers =new HashMap<>();
        }
        if(!headers.containsKey("Authorization")){
            ClientCredentials clientCredentials=getClientCredentials();
            headers.put("Authorization",clientCredentials.getAccessToken());
        }

        return httpEexcCall(headers,builder);
    }
    private String httpEexcCall(Map<String, String> headers, Request.Builder builder) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        if(headers ==null){
            headers =new HashMap<>();
        }
        for(String key: headers.keySet()){
            builder.addHeader(key, headers.get(key));
        }
        Request request = builder.build();
        Response response = client.newCall(request).execute();

        JSONObject responseObj=JSON.parseObject(response.body().string());
        JSONObject resultData=new JSONObject();
        resultData.put("data",responseObj);
        String resultStr=resultData.toString();
        logger.info("resultStr:{}",resultStr);
        return resultStr;
    }
    /*public GraphServiceClient getGraphClient() {
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenant)
                .build();
        TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(clientSecretCredential);
        GraphServiceClient graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(tokenCredentialAuthProvider)
                        .buildClient();
        return graphClient;
    }*/
}
