package com.moblin.newstickerdemo;

import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.moblin.widget.NewsTicker;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final List<String> HEADLINES = Arrays.asList(
            "Android Instant Apps starts initial live testing",
            "Fashion gets a digital upgrade with the Google Awareness API",
            "Android Wear 2.0 is here with new hardware features"
    );

    /** Activity methods */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupGui();
    }

    /** Private methods */

    private void setupGui() {
        NewsTicker ticker = lookup(R.id.news_ticker);
        ticker.setHeadlines(HEADLINES);
        ticker.setListener(new NewsTicker.HeadlineClickListener() {
            @Override
            public void onClick(int index) {
                Toast.makeText(MainActivity.this,
                        HEADLINES.get(index), Toast.LENGTH_SHORT)
                        .show();
            }
        });
        ticker.run();
    }

    private <T extends View> T lookup(@IdRes int viewId) {
        //noinspection unchecked
        return (T) findViewById(viewId);
    }
}
