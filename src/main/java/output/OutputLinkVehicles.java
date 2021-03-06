package output;

import common.Link;
import error.OTMException;
import profiles.Profile1D;
import common.Scenario;

import java.util.Collection;
import java.util.List;

public class OutputLinkVehicles extends AbstractOutputTimedLink {

    //////////////////////////////////////////////////////
    // construction
    //////////////////////////////////////////////////////

    public OutputLinkVehicles(Scenario scenario, String prefix, String output_folder, Long commodity_id, Collection<Long> link_ids, Float outDt) throws OTMException {
        super(scenario,prefix,output_folder,commodity_id,link_ids,outDt);
        this.type = Type.link_veh;
    }

    //////////////////////////////////////////////////////
    // InterfaceOutput
    //////////////////////////////////////////////////////

    @Override
    public String get_output_file() {
        if(!write_to_file)
            return null;
        if(commodity==null)
            return String.format("%s_link_veh.txt",super.get_output_file());
        else
            return String.format("%s_link_comm%d_veh.txt",super.get_output_file(),commodity.getId());
    }

    //////////////////////////////////////////////////////
    // AbstractOutputTimed
    //////////////////////////////////////////////////////

    @Override
    public String get_yaxis_label() {
        return "vehicles [veh]";
    }

    //////////////////////////////////////////////////////
    // AbstractOutputTimedLink
    //////////////////////////////////////////////////////

    @Override
    public double get_value_for_link(Long link_id) {
        if(!linkprofiles.containsKey(link_id))
            return Double.NaN;
        Link link = linkprofiles.get(link_id).link;
        return link.get_veh_for_commodity(commodity==null?null:commodity.getId());
    }

    //////////////////////////////////////////////////////
    // final
    //////////////////////////////////////////////////////

    public final List<Double> get_density_for_link_in_vpk(Long link_id){
        if(!linkprofiles.containsKey(link_id))
            return null;
        Profile1D profile = linkprofiles.get(link_id).profile.clone();
        profile.multiply(1000d/linkprofiles.get(link_id).link.length);
        return profile.get_values();
    }

}
