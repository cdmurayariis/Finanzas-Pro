package com.example.aplicacion_financiero.ui.common;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public final class RecoveryCodeDialog {

    private RecoveryCodeDialog() {
    }

    public static void show(Context context, String code) {
        if (code == null || code.isEmpty()) {
            return;
        }
        new MaterialAlertDialogBuilder(context)
                .setTitle("Guarda tu código de recuperación")
                .setMessage("Este código te permitirá restablecer tu DNI si olvidas tu "
                        + "contraseña.\n\n" + code + "\n\nAnótalo en un lugar seguro. "
                        + "No se volverá a mostrar.")
                .setPositiveButton("Copiar", (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) context
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(ClipData.newPlainText(
                                "Código de recuperación", code));
                        Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Ya lo guardé", null)
                .setCancelable(false)
                .show();
    }
}
