package control.rampmetering;

import common.Scenario;
import control.AbstractController;
import control.command.CommandNumber;
import dispatch.Dispatcher;
import error.OTMException;
import jaxb.Controller;
import profiles.Profile1D;
import utils.OTMUtils;

import java.util.HashMap;
import java.util.Map;

public class ControllerProfileRate extends AbstractController {

    public Map<Long, Profile1D> actuator_rate_vph;

    ///////////////////////////////////////////////////
    // construction
    ///////////////////////////////////////////////////

    public ControllerProfileRate(Scenario scenario, Controller jaxb_controller) throws OTMException {
        super(scenario, jaxb_controller);
        actuator_rate_vph = new HashMap<>();
        if(jaxb_controller.getTargetActuators()!=null){
            for(jaxb.TargetActuator ta : jaxb_controller.getTargetActuators().getTargetActuator()){
                if(!actuators.containsKey(ta.getId()))
                    continue;
                Profile1D profile = new Profile1D(start_time,dt, OTMUtils.csv2list(ta.getContent()));
                actuator_rate_vph.put(ta.getId(),profile);
            }
        }
    }

    ///////////////////////////////////////////////////
    // InterfaceScenarioElement
    ///////////////////////////////////////////////////


    @Override
    public void update_command(Dispatcher dispatcher) throws OTMException {

        for(Map.Entry<Long,Profile1D> e : actuator_rate_vph.entrySet()){
            Long act_id = e.getKey();
            Profile1D profile = e.getValue();
            float rate_vph = (float) profile.get_value_for_time(dispatcher.current_time);
            command.put(act_id, new CommandNumber( rate_vph / 3600f) );
        }
    }

//    public void set_rate_vph_for_actuator(Long id,Float rate_vph){
//        actuator_rate_vph.put(id,rate_vph);
//    }

}
