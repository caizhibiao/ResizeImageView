package com.wuwind.resizeimageview;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends Activity {

    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager) findViewById(R.id.viewPager);


        viewPager.setAdapter(new PagerAdapter() {

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ResizeImageView view = new ResizeImageView(MainActivity.this);
                view.setImageResource(R.drawable.ic_2);
                ViewPager.LayoutParams lp = new ViewPager.LayoutParams();
                lp.width = ViewPager.LayoutParams.MATCH_PARENT;
                lp.height = ViewPager.LayoutParams.MATCH_PARENT;
                view.setLayoutParams(lp);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        });
    }
}
