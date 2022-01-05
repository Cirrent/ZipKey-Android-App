package com.sampleapp.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.sampleapp.CirrentApplication;
import com.sampleapp.R;
import com.sampleapp.history.OnboardingHistoryPrefManager;
import com.sampleapp.history.OnboardingInfo;
import com.sampleapp.ui.adapter.OnboardingHistoryAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OnBoardingHistoryFragment extends BaseFragment {
    private OnboardingHistoryAdapter mAdapter;
    private List<OnboardingInfo> mHistoryItems;
    private TextView mTextHint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.history_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getItemId() == R.id.menu_clear) {
            showWarningAndClearHistory();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showWarningAndClearHistory() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.question_clear_onboarding_history)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showToastIfHistoryIsEmpty();
                        new OnboardingHistoryPrefManager(new Gson()).clearHistory();
                        updateData();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }

    private void showToastIfHistoryIsEmpty() {
        boolean isEmptyHistory = mHistoryItems == null || mHistoryItems.isEmpty();
        if (isEmptyHistory) {
            String toastMsg = CirrentApplication.getAppContext().getString(R.string.history_is_empty);
            Toast.makeText(CirrentApplication.getAppContext(), toastMsg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_history, container, false);
        initViews(view);
        changeActionBarState(false, false, getString(R.string.onboarding_history));
        return view;
    }

    private void initViews(View view) {
        RecyclerView mRecyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration divider = getDividerItemDecoration(mRecyclerView.getContext());
        mRecyclerView.addItemDecoration(divider);

        mHistoryItems = new OnboardingHistoryPrefManager(new Gson()).getHistory();
        mAdapter = new OnboardingHistoryAdapter(mHistoryItems);
        mRecyclerView.setAdapter(mAdapter);

        mTextHint = view.findViewById(R.id.text_hint_empty);
        updateHintView();
    }

    private DividerItemDecoration getDividerItemDecoration(Context context) {
        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        Drawable customDividerRes = ContextCompat.getDrawable(CirrentApplication.getAppContext(), R.drawable.recycler_view_divider);
        if (customDividerRes != null) {
            divider.setDrawable(customDividerRes);
        }
        return divider;
    }

    private void updateData() {
        mHistoryItems = new OnboardingHistoryPrefManager(new Gson()).getHistory();
        mAdapter.setItems(mHistoryItems);
        mAdapter.notifyDataSetChanged();
        updateHintView();
    }

    private void updateHintView() {
        if (mHistoryItems.size() > 0) {
            mTextHint.setVisibility(View.GONE);
        } else {
            mTextHint.setVisibility(View.VISIBLE);
        }
    }
}
