# MediaLibrarySelector

Android 应用开发中，选择图片功能基本会出现在95%以上的产品中，然而，获取相册的方式千奇百怪，在Android 13中，官方提供了一个照片选择器，但是在低版本中是不可以使用的。

## 要实现的功能

- 可以在Activity和Fragment 中直接使用，不需要用户管理其由于生命周期带来的各种问题；
- 必须要异步获取、避免重复加载，以此提高性能；
- 状态的保存恢复不需要用户处理
- 可以自由选择图像，比如选择类型：视频or图片，选择格式：jpeg、png、gif等
- 提供通用适配器，为UI书写更方便，但不提供样式（照片选择器的功能不大，需求种类不少，统一的UI样式局限性较大）
- 提供拍照功能



## 使用

```
implementation 'io.github.kongxiaoan:media:0.0.4'
```

```kotlin

MediaSelector.form(this)
            .chooseMimeType(MimeType.ofAll())
            .showSingleMediaType(true)
            .capture(true)
            .captureBindParams(
                registerForActivityResult(
                    object : ActivityResultContracts.TakePicture() {
                    },
                ) {
                },
                CaptureStrategy(true, "com.kpa.test.fileProvider", "test"),
            )
            .forResult(object : MediaSelectorResultCallback {
                override fun resultCallback(mimeType: MediaMimeType, cursor: Cursor?) {
                    Log.d("MediaLibrarySelector", "type $mimeType")
                    if (mimeType === MediaMimeType.PHOTO_LIST_TYPE) {
                        mAdapter?.swapCursor(cursor)
                    } else {
                        // 分类布局处理
//                        albumAdapter.swapCursor(cursor);
                    }
                }
            })
```

