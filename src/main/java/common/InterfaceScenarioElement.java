package common;

import dispatch.Dispatcher;
import error.OTMErrorLog;
import error.OTMException;

public interface InterfaceScenarioElement {

    Long getId();
    ScenarioElementType getSEType();
    void validate(OTMErrorLog errorLog);
    void initialize(Scenario scenario) throws OTMException;
    void register_with_dispatcher(Dispatcher dispatcher);
    Object to_jaxb();

}
