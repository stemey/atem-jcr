package org.atemsource.jcr.entitytype;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;


import org.apache.jackrabbit.commons.JcrUtils;
import org.junit.Assert;
import org.junit.Test;

public class SingleNodeAttributeTest extends AbstractJcrTest {

	@Test
	public void test() throws RepositoryException {
		Node node = JcrUtils.getOrCreateByPath("a", NodeType.NT_FOLDER,NodeType.NT_UNSTRUCTURED, session,true);
		SingleNodeAttribute singleNodeAttribute = new SingleNodeAttribute();
		singleNodeAttribute.setCode("elements");
		Node child = singleNodeAttribute.createTarget(null, node);
		
		Node value = singleNodeAttribute.getValue(node);
		Assert.assertEquals(child.getIdentifier(), value.getIdentifier());
		
		
		
	}
	
	@Test
	public void testNull() throws RepositoryException {
		Node node = JcrUtils.getOrCreateByPath("a", NodeType.NT_FOLDER,NodeType.NT_UNSTRUCTURED, session,true);
		SingleNodeAttribute singleNodeAttribute = new SingleNodeAttribute();
		singleNodeAttribute.setCode("elements");
		
		Node value = singleNodeAttribute.getValue(node);
		Assert.assertNull(value);
		
		
		
	}
}
