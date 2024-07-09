package com.example.pulsmesser.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pulsmesser.R;
import com.example.pulsmesser.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private TextView textOxygen;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textOxygen;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        textOxygen = root.findViewById(R.id.text_oxygen);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void updateOxygenValue(double oxygen) {
        if (textOxygen != null) {
            textOxygen.setText("Sauerstoffgehalt: " + oxygen + "%");
        }
    }
}