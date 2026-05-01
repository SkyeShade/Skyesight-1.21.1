package com.skyeshade.skyesight;


import com.mojang.logging.LogUtils;
import com.skyeshade.skyesight.api.SkyesightApi;

import com.skyeshade.skyesight.client.view.SkyesightClientApi;
import com.skyeshade.skyesight.network.SkyesightPayloads;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Skyesight.MODID)
public final class Skyesight {
    public static final String MODID = "skyesight";
    public static final Logger LOGGER = LogUtils.getLogger();


    public Skyesight(IEventBus modBus) {
        modBus.addListener(SkyesightPayloads::register);
    }
    private static final SkyesightApi API = new SkyesightClientApi();


    public static SkyesightApi api() {
        return API;
    }
}