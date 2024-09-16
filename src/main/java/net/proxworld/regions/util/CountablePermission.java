package net.proxworld.regions.util;

import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.OptionalInt;
import java.util.stream.IntStream;

@UtilityClass
public class CountablePermission {
    public void registerRange(String permissionPrefix, int min, int max) {
        //Preconditions.checkState(min  < 1 || max < min, "Impossible range");

        IntStream.rangeClosed(min, max)
                .mapToObj(i -> new Permission(permissionPrefix + "." + i))
                .forEach(Bukkit.getPluginManager()::addPermission);

        Bukkit.getPluginManager()
                .addPermission(new Permission(permissionPrefix + ".*", PermissionDefault.OP));
    }

    public void unRegisterRange(String permissionPrefix, int min, int max) {
      //  Preconditions.checkState(min < 1 || max < min, "Impossible range");

        IntStream.rangeClosed(min, max)
                .mapToObj(i -> permissionPrefix + "." + i)
                .forEach(Bukkit.getPluginManager()::removePermission);
        Bukkit.getPluginManager()
                .removePermission(permissionPrefix + ".*");
    }

    public OptionalInt maxAvailable(CommandSender sender, String permissionPrefix) {
        if (sender.hasPermission(permissionPrefix + ".*"))
            return OptionalInt.of(Integer.MAX_VALUE);

        return Bukkit.getPluginManager().getPermissions().stream()
                .map(Permission::getName)
                .filter(name -> name.startsWith(permissionPrefix) && sender.hasPermission(name))
                .mapToInt(name -> Integer.parseInt(name.substring(name.lastIndexOf(".") + 1)))
                .max();
    }
}