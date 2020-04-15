# SlidingButton

![Min SDK](https://img.shields.io/badge/Min%20Sdk-17-orange)
![Version](https://img.shields.io/badge/Version-v2.0.0-blue)

Slide button library for android, we hope this library is useful and easy to customize as you needed. For additional information you can see [**change log**](https://github.com/Source-Set-564/SlidingButton/blob/master/CHANGELOG.md)

### Sample Preview



#### Sample A

<center style="background:#FCFCFC;">
<img src="previews/preview_sample_a_1.jpg" alt="Preview A-1" width="300" />
<img src="previews/preview_sample_a_2.jpg" alt="Preview A-2" width="300" />
<img src="previews/preview_sample_a_3.jpg" alt="Preview A-3" width="300" />
<img src="previews/preview_sample_a_4.jpg" alt="Preview A-4" width="300" />
</center>

#### Sample B
<center style="background:#FCFCFC;">
<img src="previews/preview_sample_b_1.JPG" alt="Preview B-1" width="300" />
<img src="previews/preview_sample_b_2.JPG" alt="Preview B-2" width="300" />
<img src="previews/preview_sample_b_3.JPG" alt="Preview B-3" width="300" />
<img src="previews/preview_sample_b_4.JPG" alt="Preview B-4" width="300" />
</center>


--------

# Gradle

Add **dependencies** to your `build.gradle` file at `:app` or modules level

```groovy
implementation 'id.ss564.lib.slidingbutton:slidingbutton:<latest-version>'
```

Then **sync** your gradle.

----------

# How To Use

> **Notes** : This guide is still in process, I will work on it as soon as possible. So, keep checking later if you see this note.



In your *layout.xml* file, add the view

```xml
<id.ss564.lib.slidingbutton.SlidingButton
    android:id="@+id/slidingButton"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>
```

Yeah... just add like code above. It's pretty simple :smile:

You can use `SlidingButton.OnStateChangeListener` to find out if the button is shifted or not. Just add a little bit code like below on your  `java` or `kotlin` file, for example on **MainActivity**

- Java

```java
public class MainActivity extend AppCompatActivty {
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        SlidingButton mSlidingButton = findViewById(R.id.slidingButton);
        mSlidingButton.setOnStateChangeListener(new SlidingButton.OnStateChangeListener(){
            
            @Override
            public void onChange(boolean active){
                //do what you wanna to do
            }
        })
    }
}
```


- Kotlin

```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        //access view using synthetic, do your own style to access the view :)
        slidingButton.setOnStateChangeListener { active ->
	    //or using `object : SlidingButton.OnStateChangeListener` instead of lambda
	    
	    //do what you wanna to do
        }
    }
}
```

It's pretty simple right? :smile:



If you wanna customizing, please attention to the following attributes:

- `app:sliding_text`

  Use to set text to the view, value of this attribute is *string* that can be **hardcode**, **reference**,or **null**

- `app:sliding_text_color`

  Use to set text color, value of this attribute can be **hardcode** or **reference**. Absolutely you can reference it to *ColorStateList* :smile:

- `app:sliding_text_size`

  Use to set text size, value of this attribute is *dimension* that can be **hardcode** or **reference**

- `app:sliding_text_fontFamily`

  Use to set font family by name, value of this attribute is *string* that can be **hardcode**, or  **reference**

- `app:sliding_text_textStyle`

  Use to set text style, value of this attribute `normal`, `italic`,`bold`, or `bold|italic`

- `app:sliding_text_background`

  Use to set text background, value of this attribute can be **color**, **drawable**, or **null**. Absolutely you can reference it to *ColorStateList* or *StateListDrawable* :smile:

- `app:sliding_text_paddingStart`, `app:sliding_text_paddingTop`, `app:sliding_text_paddingEnd`, `app:sliding_text_paddingBottom`

  Use to set text padding, value is *dimension* that can be **hardcode** or **reference**

- `app:sliding_button_width`, `app:sliding_button_height`

- `app:sliding_button_icon`

- `app:sliding_button_icon_tint`

- `app:sliding_icon_scaleType`

- `app:sliding_button_background`

- `app:sliding_button_paddingStart`, `app:sliding_button_paddingTop`, `app:sliding_button_paddingEnd`, `app:sliding_button_paddingBottom`

- `app:sliding_button_marginStart`, `app:sliding_button_marginTop`, `app:sliding_button_marginEnd`, `app:sliding_button_marginBottom`

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



--------

# License
    MIT License
    
    Copyright (c) 2020 Source Set 564 Contributors
    
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
