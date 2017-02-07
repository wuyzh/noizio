package com.wuyzh.noizio.modles;

import java.io.Serializable;

/**
 * Created by wuyzh on 16/5/21.
 */
public class SoundInfoSerializable implements Serializable {
    private static final long serialVersionUID = 999794470754667710L;

    private int[] images;
    private int[] seekbars;
    private boolean[] isSlidables;
    public SoundInfoSerializable(){

    }

    public SoundInfoSerializable(int[] seekbars, boolean[] isSlidables){
        this.seekbars = seekbars;
        this.isSlidables = isSlidables;
    }

    public SoundInfoSerializable(int[] images, int[] seekbars, boolean[] isSlidables) {
        this.images = images;
        this.seekbars = seekbars;
        this.isSlidables = isSlidables;
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

    public int[] getSeekbars() {
        return seekbars;
    }
    public void setSeekbar(int progress,int position){
        this.seekbars[position] = progress;
    }
    public void setSeekbars(int[] seekbars) {
        this.seekbars = seekbars;
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

    public SoundInfoParcelable getSoundInfoParcelable(){
        return new SoundInfoParcelable(seekbars,isSlidables);
    }

}
