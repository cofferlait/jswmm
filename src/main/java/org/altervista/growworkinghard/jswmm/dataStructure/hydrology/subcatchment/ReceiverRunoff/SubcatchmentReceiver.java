/*
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.ReceiverRunoff;

import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.AbstractSubcatchment;

public class SubcatchmentReceiver implements ReceiverRunoff {
    ReceiverType receiverType;
    AbstractSubcatchment receiverObject;
    Double percentage;

    public SubcatchmentReceiver(AbstractSubcatchment area, Double percentage) {
        this.receiverObject = area;
        this.percentage = percentage;
        this.receiverType = ReceiverType.SUBCATCHMENT;
    }

    @Override
    public ReceiverType getReceiverType() {
        return receiverType;
    }

    @Override
    public AbstractSubcatchment getReceiverObject() {
        return receiverObject;
    }

    @Override
    public Double getPercentage() {
        return percentage;
    }
}
