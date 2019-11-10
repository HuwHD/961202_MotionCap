/*
 * Copyright (C) 2019 Huw Hudson-Davies (961202)
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
 *
 */
package robot;

import java.awt.Point;

/**
 * I would use the AWT Rectangle but it uses all Doubles. This is simpler as it uses int's
 * 
 * I have made it immutable.
 * 
 * @author Huw
 */
public class Rect {

    private int x;
    private int y;
    private int w;
    private int h;

    public Rect(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public Point getPoint() {
        return new Point(getX(), getY());
    }

    public Point getPointBR() {
        return new Point(getX() + getW() - 1, getY() + getH() - 1);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public int getMaxX() {
        return x + w -1;
    }
    
    public int getMaxY() {
        return y + h -1;
    }
}
