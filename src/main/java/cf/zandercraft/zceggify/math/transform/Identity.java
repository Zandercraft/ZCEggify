/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package cf.zandercraft.zceggify.math.transform;

import cf.zandercraft.zceggify.math.Vector3;

/**
 * Makes no transformation to given vectors.
 */
public class Identity implements Transform {

    @Override
    public boolean isIdentity() {
        return true;
    }

    @Override
    public Vector3 apply(Vector3 vector) {
        return vector;
    }

    @Override
    public Transform inverse() {
        return this;
    }

    @Override
    public Transform combine(Transform other) {
        if (other instanceof Identity) {
            return this;
        } else {
            return other;
        }
    }

}
