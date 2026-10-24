package com.earth2me.essentials;

import com.earth2me.essentials.commands.Commandmsg;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanishWeightTest {
    private ServerMock server;
    private Essentials ess;
    private User mod;
    private User hadmin;
    private User owner;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        Essentials.TESTING = true;
        ess = MockBukkit.load(Essentials.class);
        // Enable real permission checks after the test-only plugin bootstrap.
        Essentials.TESTING = false;
        mod = addStaff("mod", 10);
        hadmin = addStaff("hadmin", 50);
        owner = addStaff("owner", 100);
    }

    @AfterEach
    void tearDown() {
        Essentials.TESTING = true;
        MockBukkit.unmock();
    }

    private User addStaff(final String name, final int weight) {
        final PlayerMock player = server.addPlayer(name);
        final User user = ess.getUser(player);
        final PermissionAttachment attachment = player.addAttachment(ess);
        attachment.setPermission("essentials.vanish.see", true);
        attachment.setPermission("essentials.vanish.weight." + weight, true);
        return user;
    }

    @Test
    void higherRanksSeeLowerRanksButLowerRanksCannotSeeHigherRanks() {
        mod.setVanished(true);
        hadmin.setVanished(true);
        owner.setVanished(true);

        assertFalse(mod.getBase().canSee(hadmin.getBase()));
        assertFalse(mod.getBase().canSee(owner.getBase()));
        assertTrue(hadmin.getBase().canSee(mod.getBase()));
        assertFalse(hadmin.getBase().canSee(owner.getBase()));
        assertTrue(owner.getBase().canSee(mod.getBase()));
        assertTrue(owner.getBase().canSee(hadmin.getBase()));
        assertTrue(owner.getBase().canSee(owner.getBase()));
    }

    @Test
    void equalRanksCanSeeEachOther() {
        final User peer = addStaff("peer", 50);
        hadmin.setVanished(true);
        assertTrue(peer.getBase().canSee(hadmin.getBase()));
    }

    @Test
    void weightDoesNotGrantSeePermission() {
        final PermissionAttachment attachment = owner.getBase().addAttachment(ess);
        attachment.setPermission("essentials.vanish.see", false);
        mod.setVanished(true);
        assertFalse(owner.getBase().canSee(mod.getBase()));
    }

    @Test
    void highestGrantedWeightWinsAndInvalidWeightsAreIgnored() {
        final PermissionAttachment attachment = mod.getBase().addAttachment(ess);
        attachment.setPermission("essentials.vanish.weight.50", true);
        attachment.setPermission("essentials.vanish.weight.100", false);
        attachment.setPermission("essentials.vanish.weight.*", true);
        attachment.setPermission("essentials.vanish.weight.-200", true);
        attachment.setPermission("essentials.vanish.weight.99999999999999999", true);
        attachment.setPermission("essentials.vanish.weight.200bad", true);
        attachment.setPermission("essentials.vanish.weight.+200", true);
        attachment.setPermission("essentials.vanish.weight.", true);
        assertEquals(50, mod.getVanishWeight());
    }

    @Test
    void unweightedPlayersKeepLegacyVisibility() {
        final User target = addStaff("legacyTarget", 0);
        final User viewer = ess.getUser(server.addPlayer("legacyViewer"));
        assertEquals(0, viewer.getVanishWeight());
        target.setVanished(true);
        assertFalse(viewer.getBase().canSee(target.getBase()));
        viewer.getBase().addAttachment(ess, "essentials.vanish.see", true);
        viewer.refreshVanishVisibility();
        assertTrue(viewer.getBase().canSee(target.getBase()));
    }

    @Test
    void operatorAndWildcardDoNotBypassWeights() {
        mod.getBase().setOp(true);
        mod.getBase().addAttachment(ess, "essentials.*", true);
        owner.setVanished(true);
        assertEquals(10, mod.getVanishWeight());
        assertFalse(mod.getBase().canSee(owner.getBase()));
    }

    @Test
    void refreshUpdatesBothDirectionsAfterPromotionAndDemotion() {
        mod.setVanished(true);
        hadmin.setVanished(true);
        final PermissionAttachment promotion = mod.getBase().addAttachment(ess);
        promotion.setPermission("essentials.vanish.weight.100", true);
        mod.refreshVanishVisibility();
        assertTrue(mod.getBase().canSee(hadmin.getBase()));
        assertFalse(hadmin.getBase().canSee(mod.getBase()));

        promotion.setPermission("essentials.vanish.weight.100", false);
        mod.refreshVanishVisibility();
        assertFalse(mod.getBase().canSee(hadmin.getBase()));
        assertTrue(hadmin.getBase().canSee(mod.getBase()));
    }

    @Test
    void joiningViewerIsCheckedEvenWithSeePermission() {
        owner.setVanished(true);
        final User joiningMod = addStaff("joiningMod", 10);
        final User joiningOwner = addStaff("joiningOwner", 100);
        owner.updateVanishVisibility(joiningMod);
        owner.updateVanishVisibility(joiningOwner);
        assertFalse(joiningMod.getBase().canSee(owner.getBase()));
        assertTrue(joiningOwner.getBase().canSee(owner.getBase()));
    }

    @Test
    void disablingVanishShowsPlayerToEveryRank() {
        owner.setVanished(true);
        owner.setVanished(false);
        assertTrue(mod.getBase().canSee(owner.getBase()));
        assertTrue(hadmin.getBase().canSee(owner.getBase()));
    }

    @Test
    void listHiddenPermissionDoesNotRevealHigherRanks() {
        mod.setVanished(true);
        owner.setVanished(true);
        assertFalse(PlayerList.getPlayerLists(ess, mod, true).values().stream()
                .flatMap(java.util.Collection::stream).anyMatch(user -> user.equals(owner)));
        assertTrue(PlayerList.getPlayerLists(ess, owner, true).values().stream()
                .flatMap(java.util.Collection::stream).anyMatch(user -> user.equals(mod)));
        assertTrue(PlayerList.getPlayerLists(ess, null, true).values().stream()
                .flatMap(java.util.Collection::stream).anyMatch(user -> user.equals(owner)));
    }

    @Test
    void hiddenCountDoesNotRevealHigherRanks() {
        owner.setVanished(true);
        assertEquals(mod.playerTl("listAmount", 2, server.getMaxPlayers()), PlayerList.listSummary(ess, mod, true));
    }

    @Test
    void tabCompletionDoesNotRevealHigherRanks() {
        owner.setVanished(true);
        final Commandmsg command = new Commandmsg();
        command.setEssentials(ess);
        assertFalse(command.tabComplete(server, mod, "msg", null, new String[] {""}).contains(owner.getName()));
        assertTrue(command.tabComplete(server, owner, "msg", null, new String[] {""}).contains(mod.getName()));
    }

    @Test
    void changingWorldRefreshesViewerPermissions() {
        owner.setVanished(true);
        mod.getBase().addAttachment(ess, "essentials.vanish.weight.100", true);
        new EssentialsPlayerListener(ess).onPlayerChangedWorld(new PlayerChangedWorldEvent(mod.getBase(), mod.getWorld()));
        assertTrue(mod.getBase().canSee(owner.getBase()));
    }

    @Test
    void maximumIntegerWeightIsSupported() {
        mod.getBase().addAttachment(ess, "essentials.vanish.weight.2147483647", true);
        assertEquals(Integer.MAX_VALUE, mod.getVanishWeight());
    }
}
