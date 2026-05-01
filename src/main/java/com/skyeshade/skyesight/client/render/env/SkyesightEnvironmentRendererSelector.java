package com.skyeshade.skyesight.client.render.env;

import com.skyeshade.skyesight.client.compat.iris.SkyesightIrisCompat;

public final class SkyesightEnvironmentRendererSelector {
    private static final SkyesightEnvironmentRenderer VANILLA =
            new SkyesightVanillaEnvironmentRenderer();

    private SkyesightEnvironmentRendererSelector() {}

    public static SkyesightEnvironmentRenderer get() {
        if (SkyesightIrisCompat.isShaderPackInUse()) {
            // Placeholder until a real Iris pipeline renderer exists.
            return VANILLA;
        }

        return VANILLA;
    }
}