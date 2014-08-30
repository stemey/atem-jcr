package org.atemsource.jcr.entitytype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.atemsource.atem.api.attribute.AssociationAttribute;
import org.atemsource.atem.api.attribute.CollectionAttribute;
import org.atemsource.atem.api.attribute.CollectionSortType;
import org.atemsource.atem.api.infrastructure.exception.TechnicalException;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.impl.common.attribute.AbstractAttribute;

public class CollectionNodeAttribute extends AbstractAttribute<Node, Node>
		implements CollectionAttribute<Node, Node>,
		AssociationAttribute<Node, Node> {


	@Override
	public Class<Node> getAssociationType() {
		return Node.class;
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
		Node node = (Node) entity;
		try {
			return node.getNode(getCode());
		} catch (PathNotFoundException e) {
			return null;
		} catch (RepositoryException e) {
			throw new TechnicalException("cannot retrieve child nodes", e);
		}

	}

	@Override
	public void addElement(Object entity, Node element) {
		try {
			if (!element.getParent().getParent().getIdentifier().equals(((Node)entity).getIdentifier())) {
				throw new TechnicalException("cannot add child");
			}
		} catch (Exception e) {
			throw new TechnicalException("cannot get parent", e);
		}

	}

	@Override
	public void clear(Object entity) {
		try {
			Node value = getValue(entity);
			if (value == null) {
				((Node) entity).addNode(getCode());
			} else {
				NodeIterator nodes = getValue(entity).getNodes();
				for (; nodes.hasNext();) {
					nodes.nextNode().remove();
				}
			}
		} catch (Exception e) {
			throw new TechnicalException("cannot clear elements", e);
		}

	}

	@Override
	public boolean contains(Object entity, Node element) {
		return getElements(entity).contains(element);
	}

	@Override
	public CollectionSortType getCollectionSortType() {
		return CollectionSortType.ORDERABLE;
	}

	@Override
	public Collection<Node> getElements(Object entity) {
		List<Node> elements = new ArrayList<Node>();
		Iterator<Node> iterator = getIterator(entity);
		for (; iterator.hasNext();) {
			Node node = iterator.next();
			elements.add(node);
		}
		return elements;
	}

	@Override
	public Iterator<Node> getIterator(Object entity) {
		try {
			return getOrCreateValue(entity).getNodes();
		} catch (RepositoryException e) {
			throw new TechnicalException("cannot get elements", e);
		}
	}

	@Override
	public int getSize(Object entity) {
		try {
			NodeIterator nodes = getOrCreateValue(entity).getNodes();
			nodes.next();
			return new Long(nodes.getSize()).intValue();
		} catch (RepositoryException e) {
			throw new TechnicalException("cannot get size", e);
		} catch (NoSuchElementException e) {
			return 0;
		}
	}

	@Override
	public void removeElement(Object entity, Node element) {
		try {
			element.remove();
		} catch (Exception e) {
			throw new TechnicalException("cannot remove child");
		}
	}

	@Override
	public <T extends Node> T createTarget(EntityType<T> targetType,
			Object entity) {
		try {
			Node parent = getOrCreateValue(entity);
			long childId = parent.getProperty("childId").getLong();
			childId++;
			Node child = parent.addNode("child" + childId);
			parent.getProperty("childId").setValue(childId);
			return (T) child;
		} catch (Exception e) {
			throw new TechnicalException("cannot add child", e);
		}
	}

	private Node getOrCreateValue(Object entity) {
		Node node = (Node) entity;
		try {
			Node child = node.getNode(getCode());
			if (!child.hasProperty("childId")) {
				child.setProperty("childId",1L);
			}
			return child;
		} catch (PathNotFoundException e) {
			try {
				Node addNode = node.addNode(getCode());
				addNode.setProperty("childId", 1L);
				return addNode;
			} catch (Exception e1) {
				throw new TechnicalException("cannot add child", e);
			}
		} catch (RepositoryException e) {
			throw new TechnicalException("cannot retrieve child nodes", e);
		}
	}

}
