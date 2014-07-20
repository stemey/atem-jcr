package org.atemsource.jcr.entitytype;

import java.util.Iterator;

import javax.jcr.Value;

public class ArrayIterator<T> implements Iterator<T> {

	
	private Value[] values;
	private ValueConverter<T> valueConverter;
	int index=0;
	@Override
	public boolean hasNext() {
		return index<values.length;
	}

	@Override
	public T next() {
		try {
			return valueConverter.convertFromValue(values[index++]);
		} catch (Exception e) {
			throw new RuntimeException("cannot get next element",e);
		}
		
	}


	public ArrayIterator(Value[] values, ValueConverter<T> valueConverter) {
		super();
		this.values = values;
		this.valueConverter = valueConverter;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("cannot remove on this iterator");
	}

}
