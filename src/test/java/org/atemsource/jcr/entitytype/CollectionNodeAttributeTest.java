package org.atemsource.jcr.entitytype;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;


import org.apache.jackrabbit.commons.JcrUtils;
import org.junit.Assert;
import org.junit.Test;

public class CollectionNodeAttributeTest extends AbstractJcrTest {

	@Test
	public void testAdd() throws RepositoryException {
		Node node = JcrUtils.getOrCreateByPath("a", NodeType.NT_FOLDER,NodeType.NT_UNSTRUCTURED, session,true);
		CollectionNodeAttribute nodeAttribute = new CollectionNodeAttribute();
		nodeAttribute.setCode("elements");
		Node child = nodeAttribute.createTarget(null, node);
		
		int size = nodeAttribute.getSize(node);
		Assert.assertEquals(1,size);
		Node child2 = nodeAttribute.createTarget(null, node);
		size = nodeAttribute.getSize(node);
		Assert.assertEquals(2,size);
		
		
		
	}
	
	@Test
	public void testRemove() throws RepositoryException {
		Node node = JcrUtils.getOrCreateByPath("a", NodeType.NT_FOLDER,NodeType.NT_UNSTRUCTURED, session,true);
		CollectionNodeAttribute nodeAttribute = new CollectionNodeAttribute();
		nodeAttribute.setCode("elements");
		Node child = nodeAttribute.createTarget(null, node);
		
		nodeAttribute.removeElement(node,child);
		Assert.assertEquals(0,nodeAttribute.getSize(node));
		
		
		
	}
	
	
}
