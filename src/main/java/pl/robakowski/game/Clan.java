package pl.robakowski.game;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public record Clan(int numberOfPlayers, int points) {
}
