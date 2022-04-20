package io.github.pronze.sba.lang;

import io.github.pronze.sba.AddonAPI;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.screamingsandals.lib.sender.CommandSenderWrapper;
import org.screamingsandals.lib.utils.AdventureHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
public class Message {
    private List<String> original = new ArrayList<>();
    private boolean prefix;

    public static Message of(List<String> text) {
        return new Message(text);
    }

    private Message(List<String> text) {
        original.addAll(text);
    }

    public Message replace(String key, String value) {
        original = original
                .stream()
                .map(str -> str.replaceAll(key, value))
                .collect(Collectors.toList());
        return this;
    }

    public Message replace(String key, Supplier<String> replacer) {
        original = original
                .stream()
                .map(str -> {
                    return replacer.get();
                })
                .collect(Collectors.toList());
        return this;
    }

    public Message replace(Function<String, String> replacer) {
        var pattern = Pattern.compile("%([a-zA-Z_.,0-9]+)%");
        original = original
                .stream()
                .map(str -> pattern.matcher(str).replaceAll(mr->replacer.apply(mr.group(1))))
                .collect(Collectors.toList());
        return this;
    }

    public Message withPrefix() {
        prefix = true;
        return this;
    }

    public Component toComponent() {
        final var component = Component.text();
        original.forEach(str -> {
            if (prefix) {
                str = AddonAPI
                        .getInstance()
                        .getConfigurator()
                        .getString("prefix", "[SBA]") + ": " + str;
            }
            component.append(MiniMessage.get().parse(str));
            if (original.indexOf(str) + 1 != original.size()) {
                component.append(Component.text("\n"));
            }
        });
        return component.build();
    }

    @Override
    public String toString() {
        var string = original.get(0);
        if (prefix) {
            string = AddonAPI
                    .getInstance()
                    .getConfigurator()
                    .getString("prefix", "[SBA]") + ": ";
        }
        return AdventureHelper.toLegacy(MiniMessage.get().parse(string));
    }

    public List<String> toStringList() {
        return toComponentList()
                .stream()
                .map(AdventureHelper::toLegacy)
                .collect(Collectors.toList());
    }

    public List<Component> toComponentList() {
        return original
                .stream()
                .map(str -> {
                    if (prefix) {
                        str = AddonAPI
                                .getInstance()
                                .getConfigurator()
                                .getString("prefix", "[SBA]") + ": " + str;
                    }
                    return str;
                })
                .map(MiniMessage.get()::parse)
                .collect(Collectors.toList());
    }

    public void send(CommandSenderWrapper... wrapper) {
        var message = toComponentList();
        for (var sender : wrapper) {
            message.forEach(sender::sendMessage);
        }
    }

    public void send(List<CommandSenderWrapper> wrapperList) {
        var message = toComponentList();
        wrapperList.forEach(wrapper -> message.forEach(wrapper::sendMessage));
    }
}
