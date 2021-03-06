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

package com.github.geoframecomponents.jswmm.dataStructure.hydraulics.nodeObject;

import com.github.geoframecomponents.jswmm.dataStructure.formatData.writeData.WriteDataToFile;
import com.github.geoframecomponents.jswmm.dataStructure.formatData.readData.ReadDataFromFile;
import com.github.geoframecomponents.jswmm.dataStructure.options.units.ProjectUnits;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;

public abstract class AbstractNode {

    ReadDataFromFile readDataFromFile;
    WriteDataToFile writeDataToFile;
    ExternalInflow dryWeatherInflow;
    ExternalInflow rainfallDependentInfiltrationInflow;

    ProjectUnits nodeUnits;

    String nodeName;
    Double nodeElevation;

    HashMap<Integer, LinkedHashMap<Instant, Double>> nodeFlowRate = new HashMap<>();
    LinkedHashMap<Instant, Double> nodeDepth;

    public abstract void sumFlowRate(HashMap<Integer, LinkedHashMap<Instant, Double>> newFlowRate);

    public abstract HashMap<Integer, LinkedHashMap<Instant, Double>> getFlowRate();
}


