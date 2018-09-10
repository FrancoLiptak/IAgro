package com.fg.franco.i_agro;

import java.util.Map;

class MaxAnalyzer implements Analyzer {

    public String analize(Map<String, Float> response){
        Map.Entry<String, Float> maxEntry = null;

        for (Map.Entry<String, Float> entry : response.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        return maxEntry.getKey();
    }
}
