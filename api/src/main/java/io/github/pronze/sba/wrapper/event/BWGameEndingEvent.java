package io.github.pronze.sba.wrapper.event;

import io.github.pronze.sba.game.GameWrapper;
import io.github.pronze.sba.wrapper.RunningTeamWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.screamingsandals.lib.event.AbstractEvent;

@RequiredArgsConstructor
@Getter
public class BWGameEndingEvent extends AbstractEvent {
    private final GameWrapper game;
    private final RunningTeamWrapper winningTeam;
}
