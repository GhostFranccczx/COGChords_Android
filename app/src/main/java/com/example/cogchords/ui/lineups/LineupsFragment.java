package com.example.cogchords.ui.lineups;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cogchords.R;
import com.example.cogchords.data.entity.Lineup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class LineupsFragment extends Fragment implements LineupAdapter.OnLineupClickListener {

    private LineupViewModel lineupViewModel;
    private LineupAdapter lineupAdapter;
    private RecyclerView rvLineups;
    private View layoutEmptyState;

    public LineupsFragment() {
    }

    public static LineupsFragment newInstance() {
        return new LineupsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lineups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvLineups = view.findViewById(R.id.rv_lineups);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state_lineups);
        FloatingActionButton fabAddLineup = view.findViewById(R.id.fab_add_lineup);

        setupRecyclerView();
        setupViewModel();

        if (fabAddLineup != null) {
            fabAddLineup.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddEditLineupActivity.class);
                startActivity(intent);
            });
        }

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
    }

    private void setupRecyclerView() {
        lineupAdapter = new LineupAdapter(this);
        rvLineups.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvLineups.setAdapter(lineupAdapter);
    }

    private void setupViewModel() {
        lineupViewModel = new ViewModelProvider(this).get(LineupViewModel.class);
        lineupViewModel.getAllLineups().observe(getViewLifecycleOwner(), lineups -> {
            if (lineups == null || lineups.isEmpty()) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                rvLineups.setVisibility(View.GONE);
            } else {
                layoutEmptyState.setVisibility(View.GONE);
                rvLineups.setVisibility(View.VISIBLE);
            }
            lineupAdapter.setLineups(lineups);
        });
    }

    @Override
    public void onLineupClick(Lineup lineup) {
        Intent intent = new Intent(requireContext(), LineupViewActivity.class);
        intent.putExtra(LineupViewActivity.EXTRA_LINEUP_ID, lineup.getId());
        startActivity(intent);
    }

    @Override
    public void onLineupEdit(Lineup lineup) {
        Intent intent = new Intent(requireContext(), AddEditLineupActivity.class);
        intent.putExtra(AddEditLineupActivity.EXTRA_LINEUP_ID, lineup.getId());
        startActivity(intent);
    }

    @Override
    public void onLineupDelete(Lineup lineup) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Lineup")
                .setMessage("Are you sure you want to delete \"" + lineup.getName() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    lineupViewModel.deleteLineup(lineup, () -> {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "Lineup deleted", Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
