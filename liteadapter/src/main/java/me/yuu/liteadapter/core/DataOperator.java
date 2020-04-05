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

    void addAll(List<D> items);

    void addAll(int position, List<D> items);

    void remove(int position);

    void modify(int position, D newData);

    void modify(int position, Action<D> action);

    /**
     * 设置新的数据集合，自动应用DiffUtil进行差量更新
     */
    void updateData(List<D> items);

    void clear();

    interface Action<T> {
        void doAction(T data);
    }
}
