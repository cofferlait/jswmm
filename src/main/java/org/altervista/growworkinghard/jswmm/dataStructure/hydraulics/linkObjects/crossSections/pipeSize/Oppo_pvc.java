package org.altervista.growworkinghard.jswmm.dataStructure.hydraulics.linkObjects.crossSections.pipeSize;

import java.util.HashMap;
import java.util.Map;

public class Oppo_pvc extends CommercialPipeSize {

    private final Map<Double, Double> pipe = new HashMap() {{
        put(153.6,160.0);
        put(192.2,200.0);
        put(240.2,250.0);
        put(302.6,315.0);
        put(384.2,400.0);
        put(480.4,500.0);
        put(605.4,630.0);
    }};

    @Override
    public double[] getCommercialDiameter(double designedDiameter) {
        double[] commercialSize = null;
        for(Map.Entry<Double, Double> diameter : pipe.entrySet()) {
            double innerSize = diameter.getKey();
            if (innerSize > designedDiameter) {
                double outerSize = diameter.getValue();
                commercialSize = new double[]{innerSize, outerSize};
            }
        }
        if (commercialSize == null) {
            String msg = "Designed diameter " + designedDiameter;
            msg += " is bigger than the biggest diameter available.";
            throw new NullPointerException(msg);
        }
        return commercialSize;
    }
}
