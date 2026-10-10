package com.example.cogchords.ui.chords;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cogchords.R;
import com.example.cogchords.data.model.ChordModel;
import com.example.cogchords.util.ChordDictionary;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class ChordReferenceFragment extends Fragment {

    private RecyclerView rvChordReference;
    private ChordReferenceAdapter adapter;
    private View layoutEmptyChords;
    private EditText etSearchChords;
    private ChipGroup chipGroupRootNotes;

    private String selectedRoot = "ALL";
    private String currentQuery = "";

    public ChordReferenceFragment() {
        // Required empty public constructor
    }

    public static ChordReferenceFragment newInstance() {
        return new ChordReferenceFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chord_ref, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvChordReference = view.findViewById(R.id.rv_chord_reference);
        layoutEmptyChords = view.findViewById(R.id.layout_empty_chords);
        etSearchChords = view.findViewById(R.id.et_search_chords);
        chipGroupRootNotes = view.findViewById(R.id.chip_group_root_notes);

        View ivHeaderLogo = view.findViewById(R.id.iv_header_logo);
        if (ivHeaderLogo != null) {
            ivHeaderLogo.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), com.example.cogchords.ui.settings.SettingsActivity.class);
                startActivity(intent);
            });
        }

        View btnSettings = view.findViewById(R.id.btn_settings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), com.example.cogchords.ui.settings.SettingsActivity.class);
                startActivity(intent);
            });
        }

        setupRecyclerView();
        setupFilters();
        applyFilter();
    }

    private void setupRecyclerView() {
        adapter = new ChordReferenceAdapter();
        // Dynamically compute columns based on screen width (2 on standard phones, 3-5 on tablets)
        int screenWidthDp = getResources().getConfiguration().screenWidthDp;
        int columns = Math.max(2, screenWidthDp / 170);
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), columns);
        rvChordReference.setLayoutManager(layoutManager);
        rvChordReference.setAdapter(adapter);
    }

    private void setupFilters() {
        if (etSearchChords != null) {
            etSearchChords.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentQuery = s != null ? s.toString() : "";
                    applyFilter();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (chipGroupRootNotes != null) {
            chipGroupRootNotes.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) {
                    selectedRoot = "ALL";
                } else {
                    int checkedId = checkedIds.get(0);
                    Chip chip = group.findViewById(checkedId);
                    if (chip != null) {
                        selectedRoot = chip.getText().toString();
                    } else {
                        selectedRoot = "ALL";
                    }
                }
                applyFilter();
            });
        }
    }

    private void applyFilter() {
        List<ChordModel> results = ChordDictionary.filterChords(selectedRoot, currentQuery);
        adapter.setChords(results);

        if (results.isEmpty()) {
            layoutEmptyChords.setVisibility(View.VISIBLE);
            rvChordReference.setVisibility(View.GONE);
        } else {
            layoutEmptyChords.setVisibility(View.GONE);
            rvChordReference.setVisibility(View.VISIBLE);
        }
    }
}
