/*
 * Copyright 2013 James Geboski <jgeboski@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jgeboski.mobsweeper.util;

import org.bukkit.command.CommandSender;

public class Utils
{
    public static boolean hasPermission(CommandSender sender, String perm,
                                        boolean errmsg)
    {
        if (sender.hasPermission(perm))
            return true;

        if (errmsg)
            Message.severe(sender, "You don't have permission for that.");

        return false;
    }

    public static boolean hasPermission(CommandSender sender, String perm)
    {
        return hasPermission(sender, perm, true);
    }
}
