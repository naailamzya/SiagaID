package com.siagaid.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.siagaid.R;
import com.siagaid.adapter.GempaTabAdapter;
import com.siagaid.filter.FilterBottomSheet;
import com.siagaid.filter.FilterState;
import com.siagaid.utils.ThemeManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * HomeFragment: menampilkan 3 tab gempa + search + filter + sort.
 */
public class HomeFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private GempaTabAdapter tabAdapter;

    private EditText etSearch;
    private ImageButton btnFilter;
    private ImageButton btnSort;
    private ImageButton btnTheme;
    private TextView tvFilterBadge;
    private TextView tvFilterSummary;
    private ChipGroup chipGroupFilter;

    private FilterState currentFilter;

    // Debounce untuk search
    private final ScheduledExecutorService debounceExecutor =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> debounceTask;

    // ========================
    // LIFECYCLE
    // ========================
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentFilter = FilterState.load(requireContext());
        initViews(view);
        setupViewPager();
        setupSearch();
        setupFilterButton();
        setupSortButton();
        setupChips();
        setupThemeButton();
        updateFilterBadge();
        updateFilterSummary();
    }

    // ========================
    // INIT VIEWS
    // ========================
    private void initViews(View view) {
        tabLayout       = view.findViewById(R.id.tab_layout);
        viewPager       = view.findViewById(R.id.view_pager);
        etSearch        = view.findViewById(R.id.et_search);
        btnFilter       = view.findViewById(R.id.btn_filter);
        btnSort         = view.findViewById(R.id.btn_sort);
        btnTheme        = view.findViewById(R.id.btn_theme);
        tvFilterBadge   = view.findViewById(R.id.tv_filter_badge);
        tvFilterSummary = view.findViewById(R.id.tv_filter_summary);
        chipGroupFilter = view.findViewById(R.id.chip_group_filter);
    }

    // ========================
    // SETUP VIEWPAGER + TABS
    // ========================
    private void setupViewPager() {
        tabAdapter = new GempaTabAdapter(getChildFragmentManager(), getLifecycle());
        viewPager.setAdapter(tabAdapter);
        viewPager.setOffscreenPageLimit(3);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText(getString(R.string.tab_terbaru));       break;
                case 1: tab.setText(getString(R.string.tab_dirasakan));     break;
                case 2: tab.setText(getString(R.string.tab_internasional)); break;
            }
        }).attach();
    }

    // ========================
    // SETUP SEARCH
    // ========================
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Debounce 300ms
                if (debounceTask != null) debounceTask.cancel(false);
                debounceTask = debounceExecutor.schedule(() -> {
                    String keyword = s.toString().trim();
                    requireActivity().runOnUiThread(() -> applyFilterToCurrentTab(keyword));
                }, 300, TimeUnit.MILLISECONDS);
            }
        });
    }

    // ========================
    // SETUP FILTER BUTTON
    // ========================
    private void setupFilterButton() {
        btnFilter.setOnClickListener(v -> {
            FilterBottomSheet sheet = FilterBottomSheet.newInstance(currentFilter);
            sheet.setOnFilterAppliedListener(filterState -> {
                currentFilter = filterState;
                applyFilterToCurrentTab(etSearch.getText().toString().trim());
                updateFilterBadge();
                updateFilterSummary();
            });
            sheet.show(getChildFragmentManager(), "FilterBottomSheet");
        });
    }

    // ========================
    // SETUP SORT BUTTON
    // ========================
    private void setupSortButton() {
        btnSort.setOnClickListener(v -> {
            String[] options = {
                    "Terbaru",
                    "Terlama",
                    "Magnitudo Tertinggi",
                    "Magnitudo Terendah",
                    "Kedalaman Terdangkal"
            };

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Urutkan")
                    .setItems(options, (dialog, which) -> {
                        currentFilter.sortBy = which;
                        currentFilter.save(requireContext());
                        applyFilterToCurrentTab(etSearch.getText().toString().trim());
                        updateFilterBadge();
                    })
                    .show();
        });
    }

    // ========================
    // SETUP CHIPS
    // ========================
    private void setupChips() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chip_semua) {
                currentFilter.magnitudeMin = 0.0f;
                currentFilter.magnitudeMax = 9.0f;
                currentFilter.searchKeyword = "";

            } else if (checkedId == R.id.chip_danger) {
                currentFilter.magnitudeMin = 6.0f;
                currentFilter.magnitudeMax = 9.0f;

            } else if (checkedId == R.id.chip_warning) {
                currentFilter.magnitudeMin = 5.0f;
                currentFilter.magnitudeMax = 5.9f;

            } else if (checkedId == R.id.chip_dirasakan) {
                currentFilter.magnitudeMin = 0.0f;
                currentFilter.magnitudeMax = 9.0f;
                currentFilter.showBmkgDirasakan = true;
                currentFilter.showBmkgTerbaru   = false;
                currentFilter.showUsgs           = false;

            } else if (checkedId == R.id.chip_hari_ini) {
                currentFilter.timeRange = FilterState.TIME_24JAM;
            }

            currentFilter.save(requireContext());
            applyFilterToCurrentTab(etSearch.getText().toString().trim());
            updateFilterBadge();
            updateFilterSummary();
        });
    }

    // ========================
    // SETUP THEME BUTTON
    // ========================
    private void setupThemeButton() {
        updateThemeIcon();
        btnTheme.setOnClickListener(v -> {
            ThemeManager.toggleTheme(requireContext());
            updateThemeIcon();
        });
    }

    private void updateThemeIcon() {
        if (ThemeManager.isDarkMode(requireContext())) {
            btnTheme.setImageResource(R.drawable.ic_dark_mode);
        } else {
            btnTheme.setImageResource(R.drawable.ic_dark_mode);
        }
    }

    // ========================
    // APPLY FILTER KE TAB AKTIF
    // ========================
    private void applyFilterToCurrentTab(String keyword) {
        int currentTab = viewPager.getCurrentItem();
        GempaListFragment fragment = getGempaListFragment(currentTab);
        if (fragment != null) {
            currentFilter.searchKeyword = keyword;
            fragment.applyFilter(currentFilter, keyword);
        }
    }

    private GempaListFragment getGempaListFragment(int position) {
        String tag = "f" + tabAdapter.getItemId(position);
        return (GempaListFragment) getChildFragmentManager().findFragmentByTag(tag);
    }

    // ========================
    // UPDATE FILTER BADGE
    // ========================
    private void updateFilterBadge() {
        int count = currentFilter.getActiveFilterCount();
        if (count > 0) {
            tvFilterBadge.setVisibility(View.VISIBLE);
            tvFilterBadge.setText(String.valueOf(count));
        } else {
            tvFilterBadge.setVisibility(View.GONE);
        }
    }

    // ========================
    // UPDATE FILTER SUMMARY
    // ========================
    private void updateFilterSummary() {
        String summary = currentFilter.getFilterSummary();
        if (!summary.isEmpty()) {
            tvFilterSummary.setVisibility(View.VISIBLE);
            tvFilterSummary.setText(summary);
        } else {
            tvFilterSummary.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        debounceExecutor.shutdown();
    }
}