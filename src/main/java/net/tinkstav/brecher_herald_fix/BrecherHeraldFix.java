package net.tinkstav.brecher_herald_fix;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BrecherHeraldFix.MOD_ID)
public class BrecherHeraldFix {
    public static final String MOD_ID = "brecher_herald_fix";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public BrecherHeraldFix() {
        LOGGER.info("BrecherHeraldFix loaded - Herald event listener leak fix active");
    }
}
