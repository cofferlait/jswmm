package org.altervista.growworkinghard.jswmm.dataStructure;

import org.altervista.growworkinghard.jswmm.dataStructure.formatData.writeData.WriteSWMM5RainfallToFile;
import org.altervista.growworkinghard.jswmm.dataStructure.formatData.readData.ReadDataFromFile;
import org.altervista.growworkinghard.jswmm.dataStructure.formatData.readData.ReadSWMM5RainfallFile;
import org.altervista.growworkinghard.jswmm.dataStructure.formatData.writeData.WriteDataToFile;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.linkObjects.AbstractLink;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.linkObjects.Conduit;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.linkObjects.crossSections.Circular;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.linkObjects.crossSections.CrossSectionType;
import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.nodeObject.*;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.rainData.AbstractRaingage;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.rainData.GlobalRaingage;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.*;
import org.altervista.growworkinghard.jswmm.dataStructure.infiltration.InfiltrationSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.options.*;
import org.altervista.growworkinghard.jswmm.dataStructure.options.time.*;
import org.altervista.growworkinghard.jswmm.dataStructure.routing.RoutingSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.routing.RoutingSteadySetup;
import org.altervista.growworkinghard.jswmm.dataStructure.runoff.RunoffSetup;
import org.altervista.growworkinghard.jswmm.dataStructure.runoff.SWMM5RunoffSetup;
import org.apache.commons.math3.ode.FirstOrderIntegrator;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.SubareaReceiver.SubareaReceiverRunoff.IMPERVIOUS;
import static org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.SubareaReceiver.SubareaReceiverRunoff.OUTLET;
import static org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.SubareaReceiver.SubareaReceiverRunoff.PERVIOUS;

public class SWMMobject {

    long runoffStepSize; //must be in seconds!!
    long routingStepSize;

    Instant initialTime;
    Instant finalTime;

    RunoffSetup runoffSetup = new SWMM5RunoffSetup(0.1,0.1,
            0.1,0.1, initialTime, finalTime, runoffStepSize);

    RoutingSetup routingSetup = new RoutingSteadySetup();

    InfiltrationSetup infiltrationSetup;
    SteadyStateSetup steadyStateSetup;

    ProjectUnits projectUnits = new CubicMetersperSecond();

    Instant startDate;
    Instant endDate;
    Instant reportStartDate;
    Instant reportEndDate;
    Instant sweepStart;
    Instant sweepEnd;
    Integer dryDays;

    public TimeSetup timeSetup = new GlobalTimeSetup(startDate, endDate, reportStartDate, reportEndDate, sweepStart, sweepEnd, dryDays);

    List<String> reportSubcatchment;
    List<String> reportNodes; //TODO define!!
    List<String> reportLinks; //TODO define!!
    List<LIDcontrol> reportLID; //TODO define!!

    public ReportSetup reportSetup = new ReportSetup(true, true, true, true,
                                    reportSubcatchment, reportNodes, reportLinks, reportLID);

    AbstractOptions.OffsetConvention offsetConvention = AbstractOptions.OffsetConvention.DEPTH;
    boolean ignoreRainfall = false;
    boolean ignoreSnowMelt = true;
    boolean ignoreGroundwater = true;
    boolean ignoreRDII = true;
    boolean ignoreQuality = true;
    boolean allowPonding = false;

    Integer numberOfThreads = 1;
    String temporaryDirectory = "";

    public AbstractOptions options = new GlobalOptions(runoffSetup, routingSetup, infiltrationSetup, steadyStateSetup,
            projectUnits, timeSetup, reportSetup, offsetConvention, ignoreRainfall, ignoreSnowMelt, ignoreGroundwater,
            ignoreRDII, ignoreQuality, allowPonding, numberOfThreads, temporaryDirectory);

    public List<AbstractRaingage> raingages;

    public HashMap<String, Area> areas;

    public AbstractNode[] nodes;

    public AbstractLink[] links;

    FirstOrderIntegrator firstOrderIntegrator;

    public SWMMobject() throws IOException {
        routingSetup.fillTables();
    }

    /*
    AbstractFilesData filesData = new AbstractFilesData();

    //INPparser to select data
    // [TITLE]
    // [OPTIONS]
    // [REPORT]
    // [FILES]
    HashMap<String, FilesData.RainfallFile> rainfallFileHashMap = new HashMap<>();
    rainfallFileHashMap.put("", FilesData.RainfallFile.SAVE);
    filesData.filesDataIO.setRainfallFile(rainfallFileHashMap);

    HashMap<String, FilesData.RunoffFile> runoffFileHashMap = new HashMap<>();
    runoffFileHashMap.put("", FilesData.RunoffFile.SAVE);
    filesData.filesDataIO.setRunoffFile(runoffFileHashMap);

    HashMap<String, FilesData.HotstartFile> hotstartFileSaveHashMap = new HashMap<>();
    hotstartFileSaveHashMap.put("", FilesData.HotstartFile.SAVE);
    filesData.filesDataIO.setHotstartFileSave(hotstartFileSaveHashMap);

    HashMap<String, FilesData.HotstartFile> hotstartFileUseHashMap = new HashMap<>();
    hotstartFileUseHashMap.put("", FilesData.HotstartFile.USE);
    filesData.filesDataIO.setHotstartFileUse(hotstartFileUseHashMap);

    HashMap<String, FilesData.InflowFile> inflowFileHashMap = new HashMap<>();
    inflowFileHashMap.put("", FilesData.InflowFile.USE);
    filesData.filesDataIO.setInflowFile(inflowFileHashMap);

    HashMap<String, FilesData.OutflowFile> outflowFileHashMap = new HashMap<>();
    outflowFileHashMap.put("", FilesData.OutflowFile.SAVE);
    filesData.filesDataIO.setOutflowFile(outflowFileHashMap);

    // [RAINGAGES]----
    // [EVAPORATION]
    // [TEMPERATURE]
    // [ADJUSTMENTS]
    // [SUBCATCHMENTS]----
    // [SUBAREAS]----
    // [INFILTRATION]
    // [LID_CONTROLS]
    // [LID_USAGE]
    // [AQUIFERS]
    // [GROUNDWATER]
    // [GWF]
    // [SNOWPACKS]
    // [JUNCTIONS]----
    // [OUTFALLS]----
    // [DIVIDERS]
    // [STORAGE]
    // [CONDUITS]----
    // [PUMPS]
    // [ORIFICES]
    // [WEIRS]
    // [OUTLETS]
    // [XSECTIONS]----
    // [TRANSECTS]
    // [LOSSES]
    // [CONTROLS]
    // [POLLUTANTS]
    // [LANDUSES]
    // [COVERAGES]
    // [LOADINGS]
    // [BUILDUP]
    // [WASHOFF]
    // [TREATMENT]
    // [INFLOWS]
    // [DWF]
    // [RDII]
    // [HYDROGRAPHS]
    // [CURVES]
    // [TIMESERIES]----
    // [PATTERNS]
    */

    public void run() throws IOException {

        fillTables();

        //for (each raingage)
        ReadDataFromFile readDataFromFile = new ReadSWMM5RainfallFile("ciao");
        ProjectUnits raingageUnits = new CubicMetersperSecond();
        String raingageName = "RG1";
        String dataSourceName = "test1";
        String stationName = "STA01";
        Instant rainfallStartDate = Instant.parse("2000-04-04T00:00Z");
        Instant rainfallEndDate = Instant.parse("2000-04-04T00:00Z");
        Double snowpack = 0.0;
        raingages.add(0, new GlobalRaingage(readDataFromFile, raingageUnits, raingageName, dataSourceName, stationName,
                rainfallStartDate, rainfallEndDate, snowpack));

        //////for (each subcatchment)//////

        //ReadDataFromFile subcatchmentReadDataFromFile = new ReadSWMM5RainfallFile("ciao");
        //AcquiferSetup acquiferSetup = new Acquifer();
        //SnowPackSetup subcatchmentSnowpack = new SnowPack();
        //ProjectUnits subcatchmentUnits = new CubicMetersperSecond();
        //String subcatchmentName = "SUB1";
        Double subcatchmentArea = 1000.0;

        Double roughnessCoefficientPervious = 1.0;
        Double roughnessCoefficientImpervious = 1.0;

        LinkedHashMap<Instant, Double> depressionStoragePervious = null;
        LinkedHashMap<Instant, Double> depressionStorageImpervious = null;

        Double imperviousWstoragePercentage = 1.0;

        String subareaName = "A1";
        //String relativeRaingageName = "STA01";
        //SubcatchmentReceiverRunoff subcatchmentReceiverRunoff = new SubcatchmentReceiverRunoff(SUBCATCHMENT, "");

        Double imperviousPercentage = 0.0;
        Double characteristicWidth = 100.0;
        Double subareaSlope = 0.2;
        Double curbLength = 0.0;

        SubareaReceiver.SubareaReceiverRunoff perviousTo = OUTLET;
        Double percentageFromPervious = 10.0;

        SubareaReceiver.SubareaReceiverRunoff imperviousTo = OUTLET;
        Double percentageFromImpervious = 10.0;


        Double imperviousWStorageArea = subcatchmentArea*imperviousPercentage*imperviousWstoragePercentage;
        Double imperviousWOStorageArea = subcatchmentArea*imperviousPercentage - imperviousWStorageArea;

        //readDataFromFile, acquiferSetup, subcatchmentSnowpack, subcatchmentUnits,
        //subcatchmentName, subcatchmentArea, relativeRaingageName, subcatchmentReceiverRunoff, perviousSubareaReceiverRunoff,
        //percentagePerviousReceiver, imperviousPercentage, characteristicWidth, subareaSlope, curbLength);

        List<Subarea> tmpSubareas = null;
        if(imperviousPercentage == 0.0) {
            tmpSubareas.add(new Pervious(subcatchmentArea, roughnessCoefficientPervious,
                    depressionStoragePervious, firstOrderIntegrator));
        }
        else if(imperviousPercentage == 1.0) {
            if (1 - imperviousWstoragePercentage != 0) {
                tmpSubareas.add(new ImperviousWithStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        depressionStorageImpervious, roughnessCoefficientImpervious, firstOrderIntegrator));
            }
            tmpSubareas.add(new ImperviousWithoutStorage(imperviousWStorageArea, imperviousWOStorageArea,
                    roughnessCoefficientImpervious, firstOrderIntegrator));
        }
        else {
            if (perviousTo == IMPERVIOUS) {
                tmpSubareas.add(new ImperviousWithoutStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        roughnessCoefficientImpervious, firstOrderIntegrator));

                List<Subarea> tmpConnections = null;
                tmpConnections.add(new Pervious(subcatchmentArea, roughnessCoefficientPervious,
                        depressionStoragePervious, firstOrderIntegrator));

                tmpSubareas.add(new ImperviousWithStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        depressionStorageImpervious, roughnessCoefficientImpervious,
                        percentageFromPervious, tmpConnections, firstOrderIntegrator));
            }
            else if(perviousTo == OUTLET) {
                tmpSubareas.add(new Pervious(subcatchmentArea, roughnessCoefficientPervious,
                        depressionStoragePervious, firstOrderIntegrator));
            }

            if (imperviousTo == PERVIOUS) {

                List<Subarea> tmpConnections = null;
                tmpConnections.add(new ImperviousWithoutStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        roughnessCoefficientImpervious, firstOrderIntegrator));
                tmpConnections.add(new ImperviousWithStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        depressionStorageImpervious, roughnessCoefficientImpervious, firstOrderIntegrator));

                tmpSubareas.add(new Pervious(subcatchmentArea, depressionStoragePervious, roughnessCoefficientPervious,
                        percentageFromImpervious, tmpConnections, firstOrderIntegrator));
            }
            else if (imperviousTo == OUTLET) {
                tmpSubareas.add(new ImperviousWithStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        depressionStorageImpervious, roughnessCoefficientImpervious, firstOrderIntegrator));
                tmpSubareas.add(new ImperviousWithoutStorage(imperviousWStorageArea, imperviousWOStorageArea,
                        roughnessCoefficientImpervious, firstOrderIntegrator));
            }
        }
        areas.put(subareaName, new Area(tmpSubareas));

        //for (each junction)
        ReadDataFromFile junctionReadDataFromFile = new ReadSWMM5RainfallFile("ciao");
        WriteDataToFile writeDataToFile = new WriteSWMM5RainfallToFile();
        ExternalInflow dryWeatherInflow = new DryWeatherInflow();
        ExternalInflow RDII = new RainfallDependentInfiltrationInflow();
        ProjectUnits nodeUnits = new CubicMetersperSecond();
        String nodeName = "N1";
        Double nodeElevation = 2.0;
        Double maximumDepthNode = 3.0;
        Double initialDepthNode = 0.0;
        Double maximumDepthSurcharge = 1.0;
        Double nodePondingArea = 200.0;

        //nodes[0] = new Junction(junctionReadDataFromFile, writeDataToFile, dryWeatherInflow, RDII, nodeUnits, nodeName,
        //       nodeElevation, maximumDepthNode, initialDepthNode, maximumDepthSurcharge, nodePondingArea);

        //for (each outfall)
        ReadDataFromFile outfallReadDataFromFile = new ReadSWMM5RainfallFile("ciao");
        WriteDataToFile outfallWriteDataToFile = new WriteSWMM5RainfallToFile();
        ExternalInflow outfallDryWeatherInflow = new DryWeatherInflow();
        ExternalInflow outfallRDII = new RainfallDependentInfiltrationInflow();
        ProjectUnits outfallNodeUnits = new CubicMetersperSecond();
        String outfallName = "N1";
        Double outfallElevation = 2.0;
        Double fixedStage = 3.0;
        LinkedHashMap<Instant, Double> tidalCurve = null;
        LinkedHashMap<Instant, Double> stageTimeseries = null;
        boolean gated = false;
        String routeTo = "";

        nodes[1] = new Outfall(outfallReadDataFromFile, outfallWriteDataToFile, outfallDryWeatherInflow, outfallRDII,
                outfallNodeUnits, outfallName, outfallElevation, fixedStage, tidalCurve, stageTimeseries,
                gated, routeTo);

        //for (each link)
        String linkName = "";
        String upstreamNodeName = "";
        String downstreamNodeName = "";
        Double linkLength = 0.0;
        Double linkManningRoughness = null;
        Double upstreamOffset = 0.0;
        Double downstreamOffset = 0.0;
        Double initialFlowRate = 0.0;
        Double maximumFlowRate = 0.0;

        CrossSectionType crossSectionType = new Circular();
        ProjectUnits linkUnits = new CubicMetersperSecond();

        links[0] = new Conduit(crossSectionType, linkUnits, linkName, upstreamNodeName, downstreamNodeName,
                linkLength, linkManningRoughness, upstreamOffset, downstreamOffset, initialFlowRate, maximumFlowRate);

    }
}