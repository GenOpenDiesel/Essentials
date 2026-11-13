package com.earth2me.essentials;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.NameTagManager;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Adds a temporary vanish tag through TAB without changing tablist formatting. */
final class TabVanishNametags implements Runnable {
    static final String MARKER = " &8[&7VANISH&8]&r";
    private final Essentials ess;
    private final Map<TabPlayer, Suffix> suffixes = new IdentityHashMap<>();
    private NameTagManager manager;

    TabVanishNametags(final Essentials ess) {
        this.ess = ess;
    }

    @Override
    public void run() {
        if (!ess.getServer().getPluginManager().isPluginEnabled("TAB")) {
            suffixes.clear();
            manager = null;
            return;
        }
        final TabAPI api = TabAPI.getInstance();
        if (api == null) {
            return;
        }
        final NameTagManager current = api.getNameTagManager();
        if (current != manager) {
            // TAB reloads discard previous API overrides and TabPlayer instances.
            suffixes.clear();
            manager = current;
        }
        if (manager == null) {
            return;
        }
        final Set<TabPlayer> online = new HashSet<>();
        for (final User user : ess.getOnlineUsers()) {
            final TabPlayer player = api.getPlayer(user.getUUID());
            if (player != null) {
                online.add(player);
                update(manager, player, user.isVanished());
            }
        }
        suffixes.keySet().retainAll(online);
    }

    void update(final NameTagManager tags, final TabPlayer player, final boolean vanished) {
        Suffix previous = suffixes.get(player);
        final String custom = tags.getCustomSuffix(player);
        if (!vanished) {
            if (previous != null && Objects.equals(custom, previous.applied)) {
                tags.setSuffix(player, previous.original);
            }
            suffixes.remove(player);
            return;
        }
        if (previous == null || !Objects.equals(custom, previous.applied)) {
            previous = new Suffix(custom);
            suffixes.put(player, previous);
        }
        // Keep placeholders dynamic and follow group/world changes while vanished.
        final String original = previous.original == null ? tags.getOriginalRawSuffix(player) : previous.original;
        final String applied = (original == null ? "" : original) + MARKER;
        previous.applied = applied;
        if (!applied.equals(custom)) {
            tags.setSuffix(player, applied);
        }
    }

    void close() {
        if (manager != null && ess.getServer().getPluginManager().isPluginEnabled("TAB")) {
            for (final TabPlayer player : new HashSet<>(suffixes.keySet())) {
                update(manager, player, false);
            }
        }
        suffixes.clear();
    }

    private static final class Suffix {
        private final String original;
        private String applied;

        private Suffix(final String original) {
            this.original = original;
        }
    }
}
