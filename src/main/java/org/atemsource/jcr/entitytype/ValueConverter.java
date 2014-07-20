package org.atemsource.jcr.entitytype;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.atemsource.atem.api.type.PrimitiveType;

public abstract class ValueConverter<T> {
	
	public abstract T convertFromValue(Value value) throws ValueFormatException, IllegalStateException, RepositoryException;

	public abstract Value convertToValue(T value) throws ValueFormatException, IllegalStateException, RepositoryException;

	private PrimitiveType<T> type;

	public PrimitiveType<T> getType() {
		return type;
	}

	public void setType(PrimitiveType<T> type) {
		this.type = type;
	}
}
