package net.cytonic.cytosis.utils.polar;

import net.cytonic.cytosis.utils.polar.handlers.BannerHandler;
import net.cytonic.cytosis.utils.polar.handlers.HeadHandler;
import net.cytonic.cytosis.utils.polar.handlers.SignHandler;

public class BlockHandlerUtils {

    public static void initHandlers() {
        SignHandler.setup();
        BannerHandler.setup();
        HeadHandler.setup();
    }

}
