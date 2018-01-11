package me.yuu.liteadapter;

/**
 * @author yu
 * @date 2018/1/11
 */
public interface ViewTypeLinker {
    /**
     * 通过实体和角标返回item对应的viewType(实际上是返回注册类型的顺序角标)
     * <p>
     * 注意：adapter在register新的类型时，会自动生成viewType，值等于register的顺序角标，
     * 所以要使用第一个register的类型就直接返回0，以此类推
     *
     * @param item     实体
     * @param position 角标
     * @return viewType
     */
    int viewType(Object item, int position);
}
