package io.github.freecad1211.chasingtail;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class GameEventListener implements Listener {

    private static final double FOV_HALF_ANGLE = 22.5; // 정면/뒤쪽 판정 기준 절반 각도
    private static final double REVERSE_ANGLE = 180 - FOV_HALF_ANGLE; // 뒤쪽 판정 기준 각도

    private final Chasingtail plugin;

    public GameEventListener(Chasingtail plugin) {
        this.plugin = plugin;
    }

    /**
     * 플레이어가 다이아몬드 2개를 들고 우클릭하면 타겟의 대략적인 방향을 액션바에 표시합니다.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.isGameActive()) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getItem() == null
                || event.getItem().getType() != Material.DIAMOND
                || event.getItem().getAmount() < 2) {
            return;
        }

        // 올바른 우클릭 사건이므로 다이아몬드가 소모되지 않도록 사건을 취소.
        event.setCancelled(true);

        Player player = event.getPlayer();
        Player target = plugin.getPlayerTargets().get(player);
        if (target == null) {
            player.sendMessage(Messages.notParticipating());
            return;
        }

        player.sendActionBar(Messages.targetDirection(
                plugin.getPlayerColors().get(target),
                target.getName(),
                determineDirection(player.getLocation(), target.getLocation())));
    }

    /**
     * 플레이어가 사망하면 게임 상태를 갱신하고 승리 여부를 판정합니다.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.isGameActive()) return;

        Player killed = event.getEntity();
        Player killer = killed.getKiller();

        if (killer != null
                && plugin.getPlayerTargets().containsKey(killer)
                && plugin.getPlayerTargets().containsKey(killed)) {
            plugin.updateTarget(killer, killed);
        } else {
            plugin.eliminatePlayer(killed);
        }
    }

    /**
     * 플레이어 시야 방향 기준 타겟의 대략적 방향(정면/뒤쪽/왼쪽/오른쪽)을 판정합니다.
     * Y축(수직) 차이는 무시하고 2D 평면 기준으로 계산합니다.
     */
    private String determineDirection(Location playerLoc, Location targetLoc) {
        Vector toTarget = targetLoc.toVector().subtract(playerLoc.toVector());
        toTarget.setY(0);
        toTarget.normalize();

        Vector lookDirection = playerLoc.getDirection();
        lookDirection.setY(0);
        lookDirection.normalize();

        double angle = Math.toDegrees(lookDirection.angle(toTarget));

        if (angle < FOV_HALF_ANGLE) {
            return Messages.directionFront();
        }
        if (angle > REVERSE_ANGLE) {
            return Messages.directionBack();
        }

        // 시야 방향과 타겟 방향의 외적을 통해 좌우를 판단.
        Vector cross = lookDirection.crossProduct(toTarget);
        if (cross.getY() > 0) {
            return Messages.directionLeft();
        }
        return Messages.directionRight();
    }
}