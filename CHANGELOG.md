# Change Log



## Version 2.0.0 _(15 Apr 2020)_

Behavior change on state listener, rename to `OnStateChangeListener`. So you can listen *state change* using method `setOnStateChangeListener()` with value `SlidingButton.OnStateChangeListener`.

Change state rename to `slidingButton.changeState(active,animated)`



Additional listener when button sliding. You can now listen it using method `setOnSlidingListener()` with value `SlidingButton.OnSlidingListener`. For example :

- Java

  ```java
  slidingButton.setOnSlidingListener(new SlidingButton.OnSlidingListener{
      
      @Override
      public void onChange(float progress){
          // do what you wanna do
      }
  });
  ```

  

- Kotlin

  ```kotlin
  slidingButton.setOnSlidingListener { progress ->
      // do what you wanna do
  }
  ```



Additional features like **text alpha** when sliding the button, **track indicator** and **corner radius** (especially for API 21). You can set using attributes like the following :

- `app:sliding_enabledTextAlpha`

  Use to alpha text when sliding, value of this attribute **Boolean**. Default value true

- `app:sliding_showTrack`

  Use to show track indicator when sliding, value of this attribute **Boolean**. Default value false

- `app:sliding_trackBackground`

  Use to set track indicator background.

- `app:sliding_trackBackgroundTint`

  Use to set tint track indicator background.

- `app:sliding_trackExtendTo`

  Use to set track extended to `container` or `button`

- `app:sliding_corner_radius` only on API level 21 (Lollipop)



-------

## Version 1.0.2 _(09 Apr 2020)_

Add method to change status programmatically. So you can change status use method `changeStatus(active,animated)` as you needed



-------

## Version 1.0.1 _(03 Apr 2020)_
Linked to `jcenter()`, Now not need to add maven url to repositories `build.gradle` project level.
If you still using `v1.0.0`, should add maven url to repositories `build.gradle` project level
```groovy
repositories {
    ...
    maven { url 'https://dl.bintray.com/ss564/SlidingButton' }
    ...
}
```



-------

## Version 1.0.0 _(03 Apr 2020)_

Slide button library for android, we hope this library is useful and easy to customize as you needed.



-------

## Version 1.0.0-rc _(03 Apr 2020)_

First launch as pre-release version

> **Note** : Don't use this version, coz we have an missed configuration, so this version can't be used
