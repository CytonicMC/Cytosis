package net.cytonic.cytosis.bootstrap.mixins;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public class CytosisMixinBootstrap implements IMixinServiceBootstrap {

    @Override
    public String getName() {
        return "Cytosis MixinBootstrap";
    }

    @Override
    public String getServiceClassName() {
        return "net.cytonic.cytosis.bootstrap.mixins.CytosisMixinService";
    }

    @Override
    public void bootstrap() {
    }
}