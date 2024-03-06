package cn.paper_card.debug_skin;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.text.SimpleDateFormat;

public final class DebugSkin extends JavaPlugin {

    @Override
    public void onEnable() {
        new MyCommand(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    void appendPrefix(@NotNull TextComponent.Builder builder) {
        builder.append(Component.text("[").color(NamedTextColor.GRAY));
        builder.append(Component.text(this.getName()).color(NamedTextColor.DARK_AQUA));
        builder.append(Component.text("]").color(NamedTextColor.GRAY));
    }

    void sendError(@NotNull CommandSender sender, @NotNull String error) {
        final TextComponent.Builder text = Component.text();
        this.appendPrefix(text);
        text.appendSpace();
        text.append(Component.text(error).color(NamedTextColor.RED));
        sender.sendMessage(text.build());
    }

    @NotNull TextComponent copyable(@NotNull String text) {
        return Component.text(text).decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.copyToClipboard(text))
                .hoverEvent(HoverEvent.showText(Component.text("点击复制")));
    }

    @NotNull TextComponent link(@NotNull String text) {
        return Component.text(text).decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(text))
                .hoverEvent(HoverEvent.showText(Component.text("点击打开")));
    }

    @NotNull TextComponent doIt(@NotNull Player player) {
        final TextComponent.Builder text = Component.text();
        this.appendPrefix(text);
        text.appendSpace();
        text.append(Component.text("==== 玩家属性 ===="));

        text.appendNewline();
        text.append(Component.text("客户端："));
        text.append(Component.text("%s".formatted(player.getClientBrandName())));

        final PlayerProfile playerProfile = player.getPlayerProfile();


        final PlayerTextures textures = playerProfile.getTextures();
        final URL skin = textures.getSkin();
        final URL cape = textures.getCape();
        final boolean signed = textures.isSigned();
        final PlayerTextures.SkinModel skinModel = textures.getSkinModel();
        final long timestamp = textures.getTimestamp();

        if (skin != null) {
            text.appendNewline();
            text.append(Component.text("皮肤链接："));
            text.append(link(skin.toString()));
        }

        if (cape != null) {
            text.appendNewline();
            text.append(Component.text("披风链接："));
            text.append(link(cape.toString()));
        }

        text.appendNewline();
        text.append(Component.text("签名："));
        text.append(Component.text(signed));

        text.appendNewline();
        text.append(Component.text("模型："));
        text.append(Component.text(skinModel.name()));


        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        text.appendNewline();
        text.append(Component.text("时间："));
        text.append(Component.text(format.format(timestamp)));

        for (ProfileProperty property : playerProfile.getProperties()) {
            final String name = property.getName();
            final String value = property.getValue();
            final String signature = property.getSignature();
            text.appendNewline();
            text.append(Component.text(name));
            text.append(Component.text("："));
            text.append(copyable(value));

            if (signature != null) {
                text.appendNewline();
                text.append(Component.text("signature："));
                text.append(copyable(signature));
            }
        }

        return text.build().color(NamedTextColor.GREEN);
    }
}
