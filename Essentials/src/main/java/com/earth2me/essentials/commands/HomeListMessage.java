package com.earth2me.essentials.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

final class HomeListMessage {
    private HomeListMessage() {
    }

    static String create(final List<String> homes, final String owner, final boolean showBed, final boolean hasBed) {
        final String commandPrefix = "/essentials:home " + (owner == null ? "" : owner + ":");
        Component message = Component.text("Domy: ", NamedTextColor.GRAY);
        boolean first = true;
        for (final String home : homes) {
            if (!first) {
                message = message.append(Component.text(", ", NamedTextColor.GRAY));
            }
            message = message.append(destination(home, commandPrefix + home, "Kliknij, aby teleportować do domu: " + home));
            first = false;
        }
        if (showBed) {
            if (!first) {
                message = message.append(Component.text(", ", NamedTextColor.GRAY));
            }
            if (hasBed) {
                message = message.append(destination("łóżko", commandPrefix + "bed", "Kliknij, aby teleportować do łóżka"));
            } else {
                message = message.append(Component.text("łóżko", NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH)
                    .hoverEvent(Component.text("Nie ustawiono punktu odrodzenia w łóżku", NamedTextColor.GRAY)));
            }
        }
        message = message.append(Component.newline())
            .append(Component.text("Kliknij aby teleportować", NamedTextColor.GRAY));
        return MiniMessage.miniMessage().serialize(message);
    }

    private static Component destination(final String name, final String command, final String tooltip) {
        return Component.text(name, NamedTextColor.WHITE).decorate(TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.runCommand(command))
            .hoverEvent(Component.text(tooltip, NamedTextColor.GRAY));
    }
}
