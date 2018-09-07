package commodity;

import common.AbstractLaneGroup;
import common.Link;
import error.OTMErrorLog;
import error.OTMException;
import output.InterfaceVehicleListener;
import runner.InterfaceScenarioElement;
import runner.Scenario;
import runner.ScenarioElementType;
import utils.OTMUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class Commodity implements InterfaceScenarioElement {

    protected final Long id;
    public final String name;
    public final Set<Subnetwork> subnetworks;
    public  Set<Link> all_links;
    public  Set<AbstractLaneGroup> all_lanegroups;
    public boolean pathfull;

    // this is a dispatch output writer for vehicles of this commodity
    public Set<InterfaceVehicleListener> vehicle_event_listeners;

    ///////////////////////////////////////////////////
    // construction
    ///////////////////////////////////////////////////

    public Commodity(long id, String name, List<Long> subnet_ids, Scenario scenario){
        this.id = id;
        this.name = name;
        this.subnetworks = new HashSet<>();
        this.all_lanegroups = new HashSet<>();
        this.all_links = new HashSet<>();
        if(subnet_ids!=null)
            for(Long subnet_id : subnet_ids){
                Subnetwork subnet = scenario.subnetworks.get(subnet_id);
                if(subnet!=null)
                    this.subnetworks.add(subnet);
                all_lanegroups.addAll(subnet.lanegroups);
                all_links.addAll(subnet.links);
            }
        this.vehicle_event_listeners = new HashSet<>();
        this.pathfull = false;
//        this.path = null;
    }

    public Commodity(jaxb.Commodity jaxb_comm, List<Long> subnet_ids, Scenario scenario) throws OTMException {
        this.id = jaxb_comm.getId();
        this.name = jaxb_comm.getName();
        this.pathfull = jaxb_comm.isPathfull();
        this.subnetworks = new HashSet<>();
        this.all_lanegroups = new HashSet<>();
        this.all_links = new HashSet<>();
        if(subnet_ids!=null)
            for(Long subnet_id : subnet_ids){
                Subnetwork subnet = scenario.subnetworks.get(subnet_id);
                if(subnet!=null) {
                    this.subnetworks.add(subnet);
                    all_links.addAll(subnet.links);
                    if(subnet.lanegroups!=null)
                        all_lanegroups.addAll(subnet.lanegroups);
                }
            }
        this.vehicle_event_listeners = new HashSet<>();
    }

    public void validate(OTMErrorLog errorLog){

        // pathfull commodities must have exactly one subnetwork
//        if(pathfull && subnetworks.size()!=1)
//            errorLog.addError("pathfull commodities must have exactly one subnetwork");


//        if(id<=0)
//            scenario.error_log.addError("id<=0 is prohibited for commodities");

//        // check that there is a split defined for me everywhere in my subnetwork
//        for(Link link : subnetwork.links){
//            Collection<Link> bla = link.end_node.out_links.values();
//            System.out.println("MISSING VALIDATION");
//        }

    }

    public void initialize() throws OTMException {
        for(Subnetwork subnetwork : subnetworks)
            for(Link link : subnetwork.links)
                if(link.model_type==Link.ModelType.ctm || link.model_type==Link.ModelType.mn)
                    ((models.ctm.LinkModel)link.model).register_commodity(this,subnetwork);
    }

    ///////////////////////////////////////////////////
    // get  / set
    ///////////////////////////////////////////////////

    public boolean travels_on_link(Link link){
        return all_links.contains(link);
    }

    public void add_vehicle_event_listener(InterfaceVehicleListener ev) {
        vehicle_event_listeners.add(ev);
    }

    public List<Long> get_subnetwork_ids(){
        return this.subnetworks.stream().map(x->x.id).collect(toList());
    }

    public Set<Subnetwork> get_subnetworks_for_link(Link link){
        Set<Subnetwork> x = new HashSet<>();

        // all links are members of the single subnetwork for pathless commodities
        if(!pathfull) {
            x.add(subnetworks.iterator().next());
            return x;
        }

        // otherwise check all subnetworks for this link
        for(Subnetwork subnetwork : subnetworks)
            if(subnetwork.links.contains(link))
                x.add(subnetwork);
        return x;
    }

    public Set<Subnetwork> get_subnetworks_for_lanegroup(AbstractLaneGroup lg){
        Link link = lg.link;
        Set<Long> next_link_ids = lg.get_dwn_links();
        Set<Subnetwork> x = new HashSet<>();
        for(Subnetwork subnetwork : subnetworks) {
            if (subnetwork.links.contains(link) && subnetwork.links.stream().anyMatch(z->next_link_ids.contains(z.getId())))
                x.add(subnetwork);
        }
        return x;
    }

    public String get_name(){
        return name;
    }

//    public Path get_path(){
//        return this.path;
//    }

    public jaxb.Commodity to_jaxb(){
        jaxb.Commodity jcomm = new jaxb.Commodity();
        jcomm.setId(getId());
        jcomm.setName(name);
        jcomm.setPathfull(pathfull);
        String str = OTMUtils.comma_format(subnetworks.stream().map(x->x.getId()).collect(Collectors.toList()));
        jcomm.setSubnetworks(str);
        return jcomm;
    }

    ////////////////////////////////////////////
    // InterfaceScenarioElement
    ///////////////////////////////////////////

    @Override
    public long getId() {
        return id;
    }

    @Override
    public ScenarioElementType getScenarioElementType() {
        return ScenarioElementType.commodity;
    }

}