package io.github.freecad1211.chasingtail;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import java.util.*;
import java.util.logging.Level; // Logger 레벨을 사용하기 위함

public class Chasingtail extends JavaPlugin {

    private static Chasingtail plugin; // 플러그인 인스턴스 (싱글톤)
    private boolean gameActive = false; // 게임 진행 여부
    // 플레이어와 그들의 타겟을 매핑하는 맵
    private final Map<Player, Player> playerTargets = new HashMap<>();
    // 플레이어와 그들의 할당된 색깔을 매핑하는 맵
    private final Map<Player, ChatColor> playerColors = new HashMap<>();

    // 사용할 수 있는 색깔 목록 (최대 8명)
    private final ChatColor[] availableColors = {
            ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN,
            ChatColor.AQUA, ChatColor.BLUE, ChatColor.DARK_PURPLE, ChatColor.BLACK
    };

    @Override
    public void onEnable() {
        plugin = this; // 현재 인스턴스 할당
        getLogger().log(Level.INFO, "[ChasingTail] 플러그인이 활성화되었습니다!");

        // 명령어 등록: "/꼬리잡기" 명령어를 GameCommandExecutor 클래스가 처리하도록 설정
        Objects.requireNonNull(this.getCommand("꼬리잡기")).setExecutor(new GameCommandExecutor(this));

        // 이벤트 리스너 등록: 게임 이벤트(플레이어 상호작용, 사망)를 GameEventListener 클래스가 처리하도록 설정
        getServer().getPluginManager().registerEvents(new GameEventListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "[ChasingTail] 플러그인이 비활성화되었습니다!");
        // 플러그인이 비활성화될 때, 진행 중인 게임이 있다면 종료 처리
        if (gameActive) {
            endGame();
        }
    }

    // 다른 클래스에서 플러그인 인스턴스에 접근할 수 있도록 하는 메서드
    public static Chasingtail getPlugin() {
        return plugin;
    }

    /**
     * 꼬리잡기 게임을 시작합니다.
     * @param participants 게임에 참여할 플레이어 목록
     * @return 게임 시작 성공 여부
     */
    public boolean startGame(List<Player> participants) {
        if (gameActive) {
            return false; // 이미 게임이 진행 중
        }
        if (participants.size() < 2 || participants.size() > 8) {
            return false; // 인원 제한 (2명 ~ 8명)
        }

        gameActive = true;
        playerTargets.clear();
        playerColors.clear();

        // 플레이어 목록을 섞어서 무작위 순서로 만듭니다.
        Collections.shuffle(participants);

        // 플레이어에게 색깔 할당 및 타겟 설정
        for (int i = 0; i < participants.size(); i++) {
            Player currentPlayer = participants.get(i);
            Player targetPlayer = participants.get((i + 1) % participants.size()); // 다음 플레이어가 타겟 (마지막은 첫 번째)

            playerColors.put(currentPlayer, availableColors[i]); // 순서대로 색깔 할당
            playerTargets.put(currentPlayer, targetPlayer); // 타겟 설정

            // 플레이어에게 게임 시작 정보 알림
            currentPlayer.sendMessage(ChatColor.GREEN + "--- 꼬리잡기 게임 시작 ---");
            currentPlayer.sendMessage(ChatColor.YELLOW + "당신의 색깔은 " + availableColors[i] + "■" + ChatColor.YELLOW + "입니다.");
            currentPlayer.sendMessage(ChatColor.YELLOW + "당신의 타겟은 " + playerColors.get(targetPlayer) + targetPlayer.getName() + ChatColor.YELLOW + "님입니다.");
            currentPlayer.sendMessage(ChatColor.GRAY + "다이아몬드 2개를 손에 들고 우클릭하면 타겟의 방향을 볼 수 있습니다.");
            currentPlayer.sendMessage(ChatColor.GREEN + "-------------------------");
        }

        Bukkit.broadcastMessage(ChatColor.GREEN + "[꼬리잡기] " + participants.size() + "명의 플레이어로 게임이 시작되었습니다!");
        return true;
    }

    /**
     * 꼬리잡기 게임을 종료합니다.
     */
    public void endGame() {
        gameActive = false;
        playerTargets.clear();
        playerColors.clear();


        Bukkit.broadcastMessage(ChatColor.YELLOW + "[꼬리잡기] 게임이 종료되었습니다!");
    }

    /**
     * 플레이어가 타겟을 죽였을 때 타겟을 변경하는 로직.
     * @param killer 타겟을 죽인 플레이어
     * @param killed 죽임을 당한 플레이어 (killer의 타겟이어야 함)
     */
    public void updateTarget(Player killer, Player killed) {
        if (!gameActive) return; // 게임 중이 아니면 실행 안 함

        // 죽인 플레이어의 현재 타겟
        Player killerCurrentTarget = playerTargets.get(killer);
        // 죽은 플레이어의 타겟
        Player killedPlayersTarget = playerTargets.get(killed);

        // 1. 죽인 플레이어 (killer)의 타겟이 죽은 플레이어 (killed)인지 확인
        if (killerCurrentTarget != null && killerCurrentTarget.equals(killed)) {
            // 2. 죽인 플레이어의 새로운 타겟을 죽은 플레이어의 타겟으로 설정
            playerTargets.put(killer, killedPlayersTarget);

            // 3. 죽은 플레이어를 게임에서 제거
            playerTargets.remove(killed);
            playerColors.remove(killed);

            // 4. 모든 플레이어에게 타겟 변경 및 상황 알림
            Bukkit.broadcastMessage(playerColors.get(killer) + killer.getName() + ChatColor.WHITE +
                    "님이 " + playerColors.get(killed) + killed.getName() + ChatColor.WHITE +
                    "님을 잡았습니다!");

            if (killedPlayersTarget != null) {
                Bukkit.broadcastMessage(playerColors.get(killer) + killer.getName() + ChatColor.WHITE +
                        "님의 새로운 타겟은 " + playerColors.get(killedPlayersTarget) + killedPlayersTarget.getName() + ChatColor.WHITE + "님입니다.");
                killer.sendMessage(ChatColor.AQUA + "당신의 새로운 타겟: " + playerColors.get(killedPlayersTarget) + killedPlayersTarget.getName());
            } else {
                // 죽은 플레이어의 타겟이 없었다는 것은 마지막 타겟이었을 가능성 (승리 조건)
                Bukkit.broadcastMessage(ChatColor.YELLOW + "남은 플레이어: " + playerTargets.size() + "명.");
            }

            // 5. 게임 종료 조건 확인 (예: 마지막 한 명만 남았을 때)
            if (playerTargets.size() <= 1) { // 킬러만 남거나 아무도 안 남았을 경우
                if (playerTargets.size() == 1) {
                    Player winner = playerTargets.keySet().iterator().next();
                    Bukkit.broadcastMessage(ChatColor.GOLD + "축하합니다! " + playerColors.get(winner) + winner.getName() + ChatColor.GOLD + "님이 꼬리잡기 게임에서 승리했습니다!");
                } else {
                    Bukkit.broadcastMessage(ChatColor.RED + "모든 플레이어가 제거되어 게임이 종료되었습니다. 승자가 없습니다!");
                }
                endGame(); // 게임 종료
            }

        } else {
            // 자신의 타겟이 아닌 다른 플레이어를 죽였을 경우
            Bukkit.broadcastMessage(playerColors.get(killer) + killer.getName() + ChatColor.WHITE +
                    "님이 " + playerColors.get(killed) + killed.getName() + ChatColor.WHITE +
                    "님을 죽였지만, " + ChatColor.RED + "타겟이 아니었습니다!");
            // TODO: 비 타겟을 죽였을 경우 페널티 부여 (선택 사항)
        }
    }

    // --- Getter 메서드 ---
    public boolean isGameActive() {
        return gameActive;
    }

    public Map<Player, Player> getPlayerTargets() {
        return playerTargets;
    }

    public Map<Player, ChatColor> getPlayerColors() {
        return playerColors;
    }
}