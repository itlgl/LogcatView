# LogcatView
![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)
![Api Level](https://img.shields.io/badge/api-16%2B-brightgreen.svg)
![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.itlgl.android/logcatview/badge.svg)

A tool library for displaying logcat in applications

## Quick Setup

### 1. Include library

Edit your build.gradle file and add below dependency:
```java
dependencies {
    implementation 'com.itlgl.android:logcatview:(latestVersion)'
}
```

### 2. Configure activity xml

Default:
```xml
<com.itlgl.android.logcatview.LogcatView
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

Filter specified tags:
```xml
<com.itlgl.android.logcatview.LogcatView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:filterTags="L System.out"/>
```

Custom logcat cmd,add `-v time` to ensure that the log color will not be confused:
```xml
<com.itlgl.android.logcatview.LogcatView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:customCmd="logcat System.out:V *:S -v time"/>
```
