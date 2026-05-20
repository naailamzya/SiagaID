package com.siagaid.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.siagaid.fragment.GempaListFragment;

/**
 * Adapter ViewPager2 untuk 3 tab di HomeFragment:
 * - Tab 0: BMKG Terbaru
 * - Tab 1: BMKG Dirasakan
 * - Tab 2: USGS Internasional
 */
public class GempaTabAdapter extends FragmentStateAdapter {

    public static final int TAB_TERBARU       = 0;
    public static final int TAB_DIRASAKAN     = 1;
    public static final int TAB_INTERNASIONAL = 2;
    public static final int TAB_COUNT         = 3;

    public GempaTabAdapter(@NonNull FragmentManager fm, Lifecycle lifecycle) {
        super(fm, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return GempaListFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}