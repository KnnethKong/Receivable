package gjcm.kxf.huifucenter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import gjcm.kxf.drawview.SwitchView;
import gjcm.kxf.provide.ContentTract;
import gjcm.kxf.tools.NetTools;

/**
 * Created by kxf on 2016/12/21.
 */
public class BluetoothSeeting extends AppCompatActivity implements View.OnClickListener, SwitchView.OnStateChangedListener, CompoundButton.OnCheckedChangeListener {
    private ListView listWPD;//未配对
    private ListView listYPD;//已配对
    private TextView txtYBD, textYS, textFS;//已绑定
    private LinearLayout txtLine;//jump setting
    private RelativeLayout txtSerach;//搜索
    private SharedPreferences preferences;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter
            .getDefaultAdapter();
    private ArrayList<BluetoothDevice> unbondDevices = null; // 用于存放未配对蓝牙设备
    private ArrayList<BluetoothDevice> bondDevices = null;// 用于存放已配对蓝牙设备
    private ProgressBar progressbar;
    private TextView txtOpenClose;
    private ImageView imgshow, imgsousu;
    private SwitchView switchView;
    PowerManager powerManager = null;
    PowerManager.WakeLock wakeLock = null;
    private CheckBox checkSound, checkPrint;
    private String maccode, cid, usertoken, soundPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.bluetooth_seeting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_toobal);
        toolbar.setTitle("设置");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        powerManager = (PowerManager) this.getSystemService(this.POWER_SERVICE);
        wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
        dbAdd();
        initviewevent();
        initIntentFilter();
    }

    private int versionCode = 4;

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
    }

    public void dbAdd() {
        preferences = getSharedPreferences("gjcmcenterkxf", Activity.MODE_PRIVATE);
        String firstapp = preferences.getString("firsttime", null);
        cid = preferences.getString("cid", null);
        maccode = preferences.getString("maccode", null);
        usertoken = preferences.getString("usertoken", null);
        soundPath = preferences.getString("sdpath", "");
        if (firstapp == null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("firsttime", "s");
            editor.commit();
        }

    }

    private void initviewevent() {
        progressbar = (ProgressBar) findViewById(R.id.blue_seeting_progress);
        listWPD = (ListView) findViewById(R.id.blue_seeting_wl);
        listYPD = (ListView) findViewById(R.id.blue_seeting_yp);
        txtLine = (LinearLayout) findViewById(R.id.blue_seeting_linerlin);
        txtSerach = (RelativeLayout) findViewById(R.id.blue_seeting_sousuo);
        txtYBD = (TextView) findViewById(R.id.blue_seeting_yb);
        txtOpenClose = (TextView) findViewById(R.id.blue_seeting_openclose);
        switchView = (SwitchView) findViewById(R.id.blue_seeting_switch);
        textFS = (TextView) findViewById(R.id.blue_ftxt);
        textYS = (TextView) findViewById(R.id.blue_ystxt);
        imgshow = (ImageView) findViewById(R.id.setting_showimg);
        imgsousu = (ImageView) findViewById(R.id.blue_seeting_conimg);
        switchView.setOnStateChangedListener(this);
        txtLine.setOnClickListener(this);
        txtSerach.setOnClickListener(this);
        txtOpenClose.setOnClickListener(this);
        checkSound = (CheckBox) findViewById(R.id.blue_seeting_ck1);
        checkPrint = (CheckBox) findViewById(R.id.blue_seeting_ck2);
        checkSound.setOnCheckedChangeListener(this);
        checkPrint.setOnCheckedChangeListener(this);
        unbondDevices = new ArrayList<>();
        bondDevices = new ArrayList<>();
        String dname = preferences.getString("bluename", "");
        String badrass = preferences.getString("blueadress", "");
        String isprint = preferences.getString("isprint", "");
        if (!("").equals(badrass))
            txtYBD.setText("   " + dname);
        if (("").equals(isprint)) {
            switchView.toggleSwitch(false);
        } else if (("on").equals(isprint)) {
            switchView.toggleSwitch(true);
        } else
            switchView.toggleSwitch(false);
        boolean isp = preferences.getBoolean("check2", false);
        boolean iss = preferences.getBoolean("check1", false);
        checkPrint.setChecked(isp);
        checkSound.setChecked(iss);
        Log.d("kxflog", "BluetoothSeeting    ->" + "print:" + isp + "   sound:" + iss);


    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.blue_seeting_linerlin:
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                break;
            case R.id.blue_seeting_sousuo:
                boolean isopen = isOpen();
                if (isopen) {
                    imgsousu.setVisibility(View.GONE);
                    searchDevices();
                    progressbar.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(this, "请打开蓝牙后再点击", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.blue_addf:
                String s = textFS.getText().toString();
                int add = Integer.valueOf(s);
                add = add + 1;
                textYS.setText(add);
                break;
            case R.id.blue_addys:
                String fs = textFS.getText().toString();
                int addfs = Integer.valueOf(fs);
                addfs = addfs + 1;
                textYS.setText(addfs);
                break;
            case R.id.blue_jianys:
                String js = textFS.getText().toString();
                int jian = Integer.valueOf(js);
                jian = jian - 1;
                if (jian > 0)
                    textYS.setText(jian);
                else
                    textYS.setText("1");
                break;
            case R.id.blue_jianf:
                String fsjian = textFS.getText().toString();
                int jianfs = Integer.valueOf(fsjian);
                jianfs = jianfs - 1;
                if (jianfs > 0)
                    textFS.setText(jianfs);
                else
                    textFS.setText("1");
                break;
            case R.id.blue_seeting_openclose:
                break;
            case R.id.toolbar_back:
                this.finish();
                break;
        }

    }

    private void initIntentFilter() {
        // 设置广播信息过滤
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        // 注册广播接收器，接收并处理搜索结果
        registerReceiver(receiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        wakeLock.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            this.unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 添加未绑定蓝牙设备到ListView
     */
    private void addUnbondDevicesToListView() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        int count = this.unbondDevices.size();
        System.out.println("未绑定设备数量：" + count);
        for (int i = 0; i < count; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("deviceName", this.unbondDevices.get(i).getName());
            data.add(map);// 把item项的数据加到data中
        }
        String[] from = {"deviceName"};
        int[] to = {R.id.undevice_name};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, data,
                R.layout.unbonddevice_item, from, to);
        listWPD.setAdapter(simpleAdapter);
        listWPD.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int arg2, long arg3) {
                try {
                    Method createBondMethod = BluetoothDevice.class
                            .getMethod("createBond");
                    createBondMethod
                            .invoke(unbondDevices.get(arg2));
                    // 将绑定好的设备添加的已绑定list集合
                    bondDevices.add(unbondDevices.get(arg2));
                    // 将绑定好的设备从未绑定list集合中移除
                    unbondDevices.remove(arg2);
                    addBondDevicesToListView();
                    addUnbondDevicesToListView();
                } catch (Exception e) {
                    Toast.makeText(BluetoothSeeting.this, "配对失败！", Toast.LENGTH_SHORT)
                            .show();
                }

            }
        });
    }

    /**
     * 添加已绑定蓝牙设备到ListView
     */
    private void addBondDevicesToListView() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        int count = this.bondDevices.size();
        System.out.println("已绑定设备数量：" + count);
        for (int i = 0; i < count; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("deviceName", this.bondDevices.get(i).getName());
            data.add(map);// 把item项的数据加到data中
        }
        String[] from = {"deviceName"};
        int[] to = {R.id.device_name};
        SimpleAdapter simpleAdapter = new SimpleAdapter(BluetoothSeeting.this, data,
                R.layout.bonddevice_item, from, to);
        listYPD.setAdapter(simpleAdapter);
        listYPD.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> paramAnonymousAdapterView, View paramAnonymousView, int paramAnonymousInt, long paramAnonymousLong) {
                if (dialog != null)
                    dialog.dismiss();
                dialog = ProgressDialog.show(BluetoothSeeting.this, "", "正在配置打印机", false, false);
                BluetoothDevice device = bondDevices.get(paramAnonymousInt);
                SharedPreferences.Editor edit = preferences.edit();
                String address = device.getAddress();
                String name = device.getName();
                edit.putString("blueadress", address);
                edit.putString("bluename", name);
                txtYBD.setText("  " + name);
                Log.e("kxflog", address + name);
                edit.commit();
                imgshow.setVisibility(View.VISIBLE);
                Cursor cursor = getContentResolver().query(ContentTract.MyConEntry.CONTENT_URI, new String[]{ContentTract.MyConEntry._ID}, "_ID = ?", new String[]{"33"}, null);
                if (cursor.moveToLast()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(ContentTract.MyConEntry._ID, 33);
                    contentValues.put(ContentTract.MyConEntry.COLUMN_NAME, name);
                    contentValues.put(ContentTract.MyConEntry.COLUMN_SDRESS, soundPath);
                    contentValues.put(ContentTract.MyConEntry.COLUMN_ADRESS, address);
                    getContentResolver().update(ContentTract.MyConEntry.CONTENT_URI, contentValues, "_ID = ?", new String[]{"33"});
                } else {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(ContentTract.MyConEntry._ID, 33);
                    contentValues.put(ContentTract.MyConEntry.COLUMN_NAME, name);
                    contentValues.put(ContentTract.MyConEntry.COLUMN_SDRESS, soundPath);
                    contentValues.put(ContentTract.MyConEntry.COLUMN_ADRESS, address);
                    getContentResolver().insert(ContentTract.MyConEntry.CONTENT_URI, contentValues);
                }
                cursor.close();
                dialog.dismiss();
            }
        });

    }

    /**
     * 打开蓝牙
     */
    public void openBluetooth(Activity activity) {
        Intent enableBtIntent = new Intent(
                BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, 1);

    }

    /**
     * 关闭蓝牙
     */
    public void closeBluetooth() {
        this.bluetoothAdapter.disable();
    }

    /**
     * 判断蓝牙是否打开
     *
     * @return boolean
     */
    public boolean isOpen() {
        return this.bluetoothAdapter.isEnabled();

    }

    /**
     * 搜索蓝牙设备`
     */
    public void searchDevices() {
        this.bondDevices.clear();
        this.unbondDevices.clear();
        // 寻找蓝牙设备，android会将查找到的设备以广播形式发出去
        this.bluetoothAdapter.startDiscovery();
    }

    /**
     * 添加未绑定蓝牙设备到list集合
     *
     * @param device
     */
    public void addUnbondDevices(BluetoothDevice device) {
        System.out.println("未绑定设备名称：" + device.getName());
        if (!this.unbondDevices.contains(device)) {
            this.unbondDevices.add(device);
        }
    }

    /**
     * 添加已绑定蓝牙设备到list集合
     *
     * @param device
     */
    public void addBandDevices(BluetoothDevice device) {
        System.out.println("已绑定设备名称：" + device.getName());
        if (!this.bondDevices.contains(device)) {
            this.bondDevices.add(device);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        wakeLock.acquire();

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        ProgressDialog progressDialog = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.e("kxflog", "BroadcastReceiver" + device.getName() + device.getAddress());
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    addBandDevices(device);
                } else {
                    addUnbondDevices(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                System.out.println("设备搜索完毕");
                progressbar.setVisibility(View.INVISIBLE);
                addUnbondDevicesToListView();
                addBondDevicesToListView();
                context.unregisterReceiver(receiver);
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    txtOpenClose.setText("关闭蓝牙");
                    txtSerach.setEnabled(true);
                    listYPD.setEnabled(true);
                    listWPD.setEnabled(true);
                } else if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {//10
                    txtOpenClose.setText("打开蓝牙");
                    txtSerach.setEnabled(false);
                    listYPD.setEnabled(false);
                    listWPD.setEnabled(false);
                }
            }

        }

    };


    @Override
    public void toggleToOn(SwitchView view) {
        view.toggleSwitch(true);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("isprint", "on");
        edit.commit();
    }

    @Override
    public void toggleToOff(SwitchView view) {
        view.toggleSwitch(false);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString("isprint", "off");
        edit.commit();

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.blue_seeting_ck1) {


        }
        if (compoundButton.getId() == R.id.blue_seeting_ck2) {

        }
        seetingPrintSound();
    }

    private ProgressDialog dialog;

    private void seetingPrintSound() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        dialog = ProgressDialog.show(this, "", "正在提交", false, false);
        final String isprint, issount;
        isprint = checkPrint.isChecked() ? "0" : "1";
        issount = checkSound.isChecked() ? "0" : "1";
        RequestParams params = new RequestParams(NetTools.HOMEURL + "/main/app/deviceinfo");
        params.setConnectTimeout(3 * 1000);
        params.addBodyParameter("cid", cid);
        params.addBodyParameter("macCode", maccode);
        Log.e("kxflog", "uuid--->" + maccode);
        params.addHeader("token", usertoken);
        params.addQueryStringParameter("isPrint", isprint);//0 -true 1 false
        params.addQueryStringParameter("isSound", issount);//0 -true 1 false
        params.addQueryStringParameter("versionCode", versionCode + "");//0 -true 1 false

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                dialog.dismiss();
                Log.e("kxflog", result);

                try {
                    JSONObject object = new JSONObject(result);
                    boolean success = object.getBoolean("success");
                    if (success) {
                        SharedPreferences.Editor editor = preferences.edit();
                        boolean sound = issount.equals("0") ? true : false;
                        boolean print = isprint.equals("0") ? true : false;
                        editor.putBoolean("check1", sound);
                        editor.putBoolean("check2", print);
                        editor.commit();
                    } else {
                        object.optString("err_code");
                        String err = object.optString("err_msg");
                        Toast.makeText(BluetoothSeeting.this, "" + err, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (dialog != null)
                    dialog.dismiss();
                Toast.makeText(BluetoothSeeting.this, "连接服务器异常", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

    }
}

