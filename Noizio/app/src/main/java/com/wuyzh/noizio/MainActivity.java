package com.wuyzh.noizio;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wuyzh.noizio.activity.SavedSoundsActivity;
import com.wuyzh.noizio.db.DatabaseHelper;
import com.wuyzh.noizio.modles.SoundInfoSerializable;
import com.wuyzh.noizio.service.SoundService;
import com.wuyzh.noizio.ui.ListViewEx;
import com.wuyzh.noizio.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {
    private LinearLayout mSavedLayout;
    private Button mSaveButton;
    private Button mCancelButton;
    private EditText mEditText;

    private DatabaseHelper mDatabaseHelper;

    private ActionBar mActionBar;
    private ImageButton mSaveImageButton;

    private ImageButton mSoundsListButton;
    private ImageButton mPlayOrPauseButton;
    private TextView mQuitText;
    private ImageButton mMenuButton;
    private LinearLayout mMenulayout;

    private IBinder mBinder;

    private ListViewEx mSoundListView;
    private List<Map<String, Object>> mData;
    private int[] titles;
    private int[] images;
    private int[] seekbars;
    private boolean[] isSlidables;
    private SoundInfoSerializable mSoundInfoSerializable;
    //wuyzh
    private SharedPreferenceUtil mSharedPreferenceUtil;
    //private ListView mSoundsListView;

    private Messenger mMessenger;
    private Message msg;
    private boolean isBind = false;
    private boolean isPlay = false;
    private boolean isShowMwnu = false;
    private boolean isSaving = false;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBind = true;
            mBinder = service;
            mMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBind = false;
            mMessenger = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabaseHelper = DatabaseHelper.getInstance(this);
        //wuyzh
        mSharedPreferenceUtil = new SharedPreferenceUtil(this,"test");
        setActionBar();
        initView();

        bindService();
    }
    private void setActionBar(){
        mActionBar = getActionBar();
        if (mActionBar != null){
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setElevation(0.0f);
            mActionBar.setCustomView(R.layout.actionbar_layout);
            mActionBar.setBackgroundDrawable(getDrawable(R.color.actionBarColor));

            //保存当前音乐设计
            mSaveImageButton = (ImageButton) findViewById(R.id.right_image);
            mSaveImageButton.setVisibility(View.VISIBLE);
            mSaveImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSaveImageButton.setImageResource(R.mipmap.save_mixture_selected_2x);
                    mSavedLayout.setVisibility(View.VISIBLE);
                    mSoundListView.setEnabled(false);
                    mEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
                    isSaving = true;
                }
            });
        }

    }
    private void initView(){
        mSoundListView = (ListViewEx) findViewById(R.id.sound_list);
        setData();
        mData = getData();
        MyAdapter adapter= new MyAdapter(this);
        mSoundListView.setAdapter(adapter);
        mQuitText = (TextView) findViewById(R.id.quit_text);
        mPlayOrPauseButton = (ImageButton) findViewById(R.id.play_or_pause);
        mPlayOrPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBind){
                    Log.d("wuyzhjjj","isBind = true");
                    if(isPlay){
                        Log.d("wuyzhjjj","isPlay = true");
                        mPlayOrPauseButton.setImageResource(R.mipmap.play_button_2x);
                        //暂停命令
                        sendMessage(2,0,0);
                        isPlay = false;
                    }else {
                        Log.d("wuyzhjjj","isPlay = false");
                        mPlayOrPauseButton.setImageResource(R.mipmap.pause_button_2x);
                        //播放音乐命令
                        sendMessage(3,0,0);
                        isPlay = true;
                    }
                }else {
                    Log.d("wuyzhjjj","isBind = false");
                }
            }
        });

        mSoundsListButton = (ImageButton) findViewById(R.id.sounds_list);
        mSoundsListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SavedSoundsActivity.class);
                Bundle bundle= new Bundle();
                bundle.putBinder("binder",mBinder);
                intent.putExtras(bundle);
                startActivityForResult(intent,0);
            }
        });

        mMenuButton = (ImageButton) findViewById(R.id.menu_button);
        mMenulayout = (LinearLayout) findViewById(R.id.menu_layout);
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShowMwnu){
                    mMenulayout.setVisibility(View.VISIBLE);
                    isShowMwnu = true;
                }else {
                    mMenulayout.setVisibility(View.GONE);
                    isShowMwnu = false;
                }
            }
        });

        mSavedLayout = (LinearLayout) findViewById(R.id.saved_layout);
        mEditText = (EditText) findViewById(R.id.saved_name);
        mSaveButton = (Button) findViewById(R.id.save_button);
        mCancelButton = (Button) findViewById(R.id.cancel_button);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mDatabaseHelper.isExistByName(mEditText.getText().toString())){
                    mSaveButton.setText("修改");
                    mSaveButton.setEnabled(true);
                }else {
                    mSaveButton.setText("保存");
                    mSaveButton.setEnabled(true);
                }
                if (mEditText.getText().toString().equals("")){
                    mSaveButton.setEnabled(false);
                }
            }
        });
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm =(InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                mSavedLayout.setVisibility(View.GONE);
                mSoundListView.setEnabled(true);
                mSaveImageButton.setImageResource(R.mipmap.save_mixture_2x);

                if (!mDatabaseHelper.isExistByName(mEditText.getText().toString())){
                    mDatabaseHelper.saveSounds(mSoundInfoSerializable,mEditText.getText().toString());
                }else {
                    mDatabaseHelper.updataSavedSounds(mSoundInfoSerializable,mEditText.getText().toString());
                }
                mEditText.setText("");
                isSaving = false;

            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm =(InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                mSavedLayout.setVisibility(View.GONE);
                mSoundListView.setEnabled(true);
                mSaveImageButton.setImageResource(R.mipmap.save_mixture_2x);
                mEditText.setText("");
                isSaving = false;
            }
        });


    }
    private void bindService(){
        Intent intent = new Intent(this,SoundService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("soundInfoSerializable",mSoundInfoSerializable);
        intent.putExtras(bundle);

        bindService(intent,mServiceConnection,Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //View v = getCurrentFocus();
            if (isShouldHideInput(mQuitText, ev)) {
                hideMenuLayout();
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }
    public  boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof TextView)) {
            int[] leftTop = { 0, 0 };
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSharedPreferenceUtil.setObject("mSoundInfoSerializable",mSoundInfoSerializable);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("wuyzh","onRestart()");
        isBind = true;
        hideMenuLayout();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_MENU){
            if (!isShowMwnu){
                mMenulayout.setVisibility(View.VISIBLE);
                isShowMwnu = true;
            }else {
                mMenulayout.setVisibility(View.GONE);
                isShowMwnu = false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0){
            if (resultCode == 0){
                //没做任何操作就返回

            } else if (resultCode == 1){
                //播放列表中的音乐，返回
                Bundle bundle = data.getExtras();
                mSoundInfoSerializable = (SoundInfoSerializable) bundle.getSerializable("soundInfoSerializable");
                mPlayOrPauseButton.setImageResource(R.mipmap.pause_button_2x);
                isPlay = true;
                setData();
                mData = getData();
                MyAdapter adapter= new MyAdapter(this);
                mSoundListView.setAdapter(adapter);
            }else if (resultCode == 2){
                //处于暂停状态
                Bundle bundle = data.getExtras();
                mSoundInfoSerializable = (SoundInfoSerializable) bundle.getSerializable("soundInfoSerializable");
                mPlayOrPauseButton.setImageResource(R.mipmap.play_button_2x);
                isPlay = false;
                setData();
                mData = getData();
                MyAdapter adapter= new MyAdapter(this);
                mSoundListView.setAdapter(adapter);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (isShowMwnu){
            hideMenuLayout();
        }else if (isSaving){
            mSavedLayout.setVisibility(View.GONE);
            mSaveImageButton.setImageResource(R.mipmap.save_mixture_2x);
            isSaving = false;
        }else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }


    //退出程序
    public void finishApplication(View view){
        sendMessage(4,0,0);
        //unbindService(mServiceConnection);
        finish();
    }
    private List<Map<String, Object>> getData(){
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        for(int i=0;i<images.length;i++)
        {
            map = new HashMap<String, Object>();
            map.put("img", images[i]);
            map.put("title", titles[i]);
            map.put("seekbar", seekbars[i]);
            map.put("isSlidable",isSlidables[i]);
            list.add(map);
        }
        return list;
    }
    private void setData(){

        titles = new int[]{R.string.october_rain,R.string.coffee_house,R.string.thenderstorm,
                R.string.campfire,R.string.winter_wind,R.string.sea_waves,
                R.string.river_stream,R.string.summer_night,R.string.sunny_day,
                R.string.deep_space,R.string.sailing_yacht,R.string.inside_train,
                R.string.on_the_firm,R.string.wind_chimes,R.string.blue_whales};

        if (mSoundInfoSerializable == null){
            mSoundInfoSerializable = mSharedPreferenceUtil.getObject("mSoundInfoSerializable",SoundInfoSerializable.class);
        }

        if (mSoundInfoSerializable == null){

            images = new int[]{R.mipmap.rain_icon_2x, R.mipmap.cafe_icon_2x, R.mipmap.thunderstorm_icon_2x,
                    R.mipmap.fire_icon_disabled_2x, R.mipmap.wind_icon_disabled_2x, R.mipmap.water_icon_disabled_2x,
                    R.mipmap.river_icon_disabled_2x, R.mipmap.night_icon_disabled_2x,R.mipmap.forest_icon_disabled_2x,
                    R.mipmap.space_icon_disabled_2x, R.mipmap.steering_wheel_icon_disabled_2x, R.mipmap.rails_icon_disabled_2x,
                    R.mipmap.cow_icon_disabled_2x, R.mipmap.chimes_icon_disabled_2x, R.mipmap.whale_icon_disabled_2x};
            seekbars = new int[]{75,15,50,
                    50,50,50,
                    50,50,50,
                    50,50,50,
                    50,50,50,};
            isSlidables = new boolean[]{true,true,true,
                    false,false,false,
                    false,false,false,
                    false,false,false,
                    false,false,false};
            mSoundInfoSerializable = new SoundInfoSerializable(images,seekbars,isSlidables);
        }else {
            images = mSoundInfoSerializable.getImages();
            seekbars = mSoundInfoSerializable.getSeekbars();
            isSlidables = mSoundInfoSerializable.getIsSlidables();
        }
    }
    public void hideMenuLayout(){
        if (isShowMwnu){
            mMenulayout.setVisibility(View.GONE);
            isShowMwnu = false;
        }
    }
    @Override
    protected void onStop() {
        super.onStop();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBind){
            unbindService(mServiceConnection);
            isBind = false;
        }
    }

    private void sendMessage(int flag, int positon, int typeOrProgress){
        if (isBind){
            msg = Message.obtain(null,flag,positon,typeOrProgress);
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    class MyAdapter extends BaseAdapter {
        private LayoutInflater mLayoutInflater;

        public MyAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Map<String, Object> getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.sound_item, null);
                viewHolder.img = (ImageButton) convertView.findViewById(R.id.sound_image);
                viewHolder.title = (TextView) convertView.findViewById(R.id.sound_name);
                viewHolder.seekBar = (SeekBar) convertView.findViewById(R.id.sound_num);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.img.setImageResource((int) mData.get(position).get("img"));
            viewHolder.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean slidable = (boolean) mData.get(position).get("isSlidable");
                    if (slidable){
                        switch (position){
                            case 0:
                                mData.get(position).put("img",R.mipmap.rain_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.rain_icon_disabled_2x,position);
                                break;
                            case 1:
                                mData.get(position).put("img",R.mipmap.cafe_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.cafe_icon_disabled_2x,position);
                                break;
                            case 2:
                                mData.get(position).put("img",R.mipmap.thunderstorm_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.thunderstorm_icon_disabled_2x,position);
                                break;
                            case 3:
                                mData.get(position).put("img",R.mipmap.fire_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.fire_icon_disabled_2x,position);
                                break;
                            case 4:
                                mData.get(position).put("img",R.mipmap.wind_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.wind_icon_disabled_2x,position);
                                break;
                            case 5:
                                mData.get(position).put("img",R.mipmap.water_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.water_icon_disabled_2x,position);
                                break;
                            case 6:
                                mData.get(position).put("img",R.mipmap.river_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.river_icon_disabled_2x,position);
                                break;
                            case 7:
                                mData.get(position).put("img",R.mipmap.night_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.night_icon_disabled_2x,position);
                                break;
                            case 8:
                                mData.get(position).put("img",R.mipmap.forest_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.forest_icon_disabled_2x,position);
                                break;
                            case 9:
                                mData.get(position).put("img",R.mipmap.space_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.space_icon_disabled_2x,position);
                                break;
                            case 10:
                                mData.get(position).put("img",R.mipmap.steering_wheel_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.steering_wheel_icon_disabled_2x,position);
                                break;
                            case 11:
                                mData.get(position).put("img",R.mipmap.rails_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.rails_icon_disabled_2x,position);
                                break;
                            case 12:
                                mData.get(position).put("img",R.mipmap.cow_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.cow_icon_disabled_2x,position);
                                break;
                            case 13:
                                mData.get(position).put("img",R.mipmap.chimes_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.chimes_icon_disabled_2x,position);
                                break;
                            case 14:
                                mData.get(position).put("img",R.mipmap.whale_icon_disabled_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.whale_icon_disabled_2x,position);
                                break;
                            default:
                                break;
                        }
                        //两个是同步执行的
                        mData.get(position).put("isSlidable",false);
                        sendMessage(1,position,SoundService.num_false);
                        //wuyzh
                        mSoundInfoSerializable.setIsSlidable(false,position);

                    }else {
                        switch (position){
                            case 0:
                                mData.get(position).put("img",R.mipmap.rain_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.rain_icon_2x,position);
                                break;
                            case 1:
                                mData.get(position).put("img",R.mipmap.cafe_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.cafe_icon_2x,position);
                                break;
                            case 2:
                                mData.get(position).put("img",R.mipmap.thunderstorm_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.thunderstorm_icon_2x,position);
                                break;
                            case 3:
                                mData.get(position).put("img",R.mipmap.fire_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.fire_icon_2x,position);
                                break;
                            case 4:
                                mData.get(position).put("img",R.mipmap.wind_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.wind_icon_2x,position);
                                break;
                            case 5:
                                mData.get(position).put("img",R.mipmap.water_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.water_icon_2x,position);
                                break;
                            case 6:
                                mData.get(position).put("img",R.mipmap.river_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.river_icon_2x,position);
                                break;
                            case 7:
                                mData.get(position).put("img",R.mipmap.night_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.night_icon_2x,position);
                                break;
                            case 8:
                                mData.get(position).put("img",R.mipmap.forest_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.forest_icon_2x,position);
                                break;
                            case 9:
                                mData.get(position).put("img",R.mipmap.space_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.space_icon_2x,position);
                                break;
                            case 10:
                                mData.get(position).put("img",R.mipmap.steering_wheel_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.steering_wheel_icon_2x,position);
                                break;
                            case 11:
                                mData.get(position).put("img",R.mipmap.rails_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.rails_icon_2x,position);
                                break;
                            case 12:
                                mData.get(position).put("img",R.mipmap.cow_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.cow_icon_2x,position);
                                break;
                            case 13:
                                mData.get(position).put("img",R.mipmap.chimes_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.chimes_icon_2x,position);
                                break;
                            case 14:
                                mData.get(position).put("img",R.mipmap.whale_icon_2x);
                                mSoundInfoSerializable.setImage(R.mipmap.whale_icon_2x,position);
                                break;
                            default:
                                break;
                        }
                        ////两个是同步执行的
                        mData.get(position).put("isSlidable",true);
                        sendMessage(1,position,SoundService.num_true);
                        //wuyzh
                        mSoundInfoSerializable.setIsSlidable(true,position);
                    }
                    notifyDataSetChanged();
                }
            });
            viewHolder.title.setText((int) getItem(position).get("title"));
            //viewHolder.title.setText(""+(int)getItem(position).get("title"));
            if ((boolean) mData.get(position).get("isSlidable")){
                viewHolder.title.setTextColor(Color.BLACK);
                viewHolder.seekBar.setEnabled(true);

            }else {
                viewHolder.title.setTextColor(Color.GRAY);
                viewHolder.seekBar.setEnabled(false);
                //viewHolder.seekBar.setProgressDrawable(gradientDrawable);
            }
            Log.d("wuyzhj","position:"+position+"  setProgress:"+(int)getItem(position).get("seekbar"));
            if ((int)getItem(position).get("seekbar")==0){
                viewHolder.seekBar.setProgress(0);
            }else {
                viewHolder.seekBar.setProgress((int)getItem(position).get("seekbar"));
            }
            //viewHolder.seekBar.setProgress((int)getItem(position).get("seekbar"));
            viewHolder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (mSoundListView.isSeekbar){
                        Log.d("wuyzhj","zzzzzzzzzzzzzzzz");
                        mData.get(position).put("seekbar",progress);
                        //wuyzh
                        mSoundInfoSerializable.setSeekbar(progress,position);
                        sendMessage(0,position,progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }
        public final class ViewHolder
        {
            public ImageButton img;
            public TextView title;
            public SeekBar seekBar;
        }
    }

}
