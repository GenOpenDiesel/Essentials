package com.earth2me.essentials;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.NameTagManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TabVanishNametagsTest {
    private TabVanishNametags hook;
    private NameTagManager tags;
    private TabPlayer player;
    private AtomicReference<String> custom;
    private AtomicReference<String> configured;

    @BeforeEach
    void setUp() {
        hook = new TabVanishNametags(null);
        tags = mock(NameTagManager.class);
        player = mock(TabPlayer.class);
        custom = new AtomicReference<>();
        configured = new AtomicReference<>("%luckperms-suffix%");
        when(tags.getCustomSuffix(player)).thenAnswer(invocation -> custom.get());
        when(tags.getOriginalRawSuffix(player)).thenAnswer(invocation -> configured.get());
        doAnswer(invocation -> {
            custom.set(invocation.getArgument(1));
            return null;
        }).when(tags).setSuffix(eq(player), any());
    }

    @Test
    void appendsTagOnceAndRestoresConfigurationOnUnvanish() {
        hook.update(tags, player, true);
        hook.update(tags, player, true);
        assertEquals("%luckperms-suffix%" + TabVanishNametags.MARKER, custom.get());
        hook.update(tags, player, false);
        assertNull(custom.get());
    }

    @Test
    void preservesExistingApiSuffix() {
        custom.set(" &6Admin");
        hook.update(tags, player, true);
        assertEquals(" &6Admin" + TabVanishNametags.MARKER, custom.get());
        hook.update(tags, player, false);
        assertEquals(" &6Admin", custom.get());
    }

    @Test
    void followsWorldOrGroupSuffixChangesWhileVanished() {
        hook.update(tags, player, true);
        configured.set(" &bOwner");
        hook.update(tags, player, true);
        assertEquals(" &bOwner" + TabVanishNametags.MARKER, custom.get());
    }

    @Test
    void preservesAnotherPluginsNewSuffix() {
        hook.update(tags, player, true);
        custom.set(" &cNew");
        hook.update(tags, player, false);
        assertEquals(" &cNew", custom.get());
    }

    @Test
    void adoptsAnotherPluginsSuffixChangeDuringVanish() {
        hook.update(tags, player, true);
        custom.set(" &cNew");
        hook.update(tags, player, true);
        assertEquals(" &cNew" + TabVanishNametags.MARKER, custom.get());
        hook.update(tags, player, false);
        assertEquals(" &cNew", custom.get());
    }
}
