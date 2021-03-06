package models.fluid;

import dispatch.AbstractEvent;
import dispatch.Dispatcher;
import error.OTMException;

public class EventFluidStateUpdate extends AbstractEvent {

    public EventFluidStateUpdate(Dispatcher dispatcher, float timestamp, Object model){
        super(dispatcher,55,timestamp,model);
    }

    @Override
    public void action(boolean verbose) throws OTMException {

        super.action(verbose);

        AbstractFluidModel model = (AbstractFluidModel)recipient;

        // update the models.fluid.ctm state
        model.update_fluid_state(timestamp);

        // register next clock tick
        float next_timestamp = timestamp + model.dt_sec;
        if(next_timestamp<=dispatcher.stop_time)
            dispatcher.register_event(new EventFluidStateUpdate(dispatcher,next_timestamp,model));
    }

}
