package org.atemsource.jcr.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.api.ContentRepository;
import org.apache.jackrabbit.oak.jcr.repository.RepositoryImpl;
import org.apache.jackrabbit.oak.plugins.index.property.PropertyIndexProvider;
import org.apache.jackrabbit.oak.plugins.name.NameValidatorProvider;
import org.apache.jackrabbit.oak.plugins.nodetype.write.InitialContent;
import org.apache.jackrabbit.oak.spi.security.OpenSecurityProvider;
import org.apache.jackrabbit.oak.spi.security.SecurityProvider;
import org.apache.jackrabbit.oak.spi.whiteboard.DefaultWhiteboard;
import org.atemsource.atem.api.EntityTypeRepository;
import org.atemsource.atem.api.attribute.relation.SingleAttribute;
import org.atemsource.atem.api.service.DeletionService;
import org.atemsource.atem.api.service.IdentityAttributeService;
import org.atemsource.atem.api.service.InsertionCallback;
import org.atemsource.atem.api.service.InsertionService;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.EntityTypeBuilder;
import org.atemsource.atem.impl.json.JsonEntityTypeRepository;
import org.atemsource.atem.service.entity.EntityRestService.Result;
import org.atemsource.atem.service.entity.FindByIdService;
import org.atemsource.atem.service.entity.FindByTypeService;
import org.atemsource.atem.service.entity.ListCallback;
import org.atemsource.atem.service.entity.ReturnErrorObject;
import org.atemsource.atem.service.entity.SingleCallback;
import org.atemsource.atem.service.entity.StatefulUpdateService;
import org.atemsource.atem.service.entity.UpdateCallback;
import org.atemsource.atem.service.entity.search.AttributePredicate;
import org.atemsource.atem.service.entity.search.Operator;
import org.atemsource.atem.service.entity.search.Paging;
import org.atemsource.atem.service.entity.search.Query;
import org.atemsource.atem.utility.transform.api.JacksonTransformationContext;
import org.atemsource.atem.utility.transform.api.JavaConverter;
import org.atemsource.atem.utility.transform.api.TransformationBuilderFactory;
import org.atemsource.atem.utility.transform.api.TransformationContext;
import org.atemsource.atem.utility.transform.api.TypeTransformationBuilder;
import org.atemsource.atem.utility.transform.impl.EntityTypeTransformation;
import org.atemsource.atem.utility.transform.service.CreationService;
import org.atemsource.jcr.JcrEntityTypeRepository;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@ContextConfiguration(locations = {"classpath:/test/atem/jcr/rest.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class JcrCrudServiceTest {
	
	private static RepositoryImpl repository;

	@Inject
	JcrEntityTypeRepository jcrRepository;
	
	@Inject
	JsonEntityTypeRepository jsonRepository;
	
	@Inject
	EntityTypeRepository entityTypeRepository;
	
	@Inject
	TransformationBuilderFactory transformationBuilderFactory;
	
	
	private ObjectMapper objectMapper= new ObjectMapper();

	private EntityTypeTransformation<Node, ObjectNode> transformation;
	
	private static int test;
	
	@BeforeClass
	public static void createRepository() {
		SecurityProvider securityProvider = new OpenSecurityProvider();
		Oak oak = new Oak().with(new InitialContent()) // add initial content
				.with(new NameValidatorProvider()) // allow only valid JCR names
				.with(securityProvider) // use the default security
				.with(new PropertyIndexProvider());
		ContentRepository contentRepository = oak // search support for the
													// indexes
				.createContentRepository();
		repository= new RepositoryImpl(contentRepository,
				new DefaultWhiteboard(), securityProvider, 12, null);

	}
	
	@After
	public void teardown() throws AccessDeniedException, VersionException, LockException, ConstraintViolationException, RepositoryException {
		Session session = repository.login("default");
		NodeIterator nodes = session.getRootNode().getNodes();
		for(;nodes.hasNext();) {
			nodes.nextNode().remove();
		}
	}
	
	@Before
	public  void setupClass() {
		test++;
		final EntityTypeBuilder builder = jcrRepository.createBuilder("create"+test);
		builder.addSingleAttribute("text", String.class);
		EntityType<Node> jcrType = (EntityType<Node>) builder.createEntityType();
		
		final EntityTypeBuilder targetBuilder = jsonRepository.createBuilder("target"+test);
		TypeTransformationBuilder transformationBuilder = (TypeTransformationBuilder) transformationBuilderFactory.create(jcrType, targetBuilder);
		
		transformationBuilder.transform().from("text").to("string");
		transformationBuilder.transform().from("identifier");
		transformationBuilder.transform().from("path");
		transformationBuilder.transform().from("template").convert(new JavaConverter<String,String>() {

			@Override
			public String convertAB(String a, TransformationContext ctx) {
				return targetBuilder.getReference().getCode();
			}

			@Override
			public String convertBA(String b, TransformationContext ctx) {
				return builder.getReference().getCode();
			}
		});
		
		
		transformation = transformationBuilder.buildTypeTransformation();
		
		((JcrCrudService)jcrType.getService(CreationService.class)).setRepository(repository);
		((JcrCrudService)jcrType.getService(CreationService.class)).setCredentials(null);
		
		
	}
	
	
	@Test
	public void create() throws RepositoryException {
		
		CreationService service = transformation.getEntityTypeA().getService(CreationService.class);
		
		Session session=((JcrCrudService)service).createSession();
		try {
		
		Node parent = JcrUtils.getOrCreateByPath("/a", NodeType.NT_FOLDER, NodeType.NT_UNSTRUCTURED, session, true);
		
		
		ObjectNode node = objectMapper.createObjectNode();
		node.put("path","/a/b2");
		node.put("string", "hallo");
		node.put("template", transformation.getEntityTypeB().getCode());
		
		
		Node newNode = (Node) service.create(transformation.getEntityTypeA(), transformation.getEntityTypeB(), node);
		newNode.setProperty("test", 33L);
		Assert.assertNotNull(newNode);
		Assert.assertEquals("/a/b2",newNode.getPath());
		Assert.assertNotNull(newNode.getIdentifier());
		
		
		} finally {
			((JcrCrudService)service).closeSession();
		}
	}
	
	
	@Test
	public void delete() throws RepositoryException {
		
		EntityType<Node> entityTypeA = transformation.getEntityTypeA();
		InsertionService service = entityTypeA.getService(InsertionService.class);
		
		
		
		
		final ObjectNode node = objectMapper.createObjectNode();
		node.put("path","/b");
		node.put("string", "hallo");
		node.put("template", transformation.getEntityTypeB().getCode());
		
		
		String id = insert(service, node);
		
		
		entityTypeA.getService(DeletionService.class).delete(entityTypeA, id);
		
		
		FindByIdService findById = entityTypeA.getService(FindByIdService.class);
		ObjectNode insertedNode = findById.findById(entityTypeA, id, new SingleCallback<Node,ObjectNode>() {

			@Override
			public ObjectNode process(Node entity) {
				return transformation.getAB().convert(entity, new JacksonTransformationContext(entityTypeRepository));
			}
			
		});
		Assert.assertNull(insertedNode);
		
	}
	
	@Test
	public void update() throws RepositoryException {
		
		EntityType<Node> entityTypeA = transformation.getEntityTypeA();
		InsertionService service = entityTypeA.getService(InsertionService.class);
		
		
		
		
		final ObjectNode node = objectMapper.createObjectNode();
		node.put("path","/b");
		node.put("string", "hallo");
		node.put("template", transformation.getEntityTypeB().getCode());
		
		
		String id = insert(service, node);
		
		Assert.assertEquals("/b", id);
		
		node.put("string", "hanswurst");
		
		StatefulUpdateService updateService = entityTypeA.getService(StatefulUpdateService.class);
		updateService.update(id, entityTypeA, new UpdateCallback<Node>() {

			@Override
			public ReturnErrorObject update(Node entity) {
				transformation.getBA().merge(node, entity, new JacksonTransformationContext(entityTypeRepository));
				return null;
			}
		});
		
		
		FindByIdService findById = entityTypeA.getService(FindByIdService.class);
		ObjectNode updateNode = findById.findById(entityTypeA, id, new SingleCallback<Node,ObjectNode>() {

			@Override
			public ObjectNode process(Node entity) {
				return transformation.getAB().convert(entity, new JacksonTransformationContext(entityTypeRepository));
			}
			
		});
		Assert.assertEquals("hanswurst",updateNode.get("string").getTextValue());
		
	}
	
	@Test
	public void move() throws RepositoryException {
		
		EntityType<Node> entityTypeA = transformation.getEntityTypeA();
		InsertionService service = entityTypeA.getService(InsertionService.class);
		
		
		
		
		final ObjectNode node = objectMapper.createObjectNode();
		node.put("path","/bb");
		node.put("string", "hallo");
		node.put("template", transformation.getEntityTypeB().getCode());
		
		
		String id = insert(service, node);
		
		node.put("string", "hanswurst");
		String newId = "/ba";
		node.put("path", newId);
		
		StatefulUpdateService updateService = entityTypeA.getService(StatefulUpdateService.class);
		updateService.update(id, entityTypeA, new UpdateCallback<Node>() {

			@Override
			public ReturnErrorObject update(Node entity) {
				transformation.getBA().merge(node, entity, new JacksonTransformationContext(entityTypeRepository));
				return null;
			}
		});
		
		
		FindByIdService findById = entityTypeA.getService(FindByIdService.class);
		ObjectNode updateNode = findById.findById(entityTypeA, newId, new SingleCallback<Node,ObjectNode>() {

			@Override
			public ObjectNode process(Node entity) {
				return transformation.getAB().convert(entity, new JacksonTransformationContext(entityTypeRepository));
			}
			
		});
		Assert.assertNotNull(updateNode);
		
	}
	

	
	@Test
	public void insert() throws RepositoryException {
		
		EntityType<Node> entityTypeA = transformation.getEntityTypeA();
		InsertionService service = entityTypeA.getService(InsertionService.class);
		
		
		
		
		final ObjectNode node = objectMapper.createObjectNode();
		node.put("path","/bc");
		node.put("string", "hallo");
		node.put("template", transformation.getEntityTypeB().getCode());
		
		
		String id = (String) service.insert(entityTypeA, new InsertionCallback<Node>() {

			@Override
			public Node get() {
				return transformation.getBA().convert(node, new JacksonTransformationContext(entityTypeRepository));
			}
		});
		Assert.assertNotNull(id);
		
		FindByIdService findById = entityTypeA.getService(FindByIdService.class);
		ObjectNode insertedNode = findById.findById(entityTypeA, id, new SingleCallback<Node,ObjectNode>() {

			@Override
			public ObjectNode process(Node entity) {
				return transformation.getAB().convert(entity, new JacksonTransformationContext(entityTypeRepository));
			}
			
		});
		Assert.assertNotNull(insertedNode);
		Assert.assertEquals("hallo",insertedNode.get("string").getTextValue());
		Assert.assertNotNull(insertedNode.get("template").getTextValue());
		
		
	}
	
	
	
	@Test
	public void findByType() throws RepositoryException {
		
		EntityType<Node> entityTypeA = transformation.getEntityTypeA();
		InsertionService service = entityTypeA.getService(InsertionService.class);
		
		

		final ObjectNode node2 = objectMapper.createObjectNode();
		node2.put("path","/b1");
		node2.put("string", "hallo");
		node2.put("template", transformation.getEntityTypeB().getCode());
		insert(service, node2);

		final ObjectNode node1 = objectMapper.createObjectNode();
		node1.put("path","/b2");
		node1.put("string", "hallo");
		node1.put("template", transformation.getEntityTypeB().getCode());
		insert(service, node1);

		
		final ObjectNode node = objectMapper.createObjectNode();
		node.put("path","/b3");
		node.put("string", "hallo");
		node.put("template", transformation.getEntityTypeB().getCode());
		insert(service, node);
		

		
		
		FindByTypeService finder = entityTypeA.getService(FindByTypeService.class);
		Result result = finder.getEntities(entityTypeA,null,null,new Paging(0,2), new ListCallback<Node>() {

			@Override
			public Result process(List<Node> entities, long totalCount) {
				Result result = new Result();
				result.totalCount=totalCount;
				
				result.entities=new ObjectMapper().createArrayNode();
				for (Node node:entities) {
					 ObjectNode json = transformation.getAB().convert(node, new JacksonTransformationContext(entityTypeRepository));
					 result.entities.add(json);
				}
				return result;
			}
		

			
		});
		Assert.assertNotNull(result);
		Assert.assertEquals(2,result.entities.size());
		
		
		
	}

	@Test
	public void findByTypeAndName() throws RepositoryException {
		
		EntityType<Node> entityTypeA = transformation.getEntityTypeA();
		InsertionService service = entityTypeA.getService(InsertionService.class);
		
		

		final ObjectNode node2 = objectMapper.createObjectNode();
		node2.put("path","/b1");
		node2.put("string", "byllo");
		node2.put("template", transformation.getEntityTypeB().getCode());
		insert(service, node2);

		final ObjectNode node1 = objectMapper.createObjectNode();
		node1.put("path","/b2");
		node1.put("string", "bye");
		node1.put("template", transformation.getEntityTypeB().getCode());
		insert(service, node1);

		
		final ObjectNode node = objectMapper.createObjectNode();
		node.put("path","/b3");
		node.put("string", "welcome");
		node.put("template", transformation.getEntityTypeB().getCode());
		insert(service, node);
		
		SingleAttribute<String> attribute = (SingleAttribute<String>) transformation.getEntityTypeA().getAttribute("text");
		
		Query query = new Query(true, new ArrayList<AttributePredicate<?>>());
		query.getPredicates().add(new AttributePredicate<String>(attribute, Operator.LIKE, "by%"));
		FindByTypeService finder = entityTypeA.getService(FindByTypeService.class);
		Result result = finder.getEntities(entityTypeA,query,null,null, new ListCallback<Node>() {

			@Override
			public Result process(List<Node> entities, long totalCount) {
				Result result = new Result();
				result.totalCount=totalCount;
				
				result.entities=new ObjectMapper().createArrayNode();
				for (Node node:entities) {
					 ObjectNode json = transformation.getAB().convert(node, new JacksonTransformationContext(entityTypeRepository));
					 result.entities.add(json);
				}
				return result;
			}
		

			
		});
		Assert.assertNotNull(result);
		Assert.assertEquals(2,result.entities.size());
		
		
		
	}

	
	@Test
	public void findByIn() throws RepositoryException {
		
		EntityType<Node> entityTypeA = transformation.getEntityTypeA();
		InsertionService service = entityTypeA.getService(InsertionService.class);
		
		

		final ObjectNode node2 = objectMapper.createObjectNode();
		node2.put("path","/b1");
		node2.put("string", "ballo");
		node2.put("template", transformation.getEntityTypeB().getCode());
		insert(service, node2);

		final ObjectNode node1 = objectMapper.createObjectNode();
		node1.put("path","/b2");
		node1.put("string", "bye");
		node1.put("template", transformation.getEntityTypeB().getCode());
		insert(service, node1);

		
		final ObjectNode node = objectMapper.createObjectNode();
		node.put("path","/b3");
		node.put("string", "welcome");
		node.put("template", transformation.getEntityTypeB().getCode());
		insert(service, node);
		
		SingleAttribute<String> attribute = (SingleAttribute<String>) transformation.getEntityTypeA().getAttribute("text");
		
		Query query = new Query(true, new ArrayList<AttributePredicate<?>>());
		query.getPredicates().add(new AttributePredicate<String>(attribute, Operator.IN, new String[]{"welcome","bye"}));
		FindByTypeService finder = entityTypeA.getService(FindByTypeService.class);
		Result result = finder.getEntities(entityTypeA,query,null,null, new ListCallback<Node>() {

			@Override
			public Result process(List<Node> entities, long totalCount) {
				Result result = new Result();
				result.totalCount=totalCount;
				
				result.entities=new ObjectMapper().createArrayNode();
				for (Node node:entities) {
					 ObjectNode json = transformation.getAB().convert(node, new JacksonTransformationContext(entityTypeRepository));
					 result.entities.add(json);
				}
				return result;
			}
		

			
		});
		Assert.assertNotNull(result);
		Assert.assertEquals(2,result.entities.size());
		
		
		
	}
	
	private String insert(
			InsertionService service, final ObjectNode node) {
		EntityType<Node> entityTypeA=transformation.getEntityTypeA();
		String id = (String) service.insert(entityTypeA, new InsertionCallback<Node>() {

			@Override
			public Node get() {
				return transformation.getBA().convert(node, new JacksonTransformationContext(entityTypeRepository));
			}
		});
		return id;
	}
	
	public void testIdentity() {
		SingleAttribute<? extends Serializable> idAttribute = transformation.getEntityTypeA().getService(IdentityAttributeService.class).getIdAttribute(transformation.getEntityTypeA());
		Assert.assertEquals("identifier", idAttribute.getCode());
	}
	
	

}
