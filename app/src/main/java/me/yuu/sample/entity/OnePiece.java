package me.yuu.sample.entity;

import me.yuu.liteadapter.entity.RadioItem;

/**
 * @author yu
 * @date 2018/1/11
 */

public class OnePiece implements RadioItem {

    private String desc;
    private int imageRes;
    private boolean isBigType;
    private boolean isChecked;

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public OnePiece(String desc) {
        this(desc, -1, false);
    }

    public OnePiece(String desc, int imageRes, boolean isBigType) {
        this.desc = desc;
        this.imageRes = imageRes;
        this.isBigType = isBigType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isBigType() {
        return isBigType;
    }

    public void setBigType(boolean bigType) {
        isBigType = bigType;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

}
