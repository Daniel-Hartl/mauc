package com.example.pulsmesser.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pulsmesser.databinding.FragmentNotificationsBinding;
import com.example.pulsmesser.ui.home.HomeViewModel;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    private TextView textView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        textView = binding.textNotifications;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public void updateOxygenValue(double oxygen) {
        NotificationsViewModel notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        notificationsViewModel.setText(oxygen);
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}