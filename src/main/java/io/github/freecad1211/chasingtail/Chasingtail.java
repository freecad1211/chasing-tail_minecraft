package io.github.freecad1211.chasingtail;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Chasingtail extends JavaPlugin {

    /** 게임 참여 가능 인원 범위 (2명 ~ 8명). */
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 8;

    private static final ChatColor[] AVAILABLE_COLORS = {
            ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN,
            ChatColor.AQUA, ChatColor.BLUE, ChatColor.DARK_PURPLE, ChatColor.BLACK
    };

    private boolean gameActive = false;
    private final Map<Player, Player> playerTargets = new HashMap<>();
    private final Map<Player, ChatColor> playerColors = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().log(Level.INFO, "[ChasingTail] 플러그인이 활성화되었습니다!");
        getCommand("꼬리잡기").setExecutor(new GameCommandExecutor(this));
        getServer().getPluginManager().registerEvents(new GameEventListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "[ChasingTail] 플러그인이 비활성화되었습니다!");
        if (gameActive) {
            endGame();
        }
    }

    /**
     * 꼬리잡기 게임을 시작합니다.
     *
     * @param participants 게임에 참여할 플레이어 목록
     * @return 게임 시작 성공 여부
     */
    public boolean startGame(List<Player> participants) {
        if (gameActive || participants.size() < MIN_PLAYERS || participants.size() > MAX_PLAYERS) {
            return false;
        }

        gameActive = true;
        playerTargets.clear();
        playerColors.clear();

        Collections.shuffle(participants);

        // 각 플레이어에게 색깔을 할당하고 다음 순서 플레이어를 타겟으로 지정 (마지막은 첫 번째).
        for (int i = 0; i < participants.size(); i++) {
            Player current = participants.get(i);
            Player target = participants.get((i + 1) % participants.size());
            ChatColor color = AVAILABLE_COLORS[i];

            playerColors.put(current, color);
            playerTargets.put(current, target);

            current.sendMessage(Messages.gameStartTitle());
            current.sendMessage(Messages.myColorMessage(color));
            current.sendMessage(Messages.myTargetMessage(playerColors.get(target), target.getName()));
            current.sendMessage(Messages.controllHint());
            current.sendMessage(Messages.boardDivider());
        }

        Bukkit.broadcastMessage(Messages.gameStarted(participants.size()));
        return true;
    }

    /**
     * 꼬리잡기 게임을 종료하고 모든 게임 상태를 초기화합니다.
     */
    public void endGame() {
        gameActive = false;
        playerTargets.clear();
        playerColors.clear();
        Bukkit.broadcastMessage(Messages.gameEnded());
    }

    /**
     * 플레이어가 자신의 타겟을 제거했을 때 타겟을 변경하는 로직.
     *
     * @param killer 타겟을 제거한 플레이어
     * @param killed  제거당한 플레이어 (killer의 타겟이어야 함)
     */
    public void updateTarget(Player killer, Player killed) {
        if (!gameActive) return;

        Player killerCurrentTarget = playerTargets.get(killer);
        Player killedPlayersTarget = playerTargets.get(killed);
        ChatColor killerColor = playerColors.get(killer);
        ChatColor killedColor = playerColors.get(killed);

        // killer의 타겟이 killed인 경우에만 타겟 변경을 진행.
        if (killerCurrentTarget == null || !killerCurrentTarget.equals(killed)) {
            Bukkit.broadcastMessage(Messages.wrongTarget(killerColor, killer.getName(),
                    killedColor, killed.getName()));
            return;
        }

        // killer의 새로운 타겟을 killed의 타겟으로 지정하고 killed를 게임에서 제거.
        playerTargets.put(killer, killedPlayersTarget);
        playerTargets.remove(killed);
        playerColors.remove(killed);

        Bukkit.broadcastMessage(Messages.caughtBy(killerColor, killer.getName(),
                killedColor, killed.getName()));

        if (killedPlayersTarget != null) {
            Bukkit.broadcastMessage(Messages.newTargetBroadcast(killerColor, killer.getName(),
                    playerColors.get(killedPlayersTarget), killedPlayersTarget.getName()));
            killer.sendMessage(Messages.newTargetMessage(
                    playerColors.get(killedPlayersTarget), killedPlayersTarget.getName()));
            return;
        }

        // killed가 마지막 타겟이었다면 남은 인원을 표시하고 승리 여부를 판정.
        Bukkit.broadcastMessage(Messages.remainingPlayers(playerTargets.size()));
        checkForVictory();
    }

    /**
     * 남은 인원이 1명 이하가 되면 승리자(또는 승자 없는 종료)를 선언하고 게임을 종료합니다.
     */
    public void checkForVictory() {
        if (playerTargets.size() == 1) {
            Player winner = playerTargets.keySet().iterator().next();
            Bukkit.broadcastMessage(Messages.winner(playerColors.get(winner), winner.getName()));
            endGame();
        } else if (playerTargets.isEmpty()) {
            Bukkit.broadcastMessage(Messages.noWinner());
            endGame();
        }
    }

    /**
     * 게임 참여자가 사망한 경우 게임에서 제거하고 승리 여부를 판정합니다.
     * 사망이 killer의 타겟 제거로 이어지면 {@link #updateTarget(Player, Player)}가 처리합니다.
     */
    public void eliminatePlayer(Player killed) {
        if (!playerTargets.containsKey(killed)) return;

        Bukkit.broadcastMessage(Messages.removedFromGame(playerColors.get(killed), killed.getName()));
        playerTargets.remove(killed);
        playerColors.remove(killed);
        checkForVictory();
    }

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