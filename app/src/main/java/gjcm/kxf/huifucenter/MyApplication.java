package gjcm.kxf.huifucenter;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.igexin.sdk.PushManager;

import org.xutils.x;

import gjcm.kxf.getui.GeTuiIntentService;

/**
 * Created by kxf on 2016/11/28.
 */
public class MyApplication extends Application {
    //cc52219feff1c198f6353d9652c5f123

    public static boolean isprint = false, issound = false;

    @Override
    public void onCreate() {
        super.onCreate();
        setMyvalue("11111");
        x.Ext.init(this);
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=58426087," + SpeechConstant.FORCE_LOGIN + "=true");
        PushManager.getInstance().registerPushIntentService(this, GeTuiIntentService.class);
    }


    private static String myvalue;

    public static String getMyvalue() {
        return myvalue;
    }

    public static void setMyvalue(String myvalue) {
        MyApplication.myvalue = myvalue;
    }
}
