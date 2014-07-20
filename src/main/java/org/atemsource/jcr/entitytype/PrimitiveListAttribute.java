package org.atemsource.jcr.entitytype;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.atemsource.atem.api.attribute.CollectionAttribute;
import org.atemsource.atem.api.attribute.CollectionSortType;
import org.atemsource.atem.api.infrastructure.exception.TechnicalException;
import org.atemsource.atem.api.type.Type;
import org.atemsource.atem.impl.common.attribute.AbstractAttribute;

public class PrimitiveListAttribute<T, R> extends AbstractAttribute<T, R>
		implements CollectionAttribute<T, R> {

	private ValueConverter<T> valueConverter;

	public void setValueConverter(ValueConverter<T> valueConverter) {
		this.valueConverter = valueConverter;
	}

	@Override
	public Class<R> getAssociationType() {
		return (Class<R>) Object.class;
	}

	@Override
	public boolean isWriteable() {
		return true;
	}

	@Override
	public void setValue(Object entity, R value) {
		try {
			if (value == null) {
				Node node=(Node) entity;
				if (node.hasProperty(getCode())) {
					node.getProperty(getCode()).remove();
				}
		
			} else {
				T[] array = (T[]) value;
				Value[] values = new Value[array.length];
				int i = 0;
				for (T t : array) {
					values[i++] = valueConverter.convertToValue(t);
				}
				((Node) entity).setProperty(getCode(), values);
			}
		} catch (Exception e) {
			throw new TechnicalException("cannot set values", e);
		}
	}

	@Override
	public Class getReturnType() {
		return Object.class;
	}

	@Override
	public Type<T> getTargetType(T value) {
		return getTargetType();
	}

	@Override
	public Type<T> getTargetType() {
		return valueConverter.getType();
	}

	@Override
	public R getValue(Object entity) {
		Collection<T> elements = getElements(entity);
		T[] newArray = (T[]) Array.newInstance(getTargetType().getJavaType(),
				elements.size());
		return (R) elements.toArray(newArray);

	}

	private Value[] getValues(Object entity) {
		try {
			return ((Node) entity).getProperty(getCode()).getValues();
		} catch (Exception e) {
			throw new TechnicalException("cannot get value", e);
		}
	}

	@Override
	public void addElement(Object entity, T element) {
		try {
			Value[] values = getValues(entity);
			Value[] newValues = new Value[values.length + 1];
			for (int i = 0; i < values.length; i++) {
				newValues[i] = values[i];
			}
			newValues[newValues.length - 1] = valueConverter
					.convertToValue(element);
			((Node) entity).getProperty(getCode()).setValue(newValues);
		} catch (Exception e) {
			throw new TechnicalException("cannot add element", e);
		}
	}

	@Override
	public void clear(Object entity) {
		try {
			Value[] newValues = new Value[0];
			((Node) entity).getProperty(getCode()).setValue(newValues);
		} catch (Exception e) {
			throw new TechnicalException("cannot add element", e);
		}
	}

	@Override
	public boolean contains(Object entity, T element) {
		try {
			Value[] values = getValues(entity);
			for (Value value : values) {
				if (valueConverter.convertFromValue(value).equals(element)) {
					return true;
				}
			}
		} catch (Exception e) {
			throw new TechnicalException("cannot add element", e);
		}
		return false;
	}

	@Override
	public CollectionSortType getCollectionSortType() {
		return CollectionSortType.ORDERABLE;
	}

	@Override
	public Collection<T> getElements(Object entity) {
		try {
			List<T> list = new ArrayList<T>();
			Value[] values = getValues(entity);
			for (Value value : values) {
				list.add(valueConverter.convertFromValue(value));
			}
			return list;
		} catch (Exception e) {
			throw new TechnicalException("cannot add element", e);
		}
	}

	@Override
	public Iterator<T> getIterator(Object entity) {

		try {
			Property property = getOrCreateProperty(entity);
			return new ArrayIterator(property.getValues(), valueConverter);
		} catch (Exception e) {
			throw new TechnicalException("cannot create itertor", e);
		}
	}

	private Property getOrCreateProperty(Object entity) {
		Node node = (Node) entity;
		try {
			return node.getProperty(getCode());
		} catch (PathNotFoundException e) {
			try {
				return node.setProperty(getCode(), new String[0]);
			} catch (Exception e1) {
				throw new TechnicalException("cannot set property", e);
			}
		} catch (RepositoryException e) {
			throw new TechnicalException("cannot set property", e);
		}
	}

	@Override
	public int getSize(Object entity) {
		try {
			return getOrCreateProperty(entity).getValues().length;
		} catch (Exception e) {
			throw new TechnicalException("cannot get size", e);
		}
	}

	@Override
	public void removeElement(Object entity, T element) {
		try {
			Collection<T> elements = getElements(entity);
			elements.remove(element);
			Value[] values = new Value[elements.size()];
			int i = 0;
			for (T t : elements) {
				values[i++] = valueConverter.convertToValue(t);
			}
			((Node) entity).setProperty(getCode(), values);
		} catch (Exception e) {
			throw new TechnicalException("cannot add element", e);
		}
	}

}
