package org.atemsource.jcr.entitytype;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.atemsource.atem.api.attribute.AssociationAttribute;
import org.atemsource.atem.api.attribute.relation.SingleAttribute;
import org.atemsource.atem.api.infrastructure.exception.TechnicalException;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.impl.common.attribute.AbstractAttribute;

public class SingleNodeAttribute extends AbstractAttribute<Node, Node>
		implements SingleAttribute<Node>, AssociationAttribute<Node, Node> {

	public SingleNodeAttribute() {
		super();
	}

	@Override
	public boolean isWriteable() {
		return true;
	}

	@Override
	public void setValue(Object entity, Node value) {
		try {
			
			Node node=(Node) entity;
			if (value == null) {
				if (node.hasNode(getCode())) {
					Node child = node.getNode(getCode());
					if (child != null) {
						child.remove();
					}
				}
			} else {
				if (!value.getParent().getIdentifier()
						.equals(node.getIdentifier())) {
					throw new UnsupportedOperationException(
							"cannot move child nodes like this");
				}
			}
		} catch (Exception e) {
			throw new TechnicalException("cannot check parent", e);
		}
	}

	@Override
	public Class<Node> getAssociationType() {
		return Node.class;
	}

	@Override
	public <T extends Node> T createTarget(EntityType<T> targetType,
			Object parent) {
		try {
			 T t=(T) ((Node) parent).addNode(getCode());
			  return t;
		} catch (Exception e) {
			throw new TechnicalException("cannot check parent", e);
		}
	}

	@Override
	public Class<Node> getReturnType() {
		return Node.class;
	}

	@Override
	public EntityType<Node> getTargetType(Node value) {
		return getTargetType();
	}

	@Override
	public EntityType<Node> getTargetType() {
		return (EntityType<Node>) super.getTargetType();
	}

	@Override
	public Node getValue(Object entity) {
		try {
			return ((Node) entity).getNode(getCode());
		} catch (PathNotFoundException e) {
			return null;
		} catch (Exception e) {
			throw new TechnicalException("cannot check parent", e);
		}
	}

}
