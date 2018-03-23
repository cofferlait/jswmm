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
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.Area;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.Subarea;
import org.altervista.growworkinghard.jswmm.dataStructure.options.time.TimeSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.runoffDS.RunoffSetup;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class Runoff {

    @In
    public LinkedHashMap<Instant, Double> adaptedRainfallData;

    private LinkedHashMap<Instant, Double> evaporationData = null;

    /**
     * Time setup of the simulation
     */
    private Instant initialTime;

    private Instant totalTime;

    /**
     * Simulation node fields
     */
    @In
    public String areaName = null;

    @In
    public String nodeName = null;

    /**
     * Area characteristics
     */
    private Area area;

    private List<Subarea> subareas;

    private Double slopeArea;

    private Double characteristicWidth;

    /**
     * Integration method setup
     */
    private Long runoffStepSize;

    private RunoffSetup runoffSetup;

    /**
     * Data structure
     */
    @In
    @Out
    public SWMMobject dataStructure;

    @OutNode
    public LinkedHashMap<Instant, Double> runoffFlowRate;

    @Initialize
    public void initialize() {
        if (dataStructure != null && areaName != null) {

            //TODO evaporation!!
            this.runoffSetup = dataStructure.getRunoffSetup();
            TimeSetup timeSetup = dataStructure.getTimeSetup();
            this.area = dataStructure.getAreas().get(areaName);

            this.initialTime = timeSetup.getStartDate();
            this.totalTime = timeSetup.getEndDate();
            this.runoffStepSize = runoffSetup.getRunoffStepSize();

            this.subareas = area.getSubareas();
            this.slopeArea = area.getAreaSlope();
            this.characteristicWidth = area.getCharacteristicWidth();
        }
        else {
            throw new NullPointerException("Nothing implemented yet");
        }
    }

    @Execute
    public void run() {

        Instant currentTime = Instant.parse(initialTime.toString());
        while (currentTime.isBefore(totalTime)) {

            //check snownelt - snowaccumulation TODO build a new component
            upgradeStepValues(currentTime);

            currentTime = currentTime.plusSeconds(runoffStepSize);
        }
    }

    private void upgradeStepValues(Instant currentTime) {
        LinkedHashMap<Instant, Double> ad = new LinkedHashMap<>(adaptedRainfallData);
        for (Subarea subarea : subareas) {
            //System.out.println("Before " + areaName);
            subarea.setDepthFactor(slopeArea, characteristicWidth);
            //System.out.println("Depth factor done " + areaName);
            subarea.evaluateFlowRate(ad.get(currentTime), 0.0, currentTime, //TODO evaporation!!
                    runoffSetup, slopeArea, characteristicWidth);
            //System.out.println("Flow rate done " + areaName);
        }
    }

    @Finalize
    public void upgradeNodeFlowRate() {
        runoffFlowRate = area.evaluateTotalFlowRate();
    }

    public void test(String fileChecks) {
        LinkedHashMap<Instant, Double> evaluated = area.getTotalAreaFlowRate();
        List<Double> defined = dataStructure.readFileList(fileChecks);

        int i = 0;
        for(Map.Entry<Instant, Double> data : evaluated.entrySet()) {
            //TODO check a method to do it better - not always is ordered
            assertEquals(data.getValue(), defined.get(i), 0.85);
            //System.out.println(data.getValue());
            //System.out.println(defined.get(i));
            i = i + 1;
        }
    }
}