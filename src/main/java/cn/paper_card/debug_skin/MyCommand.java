package cn.paper_card.debug_skin;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

class MyCommand implements CommandExecutor, TabExecutor {

    private final @NotNull DebugSkin plugin;

    MyCommand(@NotNull DebugSkin plugin) {
        this.plugin = plugin;
        final PluginCommand c = plugin.getCommand("debug-skin");
        assert c != null;
        c.setExecutor(this);
        c.setTabCompleter(this);
    }

    @Nullable Player findOnline(@NotNull String name) {
        for (Player onlinePlayer : this.plugin.getServer().getOnlinePlayers()) {
            if (name.equals(onlinePlayer.getName())) return onlinePlayer;
        }
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        final String argPlayer = args.length > 0 ? args[0] : null;

        final Player player;

        if (argPlayer == null) {
            if (!(sender instanceof final Player p)) {
                plugin.sendError(sender, "不指定玩家参数时，只能由玩家自身来执行！");
                return true;
            }
            player = p;
        } else {
            player = this.findOnline(argPlayer);

            if (player == null) {
                plugin.sendError(sender, "找不到该在线玩家：" + argPlayer);
                return true;
            }
        }

        final TextComponent textComponent = plugin.doIt(player);

        sender.sendMessage(textComponent);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            final String argName = args[0];
            final LinkedList<String> list = new LinkedList<>();
            if (argName.isEmpty()) list.add("[在线玩家名]");
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                final String name = onlinePlayer.getName();
                if (name.startsWith(argName)) list.add(name);
            }
            return list;
        }
        return null;
    }
}
