package com.earth2me.essentials.commands;

import com.earth2me.essentials.ISettings;
import com.earth2me.essentials.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HomeLimitMessageTest {
    private User user;
    private ISettings settings;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        settings = mock(ISettings.class);
        when(settings.getMultipleHomes()).thenReturn(new HashSet<>(Arrays.asList("default", "vip", "evip", "mvip", "elita", "staff")));
        when(settings.getHomeLimit("vip")).thenReturn(5);
        when(settings.getHomeLimit("evip")).thenReturn(10);
        when(settings.getHomeLimit("mvip")).thenReturn(15);
        when(settings.getHomeLimit("elita")).thenReturn(20);
        when(settings.getHomeLimit("staff")).thenReturn(100);
    }

    @Test
    void playerWithoutRankIsOfferedVip() {
        assertRank(1, "<yellow>VIP</yellow>", 5);
    }

    @Test
    void vipPermissionOffersEvipWithConfiguredLimit() {
        when(user.isAuthorized("essentials.sethome.multiple.vip")).thenReturn(true);
        assertRank(5, "<gold>EVIP</gold>", 10);
    }

    @Test
    void highestInheritedRankIsUsed() {
        when(user.isAuthorized("essentials.sethome.multiple.vip")).thenReturn(true);
        when(user.isAuthorized("essentials.sethome.multiple.evip")).thenReturn(true);
        assertRank(10, "<dark_purple>MVIP</dark_purple>", 15);
    }

    @Test
    void mvipGroupIsOfferedElita() {
        when(user.inGroup("mvip")).thenReturn(true);
        assertRank(15, "<aqua>ELITA</aqua>", 20);
    }

    @Test
    void elitaPermissionOnlyOffersExtraHomes() {
        when(user.isAuthorized("essentials.sethome.multiple.elita")).thenReturn(true);
        assertExtraHomes(1);
    }

    @Test
    void elitaPrimaryGroupNeverOffersAnotherRank() {
        when(user.getGroup()).thenReturn("ELITA");
        assertExtraHomes(1);
    }

    @Test
    void inheritedElitaGroupNeverOffersAnotherRank() {
        when(user.inGroup("elita")).thenReturn(true);
        when(user.isAuthorized("essentials.sethome.multiple.vip")).thenReturn(true);
        assertExtraHomes(5);
    }

    @Test
    void rankWithTheSameLimitIsSkipped() {
        when(settings.getHomeLimit("vip")).thenReturn(3);
        assertRank(3, "<gold>EVIP</gold>", 10);
    }

    @Test
    void rankSetNamesAreCaseInsensitive() {
        when(settings.getMultipleHomes()).thenReturn(new HashSet<>(Arrays.asList("VIP")));
        when(settings.getHomeLimit("VIP")).thenReturn(7);
        assertRank(1, "<yellow>VIP</yellow>", 7);
    }

    @Test
    void extraHomePermissionsSkipRanksWithInsufficientLimits() {
        when(user.isAuthorized("essentials.sethome.multiple.vip")).thenReturn(true);
        assertRank(12, "<dark_purple>MVIP</dark_purple>", 15);
    }

    @Test
    void noUpgradeIsAdvertisedWhenItGivesFewerHomes() {
        assertExtraHomes(25);
    }

    @Test
    void missingRankConfigurationDoesNotInventAllowances() {
        when(settings.getMultipleHomes()).thenReturn(null);
        assertExtraHomes(3);
    }

    @Test
    void hardcodedMessagesRetainClickableStoreLink() {
        assertTrue(hasStoreLink(MiniMessage.miniMessage().deserialize(HomeLimitMessage.create(user, settings, 1))));
        when(user.isAuthorized("essentials.sethome.multiple.elita")).thenReturn(true);
        assertTrue(hasStoreLink(MiniMessage.miniMessage().deserialize(HomeLimitMessage.create(user, settings, 20))));
    }

    @Test
    void usageShowsThreeUsedAndTwentyTwoAvailable() {
        when(user.getHomes()).thenReturn(Arrays.asList("baza", "kopalnia", "farma"));
        when(settings.getHomeLimit(user)).thenReturn(25);
        assertEquals("<gray>Domy — wykorzystane: <yellow>3<gray>, wolne: <green>22<gray>.", HomeLimitMessage.usage(user, settings));
    }

    @Test
    void usageAtTheLimitShowsNoAvailableHomes() {
        when(user.getHomes()).thenReturn(Collections.nCopies(25, "home"));
        when(settings.getHomeLimit(user)).thenReturn(25);
        assertTrue(HomeLimitMessage.usage(user, settings).contains("wolne: <green>0<gray>"));
    }

    @Test
    void usageNeverShowsNegativeAvailabilityAfterALimitReduction() {
        when(user.getHomes()).thenReturn(Collections.nCopies(27, "home"));
        when(settings.getHomeLimit(user)).thenReturn(25);
        assertTrue(HomeLimitMessage.usage(user, settings).contains("wykorzystane: <yellow>27<gray>, wolne: <green>0<gray>"));
    }

    @Test
    void usageRecognizesUnlimitedHomePermission() {
        when(user.getHomes()).thenReturn(Arrays.asList("baza", "farma"));
        when(user.isAuthorized("essentials.sethome.multiple.unlimited")).thenReturn(true);
        assertTrue(HomeLimitMessage.usage(user, settings).contains("wykorzystane: <yellow>2<gray>, wolne: <green>bez limitu<gray>"));
    }

    @Test
    void usageReadsTheCurrentPermissionBasedLimitEachTime() {
        when(user.getHomes()).thenReturn(Arrays.asList("baza", "kopalnia", "farma"));
        when(settings.getHomeLimit(user)).thenReturn(5, 25);
        assertTrue(HomeLimitMessage.usage(user, settings).contains("wolne: <green>2<gray>"));
        assertTrue(HomeLimitMessage.usage(user, settings).contains("wolne: <green>22<gray>"));
    }

    private boolean hasStoreLink(final Component component) {
        final ClickEvent clickEvent = component.clickEvent();
        return clickEvent != null && clickEvent.action() == ClickEvent.Action.OPEN_URL && "https://bfsmc.pl".equals(clickEvent.value())
            || component.children().stream().anyMatch(this::hasStoreLink);
    }

    private void assertRank(final int limit, final String rank, final int rankLimit) {
        final String message = HomeLimitMessage.create(user, settings, limit);
        assertTrue(message.contains("limit domów: <yellow>" + limit + "<red>"));
        assertTrue(message.contains("Z rangą " + rank));
        assertTrue(message.contains("możesz mieć <green>" + rankLimit + "<gray> domów"));
        assertFalse(message.contains("Dokup dodatkowe sethome"));
    }

    private void assertExtraHomes(final int limit) {
        final String message = HomeLimitMessage.create(user, settings, limit);
        assertTrue(message.contains("limit domów: <yellow>" + limit + "<red>"));
        assertTrue(message.contains("Dokup dodatkowe sethome na bfsmc.pl"));
        assertFalse(message.contains("Z rangą"));
    }
}
