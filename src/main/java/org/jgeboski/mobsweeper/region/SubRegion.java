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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.Location;

public class SubRegion
{
    public Region  region;
    public String  world;
    public boolean permanent;

    public int xmin;
    public int xmax;

    public int ymin;
    public int ymax;

    public HashMap<Long, HashMap<UUID, RegionEntity>> chunks;

    public SubRegion(Region region, String world, int x, int y,
                     boolean permanent)
    {
        this.region    = region;
        this.world     = world;
        this.permanent = permanent;
        this.chunks    = new HashMap<Long, HashMap<UUID, RegionEntity>>();

        redefine(x, y);
    }

    public SubRegion(Region region, String world, int x, int y)
    {
        this(region, world, x, y, false);
    }

    public boolean bounded(String world, int x, int y)
    {
        if (!this.world.equals(world))
            return false;

        if ((xmin == xmax) && (ymin == ymax) && (xmax == ymax))
            return true;

        return ((x > xmin) && (x < xmax) && (y > ymin) && (y < ymax));
    }

    public boolean bounded(Entity entity)
    {
        Location l;
        String   w;
        int      x;
        int      y;

        l = entity.getLocation();
        w = entity.getWorld().getName();
        x = (int) l.getX();
        y = (int) l.getZ();

        return bounded(w, x, y);
    }

    public boolean contains(UUID uuid)
    {
        for (HashMap<UUID, RegionEntity> e : chunks.values()) {
            if (e.containsKey(uuid))
                return true;
        }

        return false;
    }

    public boolean contains(Entity entity)
    {
        return contains(entity.getUniqueId());
    }

    public void redefine(int x, int y)
    {
        if (region.radius > 0) {
            xmin = x - region.radius;
            xmax = x + region.radius;
            ymin = y - region.radius;
            ymax = y + region.radius;
        } else {
            xmin = xmax = 0;
            ymin = ymax = 0;
        }
    }

    public void redefine(Location location)
    {
        int x;
        int y;

        x = (int) location.getX();
        y = (int) location.getZ();

        redefine(x, y);
    }

    public void refresh()
    {
        Iterator<HashMap<UUID, RegionEntity>> iter;
        Iterator<RegionEntity>                eter;

        HashMap<UUID, RegionEntity> ents;
        RegionEntity                ent;

        iter = chunks.values().iterator();

        while (iter.hasNext()) {
            ents = iter.next();
            eter = ents.values().iterator();

            while (eter.hasNext()) {
                ent = eter.next();

                if (ent.removable())
                    eter.remove();
            }

            if (ents.size() < 1)
                iter.remove();
        }
    }

    public boolean register(Entity entity, long time)
    {
        HashMap<UUID, RegionEntity> ents;

        RegionEntity ent;
        Location     l;
        UUID         id;

        long hash;
        int  size;
        int  x;
        int  y;

        if (!bounded(entity) || contains(entity))
            return false;

        hash = (region.chunked) ? chunkpos(entity) : chunkpos(0, 0);
        ents = chunks.get(hash);

        if (ents == null) {
            ents = new HashMap<UUID, RegionEntity>();
            chunks.put(hash, ents);
        }

        id = entity.getUniqueId();
        l  = entity.getLocation();
        x  = (int) l.getX();
        y  = (int) l.getZ();

        ent = new RegionEntity(id, x, y, time);
        ents.put(ent.uuid, ent);
        size = ents.size();

        if ((xmin == xmax) && (ymin == ymax) && (xmax == ymax))
            return true;

        if (size < 2) {
            if (size < 1)
                return true;

            redefine(x, y);
            return true;
        }

        x = y = 0;

        for (RegionEntity e : ents.values()) {
            x += (int) e.x;
            y += (int) e.y;
        }

        x /= size;
        y /= size;

        redefine(x, y);
        return true;
    }

    public boolean spawnable(Entity entity)
    {
        long hash;
        int  entc;

        if (!bounded(entity) && !contains(entity))
            return true;

        if (!region.chunked) {
            entc = 0;

            for (HashMap<UUID, RegionEntity> es : chunks.values())
                entc += es.size();

            return (entc <= region.maximum);
        }

        for (HashMap<UUID, RegionEntity> es : chunks.values()) {
            if (es.size() > region.maximum)
                return false;
        }

        return true;
    }

    private long chunkpos(int x, int y)
    {
        return ((long) x << 32) + y - Integer.MIN_VALUE;
    }

    private long chunkpos(Entity entity)
    {
        Location l;
        int      x;
        int      y;

        l = entity.getLocation();
        x = ((int) l.getX()) >> 4;
        y = ((int) l.getZ()) >> 4;

        return chunkpos(x, y);
    }
}
