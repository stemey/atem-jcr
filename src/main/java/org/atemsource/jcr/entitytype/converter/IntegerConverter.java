package org.atemsource.jcr.entitytype.converter;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.atemsource.atem.impl.common.attribute.primitive.IntegerType;
import org.atemsource.jcr.entitytype.ValueConverter;

public class IntegerConverter extends ValueConverter<Integer>{

	private ValueFactory valueFactory= new ValueFactory();
	
	public IntegerConverter() {
		super();
		setType(new IntegerType(true));
	}

	@Override
	public Integer convertFromValue(Value value) throws ValueFormatException, IllegalStateException, RepositoryException {
		return new Long(value.getLong()).intValue();
	}

	@Override
	public Value convertToValue(Integer value) throws ValueFormatException,
			IllegalStateException, RepositoryException {
		Long longValue=new Long(value);
		return valueFactory.createValue(value);
	}

	

}
