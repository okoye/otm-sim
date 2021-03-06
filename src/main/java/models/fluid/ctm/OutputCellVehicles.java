package models.fluid.ctm;

import common.Link;
import error.OTMException;
import common.AbstractLaneGroup;
import models.fluid.AbstractFluidModel;
import models.fluid.FluidLaneGroup;
import output.AbstractOutputTimed;
import profiles.Profile1D;
import common.Scenario;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OutputCellVehicles extends AbstractOutputTimed {

    public AbstractFluidModel model;
    public ArrayList<FluidLaneGroup> ordered_lgs;               // An ordered map would be really helpful here
    public Map<Long, LaneGroupProfile> lgprofiles;

    //////////////////////////////////////////////////////
    // construction
    //////////////////////////////////////////////////////

    public OutputCellVehicles(Scenario scenario, AbstractFluidModel model, String prefix, String output_folder, Long commodity_id, Float outDt) throws OTMException {
        super(scenario, prefix, output_folder, commodity_id, outDt);
        this.model = model;
        ordered_lgs = new ArrayList<>();
        lgprofiles = new HashMap<>();
        for(Link link : model.links){
            for(AbstractLaneGroup lg : link.lanegroups_flwdn.values() ){
                ordered_lgs.add((FluidLaneGroup)lg);
                lgprofiles.put(lg.id, new LaneGroupProfile((FluidLaneGroup)lg));
            }
        }
    }

    //////////////////////////////////////////////////////
    // InterfaceOutput
    //////////////////////////////////////////////////////

    @Override
    public String get_output_file() {
        if(!write_to_file)
            return null;
        return  output_folder + File.separator + prefix + "_" +
                String.format("%.0f", outDt) + "_" +
                (commodity==null ? "g" : commodity.getId()) + "_" +
                model.name + "_cellveh.txt";
    }

    //////////////////////////////////////////////////////
    // AbstractOutputTimed
    //////////////////////////////////////////////////////

    @Override
    public void write(float timestamp) throws OTMException {
        super.write(timestamp);
        if(write_to_file){
            try {
                boolean isfirst=true;
                for(FluidLaneGroup lg : ordered_lgs){
                    for(int i=0;i<lg.cells.size();i++){
                        if(!isfirst)
                            writer.write(AbstractOutputTimed.delim);
                        isfirst = false;
                        writer.write(String.format("%f",get_value_for_cell(lg,i)));
                    }
                }
                writer.write("\n");
            } catch (IOException e) {
                throw new OTMException(e);
            }
        } else {
            throw new OTMException("Not implemented code: 09242je");
//            for(Long lg_id : ordered_lg_ids){
//                LaneGroupProfile lgProfile = lgprofiles.get(lg_id);
//                lgProfile.add_value(get_value_for_lanegroup(lg_id));
//            }
        }
    }

    //////////////////////////////////////////////////////
    // InterfacePlottable
    //////////////////////////////////////////////////////

    @Override
    public String get_yaxis_label() {
        return "veh";
    }

    @Override
    public void plot(String filename) throws OTMException {
        throw new OTMException("Plot not implemented for OutputCellVehicles output.");
    }

    //////////////////////////////////////////////////////
    // AbstractOutput
    //////////////////////////////////////////////////////

    @Override
    public void initialize(Scenario scenario) throws OTMException {
        super.initialize(scenario);

        if(write_to_file){
            try {
                String filename = get_output_file();
                if(filename!=null) {
                    String subfilename = filename.substring(0,filename.length()-4);
                    Writer cells_writer = new OutputStreamWriter(new FileOutputStream(subfilename + "_cells.txt"));
                    for(FluidLaneGroup lg: ordered_lgs)
                        for(int i=0;i<lg.cells.size();i++)
                            cells_writer.write(lg.id+" "+i+"\n");
                    cells_writer.close();
                }
            } catch (FileNotFoundException exc) {
                throw new OTMException(exc);
            } catch (IOException e) {
                throw new OTMException(e);
            }
        } else {
            throw new OTMException("Not implemented code: 09242je");
//            for(LaneGroupProfile lgProfile : lgprofiles.values())
//                lgProfile.initialize(outDt);
        }

    }


    //////////////////////////////////////////////////////
    // private
    //////////////////////////////////////////////////////

    private double get_value_for_cell(FluidLaneGroup lg, int i){
        return lg.cells.get(i).get_veh_for_commodity(commodity==null? null : commodity.getId());
    }

    //////////////////////////////////////////////////////
    // class
    //////////////////////////////////////////////////////

    public class LaneGroupProfile {
        public FluidLaneGroup lg;
        public ArrayList<Profile1D> cell_profile;
        public LaneGroupProfile(FluidLaneGroup lg){
            this.lg = lg;
        }
        public void initialize(float outDt){
            cell_profile = new ArrayList<>();
            lg.cells.forEach(x->cell_profile.add(new Profile1D(null,outDt)));
        }
        public void add_value(double value){
//            profile.add(value);
        }
    }

}
