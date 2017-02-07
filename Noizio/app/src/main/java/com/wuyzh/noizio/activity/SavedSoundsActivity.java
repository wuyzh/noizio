package com.wuyzh.noizio.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.wuyzh.noizio.R;
import com.wuyzh.noizio.db.DatabaseHelper;
import com.wuyzh.noizio.modles.SoundInfoSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavedSoundsActivity extends Activity {
    private int operationFlag = 0;

    private Messenger mMessenger;
    private Message msg;
    private SoundInfoSerializable mSoundInfoSerializable;


    private DatabaseHelper mDatabaseHelper;

    private List<Map<String, Object>> mData;
    private ListView mListView;

    private ActionBar mActionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_sounds);

        mDatabaseHelper = DatabaseHelper.getInstance(this);

        Bundle bundle = getIntent().getExtras();
        IBinder binder = bundle.getBinder("binder");
        mMessenger = new Messenger(binder);

        setActionBar();

        mListView = (ListView) findViewById(R.id.saved_sounds_list);
        mData = getData();
        MyAdapter adapter= new MyAdapter(this);
        mListView.setAdapter(adapter);
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

            ImageButton backImageButton = (ImageButton) findViewById(R.id.back_image);
            ImageButton leftImageButton = (ImageButton) findViewById(R.id.left_image);
            TextView actionBarTile = (TextView) findViewById(R.id.action_bar_title);
            leftImageButton.setVisibility(View.GONE);
            actionBarTile.setVisibility(View.VISIBLE);
            backImageButton.setVisibility(View.VISIBLE);
            backImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("soundInfoSerializable",mSoundInfoSerializable);
                    intent.putExtras(bundle);
                    setResult(operationFlag,intent);
                    finish();
                }
            });
        }

    }

    private List<Map<String, Object>> getData(){
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        List<String> names =  mDatabaseHelper.getAllSavedName();
        if (names.size()<8){
            for (int i = names.size();i<8;i++){
                names.add(null);
            }
        }
        for(int i=0;i<names.size();i++)
        {
            map = new HashMap<String, Object>();
            map.put("name", names.get(i));
            map.put("isPlaying", false);
            list.add(map);
        }
        return list;
    }

    private void sendMessage(Message msg){
        //msg = Message.obtain(null,5,mSoundInfoSerializable);
        try {
            mMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("soundInfoSerializable",mSoundInfoSerializable);
        intent.putExtras(bundle);
        setResult(operationFlag,intent);
        super.onBackPressed();
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
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.saved_sounds_item,null);
                viewHolder.deleteSaved = (ImageButton) convertView.findViewById(R.id.delete_button);
                viewHolder.title = (TextView) convertView.findViewById(R.id.saved_sound_name);
                viewHolder.playOrPause = (ImageButton) convertView.findViewById(R.id.play_or_pause_saved);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.title.setText((CharSequence) mData.get(position).get("name"));
            if (mData.get(position).get("name") == null){
                viewHolder.title.setVisibility(View.GONE);
                viewHolder.playOrPause.setVisibility(View.GONE);
                viewHolder.deleteSaved.setVisibility(View.GONE);
            }
            viewHolder.deleteSaved.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDatabaseHelper.deleteSavedSound((String) mData.get(position).get("name"));
                    mData.remove(position);
                    notifyDataSetChanged();
                }
            });
            if ((boolean) mData.get(position).get("isPlaying")){
                viewHolder.playOrPause.setImageResource(R.mipmap.pause_button_2x);
            }else {
                viewHolder.playOrPause.setImageResource(R.mipmap.play_button_2x);
            }

            viewHolder.playOrPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = null;
                    boolean isPlaying = (boolean) mData.get(position).get("isPlaying");
                    if (isPlaying){
                        //发送消息暂停音乐播放
                        msg = Message.obtain(null,2,0,0);
                        sendMessage(msg);
                        viewHolder.playOrPause.setImageResource(R.mipmap.play_button_2x);
                        mData.get(position).put("isPlaying",false);
                        operationFlag = 2;
                    }
                    else {
                        mSoundInfoSerializable = mDatabaseHelper.getSoundInfoByName((String) mData.get(position).get("name"));
                        //发送消息播放音乐
                        msg = Message.obtain(null,5,mSoundInfoSerializable);
                        sendMessage(msg);
                        viewHolder.playOrPause.setImageResource(R.mipmap.pause_button_2x);
                        for (int i=0; i<mData.size();i++){
                            mData.get(i).put("isPlaying",false);
                        }
                        mData.get(position).put("isPlaying",true);
                        operationFlag = 1;
                    }
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }
        public final class ViewHolder
        {
            public ImageButton deleteSaved;
            public TextView title;
            public ImageButton playOrPause;
        }
    }
}
