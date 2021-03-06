package common;

import actuator.AbstractActuator;
import actuator.AbstractActuatorLanegroupCapacity;
import actuator.InterfaceActuatorTarget;
import error.OTMException;
import geometry.FlowPosition;
import geometry.Side;
import keys.KeyCommPathOrLink;
import packet.StateContainer;
import traveltime.AbstractLaneGroupTimer;
import utils.OTMUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public abstract class AbstractLaneGroup implements Comparable<AbstractLaneGroup>, InterfaceLaneGroup, InterfaceActuatorTarget {

    public final long id;
    public Link link;
    public final Side side;               // inner, stay, or outer
    public final FlowPosition flwpos;
    public int start_lane_up = -1;       // counted with respect to upstream boundary
    public int start_lane_dn = -1;       // counted with respect to downstream boundary
    public final int num_lanes;
    public float length;        // [m]

//    public double max_vehicles;
//    public double max_cong_speed_kph;

    public AbstractLaneGroup neighbor_in;       // lanegroup down and in
    public AbstractLaneGroup neighbor_out;      // lanegroup down and out
    public AbstractLaneGroup neighbor_up_in;    // lanegroup up and in (stay lanes only)
    public AbstractLaneGroup neighbor_up_out;   // lanegroup up and out (stay lanes only)

    // set of keys for states in this lanegroup
    public Set<KeyCommPathOrLink> states;   // TODO MOVE THIS TO DISCRETE TIME ONLY?

    public StateContainer buffer;

    protected double supply;       // [veh]

    public AbstractActuatorLanegroupCapacity actuator_capacity;

    // flow accumulator
    public FlowAccumulatorState flw_acc;

//    // map from outlink to road-connection. For one-to-one links with no road connection defined,
//    // this returns a null.
//    public Map<Long, RoadConnection> outlink2roadconnection;

    // state to the road connection it must use (should be avoided in the one-to-one case)
    public Map<KeyCommPathOrLink,Long> state2roadconnection;

    // target lane group to direction
    public Map<KeyCommPathOrLink,Side> state2lanechangedirection = new HashMap<>();

    public AbstractLaneGroupTimer travel_timer;

    ///////////////////////////////////////////////////
    // construction
    ///////////////////////////////////////////////////

    public AbstractLaneGroup(Link link, Side side, FlowPosition flwpos, float length, int num_lanes, int start_lane, Set<RoadConnection> out_rcs){
        this.link = link;
        this.side = side;
        this.flwpos = flwpos;
        this.length = length;
        this.num_lanes = num_lanes;
        this.id = OTMUtils.get_lanegroup_id();
        this.states = new HashSet<>();
        switch(flwpos){
            case up:
                this.start_lane_up = start_lane;
                break;
            case dn:
                this.start_lane_dn = start_lane;
                break;
        }
        this.state2roadconnection = new HashMap<>();

        // barriers
//        in_barriers
//                out_barriers

    }

    ///////////////////////////////////////////////////
    // InterfaceActuatorTarget
    ///////////////////////////////////////////////////

    @Override
    public void register_actuator(AbstractActuator act) throws OTMException {
        if(act instanceof AbstractActuatorLanegroupCapacity){
            if(this.actuator_capacity!=null)
                throw new OTMException(String.format("Multiple capacity actuators on link %d, lanes %d through %d.",link.getId(),start_lane_dn,start_lane_dn+num_lanes-1));
            this.actuator_capacity = (AbstractActuatorLanegroupCapacity) act;
        }
    }

    @Override
    public long getIdAsTarget() {
        return id;
    }

    ///////////////////////////////////////////////////
    // Comparable
    ///////////////////////////////////////////////////

    @Override
    public final int compareTo(AbstractLaneGroup that) {

        int this_start = this.start_lane_up;
        int that_start = that.start_lane_up;
        if(this_start < that_start)
            return -1;
        if(that_start < this_start)
            return 1;

        int this_end = this.start_lane_up + this.num_lanes;
        int that_end = that.start_lane_up + that.num_lanes;
        if(this_end < that_end)
            return -1;
        if(that_end < this_end)
            return 1;

        System.err.println("WARNING!! FOUND EQUAL LANE GROUPS IN COMPARE TO.");
        return 0;
    }

    ///////////////////////////////////////////////////
    // overridable
    ///////////////////////////////////////////////////

    public void delete(){
        link = null;
        actuator_capacity = null;
        flw_acc = null;
    }

    public void initialize(Scenario scenario) throws OTMException {

        if(link.is_model_source_link)
            this.buffer = new StateContainer();

        if(flw_acc!=null)
            flw_acc.reset();
    }

//    public void set_road_params(jaxb.Roadparam r){
//        this.max_vehicles =  r.getJamDensity() * (length/1000.0) * num_lanes;
//    }

    ///////////////////////////////////////////////////
    // final
    ///////////////////////////////////////////////////

    public final FlowAccumulatorState request_flow_accumulator(Long comm_id){
        if(flw_acc==null)
            flw_acc = new FlowAccumulatorState();
        for(KeyCommPathOrLink key : states)
                if(comm_id==null || key.commodity_id==comm_id)
                    flw_acc.add_key(key);
        return flw_acc;
    }

    public final void add_state(long comm_id, Long path_id,Long next_link_id, boolean ispathfull) throws OTMException {

        KeyCommPathOrLink state = ispathfull ?
                new KeyCommPathOrLink(comm_id, path_id, true) :
                new KeyCommPathOrLink(comm_id, next_link_id, false);

        states.add(state);

        // state2roadconnection
        // state2lanechangedirection
        if(link.is_sink){
            state2roadconnection.put(state,null);
            state2lanechangedirection.put(state, Side.middle);
        } else {

            // store in map
            RoadConnection rc = link.outlink2roadconnection.get(next_link_id);
            if(rc!=null) {

                // state2roadconnection
                state2roadconnection.put(state, rc.getId());

                // state2lanechangedirection
                Set<AbstractLaneGroup> target_lgs = rc.in_lanegroups;
                Set<Side> sides = target_lgs.stream().map(x -> x.get_side_with_respect_to_lg(this)).collect(Collectors.toSet());

                if(sides.contains(Side.middle))
                    state2lanechangedirection.put(state, Side.middle);
                else {
                    if (sides.size() != 1)
                        throw new OTMException("asd;liqwr g-q4iwq jg");
                    state2lanechangedirection.put(state, sides.iterator().next());
                }
            }
        }

    }

    public final float get_total_vehicles() {
        return vehs_dwn_for_comm(null);
    }

    public final double get_supply(){
        return supply;
    }

    public final double get_supply_per_lane() {
        return supply/num_lanes;
    }

//    public final float get_space() {
//        return max_vehicles- vehs_dwn_for_comm(null);
//    }

    public final List<Integer> get_dn_lanes(){
        return IntStream.range(start_lane_dn,start_lane_dn+num_lanes).boxed().collect(toList());
    }

    public final List<Integer> get_up_lanes(){
        return IntStream.range(start_lane_up,start_lane_up+num_lanes).boxed().collect(toList());
    }

    public final Side get_side_with_respect_to_lg(AbstractLaneGroup lg){

        // This is more complicated with up addlanes
        assert(lg.flwpos == FlowPosition.dn);
        assert(this.flwpos == FlowPosition.dn);

        if(!this.link.getId().equals(link.getId()))
            return null;

        if(this==lg)
            return Side.middle;

        if (this.start_lane_dn < lg.start_lane_dn)
            return Side.in;
        else
            return Side.out;
    }

    public final Set<Link> get_out_links(){
        return link.end_node.out_links;
    }

    public final boolean link_is_link_reachable(Long link_id){
        return link.outlink2roadconnection.containsKey(link_id);
    }

    public final void update_flow_accummulators(KeyCommPathOrLink key,double num_vehicles){
        if(flw_acc!=null)
            flw_acc.increment(key,num_vehicles);
    }

    public final RoadConnection get_target_road_connection_for_state(KeyCommPathOrLink key){
        Long outlink_id = key.isPath ? link.path2outlink.get(key.pathOrlink_id).getId() : key.pathOrlink_id;
        return link.outlink2roadconnection.get(outlink_id);
    }

    ///////////////////////////////////////////////////
    // toString
    ///////////////////////////////////////////////////

    @Override
    public String toString() {
        return String.format("link %d, lg %d, lanes %d, start_dn %d, start_up %d",link.getId(),id,num_lanes,start_lane_dn,start_lane_up);
    }

}
