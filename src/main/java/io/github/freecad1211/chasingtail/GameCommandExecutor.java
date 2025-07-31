package io.github.freecad1211.chasingtail;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import java.util.List;

public class GameCommandExecutor implements CommandExecutor {

    private final Chasingtail plugin; // 메인 클래스 인스턴스 참조

    public GameCommandExecutor(Chasingtail plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // "꼬리잡기" 명령어인지 확인
        if (command.getName().equalsIgnoreCase("꼬리잡기")) {
            // 인자가 있고, 첫 번째 인자가 "시작"인지 확인
            if (args.length > 0 && args[0].equalsIgnoreCase("시작")) {
                // 명령어를 콘솔이 아닌 플레이어가 사용했는지 확인
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다.");
                    return true;
                }

                // 게임이 이미 진행 중인지 확인
                if (plugin.isGameActive()) {
                    sender.sendMessage(ChatColor.RED + "게임이 이미 진행 중입니다.");
                    return true;
                }

                // 현재 접속 중인 플레이어 목록 가져오기
                List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());

                // 플레이어 수 제한 확인 (2명 미만 또는 8명 초과)
                if (onlinePlayers.size() < 2) {
                    sender.sendMessage(ChatColor.RED + "꼬리잡기 게임 시작에는 최소 2명 이상의 플레이어가 필요합니다.");
                    return true;
                }
                if (onlinePlayers.size() > 8) {
                    sender.sendMessage(ChatColor.RED + "꼬리잡기 게임에는 최대 8명까지 참여할 수 있습니다.");
                    return true;
                }

                // 메인 플러그인 클래스의 startGame 메서드 호출
                boolean started = plugin.startGame(onlinePlayers);
                if (started) {
                    // 게임 시작 성공 메시지는 startGame 메서드에서 브로드캐스트
                } else {
                    sender.sendMessage(ChatColor.RED + "게임 시작에 실패했습니다. (내부 오류 또는 조건 불충족)");
                }
                return true; // 명령어가 성공적으로 처리되었음을 알림
            }
        }
        return false; // 명령어를 찾을 수 없거나 올바르지 않은 사용법일 경우
    }
}