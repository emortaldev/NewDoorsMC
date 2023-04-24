package dev.emortal.commands;

import dev.emortal.utils.text.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.Command;

import java.util.*;

public class CreditsCommand extends Command {

    public final List<String> DEVELOPERS = List.of(
            "emortaldev",
            "DasLixou",
            "OwOcast"
    );
    public final List<String> BUILDERS = List.of(
            "emortaldev",
            "Spaghetti_InSpace",
            "Hazardcake",
            "ufwcam",
            "W_MNQ",
            "OwOcast",
            "lforce2007",
            "natergaterdude"
    );

    public CreditsCommand() {
        super("credits");

        List<String> developers = new ArrayList<>(DEVELOPERS);
        List<String> builders = new ArrayList<>(BUILDERS);
        developers.sort(Comparator.comparing(String::toLowerCase));
        builders.sort(Comparator.comparing(String::toLowerCase));

        setDefaultExecutor((sender, ctx) -> {
            var message = Component.text();
            message.append(Component.text(" ".repeat(20) + TextUtils.smallText("credits"), NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));

            message.append(Component.text("\n\n  " + TextUtils.smallText("development"), TextColor.fromHexString("#FFF3DE")));
            for (String name : developers) {
                message.append(Component.text("\n - " + name, NamedTextColor.GRAY));
            }

            message.append(Component.text("\n\n  " + TextUtils.smallText("building"), TextColor.fromHexString("#FFF3DE")));
            for (String name : builders) {
                message.append(Component.text("\n - " + name, NamedTextColor.GRAY));
            }

            message.append(Component.text("\n\n  " + TextUtils.smallText("misc"), TextColor.fromHexString("#FFF3DE")));
            message.append(Component.text("\n - the pog fish (resource pack help)", NamedTextColor.GRAY));

            sender.sendMessage(message);
        });
    }
}
