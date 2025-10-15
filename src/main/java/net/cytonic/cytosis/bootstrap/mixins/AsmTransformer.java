package net.cytonic.cytosis.bootstrap.mixins;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 * A bytecode editing API that interfaces with objectweb ASM {@link ClassNode ClassNodes}. This allows for harmonious
 * runtime editing of classes without the use of agents.
 *
 * @since 2.1.0
 */
public abstract class AsmTransformer implements Comparable<AsmTransformer> {

    /**
     * Transforms the provided {@link ClassNode} to the transformer's liking. Must return true if the class node has
     * been modified, should it not returns false, then the transformation MAY be discarded, however it could also not
     * be discarded. It is best to assume that both are equally possible events.
     *
     * @param node The node to transform
     * @return True if the node has been modified.
     * @since 2.1.0
     */
    public abstract boolean accept(@NotNull ClassNode node);

    /**
     * Base implementation of {@link Comparable#compareTo(Object)} based on {@link AsmTransformer#getPriority()}. A
     * return value of 0 indicates that both transformers have the same priority, at which point the application order
     * may be arbitrary.
     *
     * @param o The transformer to compare this transformer against. May not be null.
     * @return The difference in priorities.
     * @since 4.0.0
     */
    @Override
    public int compareTo(AsmTransformer o) {
        return this.getPriority() - o.getPriority();
    }

    /**
     * Obtains the priority of this transformer. The priority is used to note when a transformer should be applied in
     * relation to other transformers. Should two transformers have the same priority, the order will be arbitrary.
     *
     * <p>Transformers with a lower numeric priority value are run first. That is, the higher the
     * priority of a transformer, the more "final" it's descision is.
     *
     * <p>Important transform priorities are:
     * <ul>
     *  <li>-10_000: {@link AsmMixinTransformer}</li>
     *  <li>-9_900: Most SLAPI transformers</li>
     *  <li>100: SLAPI GL transformer (intercepts GL11#glScissor calls)</li>
     * </ul>
     *
     * @return The priority of the transformer
     * @since 4.0.0
     */
    public int getPriority() {
        return 0;
    }

    /**
     * Checks whether the given class denoted by the internal name would be a valid potential transformation target. If
     * this method returns false, then {@link #accept(ClassNode)} should NOT be called, however, if it returns true,
     * then it may get called. {@link #accept(ClassNode)} will not always return true when this method does, however,
     * that method should never return true when this method returns false.
     *
     * @param internalName The internal name of the class
     * @return Whether it is a potential target
     * @see Type#getInternalName()
     * @since 4.0.0
     */
    public abstract boolean isValidTarget(@NotNull String internalName);
}
