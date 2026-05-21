package com.siagaid.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.siagaid.R;
import com.siagaid.activity.DetailActivity;
import com.siagaid.adapter.BookmarkAdapter;
import com.siagaid.database.BookmarkDao;
import com.siagaid.database.DatabaseHelper;
import com.siagaid.filter.FilterState;
import com.siagaid.filter.GempaFilterHelper;
import com.siagaid.model.Gempa;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment untuk menampilkan daftar bookmark gempa.
 * Load dari SQLite via Executor (background thread).
 * Support swipe-to-delete dengan Snackbar undo.
 */
public class BookmarkFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private EditText etSearch;

    private BookmarkAdapter adapter;
    private BookmarkDao bookmarkDao;
    private List<Gempa> originalList = new ArrayList<>();

    // ========================
    // LIFECYCLE
    // ========================
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initDatabase();
        initViews(view);
        setupRecyclerView();
        setupSearch();
        loadBookmarks();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh saat kembali dari DetailActivity
        loadBookmarks();
    }

    // ========================
    // INIT
    // ========================
    private void initDatabase() {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        bookmarkDao = new BookmarkDao(dbHelper);
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_bookmark);
        layoutEmpty  = view.findViewById(R.id.layout_empty);
        progressBar  = view.findViewById(R.id.progress_bar);
        etSearch     = view.findViewById(R.id.et_search_bookmark);
    }

    private void setupRecyclerView() {
        adapter = new BookmarkAdapter(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        // Click → DetailActivity
        adapter.setOnItemClickListener(gempa -> {
            Intent intent = new Intent(requireContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_GEMPA, gempa);
            startActivity(intent);
        });

        // Swipe to delete
        adapter.setOnItemSwipeListener((gempa, position) -> {
            // Hapus dari database
            bookmarkDao.delete(gempa, success -> {
                if (success) {
                    updateEmptyState();
                    // Snackbar undo
                    Snackbar.make(requireView(),
                                    "Bookmark dihapus", Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> {
                                // Restore ke database
                                bookmarkDao.insert(gempa, restored -> {
                                    if (restored) {
                                        adapter.restoreItem(gempa, position);
                                        updateEmptyState();
                                    }
                                });
                            })
                            .show();
                } else {
                    Toast.makeText(requireContext(),
                            "Gagal menghapus bookmark",
                            Toast.LENGTH_SHORT).show();
                    // Restore item di adapter
                    adapter.restoreItem(gempa, position);
                }
            });
        });

        adapter.attachSwipeToDelete(recyclerView);
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
                filterByKeyword(s.toString().trim());
            }
        });
    }

    private void filterByKeyword(String keyword) {
        if (originalList == null) return;

        List<Gempa> filtered;
        if (keyword.isEmpty()) {
            filtered = new ArrayList<>(originalList);
        } else {
            FilterState state = new FilterState();
            state.searchKeyword = keyword;
            filtered = GempaFilterHelper.filterByKeyword(originalList, keyword);
        }

        adapter.setData(filtered);
        updateEmptyState();
    }

    // ========================
    // LOAD BOOKMARKS
    // ========================
    private void loadBookmarks() {
        showLoading(true);

        bookmarkDao.getAll(list -> {
            showLoading(false);
            originalList = list != null ? list : new ArrayList<>();

            String keyword = etSearch != null ?
                    etSearch.getText().toString().trim() : "";

            if (!keyword.isEmpty()) {
                filterByKeyword(keyword);
            } else {
                adapter.setData(originalList);
            }

            updateEmptyState();
        });
    }

    // ========================
    // UI STATE
    // ========================
    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}