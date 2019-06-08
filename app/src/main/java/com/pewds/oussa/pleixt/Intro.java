package com.pewds.oussa.pleixt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.chyrta.onboarder.OnboarderActivity;
import com.chyrta.onboarder.OnboarderPage;

import java.util.ArrayList;
import java.util.List;


public class Intro extends OnboarderActivity {
    List<OnboarderPage> onboarderPages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onboarderPages = new ArrayList<OnboarderPage>();

        String[] texts = {"Messaging like never done before !", "Discover a new intuitive easy and fast way of chatting !"
                , "Sharing locations !", "Sharing your location was never easier before, We reinvented sharing and sending locations with friends, family and other people !"
                , "Simplified maps !", "We simplified maps to make them easier to understand, you can know the path to a place by one click !"
                , "Have fun !", "Plan trips with your friends , choose your first date's place or show a plumber your house !"};

        OnboarderPage onboarderPage1 = new OnboarderPage(texts[0], texts[1]);
        onboarderPage1.setBackgroundColor(R.color.colorPrimary);
        onboarderPage1.setImageResourceId(R.drawable.ic_intro_app);

        OnboarderPage onboarderPage2 = new OnboarderPage(texts[2], texts[3]);
        onboarderPage2.setBackgroundColor(R.color.colorPrimaryDark);
        onboarderPage2.setImageResourceId(R.drawable.location_white);

        OnboarderPage onboarderPage3 = new OnboarderPage(texts[4], texts[5]);
        onboarderPage3.setBackgroundColor(R.color.colorAccent);
        onboarderPage3.setImageResourceId(R.drawable.ic_map_white_24dp);
        OnboarderPage onboarderPage4 = new OnboarderPage(texts[6], texts[7]);
        onboarderPage4.setBackgroundColor(R.color.colorAccentDark);
        onboarderPage4.setImageResourceId(R.drawable.ic_confetti);

        onboarderPages.add(onboarderPage1);
        onboarderPages.add(onboarderPage2);
        onboarderPages.add(onboarderPage3);
        onboarderPages.add(onboarderPage4);
        setDividerVisibility(View.GONE);
        shouldUseFloatingActionButton(true);
        setSkipButtonHidden();

        setOnboardPagesReady(onboarderPages);

    }

    @Override
    public void onFinishButtonPressed() {
        getSharedPreferences("Meet", MODE_PRIVATE).edit().putBoolean("first",false).apply();
        startActivity(new Intent(Intro.this, MainActivity.class));

    }

    @Override
    public void onBackPressed() {
    }
}
