package com.example.aplicacion_financiero.ui.common;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aplicacion_financiero.utils.UiAnimations;

public class AnimatedFragment extends Fragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!UiAnimations.areAnimationsEnabled(view.getContext())) {
            return;
        }
        float offset = 18f * view.getResources().getDisplayMetrics().density;
        view.setAlpha(0f);
        view.setTranslationY(offset);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .start();
    }
}
