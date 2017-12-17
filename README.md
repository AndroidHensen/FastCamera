# FastCamera

A simple, practical camera for Android.You can use FastCamera to Start system camera,then Compress images,crop pictures,or do nothing.It can be used to upload pictures and modify avatar and so on.

<img src="/preview/preview1.png" height="400px"></img>
<img src="/preview/preview2.png" height="400px"></img>
<img src="/preview/preview3.png" height="400px"></img>

# Usage

### Step1

Add it in your root build.gradle at the end of repositories

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

### Step2

Add the dependency

```
dependencies {
	compile 'com.github.AndroidHensen:FastCamera:1.0.1'
}
```

### Step3

Open the FastCamera

```
FastCamera.with(this)
	.requestCode(REQUEST_CAMERA_CODE)
	.start();
```

And you can get the raw photo path.Note that the unprocessed file size is too large

```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK && data != null) {
		String path = data.getStringExtra("result");
	}
}
```

If you want to get the compressed path,you can use

```
FastCamera.with(this)
	.requestCode(REQUEST_CAMERA_CODE)
	.needCompress(true)
	.start();
```

If you want to get the crop picture,you can use

```
FastCamera.with(this)
	.requestCode(REQUEST_CAMERA_CODE)
	.needCrop(true)
	.cropSize(1, 1, 800, 800)
	.start();
```

# Changelog

* 1.0.1
    * Change the package name
    * Modify the minSdkVersion 14
* 1.0
	* Initial release
	
# Thanks

* Compress:[compile 'top.zibin:Luban:1.1.3'](https://github.com/Curzibn/Luban)

# License

```
Copyright 2017 AndroidHensen

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
