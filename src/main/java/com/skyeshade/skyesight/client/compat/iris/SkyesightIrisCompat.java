package com.skyeshade.skyesight.client.compat.iris;

import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

public final class SkyesightIrisCompat {
    private static final String IRIS_MOD_ID = "iris";
    private static Boolean shaderPackInUse;
    private static Float sunPathRotation;

    private SkyesightIrisCompat() {}

    public static boolean isIrisLoaded() {
        return ModList.get().isLoaded(IRIS_MOD_ID);
    }

    public static boolean isShaderPackInUse() {
        if (!isIrisLoaded()) {
            return false;
        }

        try {
            Object api = getIrisApi();
            Method method = api.getClass().getMethod("isShaderPackInUse");
            return (boolean) method.invoke(api);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    public static float getSunPathRotation() {
        if (!isIrisLoaded()) {
            return 0.0F;
        }

        try {
            Object api = getIrisApi();
            Method method = api.getClass().getMethod("getSunPathRotation");
            return (float) method.invoke(api);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return 0.0F;
        }
    }

    private static Object getIrisApi() throws ReflectiveOperationException {
        Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
        Method getInstance = apiClass.getMethod("getInstance");
        return getInstance.invoke(null);
    }
}