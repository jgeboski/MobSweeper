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

import java.util.UUID;

import org.bukkit.Bukkit;

public class RegionEntity
{
    public UUID uuid;
    public int  x;
    public int  y;
    public long time;

    public RegionEntity(UUID uuid, int x, int y, long time)
    {
        if (time > 0)
            time += Bukkit.getWorlds().get(0).getFullTime();

        this.uuid = uuid;
        this.x    = x;
        this.y    = y;
        this.time = time;
    }

    public boolean removable()
    {
        long ticks;

        ticks = Bukkit.getWorlds().get(0).getFullTime();
        return (time < ticks);
    }
}
