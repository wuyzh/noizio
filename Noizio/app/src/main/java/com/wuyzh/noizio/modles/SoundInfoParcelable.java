package com.wuyzh.noizio.modles;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by wuyzh on 16/5/21.
 */
public class SoundInfoParcelable implements Parcelable{
    private int[] images;
    private int[] seekbars;
    private boolean[] isSlidables;
    public SoundInfoParcelable(){

    }

    public SoundInfoParcelable(int[] seekbars, boolean[] isSlidables) {
        this.seekbars = seekbars;
        this.isSlidables = isSlidables;
    }

    public int[] getSeekbars() {
        return seekbars;
    }
    public void setSeekbar(int progress,int position){
        this.seekbars[position] = progress;
    }
    public void setSeekbars(int[] seekbars) {
        this.seekbars = seekbars;
    }

    public int[] getImages() {
        return images;
    }
    public void setImage(int image,int position){
        this.images[position] = image;
    }
    public void setImages(int[] images) {
        this.images = images;
    }

    public boolean[] getIsSlidables() {
        return isSlidables;
    }
    public void setIsSlidable(boolean isSlidable,int position) {
        this.isSlidables[position] = isSlidable;
    }
    public void setIsSlidables(boolean[] isSlidables) {
        this.isSlidables = isSlidables;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (seekbars == null){
            dest.writeInt(0);
        }else {
            dest.writeInt(seekbars.length);
        }
        if (seekbars != null){
            dest.writeIntArray(seekbars);
        }

        if (isSlidables == null){
            dest.writeInt(0);
        }else {
            dest.writeInt(isSlidables.length);
        }
        if (isSlidables != null){
            dest.writeBooleanArray(isSlidables);
        }
    }
    public final static Parcelable.Creator<SoundInfoParcelable> CREATOR = new Parcelable.Creator<SoundInfoParcelable>() {

        @Override
        public SoundInfoParcelable createFromParcel(Parcel source) {
            int[] seekbars = null;
            boolean[] isSlidables = null;
            int length;
            length = source.readInt();
            if (length != 0){
                seekbars = new int[length];
                source.readIntArray(seekbars);
            }
            length = source.readInt();
            if (length != 0){
                isSlidables = new boolean[length];
                source.readBooleanArray(isSlidables);
            }

            SoundInfoParcelable soundInfo = new SoundInfoParcelable(seekbars,isSlidables);
            return soundInfo;
        }

        @Override
        public SoundInfoParcelable[] newArray(int size) {
            // TODO Auto-generated method stub
            return new SoundInfoParcelable[size];
        }
    };
}




