package output;

import common.Scenario;
import error.OTMException;
import models.fluid.FluidLaneGroup;

import java.util.Collection;

public class OutputCellVehicles extends AbstractOutputTimedCell {

    //////////////////////////////////////////////////////
    // construction
    //////////////////////////////////////////////////////

    public OutputCellVehicles(Scenario scenario, String prefix, String output_folder, Long commodity_id, Collection<Long> link_ids, Float outDt) throws OTMException {
        super(scenario,prefix,output_folder,commodity_id,link_ids,outDt);
        this.type = Type.cell_veh;
    }

    //////////////////////////////////////////////////////
    // InterfaceOutput
    //////////////////////////////////////////////////////

    @Override
    public String get_output_file() {
        if(!write_to_file)
            return null;
        if(commodity==null)
            return String.format("%s_cell_veh.txt",super.get_output_file());
        else
            return String.format("%s_cell_comm%d_veh.txt",super.get_output_file(),commodity.getId());
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
        throw new OTMException("Plot not implemented for Cell output.");
    }

    //////////////////////////////////////////////////////
    // AbstractOutputTimedLanegroup
    //////////////////////////////////////////////////////

    @Override
    protected Double[] get_value_for_lanegroup(FluidLaneGroup lg){
        Double [] X = new Double[lg.cells.size()];
        for(int i=0;i<lg.cells.size();i++)
            X[i] = lg.cells.get(i).get_veh_for_commodity(commodity == null ? null : commodity.getId());
        return X;
    }

}
