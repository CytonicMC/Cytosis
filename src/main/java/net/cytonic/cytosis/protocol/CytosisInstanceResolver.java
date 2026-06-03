package net.cytonic.cytosis.protocol;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.protocol.utils.InstanceResolver;
import net.cytonic.protocol.utils.ReflectionUtils;

public class CytosisInstanceResolver implements InstanceResolver {

    @Override
    public <T> T resolve(Class<T> clazz) throws Exception {
        Object component = Cytosis.CONTEXT.getComponents().get(clazz);
        if (component != null) {
            //noinspection unchecked
            return (T) component;
        }
        return ReflectionUtils.newInstance(clazz);
    }
}