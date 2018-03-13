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

package org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment;

import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.rainData.RaingageSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.ReceiverRunoff.ReceiverRunoff;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;

public class Area extends AbstractSubcatchment {

    RaingageSetup raingageSetup;
    List<ReceiverRunoff> receivers;

    //Double imperviousPercentage; //TODO evaluate from subareas
    //Double percentageImperviousWOstorage; //TODO evaluate from subareas

    Double characteristicWidth;
    Double areaSlope;
    //Double curbLength;

    List<Subarea> subareas;
    LinkedHashMap<Instant, Double> totalAreaFlowRate = new LinkedHashMap<>();

    public Area(Double subcatchmentArea, RaingageSetup raingageSetup, Double characteristicWidth, Double areaSlope,
                List<Subarea> subareas) {
        this.subcatchmentArea = subcatchmentArea;
        this.raingageSetup = raingageSetup;
        this.characteristicWidth = characteristicWidth;
        this.areaSlope = areaSlope;
        this.subareas = subareas;
    }

    public LinkedHashMap<Instant, Double> evaluateTotalFlowRate() {
        for(Subarea subarea : subareas) {
            subarea.flowRate.forEach((k, v) -> totalAreaFlowRate.merge(k, v*subarea.subareaArea, Double::sum));
        }
        return getTotalAreaFlowRate();
    }

    public List<ReceiverRunoff> getReceivers() {
        return receivers;
    }

    public LinkedHashMap<Instant, Double> getTotalAreaFlowRate() {
        return totalAreaFlowRate;
    }

    public List<Subarea> getSubareas() {
        return subareas;
    }

    public void setTotalAreaFlowRate(LinkedHashMap<Instant, Double> totalAreaFlowRate) {
        this.totalAreaFlowRate = totalAreaFlowRate;
    }

    public Double getCharacteristicWidth() {
        return characteristicWidth;
    }

    public Double getAreaSlope() {
        return areaSlope;
    }
}
