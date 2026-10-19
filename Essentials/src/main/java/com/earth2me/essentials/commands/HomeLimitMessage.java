package com.earth2me.essentials.commands;

import com.earth2me.essentials.ISettings;
import com.earth2me.essentials.User;

import java.util.Locale;
import java.util.Set;

final class HomeLimitMessage {
    private static final String[] RANKS = {"vip", "evip", "mvip", "elita"};
    private static final String[] COLORS = {"yellow", "gold", "dark_purple", "aqua"};
    private static final String STORE_LINK_START = "<click:open_url:\"https://bfsmc.pl\"><hover:show_text:\"<gray>Kliknij, aby sprawdzić ofertę\"><green><underlined>";
    private static final String STORE_LINK_END = "</underlined></green></hover></click>";

    private HomeLimitMessage() {
    }

    static String usage(final User user, final ISettings settings) {
        final int used = user.getHomes().size();
        final String available = user.isAuthorized("essentials.sethome.multiple.unlimited")
            ? "bez limitu" : Integer.toString(Math.max(0, settings.getHomeLimit(user) - used));
        return "<gray>Domy — wykorzystane: <yellow>" + used + "<gray>, wolne: <green>" + available + "<gray>.";
    }

    static String create(final User user, final ISettings settings, final int limit) {
        final String limitMessage = "<red>Osiągnięto limit domów: <yellow>" + limit + "<red>. ";
        final String primaryGroup = user.getGroup();
        int currentRank = -1;
        for (int i = RANKS.length - 1; i >= 0; i--) {
            if (user.isAuthorized("essentials.sethome.multiple." + RANKS[i])
                || user.inGroup(RANKS[i]) || RANKS[i].equalsIgnoreCase(primaryGroup)) {
                currentRank = i;
                break;
            }
        }

        // Only advertise a rank when its configured allowance really exceeds the player's
        // effective permission-based limit. Never offer another rank to an ELITA player.
        final Set<String> homeSets = settings.getMultipleHomes();
        if (homeSets != null) {
            for (int i = currentRank + 1; i < RANKS.length; i++) {
                for (final String set : homeSets) {
                    if (RANKS[i].equalsIgnoreCase(set)) {
                        final int rankLimit = settings.getHomeLimit(set);
                        if (rankLimit > limit) {
                            final String rank = "<" + COLORS[i] + ">" + RANKS[i].toUpperCase(Locale.ENGLISH) + "</" + COLORS[i] + ">";
                            return limitMessage + "<gray>Z rangą " + rank + "<gray> możesz mieć <green>" + rankLimit + "<gray> domów. "
                                + STORE_LINK_START + "Sprawdź rangi na bfsmc.pl" + STORE_LINK_END;
                        }
                    }
                }
            }
        }

        return limitMessage + "<gray>Potrzebujesz więcej? " + STORE_LINK_START + "Dokup dodatkowe sethome na bfsmc.pl" + STORE_LINK_END;
    }
}
