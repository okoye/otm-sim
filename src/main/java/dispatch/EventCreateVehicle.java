package dispatch;

import common.AbstractSource;
import error.OTMException;
import models.pq.Source;

public class EventCreateVehicle extends AbstractEvent {

    public EventCreateVehicle(Dispatcher dispatcher, float timestamp, AbstractSource source) {
        super(dispatcher,0, timestamp,source);
    }

    @Override
    public void action(boolean verbose) throws OTMException {
        super.action(verbose);
        Source source = (Source)recipient;
        source.insert_vehicle(timestamp);
        source.schedule_next_vehicle(dispatcher,timestamp);
    }

}