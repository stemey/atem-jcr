package org.atemsource.jcr.entitytype.converter;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.atemsource.atem.impl.common.attribute.primitive.DoubleType;
import org.atemsource.jcr.entitytype.ValueConverter;

public class DoubleConverter extends ValueConverter<Double>{

	private ValueFactory valueFactory= new ValueFactory();
	
	public DoubleConverter() {
		super();
		setType(new DoubleType());
	}

	@Override
	public Double convertFromValue(Value value) throws ValueFormatException, IllegalStateException, RepositoryException {
		return value.getDouble();
	}

	@Override
	public Value convertToValue(Double value) throws ValueFormatException,
			IllegalStateException, RepositoryException {
		return valueFactory.createValue(value);
	}

	

}
