package com.cwp.cmoneycharge.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.speech.VoiceRecognitionService;
import com.cwp.chart.manager.SystemBarTintManager;
import com.cwp.cmoneycharge.Effectstype;
import com.cwp.cmoneycharge.R;
import com.cwp.cmoneycharge.api.Constant;
import com.cwp.cmoneycharge.app.CrashApplication;
import com.cwp.cmoneycharge.app.SysApplication;
import com.cwp.cmoneycharge.utils.DialogShowUtil;
import com.cwp.cmoneycharge.utils.DigitUtil;
import com.cwp.cmoneycharge.utils.KeyboardUtil;
import com.cwp.cmoneycharge.widget.NiftyDialogBuilder;
import com.cwp.pattern.activity.UnlockGesturePasswordActivity;
import com.example.testpic.activity.PublishedActivity;
import com.example.testpic.utils.Bimp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import cwp.moneycharge.dao.IncomeDAO;
import cwp.moneycharge.dao.ItypeDAO;
import cwp.moneycharge.dao.PayDAO;
import cwp.moneycharge.dao.PtypeDAO;
import cwp.moneycharge.model.Tb_income;
import cwp.moneycharge.model.Tb_pay;

/**
 * 添加收入的界面
 */
public class AddPayActivity extends Activity implements OnClickListener, RecognitionListener {
    protected static final int DATE_DIALOG_ID = 0;// 创建日期对话框常量
    static String type = "pay";
    String VoiceDefault = "";
    protected static String typemode = "add";
    EditText txtMoney, txtTime, txtAddress, txtMark;// 创建EditText对象
    Spinner spType;// 创建Spinner对象
    Button btnSaveButton;// 创建Button对象“保存”
    Button btnCancelButton;// 创建Button对象“取消”
    Button btnVoice;// 创建Button对象“语音识别”
    int userid;
    int Selection = 0;
    Bundle bundle = null;
    String[] strInfos = null;// 定义字符串数组
    String strno, strType;// 定义两个字符串变量，分别用来记录信息编号和管理类型
    private FrameLayout corporation_fl, address_fl = null;
    private RadioButton rb1 = null;
    private RadioButton rb2 = null;
    private ImageView left_back;//取消的按钮


    private Effectstype effect; // 自定义Dialog
    NiftyDialogBuilder dialogBuilder = null;
    Boolean firstin = true;

    private int mYear;// 年
    private int mMonth;// 月
    private int mDay;// 日

    private ArrayAdapter<String> adapter;
    private String[] spdata;

    String[] number = {"一", "二", "两", "三", "四", "五", "六", "七", "八", "九", "十"};
    String[] money = {"元", "块", "钱"};
    String[] money2 = {"十", "百", "千", "万", "亿"};
    String[] voice_pay = {"买", "吃"};
    String[] voice_income = {"卖", "获"};

    String[] VoiceSave = new String[6];
    static DialogShowUtil dialogShowUtil;
    PtypeDAO ptypeDAO = new PtypeDAO(AddPayActivity.this);
    ItypeDAO itypeDAO = new ItypeDAO(AddPayActivity.this);
    PayDAO payDAO = new PayDAO(AddPayActivity.this);// 创建PayDAO对象
    IncomeDAO incomeDAO = new IncomeDAO(AddPayActivity.this);// 创建IncomeDAO对象
    List<String> spdatalist, spdatalist2;
    private SystemBarTintManager mTintManager;
    private ImageView btn_loacte;
    private ImageView addphoto;
    protected String textphoto = "";
    private int incount = 0;
    private boolean keycount = true;
    private FrameLayout bottom_empty;
    private LinearLayout bottom_full;
    private KeyboardUtil keyBoard;
    //TODO 百度地图
    private LocationClient mLocationClient;
    private BDLocationListener mLocationListener = new MyLocationListener();


    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                return;
            }
            StringBuilder sb = new StringBuilder(1024);
            sb.append(location.getAddrStr());
            Log.d("adds", sb.toString());
            txtAddress.setText(sb.toString());
        }
    }

    //TODO 百度语音识别
    private static final String TAG = "Sdk2Api";
    private static final int REQUEST_UI = 1;
    public static final int STATUS_None = 0;
    public static final int STATUS_WaitingReady = 2;
    public static final int STATUS_Ready = 3;
    public static final int STATUS_Speaking = 4;
    public static final int STATUS_Recognition = 5;
    private SpeechRecognizer speechRecognizer;
    private int status = STATUS_None;
    private long speechEndTime = -1;
    private static final int EVENT_ERROR = 11;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);// 设置布局文件
        SysApplication.getInstance().addActivity(this); // 在销毁队列中添加this
        super.onStart();// 实现基类中的方法
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            findViewById(R.id.top).setVisibility(View.VISIBLE);
        }
        mTintManager = new SystemBarTintManager(this);
        mTintManager.setStatusBarTintEnabled(true);
        mTintManager.setStatusBarTintResource(R.color.statusbar_bg);

        txtMoney = (EditText) findViewById(R.id.txtMoney);// 获取金额文本框
        txtTime = (EditText) findViewById(R.id.txtTime);// 获取时间文本框
        txtAddress = (EditText) findViewById(R.id.txtAddress);// 获取地点文本框
        txtMark = (EditText) findViewById(R.id.txtMark);// 获取备注文本框
        spType = (Spinner) findViewById(R.id.spType);// 获取类别下拉列表
        btnSaveButton = (Button) findViewById(R.id.btnSave);// 获取保存按钮
        btnCancelButton = (Button) findViewById(R.id.btnCancel);// 获取取消按钮
        btnVoice = (Button) findViewById(R.id.btnVoice);// 获取语音识别按钮
        rb1 = (RadioButton) findViewById(R.id.payout_tab_rb);
        rb2 = (RadioButton) findViewById(R.id.income_tab_rb);
        left_back = (ImageView) findViewById(R.id.example_left3);
        btn_loacte = (ImageView) findViewById(R.id.btn_loacte);
        addphoto = (ImageView) findViewById(R.id.addphoto);
        bottom_empty = (FrameLayout) findViewById(R.id.bottom_empty);
        bottom_full = (LinearLayout) findViewById(R.id.bottom_full);


        //百度定位
        //定位
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(mLocationListener);
        //TODO　百度语音识别
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, new ComponentName(this, VoiceRecognitionService.class));
        speechRecognizer.setRecognitionListener(this);



        dialogShowUtil = new DialogShowUtil(this, this, VoiceSave, type, // 初始化dialog
                VoiceDefault);
        btn_loacte.setOnClickListener(this); // 定位的按钮的点击事件

        // 隐藏菜单
        bottom_empty.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                keyBoard.hideKeyboard();
                bottom_empty.setVisibility(View.GONE);
                bottom_full.setVisibility(View.VISIBLE);
            }
        });

        // 添加图片的按钮
        addphoto.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(AddPayActivity.this, PublishedActivity.class);
                intent.putExtra("cwp.id", userid);
                startActivityForResult(intent, 102);
            }
        });


        left_back.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent intent = null;
                type = "pay";
                typemode = "add";
                if (bundle.containsKey("cwp.frament3")) {
                    intent = new Intent(AddPayActivity.this, MainActivity.class);
                    intent.putExtra("cwp.Fragment", "3");// 设置传递数据
                } else {
                    intent = new Intent(AddPayActivity.this, MainActivity.class);
                }
                intent.putExtra("cwp.id", userid);
                startActivity(intent);
                finish();// 这个是关键
            }
        });

//        mRecognitionListener = new DialogRecognitionListener() { // 百度识别返回数据
//
//            @Override
//            public void onResults(Bundle results) {
//                ArrayList<String> rs = results != null ? results
//                        .getStringArrayList(RESULTS_RECOGNITION) : null;
//                if (rs != null && rs.size() > 0) {
//                    Recognition(rs.get(0)); // 把识别数据传入识别方法
//                    // Toast.makeText(AddPayActivity.this, rs.get(0),
//                    // Toast.LENGTH_SHORT).show();
//                }
//            }
//        };

        corporation_fl = (FrameLayout) findViewById(R.id.corporation_fl);
        address_fl = (FrameLayout) findViewById(R.id.address_fl);

        //checkBox按钮
        rb1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (rb1.isChecked()) { // 支出
                    type = "pay";
                } else // 收入
                {
                    type = "income";
                }
                updatetype();
            }
        });

        final Calendar c = Calendar.getInstance();// 获取当前系统日期
        mYear = c.get(Calendar.YEAR);// 获取年份
        mMonth = c.get(Calendar.MONTH);// 获取月份
        mDay = c.get(Calendar.DAY_OF_MONTH);// 获取天数



    }

    //TODO 百度地图的处理
    private void initBaiDuMap() {
        //设置定位的参数
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setAddrType("all");
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        //mLocationClient.requestLocation();

        if (mLocationClient != null && mLocationClient.isStarted()) {
            Log.d("MainActivity", "发起定位");
            mLocationClient.requestLocation();
        } else {
            Log.d("MainActivity", "LocClient is null or not started");
        }

    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void initData(int userid) { // 初始化数据
        if (typemode == "add") { // 添加模式
            if (type == "pay") { // 支出
                rb1.setChecked(true);
                spdatalist = ptypeDAO.getPtypeName(userid);
                txtMoney.setTextColor(Color.parseColor("#5ea98d"));
            } else { // 收入
                rb2.setChecked(true);
                spdatalist = itypeDAO.getItypeName(userid);
                txtMoney.setTextColor(Color.parseColor("#ffff0000"));
            }
        } else { // 修改模式
            bottom_empty.setVisibility(View.GONE);
            bottom_full.setVisibility(View.VISIBLE);
            rb1.setOnCheckedChangeListener(null);
            btnSaveButton.setText("修改"); // 替换修改按钮
            btnCancelButton.setText("删除"); // 替换删除按钮
            CharSequence textreAddres;
            String textreMark;
            if (type == "pay") { // 支出
                rb1.setChecked(true);
                rb1.setClickable(false);
                rb2.setClickable(false);
                // 选择列表初始化
                spdatalist = ptypeDAO.getPtypeName(userid);
                // 根据编号查找支出信息，并存储到Tb_pay对象中
                Tb_pay tb_pay = payDAO.find(userid, Integer.parseInt(strno));
                txtMoney.setText(tb_pay.getMoney2());// 显示金额
                txtMoney.setTextColor(Color.parseColor("#5ea98d"));
                txtTime.setText(tb_pay.getTime());// 显示时间
                Selection = tb_pay.getType() - 1;
                initphotodata(tb_pay.getPhoto());
                textreAddres = tb_pay.getAddress();
                textreMark = tb_pay.getMark();
                txtAddress.setText(textreAddres);// 显示地点
                txtMark.setText(textreMark);// 显示备注
            } else { // 收入
                // 选择列表初始化
                rb2.setChecked(true);
                rb1.setClickable(false);
                rb2.setClickable(false);
                spdatalist = itypeDAO.getItypeName(userid);
                // 根据编号查找收入信息，并存储到Tb_pay对象中
                Tb_income tb_income = incomeDAO.find(userid,
                        Integer.parseInt(strno));
                txtMoney.setText(tb_income.getMoney2());// 显示金额
                txtMoney.setTextColor(Color.parseColor("#ffff0000"));
                txtTime.setText(tb_income.getTime());// 显示时间
                Selection = tb_income.getType() - 1; // 显示类别
                initphotodata(tb_income.getPhoto());
                textreAddres = tb_income.getHandler();
                textreMark = tb_income.getMark();
                txtAddress.setText(textreAddres);// 显示地点
                txtMark.setText(textreMark);// 显示备注
            }
        }
    }

    private void initphotodata(String photo) { // 初始化图片数据
        if ((incount == 0) && (!photo.equals(""))) {
            String[] photoall = photo.split(",");
            for (int i = 0; i < photoall.length / 2; i++) {
                if (Bimp.drr.size() < 9) {
                    Bimp.drr.add(photoall[i]);
                }
            }
            for (int i = photoall.length / 2; i < photoall.length; i++) {
                if (Bimp.smdrr.size() < 9) {
                    Bimp.smdrr.add(photoall[i]);
                }
            }
            textphoto = photo;
            initphoto();
            incount++;
        }
    }

    @SuppressWarnings("deprecation")
    private void initphoto() {// 初始化图片
        try {
            if (textphoto.equals("")) {
                addphoto.setImageResource(R.drawable.addphoto_btn);
            } else if (Bimp.getbitmap(Bimp.smdrr.get(0)) == null) {
                Toast.makeText(AddPayActivity.this, "图片不存在", Toast.LENGTH_SHORT).show();
            } else {
                addphoto.setImageBitmap(Bimp.getbitmap(Bimp.smdrr.get(0)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updatetype() { // 更新类别
        initData(userid);
        spdata = spdatalist.toArray(new String[spdatalist.size()]);// 在tb_itype中按用户id读取
        adapter = new ArrayAdapter<String>(AddPayActivity.this, R.layout.spinner,
                spdata); // 动态生成收入类型列表
        spType.setAdapter(adapter);
        if (Selection > 0) {
            spType.setSelection(Selection);// 显示类别
        }
    }

    @Override
    protected void onStart() { // 复写onstart
        super.onStart();// 实现基类中的方法
        updateDisplay();// 显示当前系统时间

        Intent intentr = getIntent();
        userid = intentr.getIntExtra("cwp.id", 100000001);
        bundle = intentr.getExtras();// 获取传入的数据，并使用Bundle记录
        if (bundle.containsKey("cwp.message")) {
            strInfos = bundle.getStringArray("cwp.message");// 获取Bundle中记录的信息
            strno = strInfos[0];// 记录id
            strType = strInfos[1];// 记录类型
            typemode = "ModifyInPActivity";
            if (strType.equals("btnininfo")) { // 收入
                type = "income";
            } else {
                type = "pay";
            }
        }
        keyBoard = new KeyboardUtil(this, this, txtMoney, typemode); // 数字软键盘
        if (bundle.containsKey("cwp.voice")) { // 进来调用语音记账
            if (firstin) {
                bottom_empty.setVisibility(View.GONE);
                bottom_full.setVisibility(View.VISIBLE);
                dialogShowUtil.dialogShow("rotatebottom", "first", "", "");
                firstin = false;
            }
        }
        if (bundle.containsKey("cwp.photo")) {// 进来调用拍照
            if (firstin) {
                bottom_empty.setVisibility(View.GONE);
                bottom_full.setVisibility(View.VISIBLE);
                Intent intent = new Intent(AddPayActivity.this, PublishedActivity.class);
                intent.putExtra("cwp.id", userid);
                intent.putExtra("cwp.photo", "photo");
                startActivityForResult(intent, 102);
                firstin = false;
            }
        }
        if (bundle.containsKey("keyboard")) { // 进来显示键盘
            if (keycount) {
                InputMethodManager imm = (InputMethodManager) getSystemService(AddPayActivity.this.INPUT_METHOD_SERVICE); // 显示键盘
                imm.hideSoftInputFromWindow(txtMoney.getWindowToken(), 0); // 隐藏键盘　
                keyBoard.showKeyboard();
                keycount = false;
            }
        }
        updatetype();
        txtTime.setOnTouchListener(new OnTouchListener() { // 为时间文本框设置单击监听事件
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                showDialog(DATE_DIALOG_ID);// 显示日期选择对话框
                return false;
            }
        });

        txtMoney.setOnTouchListener(new OnTouchListener() { // 数字软键盘监听
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(AddPayActivity.this.INPUT_METHOD_SERVICE); // 显示键盘
                imm.hideSoftInputFromWindow(txtMoney.getWindowToken(), 0); // 隐藏键盘　
                keyBoard.showKeyboard();
                return false;
            }
        });

        //TODO  语音识别的按钮
        btnVoice.setOnClickListener(new OnClickListener() {// 语音识别监听
            @Override
            public void onClick(View v) {
                dialogShowUtil.dialogShow("rotatebottom", "first", "", "");
                //VoiceRecognition();

            }
        });

        // 为保存按钮设置监听事件
        btnSaveButton.setOnClickListener(new OnClickListener() {
            private String textreAddres;
            private String textreMark;

            @SuppressLint("NewApi")
            @Override
            public void onClick(View arg0) {
                textreAddres = txtAddress.getText().toString();
                textreMark = txtMark.getText().toString();
                if (textphoto == null) {
                    textphoto = "";
                }
                if (typemode == "add") { // 添加模式
                    String strMoney = txtMoney.getText().toString();// 获取金额文本框的值
                    if (type == "pay") { // 支出
                        if (!strMoney.isEmpty()) {// 判断金额不为空
                            // 创建InaccountDAO对象
                            PayDAO payDAO = new PayDAO(AddPayActivity.this);
                            // 创建Tb_inaccount对象
                            Tb_pay tb_pay = new Tb_pay(
                                    userid,
                                    payDAO.getMaxNo(userid) + 1,
                                    get2Double(strMoney),
                                    setTimeFormat(null),
                                    (spType.getSelectedItemPosition() + 1),
                                    textreAddres, textreMark, textphoto);
                            payDAO.add(tb_pay);// 添加收入信息
                            Toast.makeText(AddPayActivity.this,
                                    "〖新增收入〗数据添加成功！", Toast.LENGTH_SHORT)
                                    .show();
                            gotoback();
                        } else {
                            Toast.makeText(AddPayActivity.this, "请输入收入金额！",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else { // 收入
                        if (!strMoney.isEmpty()) {// 判断金额不为空
                            // 创建InaccountDAO对象
                            IncomeDAO incomeDAO = new IncomeDAO(
                                    AddPayActivity.this);
                            // 创建Tb_inaccount对象
                            Tb_income tb_income = new Tb_income(
                                    userid,
                                    payDAO.getMaxNo(userid) + 1,
                                    get2Double(strMoney),
                                    setTimeFormat(null),
                                    (spType.getSelectedItemPosition() + 1),
                                    // txtInhandler.getText().toString(),
                                    textreAddres, textreMark,
                                    textphoto, "支出");
                            incomeDAO.add(tb_income);// 添加收入信息
                            // 弹出信息提示
                            Toast.makeText(AddPayActivity.this,
                                    "〖新增收入〗数据添加成功！", Toast.LENGTH_SHORT)
                                    .show();
                            gotoback();
                        } else {
                            Toast.makeText(AddPayActivity.this, "请输入收入金额！",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else { // 修改模式
                    if (type == "pay") { // 支出
                        if (!txtMoney.getText().toString().isEmpty()) {// 判断金额不为空
                            Tb_pay tb_pay = new Tb_pay(); // 创建Tb_pay对象
                            tb_pay.set_id(userid); // 设置userid
                            tb_pay.setNo(Integer.parseInt(strno)); // 设置编号
                            tb_pay.setMoney(get2Double(txtMoney
                                    .getText().toString()));// 设置金额
                            tb_pay.setTime(setTimeFormat(txtTime
                                    .getText().toString()));// 设置时间
                            tb_pay.setType(spType
                                    .getSelectedItemPosition() + 1);// 设置类别
                            tb_pay.setAddress(textreAddres);// 设置地点
                            tb_pay.setMark(textreMark);// 设置备注
                            tb_pay.setPhoto(textphoto);// 设置备注
                            payDAO.update(tb_pay);// 更新支出信息
                            Toast.makeText(AddPayActivity.this, "〖数据〗修改成功！",
                                    Toast.LENGTH_SHORT).show();
                            gotoback();
                        } else {
                            Toast.makeText(AddPayActivity.this, "请输入收入金额！",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else { // 收入
                        if (!txtMoney.getText().toString().isEmpty()) {// 判断金额不为空
                            Tb_income tb_income = new Tb_income();// 创建Tb_income对象
                            tb_income.set_id(userid);// 设置编号
                            tb_income.setNo(Integer.parseInt(strno));// 设置编号
                            tb_income.setMoney(get2Double(txtMoney
                                    .getText().toString()));// 设置金额
                            tb_income.setTime(setTimeFormat(txtTime
                                    .getText().toString()));// 设置时间
                            tb_income.setType(spType
                                    .getSelectedItemPosition() + 1);// 设置类别
                            tb_income.setHandler(textreAddres);// 设置付款方
                            tb_income.setMark(textreMark);// 设置备注
                            tb_income.setPhoto(textphoto);// 设置备注
                            incomeDAO.update(tb_income);// 更新收入信息
                            Toast.makeText(AddPayActivity.this, "〖数据〗修改成功！",
                                    Toast.LENGTH_SHORT).show();
                            gotoback();
                        } else {
                            Toast.makeText(AddPayActivity.this, "请输入收入金额！",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        btnCancelButton.setOnClickListener(new OnClickListener() {// 为取消按钮设置单击监听事件
            @Override
            public void onClick(View arg0) {

                if (typemode == "add") { // 添加模式执行返回
                    txtMoney.setText("");// 设置金额文本框为空
                    txtMoney.setHint("0.00");// 为金额文本框设置提示
                    txtTime.setText("");// 设置时间文本框为空
                    txtAddress.setText("");// 设置地址文本框为空
                    txtMark.setText("");// 设置备注文本框为空
                    // txtInhandler.setText("");// 设置备注文本框为空
                    spType.setSelection(0);// 设置类别下拉列表默认选择第一项
                    gotoback();
                } else { // 修改模式执行删除
                    if (type == "pay") { // 支出
                        payDAO.detele(userid, Integer.parseInt(strno));// 根据编号删除支出信息
                        gotoback();
                    } else { // 收入
                        incomeDAO.detele(userid,
                                Integer.parseInt(strno));// 根据编号删除收入信息
                        gotoback();
                    }
                    Toast.makeText(AddPayActivity.this, "〖数据〗删除成功！",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 保留2位小数
    public static double get2Double(String strMoney) { // 处理小数点
        Double a = Double.parseDouble(strMoney);
        DecimalFormat df = new DecimalFormat("0.00");
        return new Double(df.format(a));
    }

    @Override
    protected Dialog onCreateDialog(int id)// 重写onCreateDialog方法
    {
        switch (id) {
            case DATE_DIALOG_ID:// 弹出日期选择对话框
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
                        mDay);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mYear = year;// 为年份赋值
            mMonth = monthOfYear;// 为月份赋值
            mDay = dayOfMonth;// 为天赋值
            updateDisplay();// 显示设置的日期
        }
    };

    private void updateDisplay() {
        // 显示设置的时间
        txtTime.setText(new StringBuilder().append(mYear).append("-")
                .append(mMonth + 1).append("-").append(mDay));

    }

    // 设置日期格式
    private String setTimeFormat(String newtxtTime) {
        String date;
        if (typemode == "add") {
            date = txtTime.getText().toString();
        } else {
            date = newtxtTime;
        }

        int y, m, d;
        String sm, sd;
        int i = 0, j = 0, k = 0;

        for (i = 0; i < date.length(); i++) {
            if (date.substring(i, i + 1).equals("-") && j == 0)
                j = i;
            else if (date.substring(i, i + 1).equals("-"))
                k = i;
        }
        y = Integer.valueOf(date.substring(0, j));
        m = Integer.valueOf(date.substring(j + 1, k));
        d = Integer.valueOf(date.substring(k + 1));
        if (m < 10) {
            sm = "0" + String.valueOf(m);
        } else
            sm = String.valueOf(m);
        if (d < 10) {
            sd = "0" + String.valueOf(d);
        } else
            sd = String.valueOf(d);

        return String.valueOf(y) + "-" + sm + "-" + sd;

    }

    /**
     * 调用百度语音识别
     */
    public void VoiceRecognition() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AddPayActivity.this);
        boolean api = sp.getBoolean("api", false);
        if (api) {
            switch (status) {
                case STATUS_None:
                    start();
                    status = STATUS_WaitingReady;
                    break;
                case STATUS_WaitingReady:
                    cancel();
                    status = STATUS_None;
                    break;
                case STATUS_Ready:
                    cancel();
                    status = STATUS_None;
                    break;
                case STATUS_Speaking:
                    stop();
                    status = STATUS_Recognition;
                    break;
                case STATUS_Recognition:
                    cancel();
                    status = STATUS_None;
                    break;
            }
        } else {
            start();
        }
    }

    public void VoiceSuccess() { // 识别成功录入数据
        if (DialogShowUtil.dialoggettype() != null) {
            type = DialogShowUtil.dialoggettype();
        }
        VoiceDefault = DialogShowUtil.dialogVoiceDefault();
        String textreMark = txtMark.getText().toString();

        if (typemode == "add") { // 添加模式
            if (type == "pay") { // 支出
                rb1.setChecked(true);
                // corporation_fl.setVisibility(View.GONE);
                // address_fl.setVisibility(View.VISIBLE);
                spdatalist = ptypeDAO.getPtypeName(userid);
                txtMoney.setText(VoiceSave[1]);// 显示金额
                txtMoney.setTextColor(Color.parseColor("#5ea98d"));
                if (VoiceDefault == "notype") { // 如果没有默认类别
                    spType.setSelection(Integer.parseInt(VoiceSave[5]));// 显示语音识别类别
                } else {
                    spType.setSelection(Integer.parseInt(VoiceSave[0]));// 显示类别
                }
                txtMark.setText(textreMark + " " + VoiceSave[2]);// 显示备注
            } else { // 收入
                rb2.setChecked(true);
                // corporation_fl.setVisibility(View.VISIBLE);
                // address_fl.setVisibility(View.GONE);
                spdatalist = ptypeDAO.getPtypeName(userid);
                txtMoney.setText(VoiceSave[1]);// 显示金额
                txtMoney.setTextColor(Color.parseColor("#ffff0000"));
                if (VoiceDefault == "notype") { // 如果没有默认类别
                    spType.setSelection(Integer.parseInt(VoiceSave[5]));// 显示类别
                } else {
                    spType.setSelection(Integer.parseInt(VoiceSave[4]));// 显示类别
                }
                txtMark.setText(textreMark + " " + VoiceSave[2]);// 显示备注
            }
        } else { // 修改模式
            if (type == "pay") { // 支出
                rb1.setChecked(true);
                // 选择列表初始化
                spdatalist = ptypeDAO.getPtypeName(userid);
                spdata = spdatalist.toArray(new String[spdatalist.size()]);// 在tb_itype中按用户id读取
                adapter = new ArrayAdapter<String>(AddPayActivity.this,
                        R.layout.spinner, spdata); // 动态生成收入类型列表
                spType.setAdapter(adapter);
                txtMoney.setText(VoiceSave[1]);// 显示金额
                txtMoney.setTextColor(Color.parseColor("#5ea98d"));
                if (VoiceDefault == "notype") { // 如果没有默认类别
                    spType.setSelection(Integer.parseInt(VoiceSave[5]));// 显示语音识别类别
                } else {
                    spType.setSelection(Integer.parseInt(VoiceSave[0]));// 显示类别
                }
                txtMark.setText(textreMark + " " + VoiceSave[2]);// 显示备注
            } else { // 收入
                // 选择列表初始化
                rb2.setChecked(true);
                spdatalist = itypeDAO.getItypeName(userid);
                spdata = spdatalist.toArray(new String[spdatalist.size()]);// 在tb_itype中按用户id读取
                adapter = new ArrayAdapter<String>(AddPayActivity.this,
                        R.layout.spinner, spdata); // 动态生成收入类型列表
                spType.setAdapter(adapter);
                txtMoney.setText(VoiceSave[1]);// 显示金额
                txtMoney.setTextColor(Color.parseColor("#ffff0000"));
                if (VoiceDefault == "notype") { // 如果没有默认类别
                    spType.setSelection(Integer.parseInt(VoiceSave[5]));// 显示类别
                } else {
                    spType.setSelection(Integer.parseInt(VoiceSave[4]));// 显示类别
                }
                txtMark.setText(textreMark + " " + VoiceSave[2]);// 显示备注
            }
        }
    }

    /*
     * 识别结果处理函数
     *
     * @param VoiceSave[0] 收入类别的值
     *
     * @param VoiceSave[1] 金额的值
     *
     * @param VoiceSave[3] 重复类别的值，仅用于显示提醒
     *
     * @param VoiceSave[4] 支出类别的值
     *
     * @param VoiceSave[5] "语音识别"类别的值
     */
    private void Recognition(String t) {
        int mfirst = 100, mend = 0, temp = 0;
        Boolean ismoney = false, intype = false, outtype = false;
        Boolean voice_ptype = false, voice_intype = false;
        String w = "", strmoney = "", inname = "1", outname = "2";
        spdatalist = ptypeDAO.getPtypeName(userid);
        spdatalist2 = itypeDAO.getItypeName(userid);
        VoiceSave[2] = t;
        for (int i = 0; i < spdatalist.size(); i++) { // 判断是否包含支出
            if (t.indexOf(spdatalist.get(i).toString()) > -1) {
                type = "pay";
                intype = true;
                inname = spdatalist.get(i).toString();
                VoiceSave[0] = Integer.toString(i); // VoiceSave[0]为收入类别的值
            }
        }
        for (int i = 0; i < voice_pay.length; i++) { // 判断是否包含支出的动词
            if (t.indexOf(voice_pay[i]) > -1) {
                voice_ptype = true;
            }
        }
        for (int i = 0; i < voice_income.length; i++) { // 判断是否包含支出的动词
            if (t.indexOf(voice_income[i]) > -1) {
                voice_intype = true;
            }
        }
        for (int i = 0; i < spdatalist2.size(); i++) { // 判断是否包含收入
            if (t.indexOf(spdatalist2.get(i).toString()) > -1) {
                type = "income";
                outtype = true;
                outname = spdatalist2.get(i).toString();
                VoiceSave[4] = Integer.toString(i); // VoiceSave[4]为支出类别的值
            }
        }
        for (int i = 0; i < number.length; i++) { // 判断是否包含金额，获得开头
            if (t.indexOf(number[i]) > -1) {
                temp = t.indexOf(number[i]);
                if (temp < mfirst) {
                    mfirst = temp;
                }
            }
        }
        for (int i = 0; i < money.length; i++) { // 判断是否包含金额，获得结尾
            if (t.indexOf(money[i]) > -1) {
                temp = t.indexOf(money[i]);
                if (temp > -1 && temp >= mend) {
                    mend = temp;
                }
            }
        }
        for (int i = 0; i < money2.length; i++) { // 判断是否包含金额，获得结尾
            if (t.indexOf(money2[i]) > -1) {
                temp = t.indexOf(money2[i]);
                if (temp > -1 && temp >= mend) {
                    mend = temp;
                }
                mend = mend + 1;
            }
        }
        if (!(mfirst == 100 || mend == 0)) { // 转换为阿拉伯数字
            ismoney = true;
            strmoney = t.substring(mfirst, mend);
            // 判断语句是否包含非数字
            char[] chs = strmoney.toCharArray();
            List<String> num = Arrays.asList(number);
            List<String> mon = Arrays.asList(money);
            List<String> mon2 = Arrays.asList(money2);
            for (int l = 0; l < chs.length; l++)
                if (!num.contains(String.valueOf(chs[l])))
                    if (!mon.contains(String.valueOf(chs[l])))
                        if (!mon2.contains(String.valueOf(chs[l])))
                            ismoney = false;
            if (ismoney) {
                DigitUtil Util = new DigitUtil();
                VoiceSave[1] = Integer.toString(Util.parse(strmoney)); // 调用工具类处理汉字的金额
            }
        }
        if (intype && outtype) { // 如果同时含有收入/支出的类别
            if (outname.equals(inname)) {
                if (ismoney) {
                    if (voice_intype) {
                        type = "income";
                        dialogShowUtil.dialogShow("rotatebottom", "OK", t, w);
                    } else if (voice_ptype) {
                        type = "pay";
                        dialogShowUtil.dialogShow("rotatebottom", "OK", t, w);
                    } else {
                        VoiceSave[3] = outname; // VoiceSave[3]为重复类别的值，仅用于显示提醒
                        dialogShowUtil.dialogShow("shake", "judge", t, w); // 如果含有金额
                    }
                } else {
                    w = "提示：\n你的话中没有包含消费或开支的<金额>\n";
                    dialogShowUtil.dialogShow("shake", "wrong", t, w);
                }
            } else {
                w = "**提示：\n一次只能记录一条记录哦\n"; // 如果含有收入并且支出的类别
                dialogShowUtil.dialogShow("shake", "wrong", t, w);
            }
        } else {
            if (!((intype || outtype) || ismoney)) { // 如果不含金额
                w = "**提示：\n你的话中没有包含<类别>（" + listToString(spdatalist, '，')
                        + "，" + listToString(spdatalist2, '，')
                        + "）\n\n**提示：\n你的话中没有包含消费或开支的<金额>";
                dialogShowUtil.dialogShow("shake", "wrong", t, w);
            } else if ((intype || outtype) && (!ismoney)) {
                w = "提示：\n你的话中没有包含消费或开支的<金额>\n或者出现多次金额";
                dialogShowUtil.dialogShow("shake", "wrong", t, w);
            } else if ((!(intype || outtype)) && ismoney) {
                for (int i = 0; i < spdatalist.size(); i++) { // 判断是否包含支出
                    if ("语音识别".indexOf(spdatalist.get(i).toString()) > -1) {
                        VoiceSave[5] = Integer.toString(i);
                        VoiceSave[3] = "语音识别";
                    }
                }
                w = "**提示：\n你的话中没有包含<（默认）类别>（" + listToString(spdatalist, '，')
                        + "）\n\n\n将会记录为<语音识别>类别，是否依然记录？\n";
                dialogShowUtil.dialogShow("shake", "notype", t, w);
            } else {
                dialogShowUtil.dialogShow("rotatebottom", "OK", t, w);
            }
        }
    }

    public String listToString(List list, char separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    private boolean gotoback() { // 返回
        Intent intent = null;
        type = "pay";
        typemode = "add";
        Bimp.drr = new ArrayList<String>();
        Bimp.smdrr = new ArrayList<String>();
        Bimp.bmp = new ArrayList<Bitmap>();
        Bimp.max = 0;
        Bimp.flag = 0;
        if (bundle.containsKey("cwp.frament3")) {
            intent = new Intent(AddPayActivity.this, MainActivity.class);
            intent.putExtra("cwp.Fragment", "3");// 设置传递数据
        } else if (bundle.containsKey("cwp.search")) {
            this.setResult(3);
            this.finish();
            return true;
        } else {
            intent = new Intent(AddPayActivity.this, MainActivity.class);
            intent.putExtra("cwp.Fragment", "1");
        }
        intent.putExtra("cwp.id", userid);
        startActivity(intent);
        finish();
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 监控/拦截/屏蔽返回键
            gotoback();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
        mLocationClient.unRegisterLocationListener(mLocationListener);

    }

    protected void onResume() {
        super.onResume();
        SharedPreferences sp = this.getSharedPreferences("preferences",
                MODE_WORLD_READABLE);
        CrashApplication myApplaction = (CrashApplication) getApplication();
        if ((myApplaction.isLocked)
                && (sp.getString("gesturepw", "").equals("开"))) {// 判断是否需要跳转到密码界面
            Intent intent = new Intent(this,
                    UnlockGesturePasswordActivity.class);
            startActivity(intent);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_loacte:
                //TODO 百度地图的处理
                initBaiDuMap();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            onResults(data.getExtras());
        }
        switch (requestCode) {
            case 102:
                if (resultCode == 3 || resultCode == 0) {
                    if ((Bimp.drr.size() != 0) && (Bimp.smdrr.size() != 0)) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < Bimp.drr.size(); i++) {
                            sb.append(Bimp.drr.get(i) + ",");
                        }
                        for (int i = 0; i < Bimp.drr.size(); i++) {
                            sb.append(Bimp.smdrr.get(i) + ",");
                        }
                        textphoto = sb.toString().substring(0, sb.length() - 1);
                        initphoto();
                    } else {
                        textphoto = "";
                        initphoto();
                    }

                }
        }

    }

    public static void showVoiveDialog() {
        dialogShowUtil.dialogShow("rotatebottom", "first", "", "");
    }

    public void bindParams(Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean("tips_sound", true)) {
            intent.putExtra(Constant.EXTRA_SOUND_START, R.raw.bdspeech_recognition_start);
            intent.putExtra(Constant.EXTRA_SOUND_END, R.raw.bdspeech_speech_end);
            intent.putExtra(Constant.EXTRA_SOUND_SUCCESS, R.raw.bdspeech_recognition_success);
            intent.putExtra(Constant.EXTRA_SOUND_ERROR, R.raw.bdspeech_recognition_error);
            intent.putExtra(Constant.EXTRA_SOUND_CANCEL, R.raw.bdspeech_recognition_cancel);
        }
        if (sp.contains(Constant.EXTRA_INFILE)) {
            String tmp = sp.getString(Constant.EXTRA_INFILE, "").replaceAll(",.*", "").trim();
            intent.putExtra(Constant.EXTRA_INFILE, tmp);
        }
        if (sp.getBoolean(Constant.EXTRA_OUTFILE, false)) {
            intent.putExtra(Constant.EXTRA_OUTFILE, "sdcard/outfile.pcm");
        }
        if (sp.contains(Constant.EXTRA_SAMPLE)) {
            String tmp = sp.getString(Constant.EXTRA_SAMPLE, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_SAMPLE, Integer.parseInt(tmp));
            }
        }
        if (sp.contains(Constant.EXTRA_LANGUAGE)) {
            String tmp = sp.getString(Constant.EXTRA_LANGUAGE, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_LANGUAGE, tmp);
            }
        }
        if (sp.contains(Constant.EXTRA_NLU)) {
            String tmp = sp.getString(Constant.EXTRA_NLU, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_NLU, tmp);
            }
        }

        if (sp.contains(Constant.EXTRA_VAD)) {
            String tmp = sp.getString(Constant.EXTRA_VAD, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_VAD, tmp);
            }
        }
        String prop = null;
        if (sp.contains(Constant.EXTRA_PROP)) {
            String tmp = sp.getString(Constant.EXTRA_PROP, "").replaceAll(",.*", "").trim();
            if (null != tmp && !"".equals(tmp)) {
                intent.putExtra(Constant.EXTRA_PROP, Integer.parseInt(tmp));
                prop = tmp;
            }
        }

        // offline asr
        {
            intent.putExtra(Constant.EXTRA_OFFLINE_ASR_BASE_FILE_PATH, "/sdcard/easr/s_1");
            intent.putExtra(Constant.EXTRA_LICENSE_FILE_PATH, "/sdcard/easr/license-tmp-20150530.txt");
            if (null != prop) {
                int propInt = Integer.parseInt(prop);
                if (propInt == 10060) {
                    intent.putExtra(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_Navi");
                } else if (propInt == 20000) {
                    intent.putExtra(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_InputMethod");
                }
            }
            intent.putExtra(Constant.EXTRA_OFFLINE_SLOT_DATA, buildTestSlotData());
        }
    }

    private String buildTestSlotData() {
        JSONObject slotData = new JSONObject();
        JSONArray name = new JSONArray().put("李涌泉").put("郭下纶");
        JSONArray song = new JSONArray().put("七里香").put("发如雪");
        JSONArray artist = new JSONArray().put("周杰伦").put("李世龙");
        JSONArray app = new JSONArray().put("手机百度").put("百度地图");
        JSONArray usercommand = new JSONArray().put("关灯").put("开门");
        try {
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_NAME, name);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_SONG, song);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_ARTIST, artist);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_APP, app);
            slotData.put(Constant.EXTRA_OFFLINE_SLOT_USERCOMMAND, usercommand);
        } catch (JSONException e) {

        }
        return slotData.toString();
    }


    //开始录音
    private void start() {
        Intent intent = new Intent();
        bindParams(intent);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        {

            String args = sp.getString("args", "");
            if (null != args) {
                intent.putExtra("args", args);
            }
        }
        boolean api = sp.getBoolean("api", false);
        if (api) {
            speechEndTime = -1;
            speechRecognizer.startListening(intent);
        } else {
            intent.setAction("com.baidu.action.RECOGNIZE_SPEECH");
            startActivityForResult(intent, REQUEST_UI);
        }

    }

    //停止录音
    private void stop() {
        speechRecognizer.stopListening();
    }

    //取消录音
    private void cancel() {
        speechRecognizer.cancel();
    }


    //百度语音识别
    @Override
    public void onReadyForSpeech(Bundle params) {
        //准备就绪
        status = STATUS_Ready;
    }

    @Override
    public void onBeginningOfSpeech() {
        //开始说话处理
        status = STATUS_Speaking;
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        //录音数据传出处理
    }

    @Override
    public void onEndOfSpeech() {
        //说话结束处理
        status = STATUS_Recognition;

    }

    @Override
    public void onError(int error) {
        //出错处理
        status = STATUS_None;
        StringBuilder sb = new StringBuilder();
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                sb.append("音频问题");
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                sb.append("没有语音输入");
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                sb.append("其它客户端错误");
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                sb.append("权限不足");
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                sb.append("网络问题");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                sb.append("没有匹配的识别结果");
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                sb.append("引擎忙");
                break;
            case SpeechRecognizer.ERROR_SERVER:
                sb.append("服务端错误");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                sb.append("连接超时");
                break;
        }
        sb.append(":" + error);
        Toast.makeText(AddPayActivity.this, "识别失败：" + sb.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResults(Bundle results) {
        //最终结果处理
        status = STATUS_None;
        ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        //print("识别成功：" + Arrays.toString(nbest.toArray(new String[nbest.size()])));
        String json_res = results.getString("origin_result");
        try {
            Toast.makeText(AddPayActivity.this, "origin_result=\n" + new JSONObject(json_res).toString(4),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(AddPayActivity.this, "origin_result=[warning: bad json]\n" + json_res, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        //临时结果处理
        ArrayList<String> nbest = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (nbest.size() > 0) {
            Toast.makeText(AddPayActivity.this, "~临时识别结果：" + Arrays.toString(nbest.toArray(new String[0])),
                    Toast.LENGTH_SHORT).show();
            //Toast.makeText().setText(nbest.get(0));
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        //处理结果回调
        switch (eventType) {
            case EVENT_ERROR:
                String reason = params.get("reason") + "";
                Log.d("Add", "EVENT_ERROR, " + reason);
                break;
            case VoiceRecognitionService.EVENT_ENGINE_SWITCH:
                int type = params.getInt("engine_type");
                Log.d("Add", "*引擎切换至" + (type == 0 ? "在线" : "离线"));
                break;
        }
    }


}









