package io.typst.bukkit.view.plugin.command;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class StringCommandExecutor {
    public static void execute(Plugin plugin, Player p, String command) {
        if (command.startsWith("@")) {
            String[] pieces = command
                    .replace("%player%", p.getName())
                    .substring(1).split("\\|");
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            try {
                for (String piece : pieces) {
                    out.writeUTF(piece);
                }
            } catch (Exception ex) {
                // ignore
            }
            p.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        } else {
            p.performCommand(command);
        }
    }
}
