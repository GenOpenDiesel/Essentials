package com.earth2me.essentials.commands;

import com.earth2me.essentials.AsyncTeleport;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import net.ess3.api.TranslatableException;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class TeleportToggleBypassTest {
    private ServerMock server;
    private Essentials ess;
    private User actor;
    private User target;
    private AsyncTeleport teleport;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        Essentials.TESTING = true;
        ess = MockBukkit.load(Essentials.class);
        Essentials.TESTING = false;
        actor = spy(ess.getUser(server.addPlayer("Moderator")));
        target = ess.getUser(server.addPlayer("Target"));
        target.setTeleportEnabled(false);
        actor.getBase().addAttachment(ess, "essentials.tpo", false);
        actor.getBase().addAttachment(ess, "essentials.tpohere", false);
        teleport = mock(AsyncTeleport.class);
        doReturn(teleport).when(actor).getAsyncTeleport();
    }

    @AfterEach
    void tearDown() {
        Essentials.TESTING = true;
        MockBukkit.unmock();
    }

    @Test
    void tpWithoutBypassStillRejectsDisabledTarget() {
        final Commandtp command = new Commandtp();
        command.setEssentials(ess);
        assertThrows(TranslatableException.class, () -> command.run(server, actor, "tp", new String[] {"Target"}));
        verifyNoInteractions(teleport);
    }

    @Test
    void tpUsesExistingOverridePermission() {
        actor.getBase().addAttachment(ess, "essentials.tpo", true);
        final Commandtp command = new Commandtp();
        command.setEssentials(ess);
        assertThrows(NoChargeException.class, () -> command.run(server, actor, "tp", new String[] {"Target"}));
        verify(teleport).teleport(eq(target.getBase()), any(), any(), any());
    }

    @Test
    void tphereWithoutBypassStillRejectsDisabledTarget() {
        final Commandtphere command = new Commandtphere();
        command.setEssentials(ess);
        assertThrows(TranslatableException.class, () -> command.run(server, actor, "tphere", new String[] {"Target"}));
        verifyNoInteractions(teleport);
    }

    @Test
    void tphereUsesItsOwnOverridePermission() {
        actor.getBase().addAttachment(ess, "essentials.tpohere", true);
        final Commandtphere command = new Commandtphere();
        command.setEssentials(ess);
        assertThrows(NoChargeException.class, () -> command.run(server, actor, "tphere", new String[] {"Target"}));
        final Player actorBase = actor.getBase();
        verify(teleport).teleportPlayer(eq(target), eq(actorBase), any(), any(), any());
    }

    @Test
    void tpoPermissionDoesNotBypassTphere() {
        actor.getBase().addAttachment(ess, "essentials.tpo", true);
        final Commandtphere command = new Commandtphere();
        command.setEssentials(ess);
        assertThrows(TranslatableException.class, () -> command.run(server, actor, "tphere", new String[] {"Target"}));
        verifyNoInteractions(teleport);
    }

    @Test
    void tpoherePermissionDoesNotBypassTp() {
        actor.getBase().addAttachment(ess, "essentials.tpohere", true);
        final Commandtp command = new Commandtp();
        command.setEssentials(ess);
        assertThrows(TranslatableException.class, () -> command.run(server, actor, "tp", new String[] {"Target"}));
        verifyNoInteractions(teleport);
    }

    @Test
    void enabledTargetStillAcceptsNormalTp() {
        target.setTeleportEnabled(true);
        final Commandtp command = new Commandtp();
        command.setEssentials(ess);
        assertThrows(NoChargeException.class, () -> command.run(server, actor, "tp", new String[] {"Target"}));
        verify(teleport).teleport(eq(target.getBase()), any(), any(), any());
    }

    @Test
    void tpOthersChecksTheExecutorsBypassForBothPlayers() throws Exception {
        final User destination = ess.getUser(server.addPlayer("Destination"));
        destination.setTeleportEnabled(false);
        final User moved = spy(target);
        doReturn(teleport).when(moved).getAsyncTeleport();
        final String[] args = {"Target", "Destination"};
        final Commandtp command = spy(new Commandtp());
        command.setEssentials(ess);
        doReturn(moved).when(command).getPlayer(server, actor, args, 0);
        actor.getBase().addAttachment(ess, "essentials.tp.others", true);

        assertThrows(TranslatableException.class, () -> command.run(server, actor, "tp", args));
        target.setTeleportEnabled(true);
        assertThrows(TranslatableException.class, () -> command.run(server, actor, "tp", args));
        verifyNoInteractions(teleport);

        target.setTeleportEnabled(false);
        actor.getBase().addAttachment(ess, "essentials.tpo", true);
        command.run(server, actor, "tp", args);
        verify(teleport).now(eq(destination.getBase()), eq(false), any(), any());
    }

    @Test
    void tpCoordinatesChecksTheExecutorsBypass() throws Exception {
        final User moved = spy(target);
        doReturn(teleport).when(moved).getAsyncTeleport();
        final String[] args = {"Target", "1", "64", "1"};
        final Commandtp command = spy(new Commandtp());
        command.setEssentials(ess);
        doReturn(moved).when(command).getPlayer(server, actor, args, 0);
        actor.getBase().addAttachment(ess, "essentials.tp.others", true);
        actor.getBase().addAttachment(ess, "essentials.tp.position", true);

        assertThrows(TranslatableException.class, () -> command.run(server, actor, "tp", args));
        verifyNoInteractions(teleport);
        actor.getBase().addAttachment(ess, "essentials.tpo", true);
        command.run(server, actor, "tp", args);
        verify(teleport).now(any(Location.class), eq(false), any(), any());
    }
}
