 ![image]()
 
## Features

* Fluent & simple API
* Multi View Type
* Auto LoadMore
* Header & Footer
* Auto Empty View
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
implementation 'com.github.LambertCoding:LiteAdapter:1.0.3'
...
```
## Usages
#### step 1: create adapter
```kotlin
LiteAdapter.Builder<SampleEntity>(this)
            .register(object : ViewInjector<SampleEntity>(R.layout.item_main) {
                override fun bindData(holder: ViewHolder, item: SampleEntity, position: Int) {
                    // step 2: bind data
                }
            })
            .create()
            .attachTo(recyclerView)
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
## Advanced usages
```kotlin
LiteAdapter.Builder<OnePiece>(this)
                // register multi view type
                .register(object : ViewInjector<OnePiece>(R.layout.item_normal) {
                    override fun bindData(holder: ViewHolder, item: OnePiece, position: Int) {
                        // bind data
                    }
                })
                .register(object : ViewInjector<OnePiece>(R.layout.item_big) {
                    override fun bindData(holder: ViewHolder, item: OnePiece, position: Int) {
                        // bind data
                    }
                })
                // multi view type must set a injectorFinder, return the index of injector
                .injectorFinder { item, position -> if (item.isBigType) 1 else 0 }
                .headerView(header)
                .footerView(footer)
                .emptyView(emptyView)
                .enableLoadMore { loadMore() }
                .itemClickListener { position, item -> doSomeThing() }
                .itemLongClickListener { position, item -> doSomeThing() }
                .create()
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
LiteAdapter.Builder<OnePiece>(this)
                .register(DataBindingInjector(R.layout.item_big_data_binding))
                .create()
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