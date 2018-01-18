 ![image](https://github.com/LambertCoding/LiteAdapter/blob/master/LiteAdapter.png)
 
 ## Features

* Fluent & simple API
* Multi View Type
* Auto LoadMore
* Header & Footer
* Auto Empty View

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
compile 'com.github.LambertCoding:LiteAdapter:1.0'
```
## Usages
#### step 1: create adapter
```java
adapter = new LiteAdapter.Builder<OnePiece>(this)
        .register(VIEW_TYPE_NORMAL, new ViewInjector<OnePiece>(R.layout.item_normal) {
            @Override
            public void bindData(ViewHolder holder, final OnePiece item, int position) {
                // step 2 : bind data
            }
        })
        .create();
recyclerView.setAdapter(adapter);
```
#### step 2: bind data
```java
holder.setText(R.id.tvDesc, item.getDesc())
        // .set...
        .with(R.id.ivImage, new ViewHolder.Action<ImageView>() {
            @Override
            public void doAction(ImageView view) {
                Glide.with(EmptyAndLoadMoreActivity.this)
                        .load(item.getImageRes())
                        .centerCrop()
                        .into(view);
            }
        });
```

#### step 3: Use LiteAdapter as normal adapter
```java
{
    data.add(new OnePiece("我是要做海贼王的男人", R.mipmap.ic_lufei, false));
    data.add(new OnePiece("路痴路痴路痴", R.mipmap.ic_suolong, false));
    data.add(new OnePiece("色河童色河童色河童", R.mipmap.ic_shanzhi, false));
}

adapter.setNewData(data);
```
## Advanced usages
```java
adapter = new LiteAdapter.Builder<OnePiece>(this)
        .register(VIEW_TYPE_NORMAL, new ViewInjector<OnePiece>(R.layout.item_normal) {
            @Override
            public void bindData(ViewHolder holder, final OnePiece item, int position) {
                // bindData
            }
        })
        .register(VIEW_TYPE_BIG, new ViewInjector<OnePiece>(R.layout.item_big) {
            @Override
            public void bindData(ViewHolder holder, final OnePiece item, int position) {
                // bindData
            }
        })
        // multi view type must set a viewTypeLinker
        .viewTypeLinker(new ViewTypeLinker<OnePiece>() {
            @Override
            public int viewType(OnePiece item, int position) {
                return item.isBigType() ? VIEW_TYPE_BIG : VIEW_TYPE_NORMAL;
            }
        })
        // empty view is disable if have header or footer view, not include load more footer
        .emptyView(emptyView)
        // You can add multi header and footer layout
        .headerView(headerView)
        .footerView(footerView)
        .enableLoadMore(new MoreLoader.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        })
        .itemClickListener(new LiteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, Object item) {

            }
        })
        .itemLongClickListener(new LiteAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position, Object item) {

            }
        })
        .create();
```
## api
```java
    D getItem(int position);
    void addData(D item);
    void addData(int position, D item);
    void addAll(List<D> items);
    void addAll(int position, List<D> items);
    void remove(int position);
    void modify(int position, D newData);
    void modify(int position, Action<D> action);
    void setNewData(List<D> items);
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