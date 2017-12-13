package org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment;

import org.altervista.growworkinghard.jswmm.dataStructure.runoff.RunoffSetup;
import org.altervista.growworkinghard.jswmm.runoff.RunoffODE;
import org.apache.commons.math3.ode.FirstOrderIntegrator;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

public class ImperviousWithStorage extends Subarea {

    LinkedHashMap<Instant, Double> depressionStorage;
    Double totalImperviousArea;

    public ImperviousWithStorage(Double imperviousWStorageArea, Double imperviousWOStorageArea, Double roughnessCoefficient) {
        this(imperviousWStorageArea, imperviousWOStorageArea, roughnessCoefficient, null, null);
    }

    public ImperviousWithStorage(Double imperviousWStorageArea, Double imperviousWOStorageArea, Double roughnessCoefficient,
                                 Double percentageRouted, List<Subarea> connections) {

        this.subareaArea = imperviousWStorageArea;
        this.totalImperviousArea = imperviousWStorageArea + imperviousWOStorageArea;
        this.roughnessCoefficient = roughnessCoefficient;
        this.percentageRouted = percentageRouted;
        this.subareaConnections = connections;
    }

    @Override
    public void setDepthFactor(Double subareaSlope, Double characteristicWidth) {
        if (!subareaConnections.isEmpty()) {
            for (Subarea connections : subareaConnections) {
                connections.setDepthFactor(subareaSlope, characteristicWidth);
                this.depthFactor = Math.pow(subareaSlope, 0.5) *
                        characteristicWidth / (roughnessCoefficient * totalImperviousArea);
            }
        }
    }

    @Override
    Double getWeightedFlowRate(Instant currentTime) {
        return flowRate.get(currentTime) * subareaArea * percentageRouted;
    }

    @Override
    void evaluateNextDepth(Instant currentTime, RunoffSetup runoffSetup, Double rainfall, Double evaporation) {

        Long runoffStepSize = runoffSetup.getRunoffStepSize();

        Instant nextTime = currentTime.plus(runoffStepSize, SECONDS);
        Double moistureVolume = rainfall * runoffStepSize +
                runoffDepth.get(currentTime) + depressionStorage.get(currentTime);

        evaporation = Math.max(evaporation, (runoffDepth.get(currentTime) + depressionStorage.get(currentTime))/runoffStepSize);

        excessRainfall = rainfall - evaporation;

        if(evaporation * runoffStepSize >= moistureVolume) {
            runoffDepth.put(nextTime, 0.0);
            flowRate.put(nextTime, 0.0);
        }
        else {
            if(excessRainfall <= depressionStorage.get(currentTime)) {
                runoffDepth.put(nextTime, runoffDepth.get(currentTime) + rainfall * runoffStepSize);
                flowRate.put(nextTime, 0.0);
            }
            else {
                runoffODEsolver(currentTime, nextTime, rainfall, runoffSetup);
            }
        }
    }
}