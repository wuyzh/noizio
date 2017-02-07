package com.wuyzh.noizio.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.wuyzh.noizio.R;
import com.wuyzh.noizio.modles.SoundInfoSerializable;

public class SoundService extends Service {
    public static final int num_true = 0;
    public static final int num_false = 1;

    int mPosition;
    int mTypeOrProgress;

    private int[] seekbars;
    private boolean[] isSlidables;

    private MediaPlayer[] mMediaPlayers;

    private SoundInfoSerializable mSoundInfoSerializable = null;

    private boolean isPlaying = false;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            mPosition = msg.arg1;
            mTypeOrProgress = msg.arg2;
            switch (msg.what){
                case 0:
                    //重新设置某一个音量
                    setMediaPlayerVolume();
                    break;
                case 1:
                    //重新设置某条是否可播放
                    startOrPauseMediaPlayer();
                    break;
                case 2:
                    //暂停所有音乐
                    pauseMediaPlayers();
                    break;
                case 3:
                    //重新开始所有音乐
                    startMediaPlayers();
                    break;
                case 4:
                    //停止所有音乐
                    stopMediaPlayers();
                    break;
                default:
                    pauseMediaPlayers();
                    mSoundInfoSerializable = (SoundInfoSerializable) msg.obj;
                    seekbars = mSoundInfoSerializable.getSeekbars();
                    for (int i = 0;i<seekbars.length;i++){
                        Log.d("wuyzhjk","seekbars["+i+"]"+seekbars[i]);
                    }
                    setMediaPlayersVolume();
                    isSlidables = mSoundInfoSerializable.getIsSlidables();
                    startMediaPlayers();
                    break;
            }
        }
    }

    private void startOrPauseMediaPlayer() {
        Log.d("wuyzh","mTypeOrProgress:"+ mTypeOrProgress);
        if (mTypeOrProgress == num_true){
            Log.d("wuyzh","mTypeOrProgress == num_true");
            if (isPlaying){
                if (mMediaPlayers[mPosition] != null){
                    mMediaPlayers[mPosition].start();
                }
            }
            isSlidables[mPosition]=true;
        }else {
            Log.d("wuyzh","mTypeOrProgress == num_false");
            if (isPlaying){
                if (mMediaPlayers[mPosition] != null){
                    mMediaPlayers[mPosition].pause();
                }
            }
            isSlidables[mPosition]=false;
        }
    }

    private void setMediaPlayerVolume() {
        if (mMediaPlayers[mPosition] != null){
            mMediaPlayers[mPosition].setVolume((float)(mTypeOrProgress/100.0), (float) (mTypeOrProgress/100.0));
        }
        seekbars[mPosition] = mTypeOrProgress;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMediaplayersResource();
    }


    @Override
    public IBinder onBind(Intent intent) {
        Bundle bundle = intent.getExtras();
        bundle.getParcelable("soundInfoParcelable");
        SoundInfoSerializable mSoundInfoSerializable = (SoundInfoSerializable) bundle.getSerializable("soundInfoSerializable");

        seekbars = mSoundInfoSerializable.getSeekbars();
        setMediaPlayersVolume();

        isSlidables = mSoundInfoSerializable.getIsSlidables();
        //startMediaPlayers();

        return mMessenger.getBinder();
    }

    //初始化所有将要播放的音乐
    private void initMediaplayersResource(){
        mMediaPlayers = new MediaPlayer[15];
        mMediaPlayers[0] = MediaPlayer.create(SoundService.this, R.raw.rain_sound_1);
        mMediaPlayers[1] = MediaPlayer.create(SoundService.this,R.raw.tea_sound_1);
        mMediaPlayers[2] = MediaPlayer.create(SoundService.this,R.raw.thunder_sound_1);
        mMediaPlayers[3] = MediaPlayer.create(SoundService.this,R.raw.fire_sound_1);
        mMediaPlayers[4] = MediaPlayer.create(SoundService.this,R.raw.wind_sound_1);
        mMediaPlayers[5] = MediaPlayer.create(SoundService.this,R.raw.water_sound_1);
        mMediaPlayers[6] = MediaPlayer.create(SoundService.this,R.raw.river_sound_1);
        mMediaPlayers[7] = MediaPlayer.create(SoundService.this,R.raw.night_sound_1);
        mMediaPlayers[8] = MediaPlayer.create(SoundService.this,R.raw.day_sound_1);
        mMediaPlayers[9] = MediaPlayer.create(SoundService.this,R.raw.space_sound_1);
        mMediaPlayers[10] = MediaPlayer.create(SoundService.this,R.raw.yacht_sound_1);
        mMediaPlayers[11] = MediaPlayer.create(SoundService.this,R.raw.train_sound_1);
        mMediaPlayers[12] = MediaPlayer.create(SoundService.this,R.raw.farm_sound_1);
        mMediaPlayers[13] = MediaPlayer.create(SoundService.this,R.raw.chimes_sound_1);
        mMediaPlayers[14] = MediaPlayer.create(SoundService.this,R.raw.whale_sound_1);

        for (int i = 0; i < mMediaPlayers.length;i++){
            if (mMediaPlayers[i] != null){
                mMediaPlayers[i].setLooping(true);
            }
        }
    }

    private void setMediaPlayersVolume(){
        for (int i = 0; i < seekbars.length;i++){
            if (mMediaPlayers[i] != null){
                mMediaPlayers[i].setVolume((float)(seekbars[i]/100.0), (float)(seekbars[i]/100.0));
            }
        }
    }

    private void startMediaPlayers(){
        for (int i = 0; i < isSlidables.length;i++){
            if (isSlidables[i]){
                if (mMediaPlayers[i] != null){
                    Log.d("wuyzhjk","kkkkkkkkkkkkkk:"+i);
                    mMediaPlayers[i].start();
                }
            }
        }
        isPlaying = true;

    }
    private void stopMediaPlayers(){
        for (int i = 0; i < isSlidables.length;i++){
            if (isSlidables[i]){
                if (mMediaPlayers[i] != null){
                    mMediaPlayers[i].stop();
                }
            }
        }
    }
    private void pauseMediaPlayers(){
        for (int i = 0; i < isSlidables.length;i++){
            if (isSlidables[i]&&isPlaying){
                if (mMediaPlayers[i] != null){
                    mMediaPlayers[i].pause();
                }
            }
        }
        isPlaying = false;
    }

    @Override
    public void onDestroy() {
        Log.d("wuyzh","onDestroy");
        super.onDestroy();
        //pauseMediaPlayers();
    }
}
