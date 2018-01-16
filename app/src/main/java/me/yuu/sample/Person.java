package me.yuu.sample;

/**
 * @author yu
 * @date 2018/1/11
 */

public class Person {

    private String name;
    private int imageRes;
    private boolean isSection = false;

    private Person(String name, int imageRes, boolean isSection) {
        this.name = name;
        this.imageRes = imageRes;
        this.isSection = isSection;
    }

    public static Person createItem(String name, int imageRes) {
        return new Person(name, imageRes, false);
    }

    public static Person createSection(String name) {
        return new Person(name, 0, true);
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
