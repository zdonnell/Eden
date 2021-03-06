package com.zdonnell.eden.character.detail;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

import com.zdonnell.androideveapi.link.ILoadingActivity;
import com.zdonnell.eden.CharacterDetailActivity;
import com.zdonnell.eden.CharacterSheetActivity;

/**
 * This is a base {@link Fragment} which all Character Detail Fragments should extend
 *
 * @author Zach
 */
public abstract class DetailFragment extends Fragment {

    /**
     * Reference to the parent activity.  This will either be {@link CharacterDetailActivity} or {@link CharacterSheetActivity}
     * depending on layout configuration.
     */
    protected ILoadingActivity parentActivity;

    /**
     * Reference to the context of the application.
     */
    protected Context context;

    /**
     * Reference to the app's shared preferences.
     */
    protected SharedPreferences sharedPreferences;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        parentActivity = (ILoadingActivity) activity;
        context = activity.getApplicationContext();

        sharedPreferences = context.getSharedPreferences("eden_chardetail_preferences", Context.MODE_PRIVATE);
    }

    /**
     * Forces a reload of the current fragments data.<br><br>Implementations of this should not
     * be expected to forcibly request new API data, simply load it from where ever is appropriate.
     * From the cached data if the cache is valid, or the API if the cache is invalid/unavailable
     */
    public abstract void loadData();
}
