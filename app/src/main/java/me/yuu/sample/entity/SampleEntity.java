package me.yuu.sample.entity;

import android.app.Activity;

/**
 * @author yu
 * @date 2018/1/16
 */
public class SampleEntity {
    private String name;
    private Class<? extends Activity> target;

    public SampleEntity(String name, Class<? extends Activity> target) {
        this.name = name;
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<? extends Activity> getTarget() {
        return target;
    }

    public void setTarget(Class<? extends Activity> target) {
        this.target = target;
    }
}
