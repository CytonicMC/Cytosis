package net.cytonic.cytosis.utils.polar;

import net.cytonic.cytosis.Bootstrappable;
import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.utils.polar.handlers.BannerHandler;
import net.cytonic.cytosis.utils.polar.handlers.HeadHandler;
import net.cytonic.cytosis.utils.polar.handlers.SignHandler;

@CytosisComponent
@SuppressWarnings("unused")
public class BlockHandlerUtils implements Bootstrappable {

    @Override
    public void init() {
        SignHandler.setup();
        BannerHandler.setup();
        HeadHandler.setup();
    }
}
