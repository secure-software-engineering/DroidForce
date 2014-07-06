package de.tum.in.i22.uc.pdp.core.shared;

import java.io.Serializable;
import java.util.Objects;

public class Param<T> implements Serializable {
	private static final long serialVersionUID = -7061921148298856812L;

	private final String name;
	private final T value;
	private final int type;

	public Param(String name, T value, int type) {
		if (name == null)
			throw new IllegalArgumentException("Name required");
		this.name = name;
		this.value = value;
		this.type = type;
	}

	public Param(String name, T value) {
		this(name, value, Constants.PARAMETER_TYPE_STRING);
	}

	public String getName() {
		return name;
	}
	public T getValue() {
		return value;
	}

	public int getType() {
		return type;
	}

	public static int getIdForName(String type) {
		if (type != null) {
			switch (type.toLowerCase()) {
				case "dataUsage":
					return Constants.PARAMETER_TYPE_DATAUSAGE;
				case "contUsage":
					return Constants.PARAMETER_TYPE_CONTUSAGE;
				case "data":
					return Constants.PARAMETER_TYPE_DATA;
				case "string":
					return Constants.PARAMETER_TYPE_STRING;
//				case "xpath":
//					return Constants.PARAMETER_TYPE_XPATH;
//				case "regex":
//					return Constants.PARAMETER_TYPE_REGEX;
//				case "context":
//					return Constants.PARAMETER_TYPE_CONTEXT;
//				case "binary":
//					return Constants.PARAMETER_TYPE_BINARY;
//				case "int":
//					return Constants.PARAMETER_TYPE_INT;
//				case "long":
//					return Constants.PARAMETER_TYPE_LONG;
//				case "bool":
//					return Constants.PARAMETER_TYPE_BOOL;
			}
		}
		return Constants.PARAMETER_TYPE_STRING;
	}

	@Override
	public String toString() {
		return name + ": " + value + " ("
				+ Constants.PARAMETER_TYPE_NAMES[type] + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Param) {
			Param<?> o = (Param<?>) obj;
			return Objects.equals(name, o.name)
					&& Objects.equals(value, o.value)
					&& Objects.equals(type, o.type);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type, value);
	}
}
