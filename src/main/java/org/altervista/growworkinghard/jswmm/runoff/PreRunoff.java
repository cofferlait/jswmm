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

package org.altervista.growworkinghard.jswmm.runoff;

import oms3.annotations.*;
import org.altervista.growworkinghard.jswmm.dataStructure.SWMMobject;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.rainData.RaingageSetup;

import java.time.Instant;
import java.util.*;

public class PreRunoff extends LinkedHashMap<Instant, Double> {

    @In
    public String areaName = null;

    private Long runoffStepSize;

    private Long rainfallStepSize;

    private Instant initialTime;

    private Instant totalTime;

    private LinkedHashMap<Instant, Double> rainfallData;

    @In
    public Double aLPP;

    @In
    public Double nLPP;

    @In
    public Integer numberOfCurves;

    @InNode
    @Out
    public SWMMobject dataStructure;

    @Out
    public HashMap<Integer, LinkedHashMap<Instant, Double>> adaptedRainfallData;

    @Out
    public LinkedHashMap<Instant, Double> adaptedInfiltrationData;

    public HashMap<Integer, LinkedHashMap<Instant, Double>> getAdaptedRainfallData() {
        return adaptedRainfallData;
    }

    @Initialize
    public void initialize() {
    }

    @Execute
    public void run() {

        if(dataStructure != null) {

            //this.dataStructure = dataStructure;

            RaingageSetup raingage = dataStructure.getRaingage(areaName);

            this.runoffStepSize = dataStructure.getRunoffSetup().getRunoffStepSize();
            this.rainfallStepSize = raingage.getRainfallStepSize();
            this.initialTime = dataStructure.getTimeSetup().getStartDate();
            this.totalTime = dataStructure.getTimeSetup().getEndDate();

            String stationRaingage = raingage.getStationName();
            this.rainfallData = raingage.getReadDataFromFile().get(stationRaingage);
        }
        else {
            throw new NullPointerException();//TODO
        }

        if (aLPP != null && nLPP != null) {
            generateRainfall();
        }
        else {
            adaptedRainfallData.put( 1,adaptRainfallData(runoffStepSize, rainfallStepSize, totalTime.getEpochSecond(),
                    initialTime.getEpochSecond(), rainfallData) );
        }

        //adaptInfiltrationData();
    }


    /**
     * Adapt runoffDS stepsize to total time
     */
    //private Double adaptRunoffStepSize(Long runoffStepSize, Long totalTime) {
    //    Long tempFactor = totalTime/runoffStepSize;
    //    return totalTime / (double)tempFactor;
    //}

    /**
     * Adapt rainfall data
     */
    private LinkedHashMap<Instant, Double> adaptRainfallData(Long runoffStepSize, Long rainfallStepSize, Long totalTime,
                                                             Long initialTime, LinkedHashMap<Instant, Double> rainfallData) {

        LinkedHashMap<Instant, Double> adaptedRainfallData = new LinkedHashMap<>();
        Long currentRainfallTime = initialTime;

        for (Long currentTime = initialTime; currentTime<totalTime; currentTime+=runoffStepSize) {

            while(currentRainfallTime <= currentTime) {
                currentRainfallTime += rainfallStepSize;
            }

            Long upperTime = currentRainfallTime;
            Double upperRainfallData = 0.0;
            if(rainfallData.get(Instant.ofEpochSecond(upperTime)) != null) {
                upperRainfallData = rainfallData.get(Instant.ofEpochSecond(upperTime));
            }

            Long lowerTime = upperTime - rainfallStepSize;
            Double lowerRainfallData = 0.0;
            if(rainfallData.get(Instant.ofEpochSecond(lowerTime)) != null) {
                lowerRainfallData = rainfallData.get(Instant.ofEpochSecond(lowerTime));
            }

            Double currentRainfall = interpolateRainfall(currentTime, lowerTime, lowerRainfallData, upperTime, upperRainfallData);

            adaptedRainfallData.put(Instant.ofEpochSecond(currentTime), currentRainfall);
        }
        adaptedRainfallData.put(Instant.ofEpochSecond(totalTime), rainfallData.get(Instant.ofEpochSecond(totalTime)));

        return adaptedRainfallData;
    }

    private Double interpolateRainfall(Long currentRunoffTime, Long lowerTime, Double lowerTimeData, Long upperTime, Double upperTimeData) {
        Long rangeTime = upperTime - lowerTime;

        if( rangeTime == 0 ) { return lowerTimeData; }
        else {
            if (upperTimeData == null) {
                upperTimeData = 0.0;
            }
            if (lowerTimeData == null) {
                lowerTimeData = 0.0;
            }

            Double numerator = upperTimeData - lowerTimeData;

            return lowerTimeData + numerator / rangeTime * (currentRunoffTime - lowerTime);
        }
    }

    private LinkedHashMap<Instant, Double> constantRainfallData(Instant rainfallTime) {

        LinkedHashMap<Instant, Double> rainfallDataGenerated = new LinkedHashMap<>();

        for (Long currentTime = initialTime.getEpochSecond(); currentTime<=totalTime.getEpochSecond(); currentTime=+runoffStepSize) {
            if (rainfallTime.getEpochSecond() < currentTime) {
                Double rainfallValue = aLPP * Math.pow(rainfallTime.getEpochSecond(), nLPP - 1.0);
                rainfallDataGenerated.put(Instant.ofEpochSecond(currentTime), rainfallValue);
            }
            else {
                rainfallDataGenerated.put(Instant.ofEpochSecond(currentTime), 0.0);
            }
        }
        return rainfallDataGenerated;
    }

    private void generateRainfall() {
        Integer counter = 1;
        Long rainfallTimeInterval = ( totalTime.getEpochSecond() - initialTime.getEpochSecond() ) / numberOfCurves;
        for (Long currentTime = initialTime.getEpochSecond(); currentTime<=totalTime.getEpochSecond(); currentTime+=rainfallTimeInterval) {
            adaptedRainfallData.put(counter, constantRainfallData(Instant.ofEpochSecond(currentTime)));
            counter++;
        }
    }

    private void generateRainfall(Long stormwaterInterval) {
        Integer counter = 1;
        Long rainfallTimeInterval = stormwaterInterval / numberOfCurves;
        for (Long currentTime = initialTime.getEpochSecond(); currentTime<=totalTime.getEpochSecond(); currentTime+=rainfallTimeInterval) {
            adaptedRainfallData.put(counter, constantRainfallData(Instant.ofEpochSecond(currentTime)));
            counter++;
        }
    }
}