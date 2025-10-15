package net.cytonic.cytosis.bootstrap.mixins;

import java.util.Collection;

import org.spongepowered.asm.launch.platform.IMixinPlatformServiceAgent;
import org.spongepowered.asm.launch.platform.MixinPlatformAgentAbstract;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.util.Constants;

@SuppressWarnings("unused")
public class CytosisPlatformAgent extends MixinPlatformAgentAbstract implements IMixinPlatformServiceAgent {

    @Override
    public void init() {
    }

    @Override
    public String getSideName() {
        return Constants.SIDE_UNKNOWN;
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return null;
    }
}