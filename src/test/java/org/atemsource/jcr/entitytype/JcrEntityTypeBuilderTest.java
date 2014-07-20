package org.atemsource.jcr.entitytype;

import javax.inject.Inject;

import junit.framework.Assert;

import org.atemsource.atem.api.attribute.CollectionSortType;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.EntityTypeBuilder;
import org.atemsource.jcr.JcrEntityTypeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@ContextConfiguration(locations = {"classpath:/test/atem/jcr/entitytype.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class JcrEntityTypeBuilderTest {
	
	@Inject
	private JcrEntityTypeRepository jcrRepository;

	@Test
	public void testPrimitives() {
		EntityTypeBuilder builder = jcrRepository.createBuilder("testPrmitive");
		builder.addSingleAttribute("string", String.class);
		EntityType<?> type = builder.createEntityType();
		Assert.assertEquals(4, type.getAttributes().size());
		Assert.assertEquals(String.class,type.getAttribute("string").getTargetType().getJavaType());
	}
	
	@Test
	public void testSingleAssociation() {
		EntityTypeBuilder embeddedBuilder = jcrRepository.createBuilder("embed");
		embeddedBuilder.addSingleAttribute("count", Integer.class);
		EntityType<?> embeddedType = embeddedBuilder.createEntityType();

		
		EntityTypeBuilder builder = jcrRepository.createBuilder("testAsoc");
		builder.addSingleAttribute("assoc",embeddedType);
		EntityType<?> type = builder.createEntityType();
		Assert.assertEquals(4, type.getAttributes().size());
		Assert.assertEquals(embeddedType,type.getAttribute("assoc").getTargetType());
	}
	
	@Test
	public void testMultiAssociation() {
		EntityTypeBuilder embeddedBuilder = jcrRepository.createBuilder("multiEmbed");
		embeddedBuilder.addSingleAttribute("count", Integer.class);
		EntityType<?> embeddedType = embeddedBuilder.createEntityType();

		
		EntityTypeBuilder builder = jcrRepository.createBuilder("testMultiAsoc");
		builder.addMultiAssociationAttribute("assoc",embeddedType,CollectionSortType.ORDERABLE);
		EntityType<?> type = builder.createEntityType();
		Assert.assertEquals(4, type.getAttributes().size());
		Assert.assertEquals(embeddedType,type.getAttribute("assoc").getTargetType());
	}
}
