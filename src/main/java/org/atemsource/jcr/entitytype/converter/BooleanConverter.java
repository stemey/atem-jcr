package org.atemsource.jcr.entitytype.converter;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.atemsource.atem.impl.common.attribute.primitive.BooleanTypeImpl;
import org.atemsource.jcr.entitytype.ValueConverter;

public class BooleanConverter extends ValueConverter<Boolean>{

	private ValueFactory valueFactory= new ValueFactory();
	
	public BooleanConverter() {
		super();
		setType(new BooleanTypeImpl());
	}

	@Override
	public Boolean convertFromValue(Value value) throws ValueFormatException, IllegalStateException, RepositoryException {
		return value.getBoolean();
	}

	@Override
	public Value convertToValue(Boolean value) throws ValueFormatException,
			IllegalStateException, RepositoryException {
		return valueFactory.createValue(value);
	}

	

}
