package me.yuu.liteadapter.core;

import java.util.List;

/**
 * @author yu.
 * @date 2018/1/12
 */
public interface DataOperator<D> {

    D getItem(int position);

    void addData(D item);

    void addData(int position, D item);

    void addDataToHead(D item);

    void addAll(List<D> items);

    void addAll(int position, List<D> items);

    void addAllToHead(List<D> items);

    void remove(int position);

    void modify(int position, D newData);

    void modify(int position, Action<D> action);

    void setNewData(List<D> items);

    void clear();

    interface Action<T> {
        void doAction(T data);
    }
}
