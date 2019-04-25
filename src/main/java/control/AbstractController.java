/**
 * Copyright (c) 2018, Gabriel Gomes
 * All rights reserved.
 * This source code is licensed under the standard 3-clause BSD license found
 * in the LICENSE file in the root directory of this source tree.
 */
package control;

import actuator.AbstractActuator;
import dispatch.Dispatcher;
import dispatch.EventPoke;
import dispatch.InterfacePokable;
import error.OTMErrorLog;
import error.OTMException;
import output.EventsController;
import runner.InterfaceScenarioElement;
import runner.Scenario;
import runner.ScenarioElementType;
import sensor.AbstractSensor;
import utils.OTMUtils;

import java.util.*;

public abstract class AbstractController implements InterfacePokable, InterfaceScenarioElement {

    public enum Algorithm {
        sig_pretimed,
        alinea,
        capacity,
        plugin
    }

    public static final Map<Algorithm, AbstractActuator.Type> map_algorithm_actuator = new HashMap<>();
    static {
        map_algorithm_actuator.put( Algorithm.sig_pretimed  , AbstractActuator.Type.signal );
        map_algorithm_actuator.put( Algorithm.alinea  , AbstractActuator.Type.capacity );
        map_algorithm_actuator.put( Algorithm.capacity  , AbstractActuator.Type.capacity );
    }

    public long id;
    public Algorithm type;
    public float dt;

    public Map<Long,AbstractActuator> actuators;
    public Map<String,AbstractActuator> actuator_by_usage;

    public Set<AbstractSensor> sensors;
    public Map<String,AbstractSensor> sensor_by_usage;

    public EventsController event_listener;

    public Map<Long,Object> command;    // actuator id -> command

    ///////////////////////////////////////////////////
    // construction
    ///////////////////////////////////////////////////

    public AbstractController(Scenario scenario, jaxb.Controller jaxb_controller) throws OTMException {
        this.id = jaxb_controller.getId();

        String controller_type = jaxb_controller.getType();
        this.type = is_inbuilt(controller_type) ? Algorithm.valueOf(controller_type) : Algorithm.plugin;
        this.dt = jaxb_controller.getDt();

        // below this does not apply for scenario-less controllers  ..............................
        if(scenario==null)
            return;

        // read actuators ..............................................................
        actuators = new HashMap<>();
        actuator_by_usage = new HashMap<>();
        if(jaxb_controller.getTargetActuators()!=null){

            // read usage-less
            if(!jaxb_controller.getTargetActuators().getIds().isEmpty()){
                List<Long> ids = OTMUtils.csv2longlist(jaxb_controller.getTargetActuators().getIds());
                Iterator it = ids.iterator();
                while(it.hasNext()){
                    AbstractActuator act = (AbstractActuator) scenario.get_element(ScenarioElementType.actuator,(Long) it.next());
                    if(act==null)
                        throw new OTMException("Bad actuator id in controller");
                    if(act.myController!=null)
                        throw new OTMException("Multiple controllers assigned to single actuator");
                    actuators.put(act.id,act);
                    act.myController=this;
                }
            }

            // read actuators with usage
            for(jaxb.TargetActuator jaxb_act : jaxb_controller.getTargetActuators().getTargetActuator()){
                AbstractActuator act = (AbstractActuator) scenario.get_element(ScenarioElementType.actuator,jaxb_act.getId());
                if(act==null)
                    throw new OTMException("Bad actuator id in controller");
                if(act.myController!=null)
                    throw new OTMException("Multiple controllers assigned to single actuator");
                actuators.put(act.id,act);
                if(actuator_by_usage.containsKey(jaxb_act.getUsage()))
                    throw new OTMException("Repeated value in actuator usage for controller " +this.id);
                actuator_by_usage.put(jaxb_act.getUsage(),act);
                act.myController=this;
            }
        }

        this.command = new HashMap<>();
        for(AbstractActuator act : actuators.values())
            command.put(act.id,null);

        // read sensors ..............................................................
        sensors = new HashSet<>();
        sensor_by_usage = new HashMap<>();
        if(jaxb_controller.getFeedbackSensors()!=null){

            // read usage-less
            if(!jaxb_controller.getFeedbackSensors().getIds().isEmpty()){
                List<Long> ids = OTMUtils.csv2longlist(jaxb_controller.getFeedbackSensors().getIds());
                Iterator it = ids.iterator();
                while(it.hasNext()){
                    AbstractSensor sensor = (AbstractSensor) scenario.get_element(ScenarioElementType.sensor,(Long) it.next());
                    if(sensor==null)
                        throw new OTMException("Bad sensor id in controller");
                    sensors.add(sensor);
                }
            }

            // read actuators with usage
            for(jaxb.FeedbackSensor jaxb_sensor : jaxb_controller.getFeedbackSensors().getFeedbackSensor()){
                AbstractSensor sensor = (AbstractSensor) scenario.get_element(ScenarioElementType.sensor,jaxb_sensor.getId());
                if(sensor==null)
                    throw new OTMException("Bad sensor id in controller");
                sensors.add(sensor);
                sensor_by_usage.put(jaxb_sensor.getUsage(),sensor);
            }
        }

    }

    public void validate(OTMErrorLog errorLog){
        if(type==null)
            errorLog.addError("myType==null");
        if(actuators.isEmpty())
            errorLog.addError("actuators.isEmpty()");
    }

    abstract public void initialize(Scenario scenario,float now) throws OTMException;

    public void register_with_dispatcher(Dispatcher dispatcher){
        dispatcher.register_event(new EventPoke(dispatcher,2,dispatcher.current_time,this));
    }

    ///////////////////////////////////////////////////
    // listeners
    ///////////////////////////////////////////////////

    public void set_event_listener(EventsController e) throws OTMException {
        if(event_listener !=null)
            throw new OTMException("multiple listeners for commodity");
        event_listener = e;
    }

    ///////////////////////////////////////////////////
    // getters
    ///////////////////////////////////////////////////

    public boolean is_inbuilt(String name){
        for (Algorithm me : Algorithm.values()) {
            if (me.name().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    public Object get_command_for_actuator_id(Long act_id){
        return command.get(act_id);
    }

    public final Object get_command_for_actuator_usage(String act_usage){
        if(!actuator_by_usage.containsKey(act_usage))
            return null;
        return get_command_for_actuator_id(actuator_by_usage.get(act_usage).getId());
    }

    ///////////////////////////////////////////////////
    // update
    ///////////////////////////////////////////////////

    abstract public void update_controller(Dispatcher dispatcher, float timestamp) throws OTMException;

    @Override
    public void poke(Dispatcher dispatcher, float timestamp) throws OTMException  {
        update_controller(dispatcher,timestamp);

        // wake up in dt, if dt is defined
        if(dt >0)
            dispatcher.register_event(new EventPoke(dispatcher,2,timestamp+ dt,this));
    }

    ////////////////////////////////////////////
    // InterfaceScenarioElement
    ///////////////////////////////////////////

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public ScenarioElementType getScenarioElementType() {
        return ScenarioElementType.controller;
    }

}
