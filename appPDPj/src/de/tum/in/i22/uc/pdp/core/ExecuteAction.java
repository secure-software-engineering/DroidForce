package de.tum.in.i22.uc.pdp.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.pdp.core.shared.IPdpExecuteAction;
import de.tum.in.i22.uc.pdp.core.shared.Param;
import de.tum.in.i22.uc.pdp.xsd.ExecuteActionType;
import de.tum.in.i22.uc.pdp.xsd.ExecuteAsyncActionType;
import de.tum.in.i22.uc.pdp.xsd.ParameterType;

public class ExecuteAction implements Serializable, IPdpExecuteAction {
	private static final long serialVersionUID = 8451999937686098519L;
	private static Logger log = LoggerFactory.getLogger(ExecuteAction.class);

	private String name = null;
	private final Set<Param<?>> parameters = new HashSet<Param<?>>();
	private String processor = null;
	private String id = null;

	public ExecuteAction(String name, List<Param<?>> params) {
		this.name = name;
		this.parameters.addAll(params);
	}

	public ExecuteAction() {
	}

	public ExecuteAction(ExecuteActionType execAction) {
		log.debug("Preparing executeAction from ExecuteActionType");
		this.name = execAction.getName();
		this.id = execAction.getId();
		for (ParameterType param : execAction.getParameter()) {
			this.parameters.add(new Param<String>(param.getName(), param
					.getValue()));
		}
	}

	public ExecuteAction(ExecuteAsyncActionType execAction) {
		log.debug("Preparing executeAction from ExecuteAsyncActionType");
		this.name = execAction.getName();
		this.id = execAction.getId();
		this.processor = execAction.getProcessor().value();
		for (ParameterType param : execAction.getParameter()) {
			this.parameters.add(new Param<String>(param.getName(), param
					.getValue()));
		}
	}

	public String getName() {
		return name;
	}

	public Collection<Param<?>> getParams() {
		return parameters;
	}

	public Param<?> getParameterForName(String name) {
		for (Param<?> p : parameters) {
			if (p.getName().equalsIgnoreCase(name)) {
				return p;
			}
		}
		return null;
	}

	public String getProcessor() {
		return processor;
	}

	public String getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return com.google.common.base.Objects.toStringHelper(this)
				.add("name", name)
				.add("id", id)
				.add(processor, processor)
				.add("parameters", parameters)
				.toString();
	}
}
