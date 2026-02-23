package net.cytonic.cytosis.report;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import io.ebean.DB;
import io.ebean.ExpressionList;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.NotNull;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.data.objects.ChatMessage;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;

interface ReportTypes {

    ReportType<ChatReport> CHAT = new ChatReport();

    static List<ReportType<?>> builtIn() {
        return Utils.list(CHAT);
    }

    record ChatReport() implements ReportType<ChatReport> {

        @Override
        public @NotNull Key getKey() {
            return Key.key("cytosis:chat_report");
        }

        @Override
        public @NotNull Codec<? extends ReportContext<ChatReport>> getContextCodec() {
            return StructCodec.struct(
                "messages", ChatMessage.CODEC.list(), ChatReportContext::getMessages,
                "channel", Codec.Enum(ChatChannel.class), ChatReportContext::getChannel,
                ChatReportContext::new
            );
        }

        @Override
        public @NotNull Component getDisplayName() {
            return Msg.mm("Chat Message(s)");
        }

        @Override
        public @NotNull Book getCustomizerBook(String user, UUID target) {
            Component page = Msg.mm("""
                    Where did you see %s's messages in?
                    
                    """, user)
                .append(Msg.black("<b>»</b> Public Chat").clickEvent(Msg.callback(p -> {
                    getSummaryBook(user, p.getUuid(), target, ChatChannel.ALL)
                        .exceptionally(t -> {
                            p.closeBook();
                            if (t.getCause() instanceof NoContextException) {
                                p.sendMessage(
                                    Msg.whoops(user + " has not sent any messages in the Public channel recently."));
                                return null;
                            }
                            Logger.error("An error occurred whilst reporting a public chat message: ", t.getCause());
                            p.sendMessage(Msg.whoops("An unknown error occurred whilst reporting " + user + "."));
                            return null;
                        }).thenAccept(book -> {
                            if (book != null) p.openBook(book);
                        });
                }))).appendNewline()
                .append(Msg.black("<b>»</b> Party Chat").clickEvent(Msg.callback(p -> {
                    getSummaryBook(user, p.getUuid(), target, ChatChannel.PARTY)
                        .exceptionally(t -> {
                            p.closeBook();
                            if (t.getCause() instanceof NoContextException) {
                                p.sendMessage(
                                    Msg.whoops(user + " has not sent any messages in the Party channel recently."));
                                return null;
                            }

                            Logger.error("An error occurred whilst reporting a party chat message: ", t.getCause());
                            p.sendMessage(Msg.whoops("An unknown error occurred whilst reporting " + user + "."));
                            return null;
                        }).thenAccept(book -> {
                            if (book != null) p.openBook(book);
                        });
                }))).appendNewline()
                .append(Msg.black("<b>»</b> Private Message").clickEvent(Msg.callback(p -> {
                    getSummaryBook(user, p.getUuid(), target, ChatChannel.PRIVATE_MESSAGE)
                        .exceptionally(t -> {
                            p.closeBook();
                            if (t.getCause() instanceof NoContextException) {
                                p.sendMessage(
                                    Msg.whoops(user + " has not sent you any messages in a Private channel recently."));
                                return null;
                            }

                            Logger.error("An error occurred whilst reporting a private message: ", t.getCause());
                            p.sendMessage(Msg.whoops("An unknown error occurred whilst reporting " + user + "."));
                            return null;
                        }).thenAccept(book -> {
                            if (book != null) p.openBook(book);
                        });
                }))).appendNewline().appendNewline().appendNewline()
                .append(Msg.red("<b>[CANCEL]</b>").clickEvent(ClickEvent.runCommand("dummy")));

            return Book.builder()
                .author(Msg.aqua("CytonicMC"))
                .addPage(page)
                .title(Msg.white("Chat Report Channel"))
                .build();
        }

        private @NotNull CompletableFuture<Book> getSummaryBook(String user, UUID sender, UUID target,
            ChatChannel channel) {
            return CompletableFuture.supplyAsync(() -> {
                ExpressionList<ChatMessage> finder = DB.find(ChatMessage.class).where()
                    .eq("sender", target).and()
                    .eq("channel", channel).and()
                    .between("sent_at", Instant.now().minusSeconds(600), Instant.now());

                if (channel == ChatChannel.PRIVATE_MESSAGE) {
                    finder = finder.and().eq("recipient", sender);
                }

                List<ChatMessage> messages = finder.findList();
                if (messages.isEmpty()) throw new NoContextException();

                ReportContext<ChatReport> ctx = new ChatReportContext(messages, channel);
                Report<ChatReport> report = Report.create(this, ctx, sender, target);

                String miniContext = "";
                int counter = 0;
                for (ChatMessage message : messages) {
                    if (counter > 10) break;
                    counter++;
                    miniContext += "\n  > " + message.getContent();
                }
                if (messages.size() > 10) {
                    miniContext += "\n<dark_gray><i>+ " + (messages.size() - 10) + " more...";
                }

                Component summary = Msg.mm("""
                        <green>Channel:</green> <aqua>%s</aqua>
                        <green>Messages:</green> <aqua>%d</aqua>
                        
                        <green>Messages:</green>\
                        %s
                        """, Utils.captializeFirstLetters(channel.name().replace("_", " ").toLowerCase()),
                    messages.size(), miniContext);

                return Cytosis.get(ReportManager.class).getSubmitMenu(user, report, summary);
            });
        }

        private static class NoContextException extends RuntimeException {

        }
    }
}
