package org.altervista.growworkinghard.jswmm.dataStructure;

import java.time.Instant;
import java.util.LinkedHashMap;

public class Outfall extends AbstractNodes {

    Double fixedStage;//TODO verify from where
    LinkedHashMap<Instant, Double> tidalCurve;
    LinkedHashMap<Instant, Double> stageTimeseries;
    boolean gated;
    String routeTo;

    public Outfall(boolean gated) {
        this.gated = gated;
    }

    public Outfall(boolean gated, String routeTo) {
        this.gated = gated;
        this.routeTo = routeTo;
    }

    //TODO solve the conflict with tidal/timeseries
    public Outfall(LinkedHashMap<Instant, Double> tidalCurve, boolean gated, String routeTo) {
        this.tidalCurve = tidalCurve;
        this.gated = gated;
        this.routeTo = routeTo;
    }
}
