package com.siagaid.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.clustering.ClusterManager;
import com.siagaid.R;
import com.siagaid.activity.DetailActivity;
import com.siagaid.maps.CustomClusterRenderer;
import com.siagaid.maps.GempaClusterItem;
import com.siagaid.model.Gempa;
import com.siagaid.network.ApiClient;
import com.siagaid.network.BmkgResponse;
import com.siagaid.model.GempaUSGS;
import com.siagaid.utils.MapsUtils;
import com.siagaid.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;

/**
 * MapsFragment: Full screen Google Maps dengan cluster, radius, dan bottom sheet.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private ClusterManager<GempaClusterItem> clusterManager;
    private Circle activeCircle;

    private TextView tvGempaCount;
    private ImageButton btnLayer;
    private FloatingActionButton fabMyLocation;

    // Bottom Sheet
    private LinearLayout bottomSheet;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private TextView bsTvMagnitude;
    private TextView bsTvWilayah;
    private TextView bsTvWaktu;
    private TextView bsTvKedalaman;
    private TextView bsTvPotensi;
    private TextView bsTvSumber;
    private MaterialButton bsBtnDetail;
    private MaterialButton bsBtnNavigasi;

    private List<Gempa> allGempaList = new ArrayList<>();
    private Gempa selectedGempa;

    private int currentMapType = GoogleMap.MAP_TYPE_NORMAL;

    // ========================
    // LIFECYCLE
    // ========================
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupMap();
        setupBottomSheet();
        setupButtons();
    }

    // ========================
    // INIT VIEWS
    // ========================
    private void initViews(View view) {
        tvGempaCount  = view.findViewById(R.id.tv_gempa_count);
        btnLayer      = view.findViewById(R.id.btn_layer);
        fabMyLocation = view.findViewById(R.id.fab_my_location);
        bottomSheet   = view.findViewById(R.id.bottom_sheet);

        bsTvMagnitude  = view.findViewById(R.id.bs_tv_magnitude);
        bsTvWilayah    = view.findViewById(R.id.bs_tv_wilayah);
        bsTvWaktu      = view.findViewById(R.id.bs_tv_waktu);
        bsTvKedalaman  = view.findViewById(R.id.bs_tv_kedalaman);
        bsTvPotensi    = view.findViewById(R.id.bs_tv_potensi);
        bsTvSumber     = view.findViewById(R.id.bs_tv_sumber);
        bsBtnDetail    = view.findViewById(R.id.bs_btn_detail);
        bsBtnNavigasi  = view.findViewById(R.id.bs_btn_navigasi);
    }

    // ========================
    // SETUP MAP
    // ========================
    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Setup ClusterManager
        clusterManager = new ClusterManager<>(requireContext(), googleMap);
        clusterManager.setRenderer(
                new CustomClusterRenderer(requireContext(), googleMap, clusterManager)
        );

        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);

        // Cluster item click → tampilkan bottom sheet
        clusterManager.setOnClusterItemClickListener(item -> {
            selectedGempa = item.getGempa();
            showBottomSheet(selectedGempa);
            drawRadiusCircle(selectedGempa);
            MapsUtils.zoomToGempa(googleMap, selectedGempa);
            return true;
        });

        // Cluster click → zoom in
        clusterManager.setOnClusterClickListener(cluster -> {
            float zoom = googleMap.getCameraPosition().zoom + 2;
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    cluster.getPosition(), zoom
            ));
            return true;
        });

        // Map tap → tutup bottom sheet
        googleMap.setOnMapClickListener(latLng -> {
            hideBottomSheet();
            clearCircle();
        });

        // My location
        enableMyLocation();

        // Default camera: Indonesia
        MapsUtils.setDefaultCamera(googleMap);

        // Load data
        loadAllGempa();
    }

    // ========================
    // LOAD DATA
    // ========================
    private void loadAllGempa() {
        if (!NetworkUtils.isConnected(requireContext())) {
            tvGempaCount.setText("Tidak ada koneksi internet");
            return;
        }

        tvGempaCount.setText("Memuat data gempa...");

        // Load BMKG Terbaru
        ApiClient.getBmkgService().getGempaTerkini()
                .enqueue(new Callback<BmkgResponse.GempaTerkiniResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<BmkgResponse.GempaTerkiniResponse> call,
                                           @NonNull Response<BmkgResponse.GempaTerkiniResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().Infogempa != null) {
                            List<Gempa> list = response.body().Infogempa.gempa;
                            if (list != null) {
                                for (Gempa g : list) g.setSumber("BMKG_TERBARU");
                                addGempaToMap(list);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BmkgResponse.GempaTerkiniResponse> call,
                                          @NonNull Throwable t) {
                        // silent fail untuk maps
                    }
                });

        // Load USGS
        ApiClient.getUsgsService().getGempaUSGS()
                .enqueue(new Callback<GempaUSGS.FeatureCollection>() {
                    @Override
                    public void onResponse(@NonNull Call<GempaUSGS.FeatureCollection> call,
                                           @NonNull Response<GempaUSGS.FeatureCollection> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<GempaUSGS.Feature> features = response.body().features;
                            if (features != null) {
                                List<Gempa> list = new ArrayList<>();
                                for (GempaUSGS.Feature f : features) {
                                    list.add(f.toGempa());
                                }
                                addGempaToMap(list);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GempaUSGS.FeatureCollection> call,
                                          @NonNull Throwable t) {
                        // silent fail
                    }
                });
    }

    private synchronized void addGempaToMap(List<Gempa> list) {
        allGempaList.addAll(list);

        if (clusterManager != null) {
            for (Gempa g : list) {
                if (g.getLatitude() != 0 || g.getLongitude() != 0) {
                    clusterManager.addItem(new GempaClusterItem(g));
                }
            }
            clusterManager.cluster();
        }

        // Update count di toolbar
        requireActivity().runOnUiThread(() ->
                tvGempaCount.setText(allGempaList.size() + " gempa aktif")
        );
    }

    // ========================
    // BOTTOM SHEET
    // ========================
    private void setupBottomSheet() {
        if (bottomSheet == null) return;
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setPeekHeight(0);
    }

    private void showBottomSheet(Gempa gempa) {
        if (gempa == null || bottomSheetBehavior == null) return;

        bsTvMagnitude.setText("M " + gempa.getMagnitude());
        bsTvWilayah.setText(gempa.getWilayah());
        bsTvWaktu.setText(gempa.getTanggal() + " " + gempa.getJam());
        bsTvKedalaman.setText(gempa.getKedalaman());
        bsTvPotensi.setText(gempa.getPotensi() != null ? gempa.getPotensi() : "-");
        bsTvSumber.setText(gempa.getSumber() != null ? gempa.getSumber() : "BMKG");

        bsBtnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_GEMPA, gempa);
            startActivity(intent);
        });

        bsBtnNavigasi.setOnClickListener(v ->
                MapsUtils.navigateToLocation(requireContext(),
                        gempa.getLatitude(), gempa.getLongitude())
        );

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(400);
    }

    private void hideBottomSheet() {
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    // ========================
    // CIRCLE OVERLAY
    // ========================
    private void drawRadiusCircle(Gempa gempa) {
        clearCircle();
        if (googleMap != null && gempa != null) {
            activeCircle = MapsUtils.drawRadiusCircle(googleMap, gempa);
        }
    }

    private void clearCircle() {
        if (activeCircle != null) {
            activeCircle.remove();
            activeCircle = null;
        }
    }

    // ========================
    // SETUP BUTTONS
    // ========================
    private void setupButtons() {
        // Layer Switcher
        btnLayer.setOnClickListener(v -> {
            String[] layers = {"Normal", "Satelit", "Terrain"};
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Pilih Layer Peta")
                    .setItems(layers, (dialog, which) -> {
                        switch (which) {
                            case 0: googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);    break;
                            case 1: googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); break;
                            case 2: googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);   break;
                        }
                    })
                    .show();
        });

        // My Location
        fabMyLocation.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (googleMap != null) {
                    googleMap.setMyLocationEnabled(true);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            } else {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1001
                );
            }
        });
    }

    // ========================
    // MY LOCATION PERMISSION
    // ========================
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1001 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            Toast.makeText(requireContext(),
                    "Izin lokasi diperlukan untuk fitur ini",
                    Toast.LENGTH_SHORT).show();
        }
    }
}