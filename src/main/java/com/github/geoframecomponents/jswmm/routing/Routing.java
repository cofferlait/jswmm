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

package com.github.geoframecomponents.jswmm.routing;

import com.github.geoframecomponents.jswmm.dataStructure.SWMMobject;
import com.github.geoframecomponents.jswmm.dataStructure.hydraulics.linkObjects.Conduit;
import oms3.annotations.*;
import com.github.geoframecomponents.jswmm.dataStructure.hydraulics.linkObjects.crossSections.pipeSize.CommercialPipeSize;
import com.github.geoframecomponents.jswmm.dataStructure.hydraulics.linkObjects.crossSections.pipeSize.Lucchese_ca;
import com.github.geoframecomponents.jswmm.dataStructure.routingDS.RoutingSetup;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Routing {

    /**
     * Time setup
     */
    private Instant initialTime;

    private Instant totalTime;


    /**
     * Simulation node fields
     */
    @In
    public String linkName = null;

    @In
    public String downstreamNodeName = null;

    /**
     * Link characteristics
     */
    @In
    public Conduit conduit;

    @In
    public CommercialPipeSize pipeCompany = new Lucchese_ca();

    /**
     * Integration method setup
     */
    private Long routingStepSize;

    private RoutingSetup routingSetup;

    /**
     * Data structure
     */
    @In
    @Out
    public SWMMobject dataStructure = null;

    @InNode
    public HashMap<Integer, List<Integer>> net3subtrees;

    @OutNode
    public HashMap<Integer, LinkedHashMap<Instant, Double>> routingFlowRate;
    @Execute
    
    public void run() {

        System.out.println("Routing on " + linkName);
        if (dataStructure != null && linkName != null) {
            this.initialTime = dataStructure.getTimeSetup().getStartDate();
            this.totalTime = dataStructure.getTimeSetup().getEndDate();

            this.routingSetup = dataStructure.getRoutingSetup();
            this.routingStepSize = routingSetup.getRoutingStepSize();

            this.conduit = dataStructure.getConduit(linkName);
        } else {
            throw new NullPointerException("Nothing implemented yet");
        }

        //evaluate the maximum flow for each SWMM timestep
        Instant currentTime = initialTime;
        double maxDischarge = 0.0;
        while (currentTime.isBefore(totalTime)) {
            maxDischarge = conduit.evaluateMaxDischarge(currentTime, maxDischarge);
            currentTime = currentTime.plusSeconds(routingStepSize);
        }

        System.out.println("Q_MAX " + maxDischarge);

        //dimensioning method!!
        conduit.evaluateDimension(maxDischarge, pipeCompany);

        //System.out.println("UPGRADING SUBTREES");

        dataStructure.upgradeSubtrees(linkName, net3subtrees);

        //route the maximum discharge to next bucket
        currentTime = initialTime;
        while (currentTime.isBefore(totalTime)) {
            conduit.evaluateFlowRate(currentTime);
            currentTime = currentTime.plusSeconds(routingStepSize);
        }
       conduit.evaluateFlowRate(currentTime);

        routingFlowRate = conduit.getDownstreamFlowRate();
        //routingFlowRate = conduit.getUpstreamFlowRate();


        HashMap<Integer, LinkedHashMap<Instant, Double>> currentFlow = routingFlowRate;
//        for (Integer id : currentFlow.keySet()) {
//            LinkedHashMap<Instant, Double> flow = currentFlow.get(id);
//            //System.out.print("ID " + id);
//            for (Instant time : flow.keySet()) {
//                //System.out.print("time " +  time);
//                System.out.println(currentFlow.get(id).get(time));
//            }
//        }
    }

//    public void test(String fileChecks) {
//        LinkedHashMap<Instant, Double> evaluated = conduit.getDownstreamFlowRate();
//        List<Double> defined = dataStructure.readFileList(fileChecks);
//
//        int i = 0;
//        for(Map.Entry<Instant, Double> data : evaluated.entrySet()) {
//            //TODO check a method to do it better - not always is ordered
//            assertEquals(data.getValue(), defined.get(i), 0.85);
//            //System.out.println(data.getValue());
//            //System.out.println(defined.get(i));
//            i = i + 1;
//        }
//    }
}
