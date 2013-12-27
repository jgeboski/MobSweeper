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

package org.jgeboski.mobsweeper.region;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.World;

import org.jgeboski.mobsweeper.MobSweeperException;

public class Region
{
    public int     radius;
    public int     maximum;
    public long    death;
    public long    sweep;
    public boolean chunked;

    public HashSet<String>     worlds;
    public HashSet<EntityType> mobs;

    public Region()
    {
        this.radius  = 0;
        this.maximum = 0;
        this.death   = 0;
        this.sweep   = 0;
        this.chunked = false;
        this.worlds  = new HashSet<String>();
        this.mobs    = new HashSet<EntityType>();
    }

    public void setMobs(List<String> mobs)
        throws MobSweeperException
    {
        EntityType type;

        this.mobs.clear();

        for (String m : mobs) {
            m = m.toUpperCase().trim().replaceAll("\\s", "_");

            try {
                type = EntityType.valueOf(m);
            } catch (IllegalArgumentException e) {
                throw new MobSweeperException("Unknown mob: %s", m);
            }

            if (!type.isAlive())
                throw new MobSweeperException("Unsupported mob: %s", m);

            this.mobs.add(type);
        }

        if (this.mobs.size() > 0)
            return;

        for (EntityType t : EntityType.values()) {
            if (t.isAlive())
                this.mobs.add(t);
        }
    }

    public void setWorlds(List<String> worlds)
        throws MobSweeperException
    {
        World world;

        this.worlds.clear();

        for (String w : worlds) {
            w = w.trim().replaceAll("\\s", "_");
            world = Bukkit.getWorld(w);

            if (world == null)
                throw new MobSweeperException("Unknown world: %s", w);

            this.worlds.add(world.getName());
        }

        if (this.worlds.size() > 0)
            return;

        for (World w : Bukkit.getWorlds())
            this.worlds.add(w.getName());
    }
}
