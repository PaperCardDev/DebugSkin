package cn.paper_card.debug_skin;

import cn.paper_card.mc_command.TheMcCommand;
import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

class MyCommand extends TheMcCommand.HasSub {

    private final @NotNull DebugSkin plugin;
    private final @NotNull Permission permission;

    MyCommand(@NotNull DebugSkin plugin) {
        super("debug-skin");
        this.plugin = plugin;
        this.permission = Objects.requireNonNull(plugin.getServer().getPluginManager().getPermission("debug-skin.command"));

        final PluginCommand c = plugin.getCommand("debug-skin");
        assert c != null;
        c.setExecutor(this);
        c.setTabCompleter(this);

        this.addSubCommand(new Debug());
        this.addSubCommand(new Update());
        this.addSubCommand(new Copy());
    }

    @Nullable Player findOnline(@NotNull String name) {
        for (Player onlinePlayer : this.plugin.getServer().getOnlinePlayers()) {
            if (name.equals(onlinePlayer.getName())) return onlinePlayer;
        }
        return null;
    }

    @NotNull LinkedList<String> tabCompletePlayerNames(@NotNull String arg, @NotNull String tip) {
        final LinkedList<String> list = new LinkedList<>();
        if (arg.isEmpty()) list.add(tip);
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            final String name = onlinePlayer.getName();
            if (name.startsWith(arg)) list.add(name);
        }
        return list;
    }

    @Override
    protected boolean canNotExecute(@NotNull CommandSender commandSender) {
        return !commandSender.hasPermission(this.permission);
    }

    class Debug extends TheMcCommand {

        private final @NotNull Permission permission;

        protected Debug() {
            super("debug");
            this.permission = plugin.addPermission(MyCommand.this.permission.getName() + "." + this.getLabel());
        }

        @Override
        protected boolean canNotExecute(@NotNull CommandSender commandSender) {
            return !commandSender.hasPermission(this.permission);
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
                player = findOnline(argPlayer);

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
                return tabCompletePlayerNames(argName, "[在线玩家名]");
            }
            return null;
        }

    }

    class Update extends TheMcCommand {

        private final @NotNull Permission permission;

        private final @NotNull Permission permSelf;
        private final @NotNull Permission permAny;

        protected Update() {
            super("update");
            this.permission = plugin.addPermission(MyCommand.this.permission.getName() + "." + this.getLabel());
            this.permSelf = plugin.addPermission(this.permission.getName() + "." + "self");
            this.permAny = plugin.addPermission(this.permission.getName() + "." + "any");
        }

        @Override
        protected boolean canNotExecute(@NotNull CommandSender commandSender) {
            return !commandSender.hasPermission(this.permission);
        }

        @Override
        public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

            final Player targetPlayer;

            final String argPlayer = strings.length > 0 ? strings[0] : null;

            if (argPlayer == null) {
                if (!(commandSender instanceof final Player player)) {
                    plugin.sendError(commandSender, "当不指定玩家参数时，该命令只能由玩家来执行！");
                    return true;
                }
                targetPlayer = player;

                if (!(commandSender.hasPermission(this.permSelf))) {
                    plugin.sendError(commandSender, "你没有权限刷新自己的皮肤！");
                    return true;
                }
            } else {

                if (!(commandSender.hasPermission(this.permAny))) {
                    plugin.sendError(commandSender, "你没有权限刷新任意玩家的皮肤！");
                    return true;
                }

                targetPlayer = findOnline(argPlayer);
                if (targetPlayer == null) {
                    plugin.sendError(commandSender, "找不到在线玩家：" + argPlayer);
                    return true;
                }
            }

            final PlayerProfile profile = targetPlayer.getPlayerProfile();
            profile.clearProperties();

            plugin.sendInfo(commandSender, "正在刷新...");

            profile.update()
                    .thenAcceptAsync(pp -> {

                        plugin.getScheduler().runTask(() -> targetPlayer.setPlayerProfile(pp));

                        plugin.sendInfo(commandSender, "刷新成功 :D");

                    }, command1 -> plugin.getScheduler().runTaskAsynchronously(command1))

                    .exceptionally(throwable -> {
                        plugin.sendInfo(commandSender, "刷新失败：" + throwable);
                        return null;
                    });

            return true;
        }

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
            return null;
        }
    }

    class Copy extends TheMcCommand {

        private final @NotNull Permission permission;

        protected Copy() {
            super("copy");
            this.permission = plugin.addPermission(MyCommand.this.permission.getName() + "." + this.getLabel());
        }

        @Override
        protected boolean canNotExecute(@NotNull CommandSender commandSender) {
            return !commandSender.hasPermission(this.permission);
        }

        @Override
        public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
            final String argTargetPlayer = strings.length > 0 ? strings[0] : null;
            final String argSourcePlayer = strings.length > 1 ? strings[1] : null;

            if (argTargetPlayer == null) {
                plugin.sendError(commandSender, "必须指定参数：目标玩家名");
                return true;
            }

            if (argSourcePlayer == null) {
                plugin.sendError(commandSender, "必须指定参数：来源玩家名");
                return true;
            }

            final Player targetPlayer = findOnline(argTargetPlayer);
            if (targetPlayer == null) {
                plugin.sendError(commandSender, "目标玩家 %s 不在线！".formatted(argTargetPlayer));
                return true;
            }

            final Player sourcePlayer = findOnline(argSourcePlayer);
            if (sourcePlayer == null) {
                plugin.sendError(commandSender, "来源玩家 %s 不在线！".formatted(argSourcePlayer));
                return true;
            }

            final PlayerProfile profileTarget = targetPlayer.getPlayerProfile();

            profileTarget.clearProperties();
            profileTarget.getProperties().addAll(sourcePlayer.getPlayerProfile().getProperties());
            profileTarget.setTextures(sourcePlayer.getPlayerProfile().getTextures());

            targetPlayer.setPlayerProfile(profileTarget);

            plugin.sendInfo(commandSender, "复制成功 :D");
            return true;
        }

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
            if (strings.length == 1) {
                return tabCompletePlayerNames(strings[0], "<目标玩家名>");
            }

            if (strings.length == 2) {
                return tabCompletePlayerNames(strings[1], "<来源玩家名>");
            }

            return null;
        }
    }
}
