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

package org.jgeboski.mobsweeper;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import org.jgeboski.mobsweeper.command.CMobSweeper;
import org.jgeboski.mobsweeper.util.Log;
import org.jgeboski.mobsweeper.util.Message;

public class MobSweeper extends JavaPlugin
{
    public Configuration config;

    public void onLoad()
    {
        config = new Configuration(new File(getDataFolder(), "config.yml"));
    }

    public void onEnable()
    {
        Log.init(getLogger());
        Message.init(getDescription().getName());

        if (!config.file.exists())
            saveResource("config.yml", false);

        try {
            config.load();
        } catch (MobSweeperException e) {
            Log.severe("Failed to load configuration: %s", e.getMessage());
        }

        getCommand("mobsweeper").setExecutor(new CMobSweeper(this));
    }

    public void onDisable()
    {

    }

    public void reload()
    {
        PluginManager pm;

        pm = getServer().getPluginManager();

        pm.disablePlugin(this);
        pm.enablePlugin(this);
    }
}
