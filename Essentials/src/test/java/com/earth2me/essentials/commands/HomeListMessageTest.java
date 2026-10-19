package com.earth2me.essentials.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeListMessageTest {
    @Test
    void eachHomeRunsItsOwnTeleportCommandAndHasATooltip() {
        final Component message = parse(HomeListMessage.create(Arrays.asList("home", "farma"), null, false, false));
        final List<Component> links = clickable(message);
        assertEquals(Arrays.asList("/essentials:home home", "/essentials:home farma"), commands(links));
        assertEquals("Domy: home, farma\nKliknij aby teleportować", text(message));
        for (final Component link : links) {
            assertEquals(ClickEvent.Action.RUN_COMMAND, link.clickEvent().action());
            assertEquals(HoverEvent.Action.SHOW_TEXT, link.hoverEvent().action());
            assertTrue(text((Component) link.hoverEvent().value()).contains("Kliknij, aby teleportować do domu: " + text(link)));
        }
    }

    @Test
    void otherPlayersHomesKeepTheCorrectOwnerInTheCommand() {
        final Component message = parse(HomeListMessage.create(Arrays.asList("home", "farma"), "Steve", false, false));
        assertEquals(Arrays.asList("/essentials:home Steve:home", "/essentials:home Steve:farma"), commands(clickable(message)));
    }

    @Test
    void existingBedIsClickableAndUsesTheBedSelector() {
        final Component message = parse(HomeListMessage.create(Arrays.asList("home"), null, true, true));
        assertEquals(Arrays.asList("/essentials:home home", "/essentials:home bed"), commands(clickable(message)));
        assertTrue(text(message).contains("home, łóżko\nKliknij aby teleportować"));
    }

    @Test
    void missingBedIsNotClickable() {
        final Component message = parse(HomeListMessage.create(Arrays.asList("home"), null, true, false));
        assertEquals(Arrays.asList("/essentials:home home"), commands(clickable(message)));
        assertTrue(text(message).contains("łóżko"));
    }

    @Test
    void bedIsHiddenWithoutPermissionAndInputListIsNotModified() {
        final List<String> homes = new ArrayList<>(Arrays.asList("home", "farma"));
        final Component message = parse(HomeListMessage.create(homes, null, false, true));
        assertFalse(text(message).contains("łóżko"));
        assertEquals(Arrays.asList("home", "farma"), homes);
    }

    @Test
    void homeNamesAreRenderedAsLiteralText() {
        final Component message = parse(HomeListMessage.create(Arrays.asList("<red>home"), null, false, false));
        assertTrue(text(message).contains("<red>home"));
        assertEquals(Arrays.asList("/essentials:home <red>home"), commands(clickable(message)));
    }

    private Component parse(final String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }

    private List<String> commands(final List<Component> links) {
        return links.stream().map(link -> link.clickEvent().value()).collect(Collectors.toList());
    }

    private List<Component> clickable(final Component component) {
        final List<Component> links = new ArrayList<>();
        if (component.clickEvent() != null) {
            links.add(component);
        }
        for (final Component child : component.children()) {
            links.addAll(clickable(child));
        }
        return links;
    }

    private String text(final Component component) {
        final StringBuilder result = new StringBuilder(component instanceof TextComponent ? ((TextComponent) component).content() : "");
        for (final Component child : component.children()) {
            result.append(text(child));
        }
        return result.toString();
    }
}
