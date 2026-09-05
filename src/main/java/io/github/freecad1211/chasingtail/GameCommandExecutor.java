package io.github.freecad1211.chasingtail;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GameCommandExecutor implements CommandExecutor {

    private final Chasingtail plugin;

    public GameCommandExecutor(Chasingtail plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("꼬리잡기")) {
            return false;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("시작")) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.playersOnly());
            return true;
        }

        if (plugin.isGameActive()) {
            sender.sendMessage(Messages.gameAlreadyActive());
            return true;
        }

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.size() < Chasingtail.MIN_PLAYERS) {
            sender.sendMessage(Messages.needPlayers(Chasingtail.MIN_PLAYERS));
            return true;
        }
        if (onlinePlayers.size() > Chasingtail.MAX_PLAYERS) {
            sender.sendMessage(Messages.maxPlayers(Chasingtail.MAX_PLAYERS));
            return true;
        }

        if (!plugin.startGame(onlinePlayers)) {
            sender.sendMessage(Messages.startFailed());
        }
        // 시작 성공 메시지는 startGame 내부에서 브로드캐스트된다.
        return true;
    }
}