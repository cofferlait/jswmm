package org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.nodeObject;

import org.altervista.growworkinghard.jswmm.dataStructure.ProjectUnits;
import org.altervista.growworkinghard.jswmm.dataStructure.formatData.readData.ReadDataFromFile;
import org.altervista.growworkinghard.jswmm.dataStructure.formatData.writeData.WriteDataToFile;

public class Junction extends AbstractNode {

    Double maximumDepthNode;
    Double initialDepthnode;
    Double maximumDepthSurcharge;
    Double pondingArea;

    public Junction(ReadDataFromFile readDataFromFile, WriteDataToFile writeDataToFile, ExternalInflow dryWeatherInflow,
                    ExternalInflow rainfallDependentInfiltrationInflow, ProjectUnits nodeUnits,
                    String nodeName, Double nodeElevation, Double maximumDepthNode, Double initialDepthnode,
                    Double maximumDepthSurcharge, Double pondingArea) {

        super(readDataFromFile, writeDataToFile, dryWeatherInflow, rainfallDependentInfiltrationInflow, nodeUnits, nodeName, nodeElevation);

        this.maximumDepthNode = maximumDepthNode;
        this.initialDepthnode = initialDepthnode;
        this.maximumDepthSurcharge = maximumDepthSurcharge;
        this.pondingArea = pondingArea;
    }
}
