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

package org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.nodeObject;

import java.time.Instant;
import java.util.LinkedHashMap;

public class Junction extends AbstractNode {

    Double maximumDepthNode;
    Double initialDepthnode;
    Double maximumDepthSurcharge;
    Double pondingArea;

    public Junction(Double nodeElevation, Double maximumDepthNode, Double initialDepthnode,
                    Double maximumDepthSurcharge, Double pondingArea) {
        this.nodeElevation = nodeElevation;
        this.maximumDepthNode = maximumDepthNode;
        this.initialDepthnode = initialDepthnode;
        this.maximumDepthSurcharge = maximumDepthSurcharge;
        this.pondingArea = pondingArea;
    }

    @Override
    public void addRoutingFlowRate(LinkedHashMap<Instant, Double> newFlowRate) {
        newFlowRate.forEach((k, v) -> routingInflow.merge(k, v, Double::sum));
    }

    public synchronized void addRunoffFlowRate(LinkedHashMap<Instant, Double> newAreaFlowRate) {
        if (nodeFlowRate == null) {
            //System.out.println(newAreaFlowRate.get(Instant.parse("2018-01-01T00:02:00Z")));
            nodeFlowRate = new LinkedHashMap<>(newAreaFlowRate);
            System.out.println("new object");
        }
        else {
            newAreaFlowRate.forEach((k, v) -> nodeFlowRate.merge(k, v, Double::sum));
            System.out.println("merge");
        }
    }

    @Override
    public LinkedHashMap<Instant, Double> getNodeFlowRate() {
        return nodeFlowRate;
    }
}
