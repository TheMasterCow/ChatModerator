package tc.oc.chatmoderator.warnings;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.google.common.base.Joiner;

import tc.oc.chatmoderator.PlayerManager;
import tc.oc.chatmoderator.PlayerViolationManager;
import tc.oc.chatmoderator.events.ViolationAddEvent;
import tc.oc.chatmoderator.violations.Violation;
import tc.oc.chatmoderator.violations.core.*;

public class ViolationPreWarningListener implements Listener {

    private final @Nonnull PlayerManager manager;

    public ViolationPreWarningListener(@Nonnull PlayerManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerRecieveViolation(ViolationAddEvent event) {
        if (!event.getPlayer().isOnline()) {
            return;
        }

        Player player = (Player) event.getPlayer();
        WarningSendEvent warningEvent = new WarningSendEvent(player, event);
        Bukkit.getServer().getPluginManager().callEvent(warningEvent);

        PlayerViolationManager violations = this.manager.getViolationSet(event.getPlayer());

        if (!violations.isWarned() && warningEvent.getMessage() != null) {
            violations.sendWarning(warningEvent.getMessage());
            violations.setWarned(true);
        }
    }

    @EventHandler
    public void handleWarningMessage(WarningSendEvent event) {
        System.out.println("handle warning message");
        Violation violation = event.getParent().getViolation();

        StringBuilder builder = new StringBuilder(ChatColor.GOLD.toString()).append("[ChatModerator]").append(ChatColor.GRAY).append(" - ");
        builder.append(ChatColor.RED + "\u2717").append(ChatColor.DARK_AQUA).append("Your message was not sent to some people\n");
        builder.append("This message failed the ");
        
        // Let's only support these violations for now, other violations are more or less utilities
        if (violation instanceof AllCapsViolation) {
            builder.append(ChatColor.DARK_RED.toString()).append("AllCapsFilter.").append(ChatColor.DARK_AQUA.toString()).append("\nPlease do not use all uppercase when sending messages.");
        } else if (violation instanceof DuplicateMessageViolation) {
            builder.append(ChatColor.DARK_RED.toString()).append("DuplicateMessageFilter.").append(ChatColor.DARK_AQUA.toString()).append("\nPlease do not send messages too fast.");
        } else if (violation instanceof ProfanityViolation) {
            ProfanityViolation v1 = (ProfanityViolation) event.getParent().getViolation();
            Joiner joiner = Joiner.on(", ").skipNulls();

            builder.append(ChatColor.DARK_RED.toString()).append("ProfanityFilter.").append(ChatColor.DARK_AQUA.toString()).append("\nPlease do not send messages that contain the following words:\n");
            builder.append(joiner.join(v1.getProfanities()));
        } else if (violation instanceof ServerIPViolation) {
            builder.append(ChatColor.DARK_RED.toString()).append("ServerIPFilter.").append(ChatColor.DARK_AQUA.toString()).append("\nPlease do not advertise your server.");
        }

        builder.append("\n").append(ChatColor.GRAY).append(ChatColor.ITALIC).append("\nIf you believe this was an error, please contact support@oc.tc");
        event.setMessage(builder.toString());
    }
}
