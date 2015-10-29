package org.zywx.wbpalmstar.plugin.uexumeng;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;

import java.util.HashMap;
import java.util.Iterator;

public class EUExUmeng extends EUExBase{
    private static final String BUNDLE_DATA = "data";
    private static final String TAG = "EUExUmeng";
    private static final int MSG_ADD_EVENT = 1;
    private static final int MSG_GET_DEVICE_INFO = 2;

    private static final String INVALID_PARAM = "Invalid Param";
    private static final String JSON_FORMAT_ERROR = "JSON Format Error";

    public static final String FUN_ON_CALLBACK = "javascript:uexUmeng.cbGetDeviceInfo";


    private Context context;

    public EUExUmeng(Context context, EBrowserView eBrowserView) {
        super(context, eBrowserView);
        this.context = context;
    }

    public static void onActivityResume(Context context) {
        MobclickAgent.onResume(context);
    }
    public static void onActivityPause(Context context) {
        MobclickAgent.onPause(context);
    }

    public void onEvent(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_ADD_EVENT;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    public void getDeviceInfo(String [] params) {
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_GET_DEVICE_INFO;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void onEventMsg(String [] params) {
        if (params.length == 0) {
            Toast.makeText(context, INVALID_PARAM, Toast.LENGTH_SHORT).show();
            return;
        }
        if (params.length == 1) {
            MobclickAgent.onEvent(context, params[0]);
        } else if (params.length == 2) {
            String eventId = params[0];
            HashMap<String, String> map = new HashMap<String, String>();
            try{
                JSONObject object = new JSONObject(params[1]);
                Iterator iterator = object.keys();
                while(iterator.hasNext()) {
                    String key = (String) iterator.next();
                    map.put(key, object.getString(key));
                }
            } catch (JSONException e) {
                Toast.makeText(context, JSON_FORMAT_ERROR, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            MobclickAgent.onEvent(mContext, eventId, map);
        }
    }

    private void getDeviceInfoMsg(String [] params) {
        String result = getDeviceInfo(context);
        onCallback(FUN_ON_CALLBACK + "('" + result + "')");
        Log.i(TAG, result);
    }

    //友盟的代码，获取device info.
    public String getDeviceInfo(Context context) {
        try{
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String device_id = tm.getDeviceId();
            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            String mac = wifi.getConnectionInfo().getMacAddress();
            json.put("mac", mac);
            if( TextUtils.isEmpty(device_id) ){
                device_id = mac;
            }
            if( TextUtils.isEmpty(device_id) ){
                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
            }
            json.put("device_id", device_id);
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onHandleMessage(Message message) {
        if(message == null){
            return;
        }
        Bundle bundle = message.getData();
        switch (message.what) {
            case MSG_ADD_EVENT:
                onEventMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_GET_DEVICE_INFO:
                getDeviceInfoMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            default:
                super.onHandleMessage(message);
        }
    }

    @Override
    protected boolean clean() {
        return false;
    }
}
