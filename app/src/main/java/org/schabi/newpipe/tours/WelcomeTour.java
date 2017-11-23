package org.schabi.newpipe.tours;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import org.schabi.newpipe.R;

/**
 * Created by Tobias on 08.11.2017.
 */

public class WelcomeTour extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();

        // Add your slide fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
        /*addSlide(firstFragment);
        addSlide(secondFragment);
        addSlide(thirdFragment);
        addSlide(fourthFragment);*/

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        //addSlide(AppIntroFragment.newInstance(title, description, image, backgroundColor));
        addSlide(AppIntroFragment.newInstance(getString(R.string.welcome_tour_welcome_title), getString(R.string.welcome_tour_welcome_description), R.mipmap.ic_launcher , Color.rgb(34,34,34)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.welcome_tour_about_title), getString(R.string.welcome_tour_about_description), R.mipmap.ic_launcher , Color.rgb(34,34,34)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.welcome_tour_playlist_title), getString(R.string.welcome_tour_playlist_description), R.mipmap.ic_launcher , Color.rgb(34,34,34)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.welcome_tour_download_title), getString(R.string.welcome_tour_download_description), R.mipmap.ic_launcher , Color.rgb(34,34,34)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.welcome_tour_explore_title), getString(R.string.welcome_tour_explore_description), R.mipmap.ic_launcher , Color.rgb(34,34,34)));


        // OPTIONAL METHODS
        // Override bar/separator color.
        //setBarColor(Color.parseColor("#222222"));
        setSeparatorColor(Color.parseColor("#505050"));

        // Hide Skip/Done button.
        showSkipButton(true);
        setProgressButtonEnabled(true);


    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
