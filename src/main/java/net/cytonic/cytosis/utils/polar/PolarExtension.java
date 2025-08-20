package net.cytonic.cytosis.utils.polar;

import net.hollowcube.polar.PolarWorldAccess;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PolarExtension implements PolarWorldAccess {


    @Override
    public void loadWorldData(@NotNull Instance instance, @Nullable NetworkBuffer userData) {
        if (userData == null) {
        }

    }


    @Override
    public void saveWorldData(@NotNull Instance instance, @NotNull NetworkBuffer userData) {

    }
}
