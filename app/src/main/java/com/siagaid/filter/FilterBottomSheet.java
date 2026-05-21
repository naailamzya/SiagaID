package com.siagaid.filter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.RangeSlider;
import com.siagaid.R;

import java.util.List;

/**
 * Bottom Sheet Dialog untuk filter gempa.
 * Menyimpan state filter ke SharedPreferences via FilterState.
 */
public class FilterBottomSheet extends BottomSheetDialogFragment {

    // ========================
    // INTERFACE CALLBACK
    // ========================
    public interface OnFilterAppliedListener {
        void onFilterApplied(FilterState filterState);
    }

    private OnFilterAppliedListener listener;
    private FilterState currentFilter;

    // Views
    private RangeSlider rangeSliderMagnitude;
    private TextView tvMagnitudeRange;
    private RadioGroup rgKedalaman;
    private CheckBox cbBmkgTerbaru;
    private CheckBox cbBmkgDirasakan;
    private CheckBox cbUsgs;
    private RadioGroup rgWaktu;
    private Button btnReset;
    private Button btnApply;

    // ========================
    // FACTORY METHOD
    // ========================
    public static FilterBottomSheet newInstance(FilterState currentFilter) {
        FilterBottomSheet sheet = new FilterBottomSheet();
        sheet.currentFilter = currentFilter;
        return sheet;
    }

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }

    // ========================
    // LIFECYCLE
    // ========================
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadCurrentFilter();
        setupListeners();
    }

    // ========================
    // INIT VIEWS
    // ========================
    private void initViews(View view) {
        rangeSliderMagnitude = view.findViewById(R.id.range_slider_magnitude);
        tvMagnitudeRange     = view.findViewById(R.id.tv_magnitude_range);
        rgKedalaman          = view.findViewById(R.id.rg_kedalaman);
        cbBmkgTerbaru        = view.findViewById(R.id.cb_bmkg_terbaru);
        cbBmkgDirasakan      = view.findViewById(R.id.cb_bmkg_dirasakan);
        cbUsgs               = view.findViewById(R.id.cb_usgs);
        rgWaktu              = view.findViewById(R.id.rg_waktu);
        btnReset             = view.findViewById(R.id.btn_reset_filter);
        btnApply             = view.findViewById(R.id.btn_apply_filter);
    }

    // ========================
    // LOAD FILTER KE UI
    // ========================
    private void loadCurrentFilter() {
        if (currentFilter == null) {
            currentFilter = new FilterState();
        }

        // Magnitude slider
        rangeSliderMagnitude.setValues(
                currentFilter.magnitudeMin,
                currentFilter.magnitudeMax
        );
        updateMagnitudeLabel(
                currentFilter.magnitudeMin,
                currentFilter.magnitudeMax
        );

        // Kedalaman
        switch (currentFilter.depthCategory) {
            case FilterState.DEPTH_DANGKAL:
                rgKedalaman.check(R.id.rb_depth_dangkal);  break;
            case FilterState.DEPTH_MENENGAH:
                rgKedalaman.check(R.id.rb_depth_menengah); break;
            case FilterState.DEPTH_DALAM:
                rgKedalaman.check(R.id.rb_depth_dalam);    break;
            default:
                rgKedalaman.check(R.id.rb_depth_semua);    break;
        }

        // Sumber
        cbBmkgTerbaru.setChecked(currentFilter.showBmkgTerbaru);
        cbBmkgDirasakan.setChecked(currentFilter.showBmkgDirasakan);
        cbUsgs.setChecked(currentFilter.showUsgs);

        // Waktu
        switch (currentFilter.timeRange) {
            case FilterState.TIME_3HARI:
                rgWaktu.check(R.id.rb_time_3hari); break;
            case FilterState.TIME_7HARI:
                rgWaktu.check(R.id.rb_time_7hari); break;
            case FilterState.TIME_SEMUA:
                rgWaktu.check(R.id.rb_time_semua); break;
            default:
                rgWaktu.check(R.id.rb_time_24jam); break;
        }
    }

    // ========================
    // SETUP LISTENERS
    // ========================
    private void setupListeners() {
        // Range slider magnitude
        rangeSliderMagnitude.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            if (values.size() >= 2) {
                updateMagnitudeLabel(values.get(0), values.get(1));
            }
        });

        // Reset
        btnReset.setOnClickListener(v -> {
            currentFilter = new FilterState();
            currentFilter.reset();
            loadCurrentFilter();
        });

        // Apply
        btnApply.setOnClickListener(v -> applyFilter());
    }

    // ========================
    // APPLY FILTER
    // ========================
    private void applyFilter() {
        if (currentFilter == null) currentFilter = new FilterState();

        // Magnitude
        List<Float> magValues = rangeSliderMagnitude.getValues();
        if (magValues.size() >= 2) {
            currentFilter.magnitudeMin = magValues.get(0);
            currentFilter.magnitudeMax = magValues.get(1);
        }

        // Kedalaman
        int depthId = rgKedalaman.getCheckedRadioButtonId();
        if (depthId == R.id.rb_depth_dangkal)       currentFilter.depthCategory = FilterState.DEPTH_DANGKAL;
        else if (depthId == R.id.rb_depth_menengah) currentFilter.depthCategory = FilterState.DEPTH_MENENGAH;
        else if (depthId == R.id.rb_depth_dalam)    currentFilter.depthCategory = FilterState.DEPTH_DALAM;
        else                                         currentFilter.depthCategory = FilterState.DEPTH_SEMUA;

        // Sumber
        currentFilter.showBmkgTerbaru   = cbBmkgTerbaru.isChecked();
        currentFilter.showBmkgDirasakan = cbBmkgDirasakan.isChecked();
        currentFilter.showUsgs          = cbUsgs.isChecked();

        // Waktu
        int waktuId = rgWaktu.getCheckedRadioButtonId();
        if (waktuId == R.id.rb_time_3hari)      currentFilter.timeRange = FilterState.TIME_3HARI;
        else if (waktuId == R.id.rb_time_7hari) currentFilter.timeRange = FilterState.TIME_7HARI;
        else if (waktuId == R.id.rb_time_semua) currentFilter.timeRange = FilterState.TIME_SEMUA;
        else                                     currentFilter.timeRange = FilterState.TIME_24JAM;

        // Simpan ke SharedPreferences
        currentFilter.save(requireContext());

        // Callback ke HomeFragment
        if (listener != null) listener.onFilterApplied(currentFilter);

        dismiss();
    }

    // ========================
    // HELPER
    // ========================
    private void updateMagnitudeLabel(float min, float max) {
        if (tvMagnitudeRange != null) {
            tvMagnitudeRange.setText(
                    String.format(java.util.Locale.getDefault(),
                            "%.1f - %.1f", min, max)
            );
        }
    }
}