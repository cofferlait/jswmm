package org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.ReceiverRunoff;

import org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.nodeObject.AbstractNode;
import org.altervista.growworkinghard.jswmm.dataStructure.hydrology.subcatchment.AbstractSubcatchment;

public interface ReceiverRunoff {
    public enum ReceiverType {
        NODE,
        SUBCATCHMENT
    }

    public ReceiverType getReceiverType();

    public AbstractReceiver getReceiverObject();

    public Double getPercentage();
}