package org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.linkObjects;

import org.altervista.growworkinghard.jswmm.dataStructure.options.units.ProjectUnits;
import org.altervista.growworkinghard.jswmm.dataStructure.routing.RoutingSetup;

public abstract class AbstractLink {

    ProjectUnits linkUnits;

    String linkName;
    String upstreamNodeName;
    String downstreamNodeName;

    public enum LinkShape {
        CIRCURAL,
        DUMMY
    }
    LinkShape linkShape;

    Long routingStepSize;
    RoutingSetup routingMethod;
}