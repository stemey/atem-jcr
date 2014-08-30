/*******************************************************************************
 * Stefan Meyer, 2012 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/
package org.atemsource.jcr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;

import org.apache.log4j.Logger;
import org.atemsource.atem.api.BeanLocator;
import org.atemsource.atem.api.EntityTypeRepository;
import org.atemsource.atem.api.infrastructure.exception.TechnicalException;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.EntityTypeBuilder;
import org.atemsource.atem.api.type.IncomingRelation;
import org.atemsource.atem.impl.common.AbstractEntityType;
import org.atemsource.atem.impl.common.AbstractEntityTypeBuilder.EntityTypeBuilderCallback;
import org.atemsource.atem.impl.common.AbstractMetaDataRepository;
import org.atemsource.atem.impl.common.attribute.PrimitiveAttributeImpl;
import org.atemsource.atem.impl.common.attribute.primitive.SimpleTextType;
import org.atemsource.atem.impl.json.TypeCodeConverter;
import org.atemsource.atem.impl.pojo.attribute.PojoAccessor;
import org.atemsource.atem.spi.DynamicEntityTypeSubrepository;
import org.atemsource.atem.spi.EntityTypeCreationContext;
import org.atemsource.jcr.entitytype.JcrEntityType;
import org.atemsource.jcr.entitytype.JcrEntityTypeBuilder;
import org.atemsource.jcr.entitytype.PathAttribute;
import org.atemsource.jcr.entitytype.ValueConverter;
import org.atemsource.jcr.entitytype.converter.BooleanConverter;
import org.atemsource.jcr.entitytype.converter.DoubleConverter;
import org.atemsource.jcr.entitytype.converter.IntegerConverter;
import org.atemsource.jcr.entitytype.converter.LongConverter;
import org.atemsource.jcr.entitytype.converter.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;

public class JcrEntityTypeRepository extends AbstractMetaDataRepository<Node>
		implements DynamicEntityTypeSubrepository<Node>,
		EntityTypeBuilderCallback {
	private static Logger logger = Logger.getLogger(JcrEntityTypeRepository.class);
	
	public class ReplaceCallback implements EntityTypeBuilderCallback {

		@Override
		public void onFinished(AbstractEntityType<?> entityType) {
			JcrEntityTypeRepository.this.onReplaced((JcrEntityType) entityType);
		}

	}

	@Autowired
	private BeanLocator beanLocator;

	private Map<Class, ValueConverter> converterMap = new HashMap<Class, ValueConverter>();

	public void setConverterMap(Map<Class, ValueConverter> converterMap) {
		this.converterMap = converterMap;
	}

	private TypeCodeConverter typeCodeConverter;

	private String typeProperty = "_type";

	@Override
	public void afterFirstInitialization(
			EntityTypeRepository entityTypeRepositoryImpl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterInitialization() {
	}

	@Override
	public EntityTypeBuilder createBuilder(String code) {
		JcrEntityType entityType = createEntityType(code);
		JcrEntityTypeBuilder builder = createBuilder(entityType);
		logger.debug("creating new type "+code);
		return builder;
	}

	private JcrEntityTypeBuilder createBuilder(JcrEntityType entityType) {
		JcrEntityTypeBuilder builder = beanLocator
				.getInstance(JcrEntityTypeBuilder.class);
		try {
			PathAttribute pathAttribute = new PathAttribute();
			pathAttribute.setAccessor(new PojoAccessor(Node.class.getMethod(
					"getPath", new Class[0])));
			pathAttribute.setCode("path");
			pathAttribute.setTargetType(new SimpleTextType());
			pathAttribute.setEntityType(entityType);
			entityType.addAttribute(pathAttribute);
			PrimitiveAttributeImpl<String> identifierAttribute = new PrimitiveAttributeImpl<String>();
			identifierAttribute.setAccessor(new PojoAccessor(Node.class
					.getMethod("getIdentifier", new Class[0])));
			identifierAttribute.setCode("identifier");
			identifierAttribute.setTargetType(new SimpleTextType());
			identifierAttribute.setEntityType(entityType);
			entityType.addAttribute(identifierAttribute);
			PrimitiveAttributeImpl<String> nameAttribute = new PrimitiveAttributeImpl<String>();
			nameAttribute.setAccessor(new PojoAccessor(Node.class.getMethod(
					"getName", new Class[0])));
			nameAttribute.setCode("name");
			nameAttribute.setTargetType(new SimpleTextType());
			nameAttribute.setEntityType(entityType);
			entityType.addAttribute(nameAttribute);

		} catch (Exception e) {
			throw new TechnicalException("cannot find accessors for jcr nodes",
					e);
		}

		builder.setEntityType(entityType);
		builder.setEntityClass(Node.class);
		builder.setRepositoryCallback(this);
		builder.setConverterMap(converterMap);

		builder.addSingleAttribute(getTypeProperty(), String.class);
		return builder;
	}

	public EntityTypeBuilder replaceBuilder(String code) {
		final JcrEntityType dynamicEntityTypeImpl = beanLocator
				.getInstance(JcrEntityType.class);
		dynamicEntityTypeImpl.setTypeCodeConverter(typeCodeConverter);
		dynamicEntityTypeImpl.setCode(code);
		dynamicEntityTypeImpl.setTypeProperty(typeProperty);

		JcrEntityTypeBuilder builder = createBuilder(dynamicEntityTypeImpl);

		builder.setRepositoryCallback(new ReplaceCallback());

		return builder;
	}

	public void onReplaced(JcrEntityType entityType) {
		AbstractEntityType<Node> previousType = nameToEntityTypes
				.get(entityType.getCode());

		Collection<IncomingRelation> incomingAssociations = previousType
				.getIncomingAssociations();

		((AbstractEntityType) previousType)
				.removeOutgoingAssociations(previousType);

		this.nameToEntityTypes.put(entityType.getCode(), entityType);
		entityTypes.add(entityType);
		entityType.setMetaType((EntityType) entityTypeCreationContext
				.getEntityTypeReference(EntityType.class));
		attacheServicesToEntityType(entityType);
		((AbstractEntityType) entityType)
				.initializeIncomingAssociations(entityTypeCreationContext);

		for (IncomingRelation<?, ?> incomingRelation : incomingAssociations) {
			entityType.addIncomingAssociation(incomingRelation);
		}
		entityTypeCreationContext.lazilyInitialized(entityType);
	}

	public JcrEntityType createEntityType(String code) {
		final JcrEntityType dynamicEntityTypeImpl = beanLocator
				.getInstance(JcrEntityType.class);
		dynamicEntityTypeImpl.setRepository(this);
		dynamicEntityTypeImpl.setTypeCodeConverter(typeCodeConverter);
		dynamicEntityTypeImpl.setCode(code);
		dynamicEntityTypeImpl.setTypeProperty(typeProperty);

		if (getEntityType(code) != null) {
			throw new IllegalArgumentException("dynamic type with name " + code
					+ " already exists.");
		}
		this.nameToEntityTypes.put(code, dynamicEntityTypeImpl);
		entityTypes.add(dynamicEntityTypeImpl);

		return dynamicEntityTypeImpl;
	}

	@Override
	public EntityType<Node> getEntityType(Object entity) {
		try {
			Node node = (Node) entity;

			try {
				Property property = node.getProperty(typeProperty);
				String typeCode = property.getString();
				return getEntityType(typeCode);
			} catch (PathNotFoundException e) {
				return null;
			}

		} catch (ClassCastException e) {
			return null;
		} catch (Exception e) {
			throw new TechnicalException("cannot get type property of Node", e);
		}

	}

	public String getTypeProperty() {
		return typeProperty;
	}

	@Override
	public void initialize(EntityTypeCreationContext entityTypeCreationContext) {
		this.entityTypeCreationContext = entityTypeCreationContext;
		converterMap.put(String.class, new StringConverter());
		converterMap.put(Long.class, new LongConverter());
		converterMap.put(Integer.class, new IntegerConverter());
		converterMap.put(Double.class, new DoubleConverter());
		converterMap.put(Boolean.class, new BooleanConverter());

	}

	@Override
	public void onFinished(AbstractEntityType entityType) {
		entityType.setMetaType((EntityType) entityTypeCreationContext
				.getEntityTypeReference(EntityType.class));
		attacheServicesToEntityType(entityType);
		((AbstractEntityType) entityType)
				.initializeIncomingAssociations(entityTypeCreationContext);
		entityTypeCreationContext.lazilyInitialized(entityType);
	}

	public void setTypeCodeConverter(TypeCodeConverter typeCodeConverter) {
		this.typeCodeConverter = typeCodeConverter;
	}

	public void setTypeProperty(String typeProperty) {
		this.typeProperty = typeProperty;
	}

}
