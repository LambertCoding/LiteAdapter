package me.yuu.sample;

/**
 * @author yu
 * @date 2018/1/11
 */

public class Girl {

    private String name;
    private int imageRes;
    private boolean isSection = false;

    private Girl(String name, int imageRes, boolean isSection) {
        this.name = name;
        this.imageRes = imageRes;
        this.isSection = isSection;
    }

    public static Girl createItem(String name, int imageRes) {
        return new Girl(name, imageRes, false);
    }

    public static Girl createSection(String name) {
        return new Girl(name, 0, true);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public boolean isSection() {
        return isSection;
    }

    public void setSection(boolean section) {
        isSection = section;
    }
}
