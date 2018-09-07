package dispatch;

import error.OTMException;
import common.Network;

public class EventMacroFlowUpdate extends AbstractEvent {

    public EventMacroFlowUpdate(Dispatcher dispatcher, float timestamp, Object network){
        super(dispatcher,1,timestamp,network);
    }

    @Override
    public void action(boolean verbose) throws OTMException {

        super.action(verbose);

        Network network = (Network)recipient;

        // update the models.ctm state
        network.update_macro_flow(timestamp);

        // register next clock tick
        float next_timestamp = timestamp+network.scenario.sim_dt;
        if(next_timestamp<=dispatcher.stop_time)
            dispatcher.register_event(new EventMacroFlowUpdate(dispatcher,next_timestamp,network));
    }

}