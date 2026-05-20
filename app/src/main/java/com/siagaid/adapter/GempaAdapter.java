package com.siagaid.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.siagaid.R;
import com.siagaid.model.Gempa;
import com.siagaid.maps.CustomClusterRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter untuk menampilkan list gempa di RecyclerView.
 * Dipakai di HomeFragment (tab Terbaru, Dirasakan, Internasional).
 */
public class GempaAdapter extends RecyclerView.Adapter<GempaAdapter.GempaViewHolder> {

    private final Context context;
    private List<Gempa> gempaList;
    private OnItemClickListener listener;

    // ========================
    // INTERFACE CLICK LISTENER
    // ========================
    public interface OnItemClickListener {
        void onItemClick(Gempa gempa);
    }

    // ========================
    // CONSTRUCTOR
    // ========================
    public GempaAdapter(Context context) {
        this.context  = context;
        this.gempaList = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ========================
    // SET DATA
    // ========================
    public void setData(List<Gempa> list) {
        this.gempaList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<Gempa> getData() {
        return gempaList;
    }

    // ========================
    // RECYCLERVIEW METHODS
    // ========================
    @NonNull
    @Override
    public GempaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_gempa, parent, false);
        return new GempaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GempaViewHolder holder, int position) {
        Gempa gempa = gempaList.get(position);
        holder.bind(gempa);
    }

    @Override
    public int getItemCount() {
        return gempaList.size();
    }

    // ========================
    // VIEW HOLDER
    // ========================
    class GempaViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout layoutMagnitude;
        private final TextView tvMagnitudeValue;
        private final TextView tvWilayah;
        private final TextView tvKedalaman;
        private final TextView tvWaktu;
        private final TextView tvTanggal;

        GempaViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutMagnitude  = itemView.findViewById(R.id.layout_magnitude);
            tvMagnitudeValue = itemView.findViewById(R.id.tv_magnitude_value);
            tvWilayah        = itemView.findViewById(R.id.tv_wilayah);
            tvKedalaman      = itemView.findViewById(R.id.tv_kedalaman);
            tvWaktu          = itemView.findViewById(R.id.tv_waktu);
            tvTanggal        = itemView.findViewById(R.id.tv_tanggal);
        }

        void bind(Gempa gempa) {
            // Magnitude
            String mag = gempa.getMagnitude() != null ? gempa.getMagnitude() : "0";
            tvMagnitudeValue.setText(mag);

            // Warna badge magnitude
            int color = CustomClusterRenderer.getColorByMagnitude(
                    gempa.getMagnitudeDouble()
            );
            layoutMagnitude.setBackgroundColor(color);

            // Wilayah
            tvWilayah.setText(gempa.getWilayah() != null ?
                    gempa.getWilayah() : "Tidak diketahui");

            // Kedalaman
            tvKedalaman.setText(gempa.getKedalaman() != null ?
                    gempa.getKedalaman() : "-");

            // Waktu
            tvWaktu.setText(gempa.getJam() != null ?
                    gempa.getJam() : "-");

            // Tanggal
            tvTanggal.setText(gempa.getTanggal() != null ?
                    gempa.getTanggal() : "-");

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(gempa);
            });
        }
    }
}