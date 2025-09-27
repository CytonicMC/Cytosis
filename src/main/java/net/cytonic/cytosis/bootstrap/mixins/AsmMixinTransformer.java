package net.cytonic.cytosis.bootstrap.mixins;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;

public final class AsmMixinTransformer extends AsmTransformer {

    @NotNull
    private final IMixinTransformer transformer;

    public AsmMixinTransformer(CytosisMixinService service) {
        IMixinTransformerFactory factory = service.getMixinInternal(IMixinTransformerFactory.class);
        if (factory == null) {
            throw new NullPointerException(
                "Unable to create IMixinTransformer instance as it's factory went unregistered.");
        }
        this.transformer = Objects.requireNonNull(factory.createTransformer(),
            "factory may not create a null transformer");
    }

    @Override
    public boolean accept(@NotNull ClassNode source) {
        return this.transformer.transformClass(MixinEnvironment.getEnvironment(MixinEnvironment.Phase.DEFAULT),
            source.name.replace("/", "."), source);
    }

    @Override
    public int getPriority() {
        return -10_000;
    }

    @Override
    public boolean isValidTarget(@NotNull String internalName) {
        return true;
    }
}