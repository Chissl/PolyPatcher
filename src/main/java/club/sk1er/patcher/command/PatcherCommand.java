package club.sk1er.patcher.command;

import club.sk1er.patcher.config.PatcherConfig;
import club.sk1er.patcher.scheduler.ScreenHandler;
import club.sk1er.vigilance.gui.SettingsGui;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class PatcherCommand extends CommandBase {
    /**
     * Gets the name of the command
     */
    @Override
    public String getCommandName() {
        return "patcher";
    }

    /**
     * Gets the usage string for the command.
     *
     * @param sender user
     */
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName();
    }

    /**
     * Callback when the command is invoked
     *
     * @param sender user
     * @param args   arguments
     */
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        ScreenHandler.open(new SettingsGui(PatcherConfig.instance.getCategories()));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return -1;
    }
}
