package net.cytonic.cytosis.report;

import java.util.UUID;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import org.jetbrains.annotations.NotNull;

/**
 * The abstract Report Type object.
 *
 * @param <T> The type of the implementation
 */
public interface ReportType<T extends ReportType<T>> extends ReportTypes {

    /**
     * The static identity of this report type.
     *
     * @return the Key of this type
     */
    @NotNull Key getKey();

    /**
     * The Codec to be used to serialize and deserialize the Context Object for the report type.
     *
     * @return The codec to be used. Note: It is only used for JSON.
     */
    @NotNull Codec<? extends ReportContext<T>> getContextCodec(); // used to parse their context

    /**
     * The name of this report type used in menus and messages.
     *
     * @return The component to use.
     */
    @NotNull Component getDisplayName();

    /**
     * The menu that is used to customize a report. It can lead to as many further menus, using click callbacks. At the
     * end, the user should be directed to the book {@link ReportManager#getSubmitMenu(String, Report, Component)}.
     *
     * @param user   The MiniMessage string of the user being reported.
     * @param target The UUID of the user being reported.
     * @return the book menu
     */
    @NotNull Book getCustomizerBook(String user, UUID target);
}
