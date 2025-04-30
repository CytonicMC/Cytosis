package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.utils.BuildInfo;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;

public class WhereAmiCommand extends CytosisCommand {

    public WhereAmiCommand() {
        super("whereami", "version");
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(
                    Msg.yellowSplash("SERVER INFO!", "Here is some basic server information:").appendNewline()
                            .append(Msg.mm("<gold>Server ID: </gold><gray>" + Cytosis.SERVER_ID)).appendNewline()
                            .append(Msg.mm("<gold>Latest Commit: </gold><gray><hover:show_text:'<gold><b>Click to copy Commit Hash!'><click:copy_to_clipboard:%s>%s", BuildInfo.GIT_COMMIT, BuildInfo.GIT_COMMIT)).appendNewline()
                            .append(Msg.mm("<gold>Build Version: </gold><gray>" + BuildInfo.BUILD_VERSION)).appendNewline()
                            .append(Msg.mm("<gold>Built: </gold><gray>" + DurationParser.unparseFull(BuildInfo.BUILT_AT) + " ago")).appendNewline()
                            .append(Msg.mm("<gold>Server Type: </gold><gray>" + Cytosis.getServerGroup().humanReadable())).appendNewline()
            );
        });
    }
}
