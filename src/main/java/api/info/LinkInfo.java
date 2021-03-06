package api.info;

import common.*;

import java.util.ArrayList;

public class LinkInfo {

    /** The unique id for the link. */
    public long id;

    /** The length of the link in meters. */
    public double full_length;

    /** The number of lanes that extend the stay length of the link. */
    public double full_lanes;

    /** Id for the upstream node. */
    public double start_node_id;

    /** Id for the downstream node. */
    public double end_node_id;

    /** True if this is a source link. */
    public boolean is_source;

    /** True if this is a sink link */
    public boolean is_sink;

    /** Additional geometrical information for this link. */
    public RoadGeomInfo road_geom;

    /** Sequence of points tracing the shape of the link. */
    public ArrayList<PointInfo> shape = new ArrayList<>();

    /** List of lanegroups in this link. */
    public ArrayList<LaneGroupInfo> lanegroups = new ArrayList<>();

    /** Free flow speed in kph */
    public Float ffspeed_kph;

    /** Jam density in veh */
    public Float jam_density_vpkpl;

    /** Capacity in veh/sec */
    public Float capacity_vphpl;

    public LinkInfo(Link x){
        this.id = x.getId();
        this.full_length = x.length;
        this.start_node_id = x.start_node.getId();
        this.end_node_id = x.end_node.getId();
        this.is_sink = x.is_sink;
        this.is_source = x.is_source;
        this.full_lanes = x.full_lanes;
        this.road_geom = x.road_geom==null ? null : new RoadGeomInfo(x.road_geom);
        if(x.shape!=null)
            x.shape.forEach(p->shape.add(new PointInfo(p)));
        if(x.lanegroups_flwdn !=null)
            x.lanegroups_flwdn.values().forEach(lg->lanegroups.add(new LaneGroupInfo(lg)));
        this.ffspeed_kph = x.road_param.getSpeed();
        this.capacity_vphpl = x.road_param.getCapacity();
        this.jam_density_vpkpl = x.road_param.getJamDensity();
    }

    public long getId() {
        return id;
    }

    public double getFull_length() {
        return full_length;
    }

    public double getFull_lanes() {
        return full_lanes;
    }

    public double getStart_node_id() {
        return start_node_id;
    }

    public double getEnd_node_id() {
        return end_node_id;
    }

    public boolean isIs_source() {
        return is_source;
    }

    public boolean isIs_sink() {
        return is_sink;
    }

    public RoadGeomInfo getRoad_geom() {
        return road_geom;
    }

    public ArrayList<PointInfo> getShape() {
        return shape;
    }

    public ArrayList<LaneGroupInfo> getLanegroups() {
        return lanegroups;
    }

    public float get_ffspeed_kph(){ return ffspeed_kph; }

    public float get_capacity_vphpl(){ return capacity_vphpl; }

    public float get_jam_density_vpkpl(){ return jam_density_vpkpl; }

    @Override
    public String toString() {
        return "LinkInfo{" +
                "id=" + id +
                ", full_length=" + full_length +
                ", full_lanes=" + full_lanes +
                ", start_node_id=" + start_node_id +
                ", end_node_id=" + end_node_id +
                ", is_source=" + is_source +
                ", is_sink=" + is_sink +
                ", road_geom=" + road_geom +
                ", shape=" + shape +
                ", lanegroups=" + lanegroups +
                '}';
    }
}
