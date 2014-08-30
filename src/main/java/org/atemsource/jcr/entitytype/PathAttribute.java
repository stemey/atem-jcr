package org.atemsource.jcr.entitytype;

import javax.jcr.Node;

import org.atemsource.atem.api.attribute.relation.SingleAttribute;
import org.atemsource.atem.api.infrastructure.exception.TechnicalException;
import org.atemsource.atem.impl.common.attribute.PrimitiveAttributeImpl;

public class PathAttribute extends PrimitiveAttributeImpl<String> implements SingleAttribute<String>{

	@Override
	public boolean isWriteable() {
		return true;
	}

	@Override
	public void setValue(Object entity, String value) {
		Node node=(Node) entity;
		try {
			if (value!=null && !node.getPath().equals(value)) {
				node.getSession().move(node.getPath(),value);
			}
		} catch (Exception e) {
			throw new TechnicalException("cannot move node to "+value);
		}
	}

}
