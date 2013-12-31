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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.World;

import org.jgeboski.mobsweeper.MobSweeperException;

public class Region implements Listener, Runnable
{
    public int     radius;
    public int     maximum;
    public long    death;
    public long    sweep;
    public boolean chunked;

    public HashSet<String>     worlds;
    public HashSet<EntityType> mobs;

    protected LinkedList<SubRegion> subregions;

    private int taskid;

    public Region()
    {
        this.radius  = 0;
        this.maximum = 0;
        this.death   = 0;
        this.sweep   = 0;
        this.chunked = false;
        this.worlds  = new HashSet<String>();
        this.mobs    = new HashSet<EntityType>();

        this.subregions = new LinkedList<SubRegion>();
        this.taskid     = -1;
    }

    public void init(Plugin plugin)
    {
        BukkitScheduler bs;
        PluginManager   pm;
        String          wn;

        if (radius < 1) {
            for (World w : Bukkit.getWorlds()) {
                wn = w.getName();

                if (worlds.contains(wn))
                    subregions.add(new SubRegion(this, wn, 0, 0, true));
            }
        }

        if (death > 0) {
            pm = plugin.getServer().getPluginManager();
            pm.registerEvents(this, plugin);
        }

        if (sweep > 0) {
            bs     = plugin.getServer().getScheduler();
            taskid = bs.scheduleSyncRepeatingTask(plugin, this, sweep, sweep);
        }
    }

    public void close(Plugin plugin)
    {
        BukkitScheduler bs;

        if (taskid >= 0) {
            bs = plugin.getServer().getScheduler();
            bs.cancelTask(taskid);
            taskid = -1;
        }

        for (HandlerList h : HandlerList.getHandlerLists())
            h.unregister(this);

        subregions.clear();
    }

    public void refresh()
    {
        Iterator<SubRegion> iter;
        SubRegion           regn;

        iter = subregions.iterator();

        while (iter.hasNext()) {
            regn = iter.next();
            regn.refresh();

            if (!regn.permanent && (regn.chunks.size() < 1))
                iter.remove();
        }
    }

    public void setMobs(List<String> mobs)
        throws MobSweeperException
    {
        EntityType type;
        Class      klass;

        this.mobs.clear();

        for (String m : mobs) {
            m = m.toUpperCase().trim().replaceAll("\\s", "_");

            try {
                type = EntityType.valueOf(m);
            } catch (IllegalArgumentException e) {
                throw new MobSweeperException("Unknown mob: %s", m);
            }

            klass = type.getEntityClass();

            if ((klass == null) || !Creature.class.isAssignableFrom(klass))
                throw new MobSweeperException("Unsupported mob: %s", m);

            this.mobs.add(type);
        }

        if (this.mobs.size() > 0)
            return;

        for (EntityType t : EntityType.values()) {
            klass = t.getEntityClass();

            if ((klass != null) && Creature.class.isAssignableFrom(klass))
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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event)
    {
        SubRegion regn;
        Entity    entity;
        Location  l;
        String    w;
        int       handled;
        int       x;
        int       y;

        entity  = event.getEntity();
        handled = 0;

        if (!worlds.contains(entity.getWorld().getName()))
            return;

        if (!mobs.contains(entity.getType()))
            return;

        for (SubRegion r : subregions) {
            if (r.register(entity, death))
                handled++;
        }

        if (handled < 1) {
            l = entity.getLocation();
            w = entity.getWorld().getName();
            x = (int) l.getX();
            y = (int) l.getZ();

            regn = new SubRegion(this, w, x, y);
            regn.register(entity, death);
            subregions.add(regn);
        }

        for (SubRegion r : subregions) {
            r.refresh();

            if (!r.spawnable(entity)) {
                event.getDrops().clear();
                event.setDroppedExp(0);
                break;
            }
        }

        refresh();
    }

    public void run()
    {
        for (World w : Bukkit.getWorlds()) {
            if (!worlds.contains(w.getName()))
                continue;

            for (Chunk c : w.getLoadedChunks()) {
                for (Entity e : c.getEntities()) {
                    if (!mobs.contains(e.getType()))
                        continue;

                    for (SubRegion r : subregions) {
                        if (!r.register(e, 0))
                            continue;

                        if (!r.spawnable(e)) {
                            e.remove();
                            break;
                        }
                    }
                }

                refresh();
            }
        }

        refresh();
    }
}
