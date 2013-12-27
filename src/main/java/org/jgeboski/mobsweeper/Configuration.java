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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import org.jgeboski.mobsweeper.region.Region;
import org.jgeboski.mobsweeper.util.Log;

public class Configuration extends YamlConfiguration
{
    public File         file;
    public List<Region> regions;

    public Configuration(File file)
    {
        this.file    = file;
        this.regions = new ArrayList<Region>();
    }

    public void load()
        throws MobSweeperException
    {
        Region regn;

        try {
            super.load(file);
        } catch (Exception e) {
            Log.warning("Failed to load: %s", file.toString());
        }

        try {
            for (Map<?, ?> map : getMapList("regions")) {
                regn = new Region();

                regn.radius  = getMapInt(map, "radius");
                regn.maximum = getMapInt(map, "maximum");
                regn.death   = getMapLong(map, "death");
                regn.sweep   = getMapLong(map, "sweep");
                regn.chunked = getMapBoolean(map, "chunked");

                regn.setWorlds(getMapStringList(map, "worlds"));
                regn.setMobs(getMapStringList(map, "mobs"));

                regions.add(regn);
            }
        } catch (MobSweeperException e) {
            regions.clear();
            throw e;
        }
    }

    private boolean getMapBoolean(Map<?, ?> map, String key)
        throws MobSweeperException
    {
        Object obj;

        obj = map.get(key);

        if (!(obj instanceof Boolean))
            throw new MobSweeperException("Failed to obtain Boolean: %s", key);

        return (Boolean) obj;
    }

    private int getMapInt(Map<?, ?> map, String key)
        throws MobSweeperException
    {
        Object obj;

        obj = map.get(key);

        if (!(obj instanceof Integer))
            throw new MobSweeperException("Failed to obtain Integer: %s", key);

        return (Integer) obj;
    }

    private long getMapLong(Map<?, ?> map, String key)
        throws MobSweeperException
    {
        Object obj;

        obj = map.get(key);

        if (obj instanceof Integer)
            obj = new Long((Integer) obj);

        if (!(obj instanceof Long))
            throw new MobSweeperException("Failed to obtain Long: %s", key);

        return (Long) obj;
    }

    private List<String> getMapStringList(Map<?, ?> map, String key)
        throws MobSweeperException
    {
        Object obj;

        obj = map.get(key);

        if (!(obj instanceof List))
            throw new MobSweeperException("Failed to obtain List: %s", key);

        return (List<String>) obj;
    }
}
