package org.atemsource.jcr.entitytype;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.jackrabbit.commons.JcrUtils;
import org.atemsource.jcr.entitytype.converter.StringConverter;
import org.junit.Assert;
import org.junit.Test;

public class JcrPrimitiveAttributeTest extends AbstractJcrTest {

	@Test
	public void testSetGet() throws RepositoryException {
		Node node = JcrUtils.getOrCreateByPath("a", NodeType.NT_FOLDER,NodeType.NT_UNSTRUCTURED, session,true);
		JcrPrimitiveAttribute<String> attribute = new JcrPrimitiveAttribute<String>();
		attribute.setValueConverter(new StringConverter());
		attribute.setCode("text");
		attribute.setValue(node,"hallo");
		
		Assert.assertEquals("hallo", attribute.getValue(node));
	}
	

}
