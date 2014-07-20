package org.atemsource.jcr.entitytype.converter;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.atemsource.atem.impl.common.attribute.primitive.LongType;
import org.atemsource.jcr.entitytype.ValueConverter;

public class LongConverter extends ValueConverter<Long>{

	private ValueFactory valueFactory= new ValueFactory();
	
	public LongConverter() {
		super();
		setType(new LongType(true));
	}

	@Override
	public Long convertFromValue(Value value) throws ValueFormatException, IllegalStateException, RepositoryException {
		return value.getLong();
	}

	@Override
	public Value convertToValue(Long value) throws ValueFormatException,
			IllegalStateException, RepositoryException {
		return valueFactory.createValue(value);
	}

	

}
