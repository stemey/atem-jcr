package org.atemsource.jcr.entitytype;

import javax.jcr.Node;
import javax.jcr.Repository;

import org.atemsource.atem.api.infrastructure.exception.TechnicalException;
import org.atemsource.atem.impl.common.AbstractEntityType;
import org.atemsource.atem.impl.json.TypeCodeConverter;

/**
 * 
 * needs to have parent and identifier.
 * 
 */
public class JcrEntityType extends AbstractEntityType<Node> {
	private TypeCodeConverter typeCodeConverter;

	private String typeProperty;

	public TypeCodeConverter getTypeCodeConverter() {
		return typeCodeConverter;
	}

	public String getTypeProperty() {
		return typeProperty;
	}

	public void setTypeCodeConverter(TypeCodeConverter typeCodeConverter) {
		this.typeCodeConverter = typeCodeConverter;
	}

	public void setTypeProperty(String typeProperty) {
		this.typeProperty = typeProperty;
	}

	@Override
	public Node createEntity() throws TechnicalException {
		throw new UnsupportedOperationException(
				"use CreationService to create jcr node");
	}

	@Override
	public Class<Node> getJavaType() {
		return Node.class;
	}

	@Override
	public boolean isEqual(Node a, Node b) {
		return a.equals(b);
	}

	@Override
	public boolean isInstance(Object value) {
		return Node.class.isInstance(value);
	}

}
