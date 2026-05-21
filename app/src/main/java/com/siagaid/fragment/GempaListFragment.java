package com.siagaid.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.siagaid.R;
import com.siagaid.activity.DetailActivity;
import com.siagaid.adapter.GempaAdapter;
import com.siagaid.filter.FilterState;
import com.siagaid.filter.GempaFilterHelper;
import com.siagaid.model.Gempa;
import com.siagaid.network.ApiClient;
import com.siagaid.network.BmkgResponse;
import com.siagaid.model.GempaUSGS;
import com.siagaid.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment yang dipakai untuk setiap tab di HomeFragment.
 * Menerima tabType via newInstance() untuk menentukan API mana yang dipanggil.
 */
public class GempaListFragment extends Fragment {

    private static final String ARG_TAB_TYPE = "tab_type";

    // Views
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout layoutLoading;
    private LinearLayout layoutError;
    private LinearLayout layoutEmpty;
    private TextView tvErrorMessage;
    private Button btnRetry;

    // Adapter & Data
    private GempaAdapter adapter;
    private List<Gempa> originalList = new ArrayList<>();
    private int tabType;

    // ========================
    // FACTORY METHOD
    // ========================
    public static GempaListFragment newInstance(int tabType) {
        GempaListFragment fragment = new GempaListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_TYPE, tabType);
        fragment.setArguments(args);
        return fragment;
    }

    // ========================
    // LIFECYCLE
    // ========================
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabType = getArguments().getInt(ARG_TAB_TYPE, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gempa_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        loadData();
    }

    // ========================
    // INIT
    // ========================
    private void initViews(View view) {
        recyclerView   = view.findViewById(R.id.recycler_view);
        swipeRefresh   = view.findViewById(R.id.swipe_refresh);
        layoutLoading  = view.findViewById(R.id.layout_loading);
        layoutError    = view.findViewById(R.id.layout_error);
        layoutEmpty    = view.findViewById(R.id.layout_empty);
        tvErrorMessage = view.findViewById(R.id.tv_error_message);
        btnRetry       = view.findViewById(R.id.btn_retry);

        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadData);
        btnRetry.setOnClickListener(v -> loadData());
    }

    private void setupRecyclerView() {
        adapter = new GempaAdapter(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        adapter.setOnItemClickListener(gempa -> {
            Intent intent = new Intent(requireContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_GEMPA, gempa);
            startActivity(intent);
        });
    }

    // ========================
    // LOAD DATA
    // ========================
    public void loadData() {
        if (!NetworkUtils.isConnected(requireContext())) {
            showError("Tidak ada koneksi internet.\nPastikan WiFi atau data aktif.");
            swipeRefresh.setRefreshing(false);
            return;
        }

        showLoading();

        switch (tabType) {
            case 0: loadBmkgTerbaru();       break;
            case 1: loadBmkgDirasakan();     break;
            case 2: loadUsgsInternasional(); break;
        }
    }

    // ========================
    // API CALLS
    // ========================
    private void loadBmkgTerbaru() {
        ApiClient.getBmkgService().getGempaTerkini()
                .enqueue(new Callback<BmkgResponse.GempaTerkiniResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<BmkgResponse.GempaTerkiniResponse> call,
                                           @NonNull Response<BmkgResponse.GempaTerkiniResponse> response) {
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().Infogempa != null) {
                            List<Gempa> list = response.body().Infogempa.gempa;
                            if (list != null) {
                                for (Gempa g : list) g.setSumber("BMKG_TERBARU");
                                setData(list);
                            } else {
                                showEmpty("Tidak ada data gempa");
                            }
                        } else {
                            showError("Gagal memuat data BMKG");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BmkgResponse.GempaTerkiniResponse> call,
                                          @NonNull Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        showError("Error: " + t.getMessage());
                    }
                });
    }

    private void loadBmkgDirasakan() {
        ApiClient.getBmkgService().getGempaDirasakan()
                .enqueue(new Callback<BmkgResponse.GempaDirasakanResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<BmkgResponse.GempaDirasakanResponse> call,
                                           @NonNull Response<BmkgResponse.GempaDirasakanResponse> response) {
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().Infogempa != null) {
                            List<Gempa> list = response.body().Infogempa.gempa;
                            if (list != null) {
                                for (Gempa g : list) g.setSumber("BMKG_DIRASAKAN");
                                setData(list);
                            } else {
                                showEmpty("Tidak ada gempa dirasakan");
                            }
                        } else {
                            showError("Gagal memuat data BMKG Dirasakan");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BmkgResponse.GempaDirasakanResponse> call,
                                          @NonNull Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        showError("Error: " + t.getMessage());
                    }
                });
    }

    private void loadUsgsInternasional() {
        ApiClient.getUsgsService().getGempaUSGS()
                .enqueue(new Callback<GempaUSGS.FeatureCollection>() {
                    @Override
                    public void onResponse(@NonNull Call<GempaUSGS.FeatureCollection> call,
                                           @NonNull Response<GempaUSGS.FeatureCollection> response) {
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<GempaUSGS.Feature> features = response.body().features;
                            if (features != null && !features.isEmpty()) {
                                List<Gempa> list = new ArrayList<>();
                                for (GempaUSGS.Feature f : features) {
                                    list.add(f.toGempa());
                                }
                                setData(list);
                            } else {
                                showEmpty("Tidak ada data gempa internasional");
                            }
                        } else {
                            showError("Gagal memuat data USGS");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GempaUSGS.FeatureCollection> call,
                                          @NonNull Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        showError("Error: " + t.getMessage());
                    }
                });
    }

    // ========================
    // DATA & FILTER
    // ========================
    private void setData(List<Gempa> list) {
        originalList = new ArrayList<>(list);
        applyFilter(FilterState.load(requireContext()), "");
    }

    /**
     * Dipanggil dari HomeFragment saat filter/search berubah.
     */
    public void applyFilter(FilterState filterState, String keyword) {
        if (originalList == null || originalList.isEmpty()) return;

        filterState.searchKeyword = keyword;
        List<Gempa> filtered = GempaFilterHelper.applyAllFilters(
                originalList, filterState
        );

        if (filtered.isEmpty()) {
            showEmpty(keyword.isEmpty() ?
                    "Tidak ada data" :
                    "Tidak ada gempa untuk '" + keyword + "'"
            );
        } else {
            showContent();
            adapter.setData(filtered);
        }
    }

    public List<Gempa> getOriginalList() {
        return originalList;
    }

    // ========================
    // STATE UI
    // ========================
    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.GONE);
    }

    private void showContent() {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.GONE);
        tvErrorMessage.setText(message);
    }

    private void showEmpty(String message) {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        swipeRefresh.setVisibility(View.GONE);
        TextView tvEmpty = requireView().findViewById(R.id.tv_empty_message);
        if (tvEmpty != null) tvEmpty.setText(message);
    }
}