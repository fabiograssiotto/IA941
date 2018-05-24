/*******************************************************************************
 * Copyright (c) 2009, 2011 The University of Memphis.  All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the LIDA Software Framework Non-Commercial License v1.0 
 * which accompanies this distribution, and is available at
 * http://ccrg.cs.memphis.edu/assets/papers/2010/LIDA-framework-non-commercial-v1.0.pdf
 *******************************************************************************/
package myagent.featuredetectors;

import java.util.HashMap;
import java.util.Map;

import edu.memphis.ccrg.lida.pam.tasks.BasicDetectionAlgorithm;

/**
 * White Background detector
 * @author Fabio Grassiotto
 */
public class WhiteBgFeatureDetector extends BasicDetectionAlgorithm {

    private int backgroundColor = -1;

    private Map<String, Object> smParams = new HashMap<String, Object>();

    @Override
    public void init() {
       super.init();
       smParams.put("mode","all");
    }

    @Override
    public double detect() {
        int[] layer = (int[]) sensoryMemory.getSensoryContent("visual",smParams);
        int area=0;
        for(int i=0;i<layer.length;i++){
            if(layer[i]!=backgroundColor){
                area++;
            }
        }
        
        if (area > 0) {
            return 0.0;
        } else {
            return 1.0;
        }
    }
}
