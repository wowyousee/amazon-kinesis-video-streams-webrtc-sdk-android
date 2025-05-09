package com.amazonaws.kinesisvideo.kvsclient;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.kinesisvideo.model.*;
import com.amazonaws.services.kinesisvideosignaling.model.GetIceServerConfigRequest;
import com.amazonaws.services.kinesisvideosignaling.model.IceServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AWSKinesisVideoClient {
    private final String endpoint;
    private final HttpClientUtil httpClient;

    public AWSKinesisVideoClient(String endpoint) {
        this.endpoint = endpoint;
        this.httpClient = new HttpClientUtil();
    }

    public DescribeSignalingChannelResult describeSignalingChannel(
            DescribeSignalingChannelRequest request) 
            throws AmazonServiceException, AmazonClientException {
        
        try {
            // 构建自定义API请求
            String requestJson = String.format("{\"ChannelName\":\"%s\"}", request.getChannelName());
            String response = httpClient.post(endpoint + "/describeSignalingChannel", requestJson);
            
            // 解析响应
            return new DescribeSignalingChannelResult()
                .withChannelInfo(new ChannelInfo()
                    .withChannelARN(parseChannelArn(response))
                    .withChannelName(request.getChannelName()));
                    
        } catch (Exception e) {
            throw new AmazonClientException("Custom API call failed", e);
        }
    }

    public CreateSignalingChannelResult createSignalingChannel(
            CreateSignalingChannelRequest request) {
        
        try {
            String requestJson = String.format("{\"channelName\":\"%s\"}", request.getChannelName());
            String response = httpClient.post(endpoint + "/createChannel", requestJson);
            
            return new CreateSignalingChannelResult()
                .withChannelARN(parseChannelArn(response));
                
        } catch (Exception e) {
            throw new AmazonClientException("Custom API call failed", e);
        }
    }

    public GetSignalingChannelEndpointResult getSignalingChannelEndpoint(
        GetSignalingChannelEndpointRequest request) 
        throws AmazonServiceException, AmazonClientException {    
        try {
            // 1. 构建符合服务端要求的JSON请求
            String role = request.getSingleMasterChannelEndpointConfiguration().getRole().toString();
            String[] protocols = request.getSingleMasterChannelEndpointConfiguration().getProtocols().toArray(new String[0]);
            
            String requestJson = String.format(
                "{\"ChannelARN\":\"%s\"," +
                "\"SingleMasterChannelEndpointConfiguration\":{" +
                "\"Protocols\":[\"%s\",\"%s\"]," +
                "\"Role\":\"%s\"}}",
                request.getChannelARN(),
                protocols[0],  // WSS
                protocols[1],  // WEBRTC
                role);
            
            Log.d(TAG, "Sending request: " + requestJson);
            
            // 2. 调用自定义API
            String response = httpClient.post(endpoint + "/getSignalingChannelEndpoint", requestJson);
            
            // 3. 解析响应并构建结果对象
            List<ResourceEndpointListItem> endpoints = new ArrayList<>();
            
            // 添加WSS端点
            endpoints.add(new ResourceEndpointListItem()
                .withProtocol("WSS")
                .withResourceEndpoint(parseEndpointFromResponse(response, "WSS")));
                
            // 添加HTTPS端点
            endpoints.add(new ResourceEndpointListItem()
                .withProtocol("HTTPS")
                .withResourceEndpoint(parseEndpointFromResponse(response, "WEBRTC")));
            
            // 4. 返回结果
            return new GetSignalingChannelEndpointResult()
                .withResourceEndpointList(endpoints);
                
        } catch (Exception e) {
            throw new AmazonClientException("Failed to get signaling channel endpoint", e);
        }
    }

    public GetIceServerConfigResult getIceServerConfig(
        GetIceServerConfigRequest request) 
        throws AmazonServiceException, AmazonClientException {
        
        try {
            // 1. 构建符合服务端要求的JSON请求
            String requestJson = String.format(
                "{\"ChannelARN\":\"%s\"," +
                "\"ClientId\":\"%s\"," +
                "\"Service\":\"kvs\"}",  // 固定值"kvs"或根据实际情况配置
                request.getChannelARN(),
                request.getClientId());
            
            Log.d(TAG, "Getting ICE server config with request: " + requestJson);
            
            // 2. 调用自定义API获取ICE服务器配置
            String response = httpClient.post(endpoint + "/v1/get-ice-server-config", requestJson);
            
            // 3. 解析JSON响应
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray iceServers = jsonResponse.getJSONArray("IceServerList");
            
            // 4. 构建结果对象
            GetIceServerConfigResult result = new GetIceServerConfigResult();
            List<IceServer> serverList = new ArrayList<>();
            
            for (int i = 0; i < iceServers.length(); i++) {
                JSONObject server = iceServers.getJSONObject(i);
                IceServer iceServer = new IceServer()
                    .withUris(toStringList(server.getJSONArray("Uris")))
                    .withUsername(server.getString("Username"))
                    .withPassword(server.getString("Password"))
                    .withTtl(server.getInt("Ttl"));
                serverList.add(iceServer);
            }
            
            return result.withIceServerList(serverList);
                
        } catch (JSONException e) {
            throw new AmazonClientException("Failed to parse ICE server config response", e);
        } catch (Exception e) {
            throw new AmazonClientException("Failed to get ICE server config", e);
        }
    }

    // 辅助方法：将JSONArray转换为List<String>
    private List<String> toStringList(JSONArray array) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(array.getString(i));
        }
        return list;
    }

    private String parseEndpointFromResponse(String response, String protocol) {
        // 这里实现从响应JSON中解析出对应协议的端点
        // 示例实现 - 实际应根据您的服务端响应格式调整
        if ("WSS".equals(protocol)) {
            return endpoint + "/wss";
        } else {
            return endpoint + "/webrtc";
        }
    }

    private String parseChannelArn(String response) {
        // 实现您的响应解析逻辑
        return "custom-channel-arn-" + System.currentTimeMillis();
    }

    public DescribeMediaStorageConfigurationResult describeMediaStorageConfiguration(DescribeMediaStorageConfigurationRequest describeMediaStorageConfigurationRequest) {
        return null;
    }
}