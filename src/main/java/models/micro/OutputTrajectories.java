package models.micro;

import common.Link;
import dispatch.Dispatcher;
import dispatch.EventPoke;
import dispatch.InterfacePokable;
import error.OTMException;
import models.AbstractLaneGroup;
import output.AbstractOutput;
import runner.RunParameters;
import runner.Scenario;

import java.io.*;

public class OutputTrajectories extends AbstractOutput implements InterfacePokable {

    public float outDt;			// output frequency in seconds
    Model_Micro model;

    @Override
    public void register(RunParameters props, Dispatcher dispatcher) throws OTMException {
        dispatcher.register_event(new EventPoke(dispatcher,5,dispatcher.current_time + outDt,this));
    }

    //////////////////////////////////////////////////////
    // construction
    //////////////////////////////////////////////////////

    public OutputTrajectories(Scenario scenario,Model_Micro model,String prefix,String output_folder,Float outDt) throws OTMException{
        super(scenario,prefix,output_folder);
        this.outDt = outDt==null ? -1 : outDt;
        this.model = model;
    }

    @Override
    public String get_output_file() {
        return  output_folder + File.separator + prefix + "_" +
                String.format("%.0f", outDt) + "_" +
                model.name + "_traj.txt";
    }

    //////////////////////////////////////////////////////
    // write
    //////////////////////////////////////////////////////

    @Override
    public void write(float timestamp,Object obj) throws OTMException {
        if(write_to_file){
            try {
                for(Link link : model.links){
                    for(AbstractLaneGroup alg : link.lanegroups_flwdn.values()){
                        models.micro.LaneGroup lg = (models.micro.LaneGroup) alg;
                        for(Vehicle vehicle : lg.vehicles)
                            writer.write(String.format("%.2f\t%d\t%d\t%.2f\n",timestamp,vehicle.getId(), lg.id,vehicle.pos));
                    }
                }
            } catch (IOException e) {
                throw new OTMException(e);
            }
        } else {
            throw new OTMException("Not implemented code: 09242je");
        }
    }

    @Override
    public void poke(Dispatcher dispatcher, float timestamp) throws OTMException {
        write(timestamp,null);
        dispatcher.register_event(new EventPoke(dispatcher,5,timestamp + outDt,this));
    }

}
