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

import org.altervista.growworkinghard.jswmm.dataStructure.runoffDS.RunoffSetup;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

public class Pervious extends Subarea {

    Double infiltration = 0.0; //TODO temporary 0.0

    public Pervious(Double subareaArea, Double depressionStoragePervious, Double roughnessCoefficient) {
        this(subareaArea, depressionStoragePervious, roughnessCoefficient, null, null);
    }

    public Pervious(Double subareaArea, Double depressionStoragePervious, Double roughnessCoefficient,
                    Double percentageRouted, List<Subarea> connections) {

        this.subareaArea = subareaArea;
        this.depressionStorage = depressionStoragePervious;
        this.roughnessCoefficient = roughnessCoefficient;
        this.percentageRouted = percentageRouted;
        this.subareaConnections = connections;

        this.totalDepth = new HashMap<>();
        this.runoffDepth = new HashMap<>();
        this.flowRate = new HashMap<>();
        this.excessRainfall = new HashMap<>();
    }

    @Override
    public void setDepthFactor(Double subareaSlope, Double characteristicWidth) {
        if (subareaConnections != null) {
            for (Subarea connections : subareaConnections) {
                connections.setDepthFactor(subareaSlope, characteristicWidth);
                this.depthFactor = Math.pow(subareaSlope, 0.5) *
                        characteristicWidth / (roughnessCoefficient * subareaArea);
            }
        }
        else {
            this.depthFactor = (Math.pow(subareaSlope, 0.5) * characteristicWidth) / (roughnessCoefficient * subareaArea);
        }
    }

    @Override
    Double getWeightedFlowRate(Integer id, Instant currentTime) {
        return flowRate.get(id).get(currentTime) * subareaArea * percentageRouted;
    }

    @Override
    void evaluateNextStep(Integer id, Instant currentTime, RunoffSetup runoffSetup, Double rainfall, Double evaporation,
                          Double subareaSlope, Double characteristicWidth) {

        Long runoffStepSize = runoffSetup.getRunoffStepSize();

        Instant nextTime = currentTime.plusSeconds(runoffStepSize);

        Double moistureVolume = rainfall * runoffStepSize + totalDepth.get(id).get(currentTime);

        if(evaporation != 0.0) {
            evaporation = Math.max(evaporation, totalDepth.get(id).get(currentTime)/runoffStepSize);
        }
        //infiltration
        //excessRainfall = rainfall - evaporation - infiltration;

        setExcessRainfall(id, rainfall - evaporation);

        if(evaporation * runoffStepSize >= moistureVolume) {
            setTotalDepth(id, nextTime, totalDepth.get(id).get(currentTime) + 0.0);
            setRunoffDepth(id, nextTime, runoffDepth.get(id).get(currentTime) + 0.0);
            setFlowRate(id, nextTime, flowRate.get(id).get(currentTime) + 0.0);
        }
        else {
            if(excessRainfall.get(id) * runoffStepSize <= depressionStorage - totalDepth.get(id).get(currentTime)) {
                setTotalDepth(id, nextTime, totalDepth.get(id).get(currentTime) + getExcessRainfall(id) * runoffStepSize);
                setRunoffDepth(id, nextTime, runoffDepth.get(id).get(currentTime) + 0.0);
                setFlowRate(id, nextTime, flowRate.get(id).get(currentTime) + 0.0);
            }
            else {
                runoffODEsolver(id, currentTime, nextTime, getExcessRainfall(id), runoffSetup);
                setFlowRate( id, nextTime, evaluateNextFlowRate(subareaSlope, characteristicWidth, runoffDepth.get(id).get(nextTime)) );
            }
        }
    }

    Double evaluateNextFlowRate(Double subareaSlope, Double characteristicWidth, Double currentDepth) {
        return Math.pow(subareaSlope, 0.5) * characteristicWidth *
                Math.pow(currentDepth, 5.0/3.0) / (subareaArea * roughnessCoefficient);
    }
}