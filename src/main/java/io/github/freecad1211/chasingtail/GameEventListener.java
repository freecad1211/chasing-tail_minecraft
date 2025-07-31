package io.github.freecad1211.chasingtail;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector; // 벡터 계산을 위함
import org.bukkit.Bukkit;
public class GameEventListener implements Listener {

    private final Chasingtail plugin; // 메인 클래스 인스턴스 참조

    public GameEventListener(Chasingtail plugin) {
        this.plugin = plugin;
    }

    /**
     * 플레이어가 아이템을 들고 상호작용(우클릭)했을 때 호출됩니다.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 게임이 활성화되어 있지 않으면 이벤트 처리 중단
        if (!plugin.isGameActive()) return;

        Player player = event.getPlayer();

        // 손에 든 아이템이 다이아몬드 2개인지 확인
        if (event.getItem() == null || event.getItem().getType() != Material.DIAMOND || event.getItem().getAmount() < 2) {
            return; // 조건 불충족 시 무시
        }

        // 플레이어가 우클릭 액션을 했는지 확인 (공중 또는 블록)
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player target = plugin.getPlayerTargets().get(player); // 현재 플레이어의 타겟 가져오기

            if (target != null) {
                // 타겟의 위치와 플레이어의 위치를 사용하여 방향 계산
                Location playerLoc = player.getLocation();
                Location targetLoc = target.getLocation();

                // 2D 평면에서의 방향 계산 (Y축 차이는 무시)
                Vector directionToTarget = targetLoc.toVector().subtract(playerLoc.toVector());
                directionToTarget.setY(0); // Y축은 평면 방향 계산에 영향 미치지 않도록 0으로 설정
                directionToTarget.normalize(); // 방향 벡터를 정규화하여 길이 1로 만듦

                // 플레이어의 현재 시야 방향
                Vector playerLookDirection = player.getLocation().getDirection();
                playerLookDirection.setY(0); // 마찬가지로 Y축 무시
                playerLookDirection.normalize();

                // 플레이어 시야 방향과 타겟 방향 간의 각도 계산 (도 단위)
                double angle = Math.toDegrees(playerLookDirection.angle(directionToTarget));

                String directionText = "알 수 없는 방향";

                // 각도에 따라 대략적인 방향 텍스트 결정
                if (angle < 22.5) { // 0도에 가까움
                    directionText = ChatColor.AQUA + "정면" + ChatColor.AQUA;
                } else if (angle > 157.5) { // 180도에 가까움
                    directionText = ChatColor.DARK_RED + "뒤쪽" + ChatColor.AQUA;
                } else {
                    // 시야 방향과 타겟 방향의 외적을 통해 좌우 판단
                    Vector crossProduct = playerLookDirection.crossProduct(directionToTarget);
                    if (crossProduct.getY() > 0) { // Y축 양수 방향이면 일반적으로 왼쪽 (마인크래프트 좌표계 기준)
                        directionText = ChatColor.BLUE + "왼쪽" + ChatColor.AQUA;
                    } else { // Y축 음수 방향이면 일반적으로 오른쪽
                        directionText = ChatColor.LIGHT_PURPLE + "오른쪽" + ChatColor.AQUA;
                    }
                }

                // 플레이어의 액션바에 타겟 정보 및 방향 표시
                player.sendActionBar(ChatColor.AQUA + "타겟 (" + plugin.getPlayerColors().get(target) + target.getName() + ChatColor.AQUA + ") 은 당신의 " + directionText + "에 있습니다.");

            } else {
                player.sendMessage(ChatColor.RED + "당신은 현재 게임에 참여하고 있지 않거나 타겟이 설정되지 않았습니다.");
            }
            event.setCancelled(true); // 다이아몬드가 소모되지 않도록 이벤트 취소
        }
    }

    /**
     * 플레이어가 사망했을 때 호출됩니다.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.isGameActive()) return; // 게임 중이 아니면 이벤트 처리 중단

        Player killed = event.getEntity(); // 사망한 플레이어
        Player killer = killed.getKiller(); // 사망시킨 플레이어

        // 사망시킨 플레이어가 있고, 두 플레이어 모두 게임에 참여 중인 경우
        if (killer != null && plugin.getPlayerTargets().containsKey(killer) && plugin.getPlayerTargets().containsKey(killed)) {
            // 메인 플러그인 클래스의 updateTarget 메서드를 호출하여 타겟 변경 로직 수행
            plugin.updateTarget(killer, killed);
        } else if (plugin.getPlayerTargets().containsKey(killed)) {
            // 타겟에게 죽지 않았지만 게임 참여자가 사망한 경우 (예: 낙사, 몬스터)
            Bukkit.broadcastMessage(plugin.getPlayerColors().get(killed) + killed.getName() +
                    ChatColor.WHITE + "님이 사망하여 게임에서 제외됩니다.");
            plugin.getPlayerTargets().remove(killed);
            plugin.getPlayerColors().remove(killed);

            // 남은 플레이어 확인 및 승리 조건 처리
            if (plugin.getPlayerTargets().size() <= 1) {
                if (plugin.getPlayerTargets().size() == 1) {
                    Player winner = plugin.getPlayerTargets().keySet().iterator().next();
                    Bukkit.broadcastMessage(ChatColor.GOLD + "축하합니다! " + plugin.getPlayerColors().get(winner) + winner.getName() + ChatColor.GOLD + "님이 꼬리잡기 게임에서 승리했습니다!");
                } else {
                    Bukkit.broadcastMessage(ChatColor.RED + "모든 플레이어가 사망하여 게임이 종료되었습니다. 승자가 없습니다!");
                }
                plugin.endGame();
            }
        }
    }
}