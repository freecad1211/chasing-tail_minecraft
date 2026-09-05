package io.github.freecad1211.chasingtail;

import org.bukkit.ChatColor;

/**
 * 게임에서 사용하는 채팅 메시지를 한 곳에 모아 중복을 없애는 상수/포맷 유틸리티.
 */
public final class Messages {

    private Messages() {
    }

    // --- 플레이어에게 개별 안내되는 메시지 ---

    public static String gameStartTitle() {
        return ChatColor.GREEN + "--- 꼬리잡기 게임 시작 ---";
    }

    public static String boardDivider() {
        return ChatColor.GREEN + "-------------------------";
    }

    public static String myColorMessage(ChatColor color) {
        return ChatColor.YELLOW + "당신의 색깔은 " + color + "■" + ChatColor.YELLOW + "입니다.";
    }

    public static String myTargetMessage(ChatColor color, String name) {
        return ChatColor.YELLOW + "당신의 타겟은 " + color + name + ChatColor.YELLOW + "님입니다.";
    }

    public static String controllHint() {
        return ChatColor.GRAY + "다이아몬드 2개를 손에 들고 우클릭하면 타겟의 방향을 볼 수 있습니다.";
    }

    public static String newTargetMessage(ChatColor color, String name) {
        return ChatColor.AQUA + "당신의 새로운 타겟: " + color + name;
    }

    // --- 전체 브로드캐스트되는 메시지 ---

    public static String gameStarted(int playerCount) {
        return ChatColor.GREEN + "[꼬리잡기] " + playerCount + "명의 플레이어로 게임이 시작되었습니다!";
    }

    public static String gameEnded() {
        return ChatColor.YELLOW + "[꼬리잡기] 게임이 종료되었습니다!";
    }

    public static String caughtBy(ChatColor killerColor, String killerName,
                                 ChatColor victimColor, String victimName) {
        return killerColor + killerName + ChatColor.WHITE + "님이 "
                + victimColor + victimName + ChatColor.WHITE + "님을 잡았습니다!";
    }

    public static String newTargetBroadcast(ChatColor killerColor, String killerName,
                                            ChatColor targetColor, String targetName) {
        return killerColor + killerName + ChatColor.WHITE + "님의 새로운 타겟은 "
                + targetColor + targetName + ChatColor.WHITE + "님입니다.";
    }

    public static String wrongTarget(ChatColor killerColor, String killerName,
                                     ChatColor victimColor, String victimName) {
        return killerColor + killerName + ChatColor.WHITE + "님이 "
                + victimColor + victimName + ChatColor.WHITE + "님을 죽였지만, "
                + ChatColor.RED + "타겟이 아니었습니다!";
    }

    public static String removedFromGame(ChatColor color, String name) {
        return color + name + ChatColor.WHITE + "님이 사망하여 게임에서 제외됩니다.";
    }

    public static String remainingPlayers(int count) {
        return ChatColor.YELLOW + "남은 플레이어: " + count + "명.";
    }

    public static String winner(ChatColor color, String name) {
        return ChatColor.GOLD + "축하합니다! " + color + name + ChatColor.GOLD + "님이 꼬리잡기 게임에서 승리했습니다!";
    }

    public static String noWinner() {
        return ChatColor.RED + "모든 플레이어가 제거되어 게임이 종료되었습니다. 승자가 없습니다!";
    }

    public static String noWinnerByDeath() {
        return ChatColor.RED + "모든 플레이어가 사망하여 게임이 종료되었습니다. 승자가 없습니다!";
    }

    // --- 오류 / 안내 메시지 ---

    public static String playersOnly() {
        return ChatColor.RED + "이 명령어는 플레이어만 사용할 수 있습니다.";
    }

    public static String gameAlreadyActive() {
        return ChatColor.RED + "게임이 이미 진행 중입니다.";
    }

    public static String needPlayers(int min) {
        return ChatColor.RED + "꼬리잡기 게임 시작에는 최소 " + min + "명 이상의 플레이어가 필요합니다.";
    }

    public static String maxPlayers(int max) {
        return ChatColor.RED + "꼬리잡기 게임에는 최대 " + max + "명까지 참여할 수 있습니다.";
    }

    public static String startFailed() {
        return ChatColor.RED + "게임 시작에 실패했습니다. (내부 오류 또는 조건 불충족)";
    }

    public static String targetDirection(ChatColor color, String name, String direction) {
        return ChatColor.AQUA + "타겟 (" + color + name + ChatColor.AQUA + ") 은 당신의 " + direction + "에 있습니다.";
    }

    public static String notParticipating() {
        return ChatColor.RED + "당신은 현재 게임에 참여하고 있지 않거나 타겟이 설정되지 않았습니다.";
    }

    // --- 방향 안내 메시지 ---

    public static String directionFront() {
        return ChatColor.AQUA + "정면";
    }

    public static String directionBack() {
        return ChatColor.DARK_RED + "뒤쪽";
    }

    public static String directionLeft() {
        return ChatColor.BLUE + "왼쪽";
    }

    public static String directionRight() {
        return ChatColor.LIGHT_PURPLE + "오른쪽";
    }
}