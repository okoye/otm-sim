package models.none;

import common.AbstractLinkModel;
import common.Link;
import packet.PacketLink;
import error.OTMErrorLog;
import error.OTMException;
import jaxb.Roadparam;
import runner.Scenario;

public class LinkModel extends AbstractLinkModel {

    private float ff_travel_time_sec;       // free flow travel time in seconds
    private float capacity_vps;

    public LinkModel(Link link) {
        super(link);
    }

    @Override
    public void reset() {
        System.out.println("IMPLEMENT THIS");
    }

    @Override
    public void set_road_param(Roadparam r, float sim_dt_sec) {
        this.capacity_vps = r.getCapacity()/3600f;
        this.ff_travel_time_sec = 3.6f * link.length / r.getSpeed();
    }

    @Override
    public void add_vehicle_packet(float timestamp, PacketLink vp) throws OTMException {

    }

    @Override
    public void validate(OTMErrorLog errorLog) {
        if(ff_travel_time_sec<=0)
            errorLog.addError("ff_travel_time_sec<=0");
        if(capacity_vps<=0)
            errorLog.addError("capacity_vps<=0");
    }

    @Override
    public void initialize(Scenario scenario) throws OTMException {

    }

    @Override
    public float get_ff_travel_time() {
        return ff_travel_time_sec;
    }

    @Override
    public float get_capacity_vps() {
        return capacity_vps;
    }
}