package org.atemsource.jcr.entitytype;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;

import org.atemsource.atem.api.infrastructure.exception.TechnicalException;
import org.atemsource.atem.impl.common.attribute.PrimitiveAttributeImpl;

public class JcrPrimitiveAttribute<T> extends PrimitiveAttributeImpl<T> {

	@Override
	public boolean isWriteable() {
		return true;
	}

	private ValueConverter<T> valueConverter;

	public ValueConverter<T> getValueConverter() {
		return valueConverter;
	}

	public void setValueConverter(ValueConverter<T> valueConverter) {
		this.valueConverter = valueConverter;
	}

	@Override
	public T getValue(Object entity) {
		try {
			return valueConverter.convertFromValue(((Node) entity).getProperty(
					getCode()).getValue());
		} catch (PathNotFoundException e) {
			return null;
		} catch (Exception e) {
			throw new TechnicalException("cannot get value", e);
		}
	}

	@Override
	public void setValue(Object entity, T value) {

		try {
			Node node = (Node) entity;
			if (value == null) {
				if (node.hasProperty(getCode())) {
					node.getProperty(getCode()).remove();
				}
			} else {
				node.setProperty(getCode(),
						valueConverter.convertToValue(value));
			}
		} catch (Exception e) {
			throw new TechnicalException("cannot set value", e);
		}
	}

}
