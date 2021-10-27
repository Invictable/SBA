package io.github.pronze.sba.events;

import io.github.pronze.sba.party.Party;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;

@RequiredArgsConstructor
@Getter
public class SBAPlayerPartyCreatedEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private final SBAPlayerWrapper player;
    private final Party party;
    private boolean cancelled;

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return SBAPlayerPartyCreatedEvent.handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
