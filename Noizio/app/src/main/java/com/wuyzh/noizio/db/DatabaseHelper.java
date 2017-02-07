package com.wuyzh.noizio.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.wuyzh.noizio.modles.SoundInfoSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyzh on 16/5/25.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "noizio-master.db";
    private static final int DATABASE_VERSION = 1;

    private Context mContext = null;
    private static DatabaseHelper mInstance = null;

    String createSqlString =
            "create table " + SoundModule.TAG_SOUND_INFO + "(" +
                    SoundModule.SOUND_IDS + " text not null, " +
                    SoundModule.SOUND_IMGES + " text not null," +
                    SoundModule.SOUND_SEEKBAR + " text not null," +
                    SoundModule.SOUND_IS_SLISABLES + " text not null " +
                    ")";
    String name = "wuyzh0";
    String imags = "wuyzh01";
    String seekbars = "wuyzh02";
    String isSlidables = "wuyzh03";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static DatabaseHelper getInstance(Context context) {
        Log.d("wuyzhhhhhhhh","getInstance");
        if (mInstance == null) {
            mInstance = new DatabaseHelper(context);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("wuyzhhhhhhhh","addSoundInfoTable");
        db.execSQL(createSqlString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void addSoundInfoTable(SQLiteDatabase db) {
        String createSqlString =
                "create table " + SoundModule.TAG_SOUND_INFO + "(" +
                        SoundModule.SOUND_IDS + " text not null, " +
                        SoundModule.SOUND_IMGES + " text not null," +
                        SoundModule.SOUND_SEEKBAR + " text not null," +
                        SoundModule.SOUND_IS_SLISABLES + " text not null " +
                        ")";
        db.execSQL(createSqlString);
    }

    //保存
    public void saveSounds(SoundInfoSerializable soundInfoSerializable , String name){
        SoundModule soundModule = getSoundModule(soundInfoSerializable);
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("insert into sound_info values('" + name + "','" + soundModule.imags + "','" + soundModule.seekbars + "','" + soundModule.isSlidables + "')");
    }
    private SoundModule getSoundModule(SoundInfoSerializable soundInfoSerializable){
        imags = "";
        for (int i = 0;i<soundInfoSerializable.getImages().length;i++){
            imags += soundInfoSerializable.getImages()[i] + ":";
        }
        Log.d("wuyzhhhhh",imags);

        seekbars = "";
        for (int i = 0;i<soundInfoSerializable.getSeekbars().length;i++){
            seekbars += soundInfoSerializable.getSeekbars()[i] + ":";
        }
        Log.d("wuyzhhhhh",seekbars);

        isSlidables = "";
        for (int i = 0;i<soundInfoSerializable.getIsSlidables().length;i++){
            isSlidables += soundInfoSerializable.getIsSlidables()[i] + ":";
        }
        Log.d("wuyzhhhhh",isSlidables);
        return new SoundModule(imags,seekbars,isSlidables);
    }

    //根据保存的名称获取里面的信息
    public SoundInfoSerializable getSoundInfoByName(String name){
        Log.d("wuyzhhhhh","wwwwww:");
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        cursor = db.rawQuery("select " + SoundModule.SOUND_IMGES  + "," + SoundModule.SOUND_SEEKBAR + "," + SoundModule.SOUND_IS_SLISABLES +
                " from " + SoundModule.TAG_SOUND_INFO +
                " where " + SoundModule.SOUND_IDS + " = ?", new String[]{name});
        if (cursor.moveToNext()) {
            Log.d("wuyzhhhhh","wwwwww:"+name);
            String[] paths = cursor.getString(0).split(":");
            int[] imgs = new int[15];
            for (int i=0;i<paths.length;i++){
                imgs[i] = Integer.parseInt(paths[i]);
                Log.d("wuyzhhhh","imgs["+i+"]:"+imgs[i]);
            }
            paths = cursor.getString(1).split(":");
            int[] sekbars = new int[15];
            for (int i=0;i<paths.length;i++){
                sekbars[i] = Integer.parseInt(paths[i]);
                Log.d("wuyzhhhh","seekbars["+i+"]:"+sekbars[i]);
            }
            paths = cursor.getString(2).split(":");
            boolean[] isSlisables = new boolean[15];
            for (int i=0;i<paths.length;i++){
                isSlisables[i] = Boolean.parseBoolean(paths[i]);
                Log.d("wuyzhhhh","isSlisables["+i+"]:"+isSlisables[i]);
            }
            cursor.close();
            cursor = null;
            return new SoundInfoSerializable(imgs,sekbars,isSlisables);
        }
        cursor.close();
        cursor = null;
        return null;
    }

    //判断当前名字是否存在
    public boolean isExistByName(String name){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        cursor = db.rawQuery("select " + SoundModule.SOUND_IMGES  +
                " from " + SoundModule.TAG_SOUND_INFO +
                " where " + SoundModule.SOUND_IDS + " = ?", new String[]{name});
        if (cursor.moveToNext()){
            cursor.close();
            cursor = null;
            return true;
        }else {
            cursor.close();
            cursor = null;
            return false;
        }

    }

    //删除一条item
    public void deleteSavedSound(String name){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(SoundModule.TAG_SOUND_INFO, SoundModule.SOUND_IDS + " = ?", new String[]{name});
    }

    //修改以存取过的信息
    public void updataSavedSounds(SoundInfoSerializable soundInfoSerializable ,String name){
        SoundModule soundModule = getSoundModule(soundInfoSerializable);
        ContentValues contentValues = new ContentValues();
        contentValues.put(SoundModule.SOUND_IMGES,soundModule.imags);
        contentValues.put(SoundModule.SOUND_SEEKBAR,soundModule.seekbars);
        contentValues.put(SoundModule.SOUND_IS_SLISABLES,soundModule.isSlidables);

        SQLiteDatabase db = getWritableDatabase();
        db.update(SoundModule.TAG_SOUND_INFO,contentValues, SoundModule.SOUND_IDS + " = ?", new String[]{name});
    }

    //获取所有item的名字
    public List<String> getAllSavedName(){
        List<String> names = new ArrayList<String>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        cursor = db.rawQuery("select " + SoundModule.SOUND_IDS +
                " from " + SoundModule.TAG_SOUND_INFO , null);
        while (cursor.moveToNext()) {
            names.add(cursor.getString(0));
            Log.d("wuyzh","sadsdsa:"+cursor.getString(0));
        }
        cursor.close();
        cursor = null;
        return names;
    }

    public class SoundModule{
        public static final String TAG_SOUND_INFO = "sound_info";
        public static final String SOUND_IDS = "_name";
        public static final String SOUND_IMGES = "sound_imgs";
        public static final String SOUND_SEEKBAR = "sound_seekbars";
        public static final String SOUND_IS_SLISABLES = "sound_is_slidables";

        private String name;
        private String imags;
        private String seekbars;
        private String isSlidables;

        public SoundModule(String imags, String seekbars, String isSlidables) {
            this.imags = imags;
            this.seekbars = seekbars;
            this.isSlidables = isSlidables;
        }
    }
}
