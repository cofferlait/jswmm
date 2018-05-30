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

package org.altervista.growworkinghard.jswmm.dataStructure;

import org.altervista.growworkinghard.jswmm.dataStructure.formatData.readData.ReadDataFromFile;
import org.altervista.growworkinghard.jswmm.dataStructure.formatData.readData.ReadSWMM5RainfallFile;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.linkObjects.Conduit;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.linkObjects.OutsideSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.linkObjects.crossSections.Circular;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.linkObjects.crossSections.CrossSectionType;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.nodeObject.Junction;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.nodeObject.Outfall;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.rainData.GlobalRaingage;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.rainData.RaingageSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.*;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.ReceiverRunoff.ReceiverRunoff;
import org.altervista.growworkinghard.jswmm.dataStructure.options.units.CubicMetersperSecond;
import org.altervista.growworkinghard.jswmm.dataStructure.options.units.ProjectUnits;
import org.altervista.growworkinghard.jswmm.dataStructure.options.time.GlobalTimeSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.options.time.TimeSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.routingDS.RoutingKinematicWaveSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.routingDS.RoutingSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.runoffDS.RunoffSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.runoffDS.SWMM5RunoffSetup;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SWMMobject {

    private TimeSetup timeSetup;
    private RunoffSetup runoffSetup;
    private RoutingSetup routingSetup;
    private HashMap<String, RaingageSetup> raingageSetup = new HashMap<>();
    private HashMap<String, Area> areas = new HashMap<>();
    private Map<String, Junction> junctions = new ConcurrentHashMap<>();
    private HashMap<String, Outfall> outfalls = new HashMap<>();
    private Map<String, Conduit> conduit = new ConcurrentHashMap<>();
    private LinkedHashMap<Instant, Double> downstreamFlowRate;

    public SWMMobject(String inpFileName) {
        setTime();
        setRunoff();
        setRouting();
        setRaingages();
        setSubcatchments();
        setNodes();
        setLinks();
    }

    public SWMMobject() {
        setTime();
        setRunoff();
        setRouting();
        setRaingages();
        setSubcatchments();
        setNodes();
        setLinks();
        setInitialValues(1);
        setInitialValues(2);
        setInitialValues(3);
    }

    public TimeSetup getTimeSetup() {
        return timeSetup;
    }

    public RunoffSetup getRunoffSetup() {
        return new SWMM5RunoffSetup(runoffSetup);
    }

    public RoutingSetup getRoutingSetup() { return routingSetup; }

    public RaingageSetup getRaingage(String areaName) {
        return raingageSetup.get(areaName);
    }

    public Area getAreas(String areaName) {
        return areas.get(areaName);
    }

    public Conduit getConduit(String conduitName) {
        return conduit.get(conduitName);
    }

    private void setTime() {
        Instant startDate = Instant.parse("2018-01-01T00:00:00Z");
        Instant endDate = Instant.parse("2018-01-01T02:00:00Z");
        Instant reportStartDate = Instant.parse("2018-01-01T00:00:00Z");
        Instant reportEndDate = Instant.parse("2018-01-01T02:00:00Z");
        Instant sweepStart = Instant.parse("2018-01-01T00:00:00Z");
        Instant sweepEnd = Instant.parse("2018-01-01T00:00:00Z");
        Integer dryDays = 0;

        this.timeSetup = new GlobalTimeSetup(startDate, endDate, reportStartDate, reportEndDate,
                sweepStart, sweepEnd, dryDays);
    }

    private void setUnits() {
        String units = "CMS";

        if (units == "CMS") {
            ProjectUnits projectUnits = new CubicMetersperSecond();
        }
    }

    private void setRunoff() {
        Long runoffStepSize = 60L; //must be in seconds!!

        Double minimumStepSize = 1.0e-8;
        Double maximumStepSize = 1.0e+3;
        Double absoluteRunoffTolerance = 1.0e-5;
        Double relativeRunoffTolerance = 1.0e-5;

        Instant initialTime = timeSetup.getStartDate();
        Instant totalTime = timeSetup.getEndDate();

        this.runoffSetup = new SWMM5RunoffSetup(initialTime, totalTime, runoffStepSize,
                minimumStepSize, maximumStepSize, absoluteRunoffTolerance, relativeRunoffTolerance);
    }

    private void setRouting() {

        Instant initialTime = timeSetup.getStartDate();
        Instant totalTime = timeSetup.getEndDate();

        Long routingStepSize = 30L;
        Double toleranceMethod = 0.0015;

        //TODO need change to parallelize
        routingSetup = new RoutingKinematicWaveSetup(routingStepSize, toleranceMethod);
    }

    private void setRaingages() {

        //for (each raingage)


        ReadDataFromFile readDataFromFile = null;
        /*TODO check if a and n or data
        try {
            readDataFromFile = new ReadSWMM5RainfallFile("./data/rainfallNetwork.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //ProjectUnits raingageUnits = new CubicMetersperSecond();
        String raingageName = "RG1";
        String dataSourceName = "rainfallNetwork.txt";
        String stationName = "RG1";
        Long rainfallStepSize = 60L;
        //TODO FORMATDATA
        //Instant rainfallStartDate = Instant.parse("2000-04-04T00:00Z");
        //Instant rainfallEndDate = Instant.parse("2000-04-04T00:00Z");
        //Double snowpack = 0.0;

        raingageSetup.put(raingageName, new GlobalRaingage(readDataFromFile, dataSourceName, stationName, rainfallStepSize));
    }

    private void setSubcatchments() {
        //for (each subcatchment)
        setAreas("1", 1.937E04);
        setAreas("2", 1.731E04);
        setAreas("3", 0.481E04);
        setAreas("4", 0.547E04);
        setAreas("5", 2.141E04);
        setAreas("6", 0.383E04);
        setAreas("7", 0.353E04);
        setAreas("8", 0.999E04);
        setAreas("9", 1.583E04);
        setAreas("10", 0.633E04);
    }

    private void setAreas(String areaName, double subcatchmentArea) {
        //ReadDataFromFile subcatchmentReadDataFromFile = new ReadSWMM5RainfallFile("ciao");
        //AcquiferSetup acquiferSetup = new Acquifer();
        //SnowPackSetup subcatchmentSnowpack = new SnowPack();
        //ProjectUnits subcatchmentUnits = new CubicMetersperSecond();
        //String subcatchmentName = "Sub1";

        Double imperviousPercentage = 0.25;
        Double imperviousWOstoragePercentage = 0.25;

        Double depressionStorageImpervious = 0.00005;
        Double depressionStoragePervious = 0.00005;

        String perviousTo = "OUTLET";
        Double percentageFromPervious = 0.0;

        String imperviousTo = "OUTLET";
        Double percentageFromImpervious = 0.0;

        Double roughnessCoefficientPervious = 0.1;
        Double roughnessCoefficientImpervious = 0.01;

        Double characteristicWidth = 100.0;
        Double areaSlope = 0.01;
        Double curbLength = 0.0;

        String raingageName = "RG1";
        ReceiverRunoff receiverSubcatchment = null;

        List<Subarea> subareas = divideAreas(imperviousPercentage, subcatchmentArea,
                imperviousWOstoragePercentage, depressionStoragePervious, depressionStorageImpervious,
                roughnessCoefficientPervious, roughnessCoefficientImpervious,
                perviousTo, imperviousTo, percentageFromPervious, percentageFromImpervious);

        areas.put(areaName, new Area(subcatchmentArea, raingageSetup.get(areaName),
                characteristicWidth, areaSlope, subareas));
    }

    private void setNodes() {
        setJunctions("J1", 0.0);
        setJunctions("J2", 0.0);
        setJunctions("J3", 0.0);
        setJunctions("J4", 0.0);
        setJunctions("J5", 0.0);
        setJunctions("J6", 0.0);
        setJunctions("J7", 0.0);
        setJunctions("J8", 0.0);
        setJunctions("J9", 0.0);
        setJunctions("J10", 0.0);
        setJunctions("J11", 0.0);
        setOutfalls();
    }

    private void setJunctions(String nodeName, double nodeElevation) {
        //for (each junction)
        //ReadDataFromFile junctionReadDataFromFile = new ReadSWMM5RainfallFile("ciao");
        //WriteDataToFile writeDataToFile = new WriteSWMM5RainfallToFile();
        //ExternalInflow dryWeatherInflow = new DryWeatherInflow();
        //ExternalInflow RDII = new RainfallDependentInfiltrationInflow();
        //ProjectUnits nodeUnits = new CubicMetersperSecond();

        Double maximumDepthNode = 3.0;
        Double initialDepthNode = 0.0;
        Double maximumDepthSurcharge = 1.0;
        Double nodePondingArea = 200.0;

        junctions.put(nodeName, new Junction(nodeElevation, maximumDepthNode, initialDepthNode,
                maximumDepthSurcharge, nodePondingArea));
    }

    private void setOutfalls() {
        //for (each outfall)
        //ReadDataFromFile outfallReadDataFromFile = new ReadSWMM5RainfallFile("ciao");
        //WriteDataToFile outfallWriteDataToFile = new WriteSWMM5RainfallToFile();
        //ExternalInflow outfallDryWeatherInflow = new DryWeatherInflow();
        //ExternalInflow outfallRDII = new RainfallDependentInfiltrationInflow();
        //ProjectUnits outfallNodeUnits = new CubicMetersperSecond();
//        String nodeName = "Out1";
//        Double nodeElevation = 0.0;
//        Double fixedStage = 0.0;
//        LinkedHashMap<Instant, Double> tidalCurve = null;
//        LinkedHashMap<Instant, Double> stageTimeseries = null;
//        boolean gated = false;
//        String routeTo = "";
//
//        outfalls.put(nodeName, new Outfall(nodeElevation, fixedStage, tidalCurve,stageTimeseries,
//                gated, routeTo));
    }

    private void setLinks() {
        //for (each link) TODO check if present
        setConduit("11", 120.0,"J1", -239.0, 197.0, 0.0,
                "J3", -119.0, 197.0, 0);
        setConduit("12", 122,  "J2",-119.0, 319.0, 0.0,
                "J3",-119.0, 197.0, 0);
        setConduit("13", 119,  "J3",-119.0, 197.0, 0.0,
                "J4",0.0, 197.0, 0);
        setConduit("14", 43,   "J5",111.0, 240.0, 0.0,
                "J7",111.0, 197.0, 0);
        setConduit("15", 92,   "J6",203.0, 197.0, 0.0,
                "J7",111.0, 197.0, 0);
        setConduit("16", 111,   "J7",111.0, 197.0, 0.0,
                "J4",0.0, 197.0, 0);
        setConduit("17", 81,   "J4",  0.0, 197.0, 0.0,
                "J8",0.0, 116.0, 0);
        setConduit("18", 150,   "J9",150.0, 116.0, 0.0,
                "J8",0.0, 116.0, 0);
        setConduit("19", 134,  "J10",-134.0, 116.0, 0.0,
                "J8",0.0, 116.0, 0);
        setConduit("20", 116,   "J8",  0.0, 116.0, 0.0,
                "J11",0.0,   0.0, 0);
    }

    private void setConduit(String linkName, double linkLength, String upName, double upX, double upY, double upZ,
                            String downName, double downX, double downY, double downZ) {

        Double linkRoughness = 0.01;
        Double upstreamOffset = 0.0;
        Double downstreamOffset = 0.0;
        Double initialFlowRate = 0.0;
        Double maximumFlowRate = 0.0;
        Double diameter = 1.0;

        CrossSectionType crossSectionType = new Circular(diameter);
        //ProjectUnits linkUnits = new CubicMetersperSecond();

        OutsideSetup upstreamOutside8 = new OutsideSetup(upName, upstreamOffset,
                maximumFlowRate, upX, upY, upZ);
        OutsideSetup downstreamOutside8 = new OutsideSetup(downName, downstreamOffset,
                maximumFlowRate, downX, downY, downZ);

        conduit.put(linkName, new Conduit(routingSetup, crossSectionType, upstreamOutside8, downstreamOutside8,
                linkLength, linkRoughness));
    }

    private List<Subarea> divideAreas(Double imperviousPercentage, Double subcatchmentArea,
                                      Double imperviousWOstoragePercentage, Double depressionStoragePervious, Double depressionStorageImpervious,
                                      Double roughnessCoefficientPervious, Double roughnessCoefficientImpervious,
                                      String perviousTo, String imperviousTo, Double percentageFromPervious, Double percentageFromImpervious) {

        Double imperviousWOStorageArea = subcatchmentArea * imperviousPercentage * imperviousWOstoragePercentage;
        Double imperviousWStorageArea = subcatchmentArea * imperviousPercentage  - imperviousWOStorageArea;
        Double perviousArea = subcatchmentArea * (1-imperviousPercentage);

        List<Subarea> tmpSubareas = new LinkedList<>();
        if(imperviousPercentage == 0.0) {
            tmpSubareas.add(new Pervious(perviousArea, depressionStoragePervious, roughnessCoefficientImpervious));
        }
        else if(imperviousPercentage == 1.0) {
            if (imperviousWOstoragePercentage != 0.0) {
                tmpSubareas.add(new ImperviousWithoutStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        roughnessCoefficientImpervious));
            }
            if (imperviousWOstoragePercentage != 1.0) {
                tmpSubareas.add(new ImperviousWithStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        depressionStorageImpervious, roughnessCoefficientImpervious));
            }

        }
        else {
            if (perviousTo.equals("IMPERVIOUS")) {
                tmpSubareas.add(new ImperviousWithoutStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        roughnessCoefficientImpervious));

                List<Subarea> tmpConnections = null;
                tmpConnections.add(new Pervious(perviousArea, depressionStoragePervious, roughnessCoefficientPervious));

                tmpSubareas.add(new ImperviousWithStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        depressionStorageImpervious, roughnessCoefficientImpervious, percentageFromPervious, tmpConnections));
            }
            else if(perviousTo.equals("OUTLET")) {
                tmpSubareas.add(new Pervious(perviousArea, depressionStoragePervious, roughnessCoefficientPervious));
            }

            if (imperviousTo.equals("PERVIOUS")) {

                List<Subarea> tmpConnections = null;
                tmpConnections.add(new ImperviousWithoutStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        roughnessCoefficientImpervious));
                tmpConnections.add(new ImperviousWithStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        depressionStorageImpervious, roughnessCoefficientImpervious, percentageFromPervious, tmpConnections));

                tmpSubareas.add(new Pervious(perviousArea, depressionStoragePervious, roughnessCoefficientPervious,
                        percentageFromImpervious, tmpConnections));
            }
            else if (imperviousTo.equals("OUTLET")) {
                tmpSubareas.add(new ImperviousWithStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        depressionStorageImpervious, roughnessCoefficientImpervious));
                tmpSubareas.add(new ImperviousWithoutStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        roughnessCoefficientImpervious));
            }
        }
        return tmpSubareas;
    }

    //TODO add at each subcatchment!
    private void setInitialValues(Integer id) {
        setSubareasInitialValue(id, "1");
        setSubareasInitialValue(id, "2");
        setSubareasInitialValue(id, "3");
        setSubareasInitialValue(id, "4");
        setSubareasInitialValue(id, "5");
        setSubareasInitialValue(id, "6");
        setSubareasInitialValue(id, "7");
        setSubareasInitialValue(id, "8");
        setSubareasInitialValue(id, "9");
        setSubareasInitialValue(id, "10");
        setInitialTime(id, "11");
        setInitialTime(id, "12");
        setInitialTime(id, "13");
        setInitialTime(id, "14");
        setInitialTime(id, "15");
        setInitialTime(id, "16");
        setInitialTime(id, "17");
        setInitialTime(id, "18");
        setInitialTime(id, "19");
        setInitialTime(id, "20");
    }

    private void setSubareasInitialValue(Integer id, String areaName) {
        for(Subarea subarea : areas.get(areaName).getSubareas()) {
            subarea.setFlowRate(id, timeSetup.getStartDate(), 0.0);
            subarea.setRunoffDepth(id, timeSetup.getStartDate(), 0.0);
            subarea.setTotalDepth(id, timeSetup.getStartDate(), 0.0);
        }
    }

    private void setInitialTime(Integer id, String linkName) {
        Instant time = timeSetup.getStartDate();
        while (time.isBefore(timeSetup.getEndDate())) {
            conduit.get(linkName).setInitialUpFlowRate(id, time, 0.0);
            conduit.get(linkName).setInitialUpWetArea(id, time, 0.0);
            time = time.plusSeconds(routingSetup.getRoutingStepSize());
        }
        conduit.get(linkName).setInitialUpFlowRate(id, time, 0.0);
        conduit.get(linkName).setInitialUpWetArea(id, time, 0.0);
    }

    public List<Double> readFileList(String fileName) {
        String line;

        List<Double> testingValues = new ArrayList<>();

        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                testingValues.add(Double.parseDouble(line));
            }

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }

        return testingValues;
    }

    public void setNodeFlowRate(String nodeName, HashMap<Integer, LinkedHashMap<Instant, Double>> flowRate) {
        junctions.get(nodeName).sumFlowRate(flowRate);
    }

    public void setLinkFlowRate(String linkName, HashMap<Integer, LinkedHashMap<Instant, Double>> flowRate) {
        conduit.get(linkName).sumUpstreamFlowRate(flowRate);
    }

    public void upgradeSubtrees(String outLink, HashMap<Integer, List<Integer>> subtrees) {

        double downstreamDepthOut = getConduit(outLink).getDownstreamOutside().getWaterDepth();
        double maxDepth = downstreamDepthOut;

        for (Integer subtreeId : subtrees.keySet()) {
            double downstreamDepth = getConduit(String.valueOf(subtreeId)).getDownstreamOutside().getWaterDepth();
            if (downstreamDepth > maxDepth) {
                maxDepth = downstreamDepth;
            }
        }

        List<Integer> outLinks = null;
        outLinks.add(Integer.decode(outLink));
        if (downstreamDepthOut - maxDepth != 0.0) {
            upgradeStream(outLinks, downstreamDepthOut - maxDepth);
        }

        for (List<Integer> subtreeList : subtrees.values()) {
            int firstSon = subtreeList.size();
            double downstreamDepth = getConduit(String.valueOf(firstSon)).getDownstreamOutside().getWaterDepth();
            if (downstreamDepth - maxDepth != 0.0) {
                upgradeStream(subtreeList, downstreamDepth - maxDepth);
            }
        }
    }

    private void upgradeStream(List<Integer> subtreeList, double delta) {
        for (Integer subtreeLink : subtreeList) {
            OutsideSetup upstream = getConduit(String.valueOf(subtreeLink)).getUpstreamOutside();
            OutsideSetup downstream = getConduit(String.valueOf(subtreeLink)).getDownstreamOutside();

            upstream.upgradeOffset(delta);
            downstream.upgradeOffset(delta);
        }
    }
}