analytics-android-integration-localytics
=======================================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.segment.analytics.android.integrations/localytics/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.segment.analytics.android.integrations/localytics)
[![Javadocs](http://javadoc-badge.appspot.com/com.segment.analytics.android.integrations/localytics.svg?label=javadoc)](http://javadoc-badge.appspot.com/com.segment.analytics.android.integrations/localytics)

Localytics integration for [analytics-android](https://github.com/segmentio/analytics-android).

## Installation

To install the Segment-Localytics integration, simply add this line to your gradle file: 

```
compile 'com.segment.analytics.android:analytics-integration-localytics:3.4.0'
```

Since Localytics does not publish to Maven Central, you will also need to add the Maven Localytics repo. 

```
repositories {
  mavenCentral()
  maven { url 'http://maven.localytics.com/public' }
}
```

## Usage

Next, register the integration with our SDK.  To do this, import the Localytics integration:


```
import com.segment.analytics.internal.integrations.LocalyticsIntegration;

```

And add the following line:

```
analytics = new Analytics.Builder(this, "write_key")
                .use(LocalyticsIntegration.FACTORY)
                .build();
```

## License

```
WWWWWW||WWWWWW
 W W W||W W W
      ||
    ( OO )__________
     /  |           \
    /o o|    MIT     \
    \___/||_||__||_|| *
         || ||  || ||
        _||_|| _||_||
       (__|__|(__|__|

The MIT License (MIT)

Copyright (c) 2014 Segment, Inc.

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
```
