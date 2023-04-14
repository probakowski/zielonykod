package pl.robakowski.game;

import com.dslplatform.json.CompiledJson;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@CompiledJson
public record Game(int groupCount, Clan[] clans) {
}
