package gjcm.kxf.huifucenter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.igexin.sdk.PushManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.ex.HttpException;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;
import java.util.UUID;

import gjcm.kxf.forgetpwd.ChangePwdActiviy;
import gjcm.kxf.fragment.MainFragment;
import gjcm.kxf.tools.NetTools;
import gjcm.kxf.tools.SDCardScanner;

/**
 * Created by kxf on 2016/12/14.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sharedPreferences;
    private EditText editName, editPwd;
    private ProgressDialog dialog;
    private String tokenStr = "", userId;
    private Button btnSend;
    private TextView txtChangePwd;
    private ImageView pwdview;
    private View wyview;
    private boolean isSoftHidden = false;
    private CheckBox checkBox;
    private int versionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        getSupportActionBar().hide();
        editName = (EditText) findViewById(R.id.login_uname);
        editPwd = (EditText) findViewById(R.id.login_pwd);
        btnSend = (Button) findViewById(R.id.login_sendbtn);
        wyview = findViewById(R.id.wyview);
        btnSend.setOnClickListener(this);
        pwdview = (ImageView) findViewById(R.id.login_edit_view);
        txtChangePwd = (TextView) findViewById(R.id.login_changepwd);
        checkBox = (CheckBox) findViewById(R.id.login_check);
        pwdview.setOnClickListener(this);
        txtChangePwd.setOnClickListener(this);
        editName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isSoftHidden = false;
                return false;
            }
        });
        editPwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isSoftHidden = true;
                return false;
            }
        });

        sharedPreferences = getSharedPreferences("gjcmcenterkxf", Activity.MODE_PRIVATE);
    }

    private void getMyUUID() {
        UUID localUUID = UUID.randomUUID();
        uuid = localUUID.toString();


    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            versionCode = packageInfo.versionCode;
//            String name = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String sdpath = sharedPreferences.getString("sdpath", "");
        if (("").equals(sdpath)) {
            List<String> list = SDCardScanner.getExtSDCardPaths();
            sdpath = list.get(0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Log.d("kxflog", "----->UUID" + sdpath);
            editor.putString("sdpath", sdpath);
            editor.commit();
        }
        getMyUUID();
    }

    private void getLoginType(final String cid, final String maccode) {
        RequestParams requestParams = new RequestParams(NetTools.HOMEURL + "/main/myselfnew");
        requestParams.addBodyParameter("token", tokenStr);
        requestParams.addHeader("token", tokenStr);
        x.http().post(requestParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.i("kxflog", result);
//                if (dialog != null)
//                    dialog.dismiss();
                try {
                    JSONObject resultObj = new JSONObject(result);
                    String success = resultObj.getString("success");
                    if (success.equals("true")) {
                        JSONObject dataObj = resultObj.getJSONObject("data");
                        String Id = dataObj.optString("id");
                        String type = dataObj.optString("type");
                        String storeName = dataObj.optString("storeName");
                        String serviceName = dataObj.optString("serviceName");
                        String servicePhone = dataObj.optString("servicePhone");
                        String payType = dataObj.optString("payTypeFlag");
                        String merchantId = dataObj.optString("merchantId");
                        String aliflag = payType.substring(0, 1);
                        String wchatlag = payType.substring(1, 2);
                        String name = dataObj.optString("name");
                        String storeId = "", storeUserId = "";
                        if (("").equals(type)) {
                            storeName = dataObj.optString("merchantName");
                            name = dataObj.optString("username");
                            type = "0";
                        } else {
                            storeId = dataObj.optString("storeId");
                            storeUserId = dataObj.optString("storeUserId");
                        }
                        boolean ischecked = checkBox.isChecked();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("usertoken", tokenStr);
                        editor.putBoolean("isremerpwd", ischecked);
                        editor.putString("userId", Id);
                        editor.putString("storeName", storeName);
                        editor.putString("shouyy", name);
                        editor.putString("usertype", type);
                        editor.putString("sername", serviceName);
                        editor.putString("serphone", servicePhone);
                        editor.putString("storeId", storeId);
                        editor.putString("aliflag", aliflag);
                        editor.putString("wchatflag", wchatlag);
                        editor.putString("storeUserId", storeUserId);
                        editor.putString("cid", cid);
                        editor.putString("maccode", maccode);
                        editor.putString("merchantId", merchantId);
                        editor.putString("logid", "100021");
                        Log.e("kxflog", "Loginactity  temp--->");
                        editor.commit();
                        seetingPrintSound(cid, maccode, tokenStr);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.i("kxflog", "  /main/myselfnew onError:" + ex.getMessage());
                if (dialog != null)
                    dialog.dismiss();

            }

            @Override
            public void onCancelled(CancelledException cex) {
                if (dialog != null)
                    dialog.dismiss();
            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void seetingPrintSound(String cid, String maccode, String usertoken) {
        boolean isp = sharedPreferences.getBoolean("check2", false);
        boolean iss = sharedPreferences.getBoolean("check1", false);
        final String isprint, issount;
        isprint = isp ? "0" : "1";
        issount = iss ? "0" : "1";
        RequestParams params = new RequestParams(NetTools.HOMEURL + "/main/app/deviceinfo");
        params.setConnectTimeout(10 * 1000);
        params.addBodyParameter("cid", cid);
        params.addBodyParameter("macCode", maccode);
        params.addHeader("token", usertoken);
        params.addQueryStringParameter("isPrint", isprint);//0 -true 1 false
        params.addQueryStringParameter("isSound", issount);//0 -true 1 false
        params.addQueryStringParameter("versionCode", versionCode + "");//0 -true 1 false

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.e("kxflog", "seetingPrintSound" + result);
                try {
                    JSONObject object = new JSONObject(result);
                    boolean success = object.getBoolean("success");
                    if (success) {
                        Intent intent = new Intent(LoginActivity.this, MainFragment.class);
                        startActivity(intent);
                        finish();
                    } else {
                        if (dialog != null)
                            dialog.dismiss();
                        dialog = null;

                        object.optString("err_code");
                        String err = object.optString("err_msg");
                        Toast.makeText(LoginActivity.this, "" + err, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.i("kxflog", "seetingPrintSound onError:" + ex.getMessage());
                if (dialog != null)
                    dialog.dismiss();
                dialog = null;
                Toast.makeText(LoginActivity.this, "连接服务器异常", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

    }


    private boolean isHidden = true;
    private String deviceno, uuid, username, userpwd;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_changepwd:
                startActivity(new Intent(this, ChangePwdActiviy.class));
                break;
            case R.id.login_edit_view:
                if (isHidden) {
                    editPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    pwdview.setImageBitmap(null);
                    pwdview.setImageResource(R.mipmap.zhengyan);
                } else {
                    pwdview.setImageResource(R.mipmap.biyan);
                    editPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                isHidden = !isHidden;
                editPwd.postInvalidate();
                CharSequence charSequence = editPwd.getText();
                if (charSequence instanceof Spannable) {
                    Spannable spanText = (Spannable) charSequence;
                    Selection.setSelection(spanText, charSequence.length());
                }

                break;
            case R.id.login_sendbtn:
                username = editName.getText().toString();
                userpwd = editPwd.getText().toString();
                if (("").equals(username) || "".equals(userpwd))
                    return;
                final boolean isnet = NetTools.isNetworkConnected(this);
                if (!isnet) {
                    Toast.makeText(this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                deviceno = PushManager.getInstance().getClientid(this);
                if (dialog != null)
                    dialog.dismiss();
                dialog = null;
                dialog = ProgressDialog.show(this, "", "正在登录", false, false);
                RequestParams params = new RequestParams(NetTools.HOMEURL + "/main/app/login");
                params.setConnectTimeout(3 * 1000);
                params.addQueryStringParameter("username", username);
                params.addQueryStringParameter("password", userpwd);
                params.addQueryStringParameter("cid", deviceno);
                params.addQueryStringParameter("macCode", uuid);
                x.http().post(params, new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        //     Log.i("kxflog", result);
                        try {
                            JSONObject resultObj = new JSONObject(result);
                            String success = resultObj.optString("success");
                            if (success.equals("true")) {
                                String str = resultObj.getString("data");
                                tokenStr = str;
                                getLoginType(deviceno, uuid);
                            } else {
                                dialog.dismiss();
                                resultObj.optString("err_code");
                                String err = resultObj.optString("err_msg");
                                Toast.makeText(LoginActivity.this, "" + err, Toast.LENGTH_SHORT).show();
                            }//
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        Log.i("kxflog", " /main/app/login  onError:" + ex.getMessage());
                        if (ex instanceof HttpException) { // 网络错误
                            HttpException httpEx = (HttpException) ex;
                            httpEx.getCode();
                            httpEx.getMessage();
                            httpEx.getResult();
                        } else { // 其他错误
                        }
                        dialog.dismiss();
                        Toast.makeText(LoginActivity.this, "服务器连接错误", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(CancelledException cex) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onFinished() {

                    }
                });

                break;
        }
    }


}
