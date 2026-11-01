package com.example.aplicacion_financiero.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CategoryDefaults {

    public static final String[] EXPENSE = {
            "Alimentaci\u00F3n", "Transporte", "Educaci\u00F3n", "Vivienda", "Salud",
            "Entretenimiento", "Servicios", "Compras", "Otros"
    };

    public static final String[] INCOME = {
            "Salario", "Negocio", "Trabajos adicionales", "Becas", "Otros ingresos"
    };

    private CategoryDefaults() {
    }

    public static List<String> forType(String type) {
        return new ArrayList<>(Arrays.asList("INCOME".equals(type) ? INCOME : EXPENSE));
    }
}
