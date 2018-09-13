package com.fg.franco.i_agro;

import java.text.DecimalFormat;
import java.util.Map;

public class SmartAnalizer implements Analyzer {

    public String analize(Map<String, Float> response) {
        Map.Entry<String, Float> maxEntry = null;

        for (Map.Entry<String, Float> entry : response.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        switch (maxEntry.getKey()){
            case "JSON error": return "JSON error.";
            case "URL error": return "Bad url, remember to set it.";
            default: break;
        }
        DecimalFormat numberFormat = new DecimalFormat("#.00");
        return (maxEntry.getValue()> 0.6)?
                maxEntry.getKey() + " (" + numberFormat.format(maxEntry.getValue()*100) + "% sure)":
                "Unsure";
    }
}
