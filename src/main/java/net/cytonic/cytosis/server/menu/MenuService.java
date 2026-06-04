package net.cytonic.cytosis.server.menu;

import java.util.List;

import me.devnatan.inventoryframework.View;

public interface MenuService {

    List<View> getMenus();

    class Noop implements MenuService {

        @Override
        public List<View> getMenus() {
            return List.of();
        }
    }
}
