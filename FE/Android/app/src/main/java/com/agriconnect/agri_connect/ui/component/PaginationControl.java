package com.agriconnect.agri_connect.ui.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.agriconnect.agri_connect.R;
import com.google.android.material.button.MaterialButton;

public class PaginationControl extends LinearLayout {

    private MaterialButton btnPrev, btnNext;
    private TextView tvPageInfo;

    private int currentPage = 0;
    private int totalPages = 1;
    private OnPageChangeListener listener;

    public interface OnPageChangeListener {
        void onPageChanged(int newPage);
    }

    public PaginationControl(Context context) {
        super(context);
        init(context);
    }

    public PaginationControl(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PaginationControl(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_pagination_control, this, true);
        
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        tvPageInfo = findViewById(R.id.tvPageInfo);

        btnPrev.setOnClickListener(v -> {
            if (listener != null && currentPage > 0) {
                listener.onPageChanged(currentPage - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (listener != null && currentPage < totalPages - 1) {
                listener.onPageChanged(currentPage + 1);
            }
        });

        updateUI();
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.listener = listener;
    }

    public void setPageData(int currentPage, int totalPages) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        if (this.totalPages < 1) this.totalPages = 1;
        updateUI();
    }

    private void updateUI() {
        tvPageInfo.setText(String.format("Trang %d / %d", currentPage + 1, totalPages));
        
        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled(currentPage < totalPages - 1);
    }
}
