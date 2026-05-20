package com.siagaid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.siagaid.R;
import com.siagaid.maps.CustomClusterRenderer;
import com.siagaid.model.Gempa;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter untuk menampilkan list bookmark di BookmarkFragment.
 * Support swipe-to-delete via ItemTouchHelper.
 */
public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder> {

    private final Context context;
    private List<Gempa> bookmarkList;
    private OnItemClickListener clickListener;
    private OnItemSwipeListener swipeListener;

    // ========================
    // INTERFACE
    // ========================
    public interface OnItemClickListener {
        void onItemClick(Gempa gempa);
    }

    public interface OnItemSwipeListener {
        void onItemSwiped(Gempa gempa, int position);
    }

    // ========================
    // CONSTRUCTOR
    // ========================
    public BookmarkAdapter(Context context) {
        this.context      = context;
        this.bookmarkList = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemSwipeListener(OnItemSwipeListener listener) {
        this.swipeListener = listener;
    }

    // ========================
    // SET DATA
    // ========================
    public void setData(List<Gempa> list) {
        this.bookmarkList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < bookmarkList.size()) {
            bookmarkList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void restoreItem(Gempa gempa, int position) {
        bookmarkList.add(position, gempa);
        notifyItemInserted(position);
    }

    public List<Gempa> getData() {
        return bookmarkList;
    }

    // ========================
    // RECYCLERVIEW METHODS
    // ========================
    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_gempa, parent, false);
        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        Gempa gempa = bookmarkList.get(position);
        holder.bind(gempa);
    }

    @Override
    public int getItemCount() {
        return bookmarkList.size();
    }

    // ========================
    // VIEW HOLDER
    // ========================
    class BookmarkViewHolder extends RecyclerView.ViewHolder {

        private final LinearLayout layoutMagnitude;
        private final TextView tvMagnitudeValue;
        private final TextView tvWilayah;
        private final TextView tvKedalaman;
        private final TextView tvWaktu;
        private final TextView tvTanggal;

        BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutMagnitude  = itemView.findViewById(R.id.layout_magnitude);
            tvMagnitudeValue = itemView.findViewById(R.id.tv_magnitude_value);
            tvWilayah        = itemView.findViewById(R.id.tv_wilayah);
            tvKedalaman      = itemView.findViewById(R.id.tv_kedalaman);
            tvWaktu          = itemView.findViewById(R.id.tv_waktu);
            tvTanggal        = itemView.findViewById(R.id.tv_tanggal);
        }

        void bind(Gempa gempa) {
            tvMagnitudeValue.setText(gempa.getMagnitude() != null ?
                    gempa.getMagnitude() : "0");

            int color = CustomClusterRenderer.getColorByMagnitude(
                    gempa.getMagnitudeDouble()
            );
            layoutMagnitude.setBackgroundColor(color);

            tvWilayah.setText(gempa.getWilayah() != null ?
                    gempa.getWilayah() : "Tidak diketahui");

            tvKedalaman.setText(gempa.getKedalaman() != null ?
                    gempa.getKedalaman() : "-");

            tvWaktu.setText(gempa.getJam() != null ?
                    gempa.getJam() : "-");

            tvTanggal.setText(gempa.getTanggal() != null ?
                    gempa.getTanggal() : "-");

            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onItemClick(gempa);
            });
        }
    }

    // ========================
    // SWIPE TO DELETE HELPER
    // ========================

    /**
     * Attach swipe-to-delete ke RecyclerView.
     * Panggil di BookmarkFragment setelah set adapter.
     */
    public void attachSwipeToDelete(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView rv,
                                          @NonNull RecyclerView.ViewHolder vh,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false; // tidak support drag
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                         int direction) {
                        int position = viewHolder.getAdapterPosition();
                        if (position != RecyclerView.NO_ID) {
                            Gempa gempa = bookmarkList.get(position);
                            removeItem(position);
                            if (swipeListener != null) {
                                swipeListener.onItemSwiped(gempa, position);
                            }
                        }
                    }
                };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }
}