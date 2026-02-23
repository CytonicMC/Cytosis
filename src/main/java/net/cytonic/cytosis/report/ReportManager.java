package net.cytonic.cytosis.report;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.ebean.DB;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.codec.Codec;
import org.jetbrains.annotations.Nullable;

import net.cytonic.cytosis.bootstrap.annotations.CytosisComponent;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;

@CytosisComponent
public class ReportManager {

    private final List<ReportType<?>> types = ReportTypes.builtIn();

    public void registerType(ReportType<?> type) {
        types.add(type);
    }

    @Nullable
    public ReportType<?> getReportType(String rawKey) {
        for (ReportType<?> type : types) {
            if (type.getKey().asString().equals(rawKey)) return type;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends ReportType<T>> CompletableFuture<List<Report<T>>> getUnresolved(T type) {
        return CompletableFuture.supplyAsync(() -> (List<Report<T>>) DB.find(ReportEntity.class)
            .where()
            .eq("type", type.getKey().asString())
            .eq("resolved", false)
            .findList().stream().map((Function<? super ReportEntity, ? extends Report<T>>) Report::from)
            .toList());
    }

    @SuppressWarnings("unchecked")
    public <T extends ReportType<T>> CompletableFuture<Void> saveReport(Report<T> report) {
        return CompletableFuture.supplyAsync(() -> {
            ReportEntity entity = new ReportEntity();
            entity.setId(report.uuid());
            entity.setReporter(report.reporter());
            entity.setPlayer(report.player());
            entity.setContext(Utils.toJson(report.context(),
                (Codec<ReportContext<T>>) report.type().getContextCodec()));
            entity.setType(report.type().getKey().asString());
            entity.setResolved(report.resolved());
            entity.save();
            return null;
        });
    }

    public Book getTypesMenu(String user, UUID target) {
        Component page = Msg.mm("""
            What are you reporting %s for?
            
            """, user);

        for (ReportType<?> type : types) {
            Component element = Msg.black("<b>»</b> ")
                .append(type.getDisplayName())
                .appendNewline()
                .clickEvent(Msg.callback(p -> p.openBook(type.getCustomizerBook(user, target))));

            page = page.append(element);
        }

        page = page.appendNewline().appendNewline()
            .append(Msg.red("<b>[CANCEL]</b>").clickEvent(ClickEvent.runCommand("dummy")));

        return Book.builder()
            .author(Msg.aqua("CytonicMC"))
            .addPage(page)
            .title(Msg.white("Report Player Options"))
            .build();
    }

    public Book getSubmitMenu(String user, Report<?> report, Component contextSummary) {
        Component page = Msg.mm("""
            You are reporting %s for %s.
            
            <hover:show_text:'%s'><i>[CONTEXT]</i></hover>
            
            Please note: Abuse of our report system may result in disciplinary action against you.
            """, user, Msg.toMini(report.type().getDisplayName()), Msg.toMini(contextSummary));

        page = page.append(Msg.darkGreen("<b>Submit Report</b>")
                .clickEvent(Msg.callback(p -> {
                    saveReport(report).whenComplete((_, throwable) -> {
                        if (throwable != null) {
                            p.sendMessage(
                                Msg.serverError("Failed to save your report. (%s)", throwable.getCause().getMessage()));
                            return;
                        }
                        p.sendMessage(Msg.success("Your report has been submitted. A member of staff will review it."));
                    });
                    p.closeBook();
                })))
            .appendNewline()
            .append(Msg.red("<b>Cancel Report</b>")
                .clickEvent(Msg.callback(CytosisPlayer::closeBook)));

        return Book.builder()
            .author(Msg.aqua("CytonicMC"))
            .addPage(page)
            .title(Msg.white("Report Player Submit"))
            .build();
    }
}
