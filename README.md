 ![image]()
 
## Features

* Fluent & simple API
* Multi View Type
* Auto LoadMore
* Auto Diff
* Auto Empty View
* Header & Footer
* support DataBinding

## Setup
```
// in project build.gradle
allprojects {
    repositories {
        ...
        maven { url 'https://www.jitpack.io' }
    }
}

// in module build.gradle

implementation 'com.github.yu1tiao:LiteAdapter:1.1.2'
...
```
## Usages
#### step 1: create adapter
```kotlin
buildAdapterEx(this) {
            // 注册一个布局
            register(R.layout.item_normal) { holder, item, _ ->
                bindData2View(holder, item)// 绑定数据
            }
            register(R.layout.item_big) { holder, item, _ ->
                bindData2View(holder, item)
            }

            // 多布局类型时，通过injectorFinder返回对应布局的角标，按register顺序，从0开始
            injectorFinder { item, _, _ ->
                if (item.isBigType) 1 else 0
            }
            // 点击事件
            itemClickListener { index, _ ->
                showToast("click position : $index")
            }
            itemLongClickListener { index, _ ->
                adapter.remove(index)
            }

            this.emptyView = emptyView // 空布局
            keepHeadAndFoot = true  // 没有数据的时候是否保持头和脚布局

            // 头布局和脚布局
            addHeader(header1)
            addHeader(header2)
            addHeader(header3)

//            autoDiff()
//            addFooter(footer)
            // 加载更多，注意和脚布局互斥，只能有一个类型存在
            enableLoadMore {
                loadMore()
            }
        }
```
#### step 2: bind data
```kotlin
holder.setText(R.id.tvDesc, item.getDesc())
        // .set...
        .with(R.id.ivImage, ViewHolder.Action<ImageView> { view -> doSomeThing() })
```

#### step 3: Use LiteAdapter as normal adapter
```
    D getItem(int position);
    void setNewData(List<D> items);
    void addData(D item);
    void addData(int position, D item);
    void addAll(List<D> items);
    void addAll(int position, List<D> items);
    void remove(int position);
    void modify(int position, D newData);
    void modify(int position, Action<D> action);
    void clear();

    void addFooter(View footer);
    void removeFooter(int footerPosition);
    void addHeader(View header);
    void removeHeader(int headerPosition);
    
    void setLoadMoreEnable(boolean enable);
    void loadMoreCompleted();
    void loadMoreError();
    void noMore();

```
## sample usages
```kotlin
// 对于只用到基础功能，不需要加载更多，头布局、空布局等等功能的，提供简单版本：
// 只包含多类型布局和点击事件，其他扩展功能请使用buildAdapterEx
buildAdapter<SampleEntity>(this) {
            register(R.layout.item_main) { holder, item, _ ->
                holder.setText(R.id.tvDesc, item.name)
            }
            itemClickListener { _, item ->
                startActivity(Intent(this@MainActivity, (item as SampleEntity).target))
            }
        }
```
## DataBinding adapter
```xml

<layout xmlns:android="http://schemas.android.com/apk/res/android">
    <data>
        <!-- you must set a variable with id "item" -->
        <variable
            name="item"
            type="xxx.xxx.XxEntity" />
    </data>

</layout>

```

```kotlin
buildAdapterEx(this) {
            // 注册DataBindingInjector
            register(DataBindingInjector(R.layout.item_big_data_binding))
            addHeader(header)
            addFooter(footer)

            itemClickListener { index, _ ->
                showToast("click position : $index")
            }
            itemLongClickListener { index, _ ->
                adapter.remove(index)
            }
}
```
## License
    MIT License

    Copyright (c) 2017 Lambert

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.