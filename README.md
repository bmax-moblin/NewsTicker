## Description
News Ticker is a widget, that shows scrolling text running from right to left across the screen.

Click on the image below to see the demo on YouTube.

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/PlRBQHb6WRo/0.jpg)](https://youtu.be/PlRBQHb6WRo)
## Usage (XML)
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.moblin.widget.NewsTicker
        android:id="@+id/news_ticker"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"
        android:background="@color/colorPrimary"/>

</FrameLayout>
```

## Usage (Java)
<pre><code>
NewsTicker ticker = (NewsTicker) findViewById(R.id.news_ticker);
ticker.setHeadlines(mHeadlines);
ticker.setListener(new NewsTicker.HeadlineClickListener() {
    @Override
    public void onClick(int index) {
        Toast.makeText(MainActivity.this, mHeadlines.get(index), Toast.LENGTH_SHORT)
             .show();
    }
});
ticker.run();
</code></pre>
