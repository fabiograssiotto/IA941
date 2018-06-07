package modules;

import edu.memphis.ccrg.lida.sensorymemory.SensoryMemoryImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ws3dproxy.model.Thing;

public class SensoryMemory extends SensoryMemoryImpl {

    private Map<String, Object> sensorParam;
    private Thing food;
    private Thing jewel;
    private List<Thing> thingAhead;
    private Thing leafletJewel;
    private Thing brick;
    private Object freespace;

    public SensoryMemory() {
        this.sensorParam = new HashMap<>();
        this.food = null;
        this.jewel = null;
        this.thingAhead = new ArrayList<>();
        this.leafletJewel = null;
        this.brick = null;
        this.freespace = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void runSensors() {
        /*
        sensorParam.clear();
        sensorParam.put("mode", "food");
        food = (Thing) environment.getState(sensorParam);
        sensorParam.clear();
        sensorParam.put("mode", "jewel");
        jewel = (Thing) environment.getState(sensorParam);
        sensorParam.clear();
        sensorParam.put("mode", "thingAhead");
        thingAhead = (List<Thing>) environment.getState(sensorParam);
        sensorParam.clear();
        sensorParam.put("mode", "leafletJewel");
        leafletJewel = (Thing) environment.getState(sensorParam);*/
        sensorParam.clear();
        sensorParam.put("mode", "brick");
        brick = (Thing) environment.getState(sensorParam);
        sensorParam.clear();
        sensorParam.put("mode", "freespace");
        freespace = environment.getState(sensorParam);
        
    }

    @Override
    public Object getSensoryContent(String modality, Map<String, Object> params) {
        Object requestedObject = null;
        String mode = (String) params.get("mode");
        switch (mode) {
            case "food":
                requestedObject = food;
                break;
            case "jewel":
                requestedObject = jewel;
                break;
            case "thingAhead":
                requestedObject = thingAhead;
                break;
            case "leafletJewel":
                requestedObject = leafletJewel;
                break;
            case "brick":
                requestedObject = brick;
                break;
            case "freespace":
                requestedObject = freespace;
                break;
            default:
                break;
        }
        return requestedObject;
    }

    @Override
    public Object getModuleContent(Object... os) {
        return null;
    }

    @Override
    public void decayModule(long ticks) {
    }
}
