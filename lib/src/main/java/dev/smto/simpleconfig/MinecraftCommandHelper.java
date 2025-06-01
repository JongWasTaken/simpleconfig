package dev.smto.simpleconfig;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

@SuppressWarnings("unused")
public class MinecraftCommandHelper {
    private final SimpleConfig instance;
    public MinecraftCommandHelper(SimpleConfig instance) {
        this.instance = instance;
    }

    public void reload() {
        this.instance.read();
    }

    public String get(String key) {
        return this.instance.toMap().getOrDefault(key, null);
    }

    public boolean set(String key, String value) {
        if (this.instance.trySet(key, value)) {
            this.instance.write();
            return true;
        }
        return false;
    }

    public RequiredArgumentBuilder<Object, String> createGetValueCommandNode(BiFunction<String, String, Integer> callback) {
        return argument("key", string()).executes(context -> {
            var key = getString(context, "key");
            var value = this.get(key);
            return callback.apply(key, value);
        }).suggests((commandContext, suggestionsBuilder) -> this.suggestMatchingKeys(suggestionsBuilder));
    }

    public RequiredArgumentBuilder<Object, String> createSetValueCommandNode(BiFunction<String, String, Integer> callback) {
        return argument("value", string()).executes(context -> {
            var key = getString(context, "key");
            var value = getString(context, "value");
            if (this.set(key, value)) return callback.apply(key, value);
            else return callback.apply(key, null);
        }).suggests((commandContext, suggestionsBuilder) -> this.suggestMatchingValues(commandContext.getArgument("key", String.class), suggestionsBuilder));

    }
    public CompletableFuture<Suggestions> suggestMatchingValues(String key, SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);

        for (String string2 : List.of(this.instance.toMap().getOrDefault(key, ""))) {
            if (MinecraftCommandHelper.shouldSuggest(string, string2.toLowerCase(Locale.ROOT))) {
                builder.suggest(string2);
            }
        }

        return builder.buildFuture();
    }

    public CompletableFuture<Suggestions> suggestMatchingKeys(SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);

        for (String string2 : this.instance.getKeys()) {
            if (MinecraftCommandHelper.shouldSuggest(string, string2.toLowerCase(Locale.ROOT))) {
                builder.suggest(string2);
            }
        }

        return builder.buildFuture();
    }

    private static boolean shouldSuggest(String remaining, String candidate) {
        for (int i = 0; !candidate.startsWith(remaining, i); i++) {
            int j = candidate.indexOf(46, i);
            int k = candidate.indexOf(95, i);
            if (Math.max(j, k) < 0) {
                return false;
            }

            if (j >= 0 && k >= 0) {
                i = Math.min(k, j);
            } else {
                i = j >= 0 ? j : k;
            }
        }

        return true;
    }
}
