package net.cytonic.cytosis.bootstrap.mixins;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;

import java.util.Objects;

public final class ASMMixinTransformer extends ASMTransformer {

    @NotNull
    private final IMixinTransformer transformer;

    public ASMMixinTransformer(CytosisMixinService service) {
        IMixinTransformerFactory factory = service.getMixinInternal(IMixinTransformerFactory.class);
        if (factory == null) {
            throw new NullPointerException("Unable to create IMixinTransformer instance as it's factory went unregistered.");
        }
        this.transformer = Objects.requireNonNull(factory.createTransformer(), "factory may not create a null transformer");
    }

    @Override
    public boolean accept(@NotNull ClassNode source) {
        return this.transformer.transformClass(MixinEnvironment.getEnvironment(MixinEnvironment.Phase.DEFAULT), source.name.replace("/", "."), source);
    }

    @Override
    public boolean isValidTarget(@NotNull String internalName) {
        return true;
    }

    @Override
    public int getPriority() {
        return -10_000;
    }
}