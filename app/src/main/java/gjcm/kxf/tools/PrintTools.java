package gjcm.kxf.tools;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import gjcm.kxf.entity.MerchantDetail;

/**
 * Created by kxf on 2016/12/21.
 */
public class PrintTools {

    private Context context = null;
    private String deviceAddress = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter
            .getDefaultAdapter();
    private BluetoothDevice device = null;
    private static BluetoothSocket bluetoothSocket = null;
    private static OutputStream outputStream = null;
    private static final UUID uuid = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean isConnection = false;
    final String[] items = {"复位打印机", "标准ASCII字体", "压缩ASCII字体", "字体不放大",
            "宽高加倍", "取消加粗模式", "选择加粗模式", "取消倒置打印", "选择倒置打印", "取消黑白反显", "选择黑白反显",
            "取消顺时针旋转90°", "选择顺时针旋转90°"};
    final byte[][] byteCommands = {{0x1b, 0x40},// 复位打印机
            {0x1b, 0x4d, 0x00},// 标准ASCII字体
            {0x1b, 0x4d, 0x01},// 压缩ASCII字体
            {0x1d, 0x21, 0x00},// 字体不放大
            {0x1d, 0x21, 0x11},// 宽高加倍
            {0x1b, 0x45, 0x00},// 取消加粗模式
            {0x1b, 0x45, 0x01},// 选择加粗模式
            {0x1b, 0x7b, 0x00},// 取消倒置打印
            {0x1b, 0x7b, 0x01},// 选择倒置打印
            {0x1d, 0x42, 0x00},// 取消黑白反显
            {0x1d, 0x42, 0x01},// 选择黑白反显
            {0x1b, 0x56, 0x00},// 取消顺时针旋转90°
            {0x1b, 0x56, 0x01},// 选择顺时针旋转90°
    };

    public PrintTools(Context context, String deviceAddress) {
        this.context = context;
        this.deviceAddress = deviceAddress;
        this.device = this.bluetoothAdapter.getRemoteDevice(this.deviceAddress);

    }

    /**
     * 获取设备名称
     *
     * @return String
     */
    public String getDeviceName() {
        return this.device.getName();
    }

    /**
     * 连接蓝牙设备
     */
    public boolean connect() {
        if (!this.isConnection) {
            try {
                bluetoothSocket = this.device
                        .createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();
                this.isConnection = true;
                if (this.bluetoothAdapter.isDiscovering()) {
                    System.out.println("关闭适配器！");
                    this.bluetoothAdapter.isDiscovering();
                }
            } catch (Exception e) {
                Log.i("kxflog", "连接失败！");
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * 断开蓝牙设备连接
     */
    public void disconnect() {
        System.out.println("断开蓝牙设备连接");
        try {
            bluetoothSocket.close();
            if (outputStream != null)
                outputStream.close();
            outputStream = null;
            Log.i("kxflog", "disconnect suc");
        } catch (IOException e) {
            Log.i("kxflog", "disconnect :" + e.getMessage());
        }
//        if (bluetoothSocket != null)
//            bluetoothSocket = null;
//        if (outputStream != null)
//            outputStream = null;
//        Log.i("kxflog", "bluetoothSocket is null:" + (null == bluetoothSocket) + "  bluetoothSocketisConnected: " + bluetoothSocket.isConnected());
//        Log.i("kxflog", "outputStream is null:" + (null == outputStream));
    }

    /**
     * 选择指令
     */
    public void selectCommand() {
        new AlertDialog.Builder(context).setTitle("请选择指令")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isConnection) {
                            try {
                                outputStream.write(byteCommands[which]);

                            } catch (IOException e) {
                                Toast.makeText(context, "设置指令失败！",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "设备未连接，请重新连接！",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).create().show();
    }

    /**
     * 发送数据
     */
    public void send(String sendData) {
        if (this.isConnection) {
            System.out.println("开始打印！！");
            try {
                byte[] data = sendData.getBytes("gbk");
                outputStream.write(data, 0, data.length);
                outputStream.flush();
            } catch (IOException e) {
                Toast.makeText(this.context, "发送失败！", Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Toast.makeText(this.context, "设备未连接，请重新连接！", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void printBitmap(Bitmap bitmap) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 指定调整后的宽度和高度
        int newWidth = 240;
        int newHeight = 75;
        Bitmap targetBmp = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Canvas targetCanvas = new Canvas(targetBmp);
        targetCanvas.drawColor(0xffffffff);
        targetCanvas.drawBitmap(bitmap, new Rect(0, 0, width, height), new Rect(0, 0, newWidth, newHeight), null);
        int ww = newWidth;
        int h = newHeight;
        int w = (ww - 1) / 8 + 1;
        byte[] data = new byte[h * w + 8];
        data[0] = 0x1D;
        data[1] = 0x76;
        data[2] = 0x30;
        data[3] = 0x00;
        data[4] = (byte) w;// xL
        data[5] = (byte) (w >> 8);// xH
        data[6] = (byte) h;
        data[7] = (byte) (h >> 8);
        int k = targetBmp.getWidth() * targetBmp.getHeight();
        int[] pixels = new int[k];
        targetBmp.getPixels(pixels, 0, targetBmp.getWidth(), 0, 0, targetBmp.getWidth(), targetBmp.getHeight());
        int j = 7;
        int index = 8;
        for (int i = 0; i < pixels.length; i++) {
            int clr = pixels[i];
            int red = (clr & 0x00ff0000) >> 16;
            int green = (clr & 0x0000ff00) >> 8;
            int blue = clr & 0x000000ff;
            if (j == -1) {
                j = 7;
                index++;
            }
            data[index] = (byte) (data[index] | (RGB2Gray(red, green, blue) << j));
            j--;
        }
        try {
            buffer.write(data);
            outputStream.write(buffer.toByteArray());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void txtcom(Bitmap bitmap, String... s) {
        Log.i("kxflog", "txtcom 1");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        // 获取这个图片的宽和高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 指定调整后的宽度和高度
        int newWidth = 240;
        int newHeight = 75;
        Bitmap targetBmp = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Canvas targetCanvas = new Canvas(targetBmp);
        targetCanvas.drawColor(0xffffffff);
        targetCanvas.drawBitmap(bitmap, new Rect(0, 0, width, height), new Rect(0, 0, newWidth, newHeight), null);
        int ww = newWidth;
        int h = newHeight;
        int w = (ww - 1) / 8 + 1;
        byte[] data = new byte[h * w + 8];
        data[0] = 0x1D;
        data[1] = 0x76;
        data[2] = 0x30;
        data[3] = 0x00;
        data[4] = (byte) w;// xL
        data[5] = (byte) (w >> 8);// xH
        data[6] = (byte) h;
        data[7] = (byte) (h >> 8);
        int k = targetBmp.getWidth() * targetBmp.getHeight();
        int[] pixels = new int[k];
        targetBmp.getPixels(pixels, 0, targetBmp.getWidth(), 0, 0, targetBmp.getWidth(), targetBmp.getHeight());
        int j = 7;
        int index = 8;
        for (int i = 0; i < pixels.length; i++) {
            int clr = pixels[i];
            int red = (clr & 0x00ff0000) >> 16;
            int green = (clr & 0x0000ff00) >> 8;
            int blue = clr & 0x000000ff;
            if (j == -1) {
                j = 7;
                index++;
            }
            data[index] = (byte) (data[index] | (RGB2Gray(red, green, blue) << j));
            j--;
        }

        String orderNumberStr = s[1];
        byte[] n1 = new byte[]{29, 104, 55};
        byte[] n2 = new byte[]{29, 119, 2};
        byte[] n3 = new byte[orderNumberStr.length()];
        int i = 0;
        while (i * 2 < orderNumberStr.length()) {
            n3[i] = Byte.valueOf(Byte.parseByte(orderNumberStr.substring(i * 2, i * 2 + 2))).byteValue();
            i += 1;
        }
        byte[] n5 = new byte[]{29, 107, 73, 14, 123, 67};
        byte[] bold = new byte[]{0x1B, 0x45, 0x01}; // 加粗
        byte[] nobold = new byte[]{0x1B, 0x45, 0x00}; // 取消加粗
        byte[] nobig = new byte[]{27, 33, 0}; //
        byte[] big = new byte[]{27, 33, 16, 1}; //0,16,32,48
        byte[] init = new byte[]{0x1B, 0x40};
        byte[] duiqi = new byte[]{0x20, 0x0A, 0x1B, 0x61, 0x00}; //  0-左对齐；1-居中对齐；2-右对齐
        byte[] youa = new byte[]{0x20, 0x0A, 0x1B, 0x61, 0x02};
        byte[] zhong = new byte[]{0x20, 0x0A, 0x1B, 0x61, 0x01};
        String blin = "------------------------------\n";
        String temtype = s[3];
        String temtypeyh = s[11];
        String temtsjyh = s[5];//商家优惠
        String note1 = "订单总金额          " + s[0] + "\n";//8
        String note2 = "不可打折金额        " + s[10] + "\n";//4
        String note4 = "商户实收           ";
        String shanghushishou = s[12] + "\n";
        String sjyh = "商家优惠             " + temtsjyh + "\n";//商家优惠 72
        String zhifutype = "支付方式             " + temtype + "\n";// 5
        String note5 = temtype + "优惠           " + temtypeyh + "\n";// 5
        String note6 = "开票金额(用户实付)  ";
        String kaipiaojine = s[4] + "\n";
        String note10 = "退款码\n";
        String note7 = "商户订单号 \n" + s[1] + "\n";
        String note8 = "商户名称   " + s[6] + "\n";
        String note11 = "操作员     " + s[7] + "\n";
        String note9 = "支付时间 " + s[9] + "\n";
        try {
            // buffer = new ByteArrayOutputStream();

            outputStream.write(new byte[]{0x1b, 0x40});//初始化
            outputStream.flush();
//            outputStream.write(new byte[]{0x1b, 0x61, 0x00});//居左
//            outputStream.flush();
//            outputStream.write(init);
//            outputStream.flush();
            outputStream.write(zhong);
            outputStream.flush();
            //outputStream.flush();
            outputStream.write(data);
            Log.i("kxflog", "txtcom 2");
            outputStream.flush();
            outputStream.write(duiqi);
            outputStream.flush();
            outputStream.write(blin.getBytes("gbk"));
            outputStream.flush();
            outputStream.write(note1.getBytes("gbk"));
            outputStream.flush();
            outputStream.write(note2.getBytes("gbk"));
            outputStream.flush();
            outputStream.write(big);
            outputStream.flush();
            outputStream.write(note4.getBytes("gbk"));
            outputStream.flush();
            outputStream.write(bold);
            outputStream.flush();
            outputStream.write(shanghushishou.getBytes("gbk"));
            outputStream.flush();
            outputStream.write(nobig);
            outputStream.flush();
            outputStream.write(nobold);
            outputStream.flush();
            outputStream.write(blin.getBytes("gbk"));
            Log.i("kxflog", "txtcom 4");
            outputStream.flush();
            outputStream.write(zhifutype.getBytes("gbk"));
            outputStream.flush();
            if (!temtsjyh.equals("0.00")) {
                outputStream.write(sjyh.getBytes("gbk"));
                outputStream.flush();
            }
            if (!temtypeyh.equals("0.00")) {
                outputStream.write(note5.getBytes("gbk"));
                outputStream.flush();
            }
            outputStream.write(big);
            outputStream.flush();
            outputStream.write(note6.getBytes("gbk"));
            outputStream.flush();
            outputStream.write(bold);
            outputStream.flush();
            outputStream.write(kaipiaojine.getBytes("gbk"));
            outputStream.flush();
            outputStream.write(nobig);
            outputStream.flush();
            outputStream.write(nobold);
            outputStream.flush();
            outputStream.write(blin.getBytes("gbk"));
            outputStream.flush();
            outputStream.write(note7.getBytes("gbk"));
            outputStream.flush();
            outputStream.write(note10.getBytes("gbk"));
            outputStream.flush();
            outputStream.write(n1);
            outputStream.flush();
            outputStream.write(n2);
            outputStream.flush();
            outputStream.write(n5);
            outputStream.flush();
            outputStream.write(n3);
            outputStream.flush();
            outputStream.write(note8.getBytes("gbk"));
            outputStream.flush();
            outputStream.write(note11.getBytes("gbk"));
            outputStream.flush();
            outputStream.write(blin.getBytes("gbk"));
            outputStream.flush();
            outputStream.write(note9.getBytes("gbk"));
            outputStream.flush();
            outputStream.write(blin.getBytes("gbk"));
            outputStream.flush();
            //outputStream.write(buffer.toByteArray());Log.i("kxflog", "txtcom 5");
            outputStream.write("\n\n".getBytes("gbk"));
            outputStream.flush();
            Log.i("kxflog", "txtcom 6");
            //disconnect();
//            outputStream.write(buffer.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private byte RGB2Gray(int r, int g, int b) {
        return (int) (0.29900 * r + 0.58700 * g + 0.11400 * b) < 150 ? (byte) 1 : (byte) 0;
    }

    //String paramString1, String paramString2, String paramString3, String paramString4, String paramString5
    public void printRefoundMonery(String mendian, String shouyy, String orderNumberStr, String orderAm, String success, String ztuikuan, String youhui, String paytime) {
        if (this.isConnection) {
            try {
                outputStream.write(new byte[]{0x1b, 0x40});//初始化
                outputStream.flush();
                outputStream.write(new byte[]{0x1b, 0x61, 0x00});//居左
                outputStream.flush();
                ztuikuan = "退款金额：" + ztuikuan + "\n";
                orderNumberStr = "订单编号：\n" + orderNumberStr + "\n";
                orderAm = "订单金额：" + orderAm + "\n";
                mendian = "门店名：" + mendian + "\n";
                String fuk = "********** 退款凭证 **********\n\n";
                shouyy = "收银员：" + shouyy + "\n";
                success = "退款状态：" + success + "\n";
                paytime = "支付时间：" + paytime + "\n\n";
//                undis = "不参加优惠金额：" + undis + "\n";
                outputStream.write(fuk.getBytes("gbk"));
                outputStream.flush();

                outputStream.write(mendian.getBytes("gbk"));
                outputStream.flush();

                outputStream.write(shouyy.getBytes("gbk"));
                outputStream.flush();

                outputStream.write(orderNumberStr.getBytes("gbk"));
                outputStream.flush();

                outputStream.write(success.getBytes("gbk"));
                outputStream.flush();

                outputStream.write(orderAm.getBytes("gbk"));
                outputStream.flush();

                if ("0.00".equals(youhui) || "0.0".equals(youhui) || "0".equals(youhui)) {
                } else {
                    youhui = "优惠金额：" + youhui + "\n";
                    outputStream.write(youhui.getBytes("gbk"));
                    outputStream.flush();

                }
                outputStream.write(ztuikuan.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(paytime.getBytes("gbk"));
                outputStream.flush();
                String note = "\n\n\n";
                outputStream.write(note.getBytes("gbk"));
                outputStream.flush();
            } catch (Exception e) {
                Log.e("kxflog", e.getMessage());
                return;
            }
            disconnect();

        }
    }

    public void selectCommand(byte[] command) {
        try {
            outputStream.write(command);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void printDeatail(String orderAm, String orderNumberStr, String note, String payType, String realAm, String youhui, String mendian, String shouyy, String success, String paytime, String undis, String typeyh, String shanghushishou, String tuiam) {
        if (this.isConnection) {
            try {
                outputStream.write(new byte[]{0x1b, 0x40});//初始化
                outputStream.flush();

                outputStream.write(new byte[]{0x1b, 0x61, 0x00});//居左
                outputStream.flush();

                String fuk = "\n********** 订单凭证 **********\n\n";
                outputStream.write(fuk.getBytes("gbk"));
                outputStream.flush();

                String s1 = payType;
                payType = "支付方式：" + payType + "\n";
                realAm = "用户实付：" + realAm + "\n";
                String orderNumberStrtw = "商家订单号：\n" + orderNumberStr + "\n";
                orderAm = "订单金额：" + orderAm + "\n";
                mendian = "门店名：" + mendian + "\n";
                shouyy = "操作员：" + shouyy + "\n";
                success = "支付状态：" + success + "\n";
                note = "备注：" + note + "\n";
                paytime = "支付时间：" + paytime + "\n";
                outputStream.write(mendian.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(shouyy.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(orderNumberStrtw.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(success.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(payType.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(orderAm.getBytes("gbk"));
                outputStream.flush();
                if ("0.00".equals(youhui) || "0.0".equals(youhui) || "0".equals(youhui)) {
                } else {
                    youhui = "商家优惠：" + youhui + "\n";
                    outputStream.write(youhui.getBytes("gbk"));
                    outputStream.flush();
                }
                if ("0.00".equals(typeyh) || "0.0".equals(typeyh) || "0".equals(typeyh)) {
                } else {
                    s1 = s1 + "优惠：" + typeyh + "\n";
                    outputStream.write(s1.getBytes("gbk"));
                    outputStream.flush();
                }
                shanghushishou = "商户实收：" + shanghushishou + "\n";
                outputStream.write(shanghushishou.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(realAm.getBytes("gbk"));
                outputStream.flush();
                if (!tuiam.equals("0")) {
                    String bufne = "部分退款金额：" + tuiam + "\n";
                    outputStream.write(bufne.getBytes("gbk"));
                    outputStream.flush();
                }
                if ("0.00".equals(undis) || "0.0".equals(undis) || "0".equals(undis)) {
                } else {
                    undis = "不打折金额：" + undis + "\n";
                    outputStream.write(undis.getBytes("gbk"));
                    outputStream.flush();
                }
                outputStream.write(paytime.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(note.getBytes("gbk"));
                outputStream.flush();
                outputStream.write("\n\n\n".getBytes("gbk"));
                // outputStream.write(buffer.toByteArray());
                outputStream.flush();
            } catch (Exception e) {
                Log.e("kxflog", e.getMessage());
                return;
            }
            disconnect();

        }
    }

    //扫码打印
    public void printScandPay(Object... s) {
        if (connect()) {
            String title = "\n********** 扫码订单 **********\n\n";
            try {
                byte[] store = ("门店名: " + s[0] + "\n").getBytes("gbk");
                byte[] ordernum = ("订单号:" + s[1] + "\n").getBytes("gbk");
                byte[] orderway = ("支付方式:  " + s[2] + "\n").getBytes("gbk");
                byte[] orederam = ("订单金额:  " + s[3] + "\n").getBytes("gbk");
                byte[] orderreal = ("用户实付:  " + s[4] + "\n").getBytes("gbk");
                byte[] paystatus = ("支付状态:  " + s[6] + "\n").getBytes("gbk");
                byte[] paytime = ("支付时间:  " + s[7] + "\n").getBytes("gbk");
                byte[] big = new byte[]{27, 33, 16, 1}; //0,16,32,48
                String shishou = "￥" + s[5].toString();
                outputStream.write(new byte[]{0x1b, 0x40});//初始化
                outputStream.flush();
                outputStream.write(new byte[]{0x1b, 0x61, 0x00});//居左
                outputStream.flush();
                outputStream.write(title.getBytes("gbk"));
                outputStream.flush();

                outputStream.write(store);
                outputStream.flush();

                outputStream.write(orderway);
                outputStream.flush();

                outputStream.write(orederam);
                outputStream.flush();

                outputStream.write(orderreal);
                outputStream.flush();
                outputStream.flush();
                outputStream.write(big);
                outputStream.flush();
                outputStream.write("商户实收:".getBytes("gbk"));
                outputStream.flush();
                byte[] zhong = new byte[]{0x20, 0x0A, 0x1B, 0x61, 0x01};
                outputStream.write(zhong);
                outputStream.write(new byte[]{27, 33, 16, 1});
                outputStream.write(shishou.getBytes("gbk"));
                outputStream.write("\n".getBytes("gbk"));
                outputStream.write("\n".getBytes("gbk"));
                outputStream.write(new byte[]{27, 33, 0});
                outputStream.write(new byte[]{0x1b, 0x61, 0x00});
                outputStream.write(paystatus);
                outputStream.flush();
                double merchantw = (double) s[8];
                if (merchantw > 0.00) {
                    byte[] merchanyh = ("商家优惠:" + merchantw + "\n").getBytes("gbk");
                    outputStream.write(merchanyh);
                    outputStream.flush();
                }
                double paytw = (double) s[9];
                if (paytw > 0.00) {
                    byte[] payyh = (s[2] + "优惠:" + paytw + "\n").getBytes("gbk");
                    outputStream.write(payyh);
                    outputStream.flush();
                }
                outputStream.write(ordernum);
                outputStream.flush();
                outputStream.write(paytime);
                outputStream.flush();
                outputStream.write("\n\n\n\n".getBytes("gbk"));
                outputStream.flush();
            } catch (Exception e) {
                Log.e("kxflog", e.getMessage());
            }
            disconnect();
        }
    }

    public void writeString(String str) {

        try {
            outputStream.write(str.getBytes("gbk"));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeByte(byte[] by) {
        try {
            outputStream.write(by);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printScale(String mendian, String dianyuan, String totalAmount, String totalOrderCount, String refundAmount, String realPayAmount, String discountAmount, String merchantTotalAmount, String cardTotalAmount, String refundCount, String serviceAmount, String beginTime, String endTime,
                           String wxPaySum, String wxRefundSum, String wxPayCount, String AliPaySum, String AliPayRefundSum, String aliPayCount, String wxshss, String zfbshss) {
        if (isConnection) {
            try {
                byte[] initprint = new byte[]{27, 64};//初始化
                byte[] leftcenter = new byte[]{27, 97, 0};//靠左
                byte[] newline = new byte[]{10};//换行
                totalAmount = "总金额:" + totalAmount;
                mendian = "门店:" + mendian;
                dianyuan = "操作员:" + dianyuan;
                totalOrderCount = "订单笔数:" + totalOrderCount;
                refundAmount = "退款金额:" + refundAmount;
                realPayAmount = "顾客实付:" + realPayAmount;
                discountAmount = "优惠金额:" + discountAmount;
                merchantTotalAmount = "商家实收:" + merchantTotalAmount;
                cardTotalAmount = "卡消费金额:" + cardTotalAmount;
                refundCount = "退款笔数:" + refundCount;
                AliPaySum = "支付宝支付金额:" + AliPaySum + "\n";
                zfbshss = "支付宝商户实收:" + zfbshss + "\n";
                AliPayRefundSum = "支付宝退款金额:" + AliPayRefundSum + "\n";
                aliPayCount = "支付宝支付笔数:" + aliPayCount + "\n";
                wxPaySum = "微信支付金额:" + wxPaySum + "\n";
                wxshss = "微信商户实收:" + wxshss + "\n";
                wxRefundSum = "微信退款金额:" + wxRefundSum + "\n";
                wxPayCount = "微信支付笔数:" + wxPayCount + "\n";
                beginTime = "开始时间:" + beginTime;
                endTime = "结束时间:" + endTime + "\n\n";
                String title = "**********   结算  **********\n";
                outputStream.write(new byte[]{0x1b, 0x40});//初始化
                outputStream.flush();
                outputStream.write(new byte[]{0x1b, 0x61, 0x00});//居左
                outputStream.flush();
                outputStream.write(title.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(leftcenter);
                outputStream.flush();
                outputStream.write(mendian.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(newline);
                outputStream.flush();
                outputStream.write(dianyuan.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(newline);//////顺序
                outputStream.flush();
                outputStream.write(totalOrderCount.getBytes("gbk"));///订单数
                outputStream.flush();
                outputStream.write(newline);
                outputStream.flush();
                outputStream.write(totalAmount.getBytes("gbk"));///总金额
                outputStream.flush();
                outputStream.write(newline);
                outputStream.flush();
                outputStream.write(merchantTotalAmount.getBytes("gbk"));///商家实收
                outputStream.flush();
                outputStream.write(newline);
                outputStream.flush();
                outputStream.write(realPayAmount.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(newline);
                outputStream.flush();
                outputStream.write(discountAmount.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(newline);
                outputStream.flush();
                outputStream.write(refundCount.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(newline);
                outputStream.flush();
                outputStream.write(refundAmount.getBytes("gbk"));///
                outputStream.flush();
                outputStream.write(newline);
                outputStream.flush();
                outputStream.write(cardTotalAmount.getBytes("gbk"));
                outputStream.flush();
//                outputStream.write(newline);
//                outputStream.write(serviceAmount.getBytes("gbk"));
                outputStream.write(newline);
                outputStream.flush();
                outputStream.write(AliPaySum.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(zfbshss.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(aliPayCount.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(AliPayRefundSum.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(wxPaySum.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(wxshss.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(wxPayCount.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(wxRefundSum.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(beginTime.getBytes("gbk"));
                outputStream.flush();
                outputStream.write(newline);
                outputStream.flush();
                outputStream.write(endTime.getBytes("gbk"));
                outputStream.flush();
                outputStream.flush();
                outputStream.flush();
                outputStream.write("\n\n\n".getBytes("gbk"));
                outputStream.flush();
            } catch (Exception paramString1) {
                Log.e("kxflog", paramString1.getMessage());
                return;
            }
            disconnect();

        }

    }


    public void printList(List<MerchantDetail> merchantDetails) {
        if (isConnection) {
            byte[] initprint = new byte[]{27, 64};//初始化
            byte[] leftcenter = new byte[]{27, 97, 0};//靠左
            byte[] newline = new byte[]{10};//换行
            String title = "---------结算订单详情-------" + "\n";
            try {
                outputStream.write(new byte[]{0x1b, 0x40});//初始化
                outputStream.flush();
                outputStream.write(new byte[]{0x1b, 0x61, 0x00});//居左
                outputStream.flush();
                outputStream.write(title.getBytes("gbk"));
                outputStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < merchantDetails.size(); i++) {
                MerchantDetail merchantDetail = merchantDetails.get(i);
                String ordernumber = "订单号:" + merchantDetail.getOrderNumber() + "\n";
                String orderamount = "订单金额:" + merchantDetail.getOrderAmount() + "\n";
                String paytime = "支付时间:" + merchantDetail.getPayTime() + "\n";
                String status = "支付状态:" + merchantDetail.getStatus() + "\n";
                String type = "支付类型:" + merchantDetail.getType() + "\n";
                String refount = merchantDetail.getRefoundAmount();
                try {
                    outputStream.write(ordernumber.getBytes("gbk"));
                    outputStream.flush();

                    outputStream.write(orderamount.getBytes("gbk"));
                    outputStream.flush();

                    outputStream.write(status.getBytes("gbk"));
                    outputStream.flush();

                    outputStream.write(type.getBytes("gbk"));
                    outputStream.flush();

                    if (!("0").equals(refount)) {
                        refount = "退款金额:" + merchantDetail.getRefoundAmount() + "\n";
                        outputStream.write(refount.getBytes("gbk"));
                        outputStream.flush();

                    }
                    outputStream.write(paytime.getBytes("gbk"));
                    outputStream.flush();

                    outputStream.write("-----------------------------".getBytes("gbk"));
                    outputStream.flush();

                    outputStream.write(newline);
                    outputStream.flush();

                    String str = "\n";
                    outputStream.write(str.getBytes("gbk"));
                    outputStream.flush();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            disconnect();
        }
    }
}
//                String str = "退款码";
//                buffer.write(str.getBytes("gbk"));
//                buffer.write(new byte[]{29, 104, 55});
//                buffer.write(new byte[]{29, 119, 2});
//                byte[] heheh = new byte[orderNumberStr.length()];
//                int i = 0;
//                while (i * 2 < orderNumberStr.length()) {
//                    heheh[i] = Byte.valueOf(Byte.parseByte(orderNumberStr.substring(i * 2, i * 2 + 2))).byteValue();
//                    i += 1;
//                }
//                byte[] paramString1 = new byte[3];
//                buffer.write(paramString1);
//                buffer.write(new byte[]{29, 107, 73, 14, 123, 67});
//                buffer.write(heheh);
//                buffer.write(paramString2);