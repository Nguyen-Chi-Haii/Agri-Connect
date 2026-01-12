package com.agriconnect.agri_connect.ui.admin.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.model.KycInfo;
import com.agriconnect.agri_connect.api.model.UserProfile;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private List<UserProfile> users = new ArrayList<>();
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onVerifyKyc(UserProfile user);

        void onRejectKyc(UserProfile user);
    }

    public void setOnUserActionListener(OnUserActionListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<UserProfile> users) {
        this.users = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserProfile user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvFullName, tvPhone, tvRole, tvKycStatus, tvCccd;
        LinearLayout layoutActions;
        MaterialButton btnVerifyKyc, btnRejectKyc;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvKycStatus = itemView.findViewById(R.id.tvKycStatus);
            tvCccd = itemView.findViewById(R.id.tvCccd);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnVerifyKyc = itemView.findViewById(R.id.btnVerifyKyc);
            btnRejectKyc = itemView.findViewById(R.id.btnRejectKyc);
        }

        void bind(UserProfile user) {
            // Avatar
            String name = user.getFullName();
            if (name != null && !name.isEmpty()) {
                tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                tvFullName.setText(name);
            } else {
                tvAvatar.setText("U");
                tvFullName.setText("Chưa cập nhật");
            }

            // Phone
            tvPhone.setText(user.getPhone() != null ? user.getPhone() : "N/A");

            // Role
            String role = user.getRole();
            int roleColor = R.color.primary;
            String roleText = "Người dùng";

            if ("ADMIN".equals(role)) {
                roleColor = R.color.error;
                roleText = "Quản trị";
            } else if ("FARMER".equals(role)) {
                roleColor = R.color.success;
                roleText = "Nông dân";
            } else if ("TRADER".equals(role)) {
                roleColor = R.color.info;
                roleText = "Thương lái";
            }

            tvRole.setText(roleText);
            GradientDrawable roleDrawable = (GradientDrawable) tvRole.getBackground();
            if (roleDrawable != null) {
                roleDrawable.setColor(ContextCompat.getColor(itemView.getContext(), roleColor));
            }

            // KYC Status
            KycInfo kyc = user.getKyc();
            if (kyc != null && kyc.getStatus() != null) {
                String status = kyc.getStatus();
                int statusColor;
                String statusText;

                switch (status) {
                    case "VERIFIED":
                        statusColor = R.color.success;
                        statusText = "Đã xác minh";
                        layoutActions.setVisibility(View.GONE);
                        break;
                    case "PENDING":
                        statusColor = R.color.warning;
                        statusText = "Chờ xác minh";
                        layoutActions.setVisibility(View.VISIBLE);
                        break;
                    case "REJECTED":
                        statusColor = R.color.error;
                        statusText = "Đã từ chối";
                        layoutActions.setVisibility(View.GONE);
                        break;
                    default:
                        statusColor = R.color.text_hint;
                        statusText = "Chưa gửi";
                        layoutActions.setVisibility(View.GONE);
                }

                tvKycStatus.setText(statusText);
                tvKycStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), statusColor));

                // CCCD
                if (kyc.getCccd() != null) {
                    tvCccd.setVisibility(View.VISIBLE);
                    tvCccd.setText("CCCD: " + kyc.getCccd());
                } else {
                    tvCccd.setVisibility(View.GONE);
                }
            } else {
                tvKycStatus.setText("Chưa gửi KYC");
                tvKycStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_hint));
                tvCccd.setVisibility(View.GONE);
                layoutActions.setVisibility(View.GONE);
            }

            // Action buttons
            btnVerifyKyc.setOnClickListener(v -> {
                if (listener != null)
                    listener.onVerifyKyc(user);
            });

            btnRejectKyc.setOnClickListener(v -> {
                if (listener != null)
                    listener.onRejectKyc(user);
            });
        }
    }
}
