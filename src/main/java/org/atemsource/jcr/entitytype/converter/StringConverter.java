package org.atemsource.jcr.entitytype.converter;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.atemsource.atem.impl.common.attribute.primitive.SimpleTextType;
import org.atemsource.jcr.entitytype.ValueConverter;

public class StringConverter extends ValueConverter<String>{

	private ValueFactory valueFactory= new ValueFactory();
	
	public StringConverter() {
		super();
		setType(new SimpleTextType());
	}

	@Override
	public String convertFromValue(Value value) throws ValueFormatException, IllegalStateException, RepositoryException {
		return value.getString();
	}

	@Override
	public Value convertToValue(String value) throws ValueFormatException,
			IllegalStateException, RepositoryException {
		return valueFactory.createValue(value);
	}

	

}
