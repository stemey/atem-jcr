package org.atemsource.jcr.entitytype;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.jackrabbit.commons.JcrUtils;
import org.atemsource.jcr.entitytype.converter.StringConverter;
import org.junit.Assert;
import org.junit.Test;

public class PrimitiveListAttributeTest extends AbstractJcrTest {

	@Test
	public void testSetGet() throws RepositoryException {
		Node node = JcrUtils.getOrCreateByPath("a", NodeType.NT_FOLDER,NodeType.NT_UNSTRUCTURED, session,true);
		PrimitiveListAttribute<String,String[]> attribute = new PrimitiveListAttribute<String, String[]>();
		attribute.setValueConverter(new StringConverter());
		attribute.setCode("strings");
		attribute.setValue(node,new String[]{"hallo"});
		
		Assert.assertEquals("hallo", attribute.getValue(node)[0]);
	}
	
	@Test
	public void testAdd() throws RepositoryException {
		Node node = JcrUtils.getOrCreateByPath("a", NodeType.NT_FOLDER,NodeType.NT_UNSTRUCTURED, session,true);
		PrimitiveListAttribute<String,String[]> attribute = new PrimitiveListAttribute<String, String[]>();
		attribute.setValueConverter(new StringConverter());
		attribute.setCode("strings");
		attribute.setValue(node,new String[]{"hallo"});
		
		attribute.addElement(node, "bye");
		
		Assert.assertEquals("hallo", attribute.getValue(node)[0]);
		Assert.assertEquals("bye", attribute.getValue(node)[1]);
		
		Assert.assertEquals(2, attribute.getSize(node));
		Assert.assertTrue( attribute.contains(node,"bye"));
		
	}
	@Test
	public void testRemove() throws RepositoryException {
		Node node = JcrUtils.getOrCreateByPath("a", NodeType.NT_FOLDER,NodeType.NT_UNSTRUCTURED, session,true);
		PrimitiveListAttribute<String,String[]> attribute = new PrimitiveListAttribute<String, String[]>();
		attribute.setValueConverter(new StringConverter());
		attribute.setCode("strings");
		attribute.setValue(node,new String[]{"hallo","bye"});
		
		attribute.removeElement(node, "hallo");
		
		Assert.assertEquals("bye", attribute.getValue(node)[0]);
	}
	
	@Test
	public void testClear() throws RepositoryException {
		Node node = JcrUtils.getOrCreateByPath("a", NodeType.NT_FOLDER,NodeType.NT_UNSTRUCTURED, session,true);
		PrimitiveListAttribute<String,String[]> attribute = new PrimitiveListAttribute<String, String[]>();
		attribute.setValueConverter(new StringConverter());
		attribute.setCode("strings");
		attribute.setValue(node,new String[]{"hallo","bye"});
		Assert.assertEquals(2, attribute.getSize(node));
		
		attribute.clear(node);
		Assert.assertEquals(0, attribute.getSize(node));
	}
	
	@Test
	public void testGetIterator() throws RepositoryException {
		Node node = JcrUtils.getOrCreateByPath("a", NodeType.NT_FOLDER,NodeType.NT_UNSTRUCTURED, session,true);
		PrimitiveListAttribute<String,String[]> attribute = new PrimitiveListAttribute<String, String[]>();
		attribute.setValueConverter(new StringConverter());
		attribute.setCode("strings");
		attribute.setValue(node,new String[]{"hallo","bye"});
		Iterator<String> iterator = attribute.getIterator(node);
		Assert.assertEquals("hallo",iterator.next());
		Assert.assertEquals("bye",iterator.next());
		Assert.assertFalse(iterator.hasNext());
	}
	
}
