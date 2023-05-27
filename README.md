# Pocketbase Android Example

Android example app to run [pocketbase](https://pocketbase.io/) from android device
using [pocketbaseMobile](https://github.com/rohitsangwan01/pocketbase_mobile)

## Setup

add a folder in `Project>app>libs` and add `pocketbaseMobile.aar` file
from [here](https://github.com/rohitsangwan01/pocketbase_mobile)

import in app level `build.gradle`

```gradle
dependencies {
    ...
    implementation fileTree(include: ['*.jar', '*.aar'], dir: 'libs')
}
```

## Usage

Use CoroutineScope to call pocketbase methods ( import kotlin coroutines libraries)

```kotlin
private val uiScope = CoroutineScope(Dispatchers.Main + Job())
```

To start pocketbase

```kotlin
// use dataPath where app have write access, for example temporary cache path `context.cacheDir.absolutePath`
uiScope.launch {
    withContext(Dispatchers.IO) {
        PocketbaseMobile.startPocketbase(dataPath, hostname, port)
    }
}
```

To stop pocketbase

```kotlin
uiScope.launch {
    withContext(Dispatchers.IO) {
        PocketbaseMobile.stopPocketbase()
    }
}
```

To listen pocketbase events, and also handle custom api requests

`pocketbaseMobile` have two custom routes as well ,`/api/nativeGet` and `/api/nativePost`, we can
get these routes in this callback and return response from kotlin

```kotlin
PocketbaseMobile.registerNativeBridgeCallback { command, data ->
    this.runOnUiThread {
        // Update ui from here
    }
    // return response back to pocketbase
    "response from native"
}
```

## Screenshot

![](https://github.com/rohitsangwan01/pocketbase_server_android_example/assets/59526499/6c50c4bb-83f1-4242-b381-6b0a0612956b)


