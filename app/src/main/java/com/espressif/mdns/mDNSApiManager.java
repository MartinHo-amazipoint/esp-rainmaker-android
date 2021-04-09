// Copyright 2020 Espressif Systems (Shanghai) PTE LTD
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.espressif.mdns;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.espressif.AppConstants;
import com.espressif.EspApplication;
import com.espressif.JsonDataParser;
import com.espressif.cloudapi.ApiResponseListener;
import com.espressif.provisioning.listeners.ResponseListener;
import com.espressif.ui.models.EspNode;
import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import rm_local_ctrl.Constants;
import rm_local_ctrl.EspLocalCtrl;

public class mDNSApiManager {

    private final String TAG = mDNSApiManager.class.getSimpleName();

    private EspApplication espApp;

    public mDNSApiManager(Context context) {
        espApp = (EspApplication) context.getApplicationContext();
    }

    public void getNodeDetails(final String nodeId, final ApiResponseListener listener) {

        Log.d(TAG, "Get Node details on local network for id : " + nodeId);

        if (espApp.mDNSDeviceMap.containsKey(nodeId)) {

            final mDNSDevice dnsDevice = espApp.mDNSDeviceMap.get(nodeId);

            final String url = "http://" + dnsDevice.getIpAddr() + ":" + dnsDevice.getPort();

            if (dnsDevice.getPropertyCount() != 0) {

                getPropertyValues(url, AppConstants.LOCAL_CONTROL_PATH, dnsDevice.getPropertyCount(), new ApiResponseListener() {

                    @Override
                    public void onSuccess(Bundle data) {

                        Log.d(TAG, "Get node detail - Success");

                        if (data != null) {

                            String configData = data.getString(AppConstants.KEY_CONFIG);
                            String paramsData = data.getString(AppConstants.KEY_PARAMS);

                            Log.d(TAG, "Config data : " + configData);
                            Log.d(TAG, "Params data : " + paramsData);

                            if (!TextUtils.isEmpty(configData)) {

                                JSONObject configJson = null;
                                try {
                                    configJson = new JSONObject(configData);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                EspNode node = espApp.nodeMap.get(nodeId);
                                boolean isDeviceFound = false;
                                if (node != null) {
                                    isDeviceFound = true;
                                }
                                EspNode localNode = JsonDataParser.setNodeConfig(node, configJson);

                                if (!TextUtils.isEmpty(paramsData) && isDeviceFound) {

                                    espApp.nodeMap.put(node.getNodeId(), localNode);
                                    JSONObject paramsJson = null;
                                    try {
                                        paramsJson = new JSONObject(paramsData);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    JsonDataParser.setAllParams(espApp, localNode, paramsJson);
                                    listener.onSuccess(null);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        listener.onFailure(exception);
                    }
                });

            } else {

                getPropertyCount(url, AppConstants.LOCAL_CONTROL_PATH, new ApiResponseListener() {

                    @Override
                    public void onSuccess(Bundle data) {

                        if (data != null) {

                            int count = data.getInt(AppConstants.KEY_PROPERTY_COUNT, 0);
                            dnsDevice.setPropertyCount(count);
                            getPropertyValues(url, AppConstants.LOCAL_CONTROL_PATH, count, listener);
                        }
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        listener.onFailure(exception);
                    }
                });
            }
        } else {
            listener.onFailure(new RuntimeException("Device not available locally."));
        }
    }

    public void getParamsValues(final String nodeId, final ApiResponseListener listener) {

        Log.d(TAG, "Get param values on local network for node : " + nodeId);

        if (espApp.mDNSDeviceMap.containsKey(nodeId)) {

            final mDNSDevice dnsDevice = espApp.mDNSDeviceMap.get(nodeId);

            final String url = "http://" + dnsDevice.getIpAddr() + ":" + dnsDevice.getPort();

            if (dnsDevice.getPropertyCount() != 0) {

                getPropertyValues(url, AppConstants.LOCAL_CONTROL_PATH, dnsDevice.getPropertyCount(), new ApiResponseListener() {

                    @Override
                    public void onSuccess(Bundle data) {

                        Log.d(TAG, "Get param values - Success");

                        if (data != null) {

                            String configData = data.getString(AppConstants.KEY_CONFIG);
                            String paramsData = data.getString(AppConstants.KEY_PARAMS);
                            Log.e(TAG, "Params data : " + paramsData);

                            if (!TextUtils.isEmpty(configData)) {

                                JSONObject configJson = null;
                                try {
                                    configJson = new JSONObject(configData);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                EspNode node = espApp.nodeMap.get(nodeId);
                                boolean isDeviceFound = false;
                                if (node != null) {
                                    isDeviceFound = true;
                                }
                                EspNode localNode = JsonDataParser.setNodeConfig(node, configJson);

                                if (!TextUtils.isEmpty(paramsData) && isDeviceFound) {

                                    JSONObject paramsJson = null;
                                    try {
                                        paramsJson = new JSONObject(paramsData);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    JsonDataParser.setAllParams(espApp, localNode, paramsJson);
                                    listener.onSuccess(null);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        listener.onFailure(exception);
                    }
                });

            } else {

                getPropertyCount(url, AppConstants.LOCAL_CONTROL_PATH, new ApiResponseListener() {

                    @Override
                    public void onSuccess(Bundle data) {

                        if (data != null) {

                            int count = data.getInt(AppConstants.KEY_PROPERTY_COUNT, 0);
                            dnsDevice.setPropertyCount(count);
                            getPropertyValues(url, AppConstants.LOCAL_CONTROL_PATH, count, listener);
                        }
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        listener.onFailure(exception);
                    }
                });
            }
        } else {
            listener.onFailure(new RuntimeException("Device not available locally."));
        }
    }

    public void updateParamValue(final String nodeId, JsonObject body, final ApiResponseListener listener) {

        Log.d(TAG, "Update param values on local network");

        if (espApp.mDNSDeviceMap.containsKey(nodeId)) {

            mDNSDevice dnsDevice = espApp.mDNSDeviceMap.get(nodeId);

            String url = "http://" + dnsDevice.getIpAddr() + ":" + dnsDevice.getPort();
            String jsonData = body.toString();
            byte[] data = createSetPropertyInfoRequest(jsonData);

            mDNSTransport transport = new mDNSTransport(url);
            transport.sendData(AppConstants.LOCAL_CONTROL_PATH, data, new ResponseListener() {

                @Override
                public void onSuccess(byte[] returnData) {

                    Log.d(TAG, "Update param values - Success");

                    if (returnData != null) {

                        // Set Property Response
                        try {
                            EspLocalCtrl.LocalCtrlMessage response = EspLocalCtrl.LocalCtrlMessage.parseFrom(returnData);
                            Constants.Status status = response.getRespSetPropVals().getStatus();

                            if (status.equals(Constants.Status.Success)) {
                                listener.onSuccess(null);
                            } else {
                                listener.onFailure(new RuntimeException("Failed to update param."));
                            }

                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }

                    } else {
                        Log.e(TAG, "returnData is null");
                        listener.onFailure(new RuntimeException("Response not received."));
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    listener.onFailure(e);
                }
            });

        } else {
            listener.onFailure(new RuntimeException("Device not available locally."));
        }
    }

    public void getPropertyCount(final String url, final String path, final ApiResponseListener listener) {

        byte[] data = createGetPropertyCountRequest();

        mDNSTransport transport = new mDNSTransport(url);
        transport.sendData(path, data, new ResponseListener() {

            @Override
            public void onSuccess(byte[] returnData) {
                if (returnData != null) {
                    int count = processGetPropertyCount(returnData);
                    Bundle bundle = new Bundle();
                    bundle.putInt(AppConstants.KEY_PROPERTY_COUNT, count);
                    listener.onSuccess(bundle);
                } else {
                    Log.e("TAG", "returnData is null");
                }
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        });
    }

    public void getPropertyValues(String url, String path, int count, final ApiResponseListener listener) {

        byte[] data = createGetAllPropertyValuesRequest(count);
        mDNSTransport transport = new mDNSTransport(url);

        transport.sendData(path, data, new ResponseListener() {

            @Override
            public void onSuccess(byte[] returnData) {
                if (returnData != null) {
                    processGetPropertyValue(returnData, listener);
                } else {
                    listener.onFailure(new RuntimeException("Response not received."));
                }
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        });
    }

    private byte[] createGetPropertyCountRequest() {

        EspLocalCtrl.LocalCtrlMsgType msgType = EspLocalCtrl.LocalCtrlMsgType.TypeCmdGetPropertyCount;
        EspLocalCtrl.LocalCtrlMessage msg = EspLocalCtrl.LocalCtrlMessage.newBuilder()
                .setMsg(msgType)
                .setCmdGetPropCount(EspLocalCtrl.CmdGetPropertyCount.newBuilder())
                .build();

        return msg.toByteArray();
    }

    private byte[] createSetPropertyInfoRequest(String jsonData) {

        EspLocalCtrl.LocalCtrlMsgType msgType = EspLocalCtrl.LocalCtrlMsgType.TypeCmdSetPropertyValues;
        EspLocalCtrl.PropertyValue prop = EspLocalCtrl.PropertyValue.newBuilder()
                .setIndex(1)
                .setValue(ByteString.copyFrom(jsonData, Charset.forName("UTF-8")))
                .build();

        EspLocalCtrl.CmdSetPropertyValues payload = EspLocalCtrl.CmdSetPropertyValues.newBuilder()
                .addProps(prop).build();

        EspLocalCtrl.LocalCtrlMessage msg = EspLocalCtrl.LocalCtrlMessage.newBuilder()
                .setMsg(msgType)
                .setCmdSetPropVals(payload)
                .build();

        return msg.toByteArray();
    }

    private byte[] createGetAllPropertyValuesRequest(int count) {

        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            indices.add(i);
        }
        EspLocalCtrl.LocalCtrlMsgType msgType = EspLocalCtrl.LocalCtrlMsgType.TypeCmdGetPropertyValues;
        EspLocalCtrl.CmdGetPropertyValues payload = EspLocalCtrl.CmdGetPropertyValues.newBuilder()
                .addAllIndices(indices).build();

        EspLocalCtrl.LocalCtrlMessage msg = EspLocalCtrl.LocalCtrlMessage.newBuilder()
                .setMsg(msgType)
                .setCmdGetPropVals(payload)
                .build();

        return msg.toByteArray();
    }

    private int processGetPropertyCount(byte[] returnData) {

        int count = 0;

        try {
            EspLocalCtrl.LocalCtrlMessage response = EspLocalCtrl.LocalCtrlMessage.parseFrom(returnData);

            if (response.getRespGetPropCount().getStatus().equals(Constants.Status.Success)) {
                count = response.getRespGetPropCount().getCount();
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return count;
    }

    private void processGetPropertyValue(byte[] returnData, final ApiResponseListener listener) {

        try {
            EspLocalCtrl.LocalCtrlMessage response = EspLocalCtrl.LocalCtrlMessage.parseFrom(returnData);

            if (response.getRespGetPropVals().getStatus().equals(Constants.Status.Success)) {

                List<EspLocalCtrl.PropertyInfo> propertyInfoList = response.getRespGetPropVals().getPropsList();
                Bundle bundle = new Bundle();

                for (int i = 0; i < propertyInfoList.size(); i++) {
                    EspLocalCtrl.PropertyInfo propertyInfo = propertyInfoList.get(i);
                    bundle.putString(propertyInfo.getName(), propertyInfo.getValue().toStringUtf8());
                }
                listener.onSuccess(bundle);
            } else {
                listener.onFailure(new RuntimeException("Failed to get data from device"));
            }

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            listener.onFailure(e);
        }
    }
}
