package org.atemsource.jcr.entitytype;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.atemsource.atem.api.attribute.CollectionAttribute;
import org.atemsource.atem.api.attribute.CollectionSortType;
import org.atemsource.atem.api.attribute.relation.SingleAttribute;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.MultiAssociationAttributeBuilder;
import org.atemsource.atem.api.type.PrimitiveType;
import org.atemsource.atem.api.type.SingleAssociationAttributeBuilder;
import org.atemsource.atem.api.type.Type;
import org.atemsource.atem.impl.common.AbstractEntityType;
import org.atemsource.atem.impl.common.AbstractEntityTypeBuilder;

public class JcrEntityTypeBuilder extends AbstractEntityTypeBuilder {

	@Override
	public <J> SingleAssociationAttributeBuilder<J> addSingleAssociationAttribute(
			String code) {
		return new JcrSingleAssociationAttributeBuilder(code,
				(AbstractEntityType) getReference(), entityTypeRepository);
	}

	private Map<Class, ValueConverter> converterMap = new HashMap<Class, ValueConverter>();

	public void setConverterMap(Map<Class, ValueConverter> converterMap) {
		this.converterMap = converterMap;
	}

	@Override
	public <J> SingleAttribute<J> addPrimitiveAttribute(String code,
			PrimitiveType<J> type) {
		JcrPrimitiveAttribute<J> attribute = new JcrPrimitiveAttribute<J>();
		attribute.setCode(code);
		attribute.setMetaType(entityTypeRepository.getEntityType(attribute));
		addAttribute(attribute);
		attribute.setEntityType(getEntityType());
		attribute.setWriteable(true);
		attribute.setTargetType(type);

		ValueConverter<J> valueConverter = createConverter(type);
		addAttribute(attribute);
		if (valueConverter==null) {
			throw new IllegalStateException("type not supported "+type.getJavaType().getName());
		}
		attribute.setValueConverter(valueConverter);

		return attribute;
	}

	@Override
	public <J> SingleAttribute<J> addSingleAttribute(String code, Type<J> type,
			Type[] validTypes) {
		if (type instanceof EntityType<?>) {
			return addSingleAssociationAttribute(code, (EntityType<J>) type,
					validTypes);
		} else {
			return addPrimitiveAttribute(code, (PrimitiveType<J>) type);
		}
	}

	@Override
	public <R, T> MultiAssociationAttributeBuilder<R, T> addMultiAssociationAttribute(
			String code) {
		return new JcrMultiAssociationAttributeBuilder(code, getEntityType(),
				entityTypeRepository);
	}

	@Override
	public CollectionAttribute addMultiAssociationAttribute(String code,
			Type targetType, Type[] validTypes,
			CollectionSortType collectionSortType) {
		if (targetType instanceof PrimitiveType<?>) {
			PrimitiveListAttribute attribute = new PrimitiveListAttribute();
			attribute.setCode(code);
			attribute
					.setMetaType(entityTypeRepository.getEntityType(attribute));
			attribute.setEntityType(getEntityType());
			attribute.setTargetType(targetType);
			ValueConverter<?> valueConverter = createConverter((PrimitiveType<?>) targetType);
			attribute.setValueConverter(valueConverter);
			addAttribute(attribute);
			attribute.setWriteable(true);
			return attribute;
		} else {
			CollectionNodeAttribute attribute = new CollectionNodeAttribute();
			attribute.setCode(code);
			attribute
					.setMetaType(entityTypeRepository.getEntityType(attribute));
			attribute.setEntityType(getEntityType());
			attribute.setTargetType(targetType);
			addAttribute(attribute);
			attribute.setWriteable(true);
			attribute.setValidTargetTypes(validTypes);

			return attribute;
		}
	}

	@Override
	public SingleAttribute addSingleAssociationAttribute(String code,
			EntityType targetType, Type[] validTypes) {
		SingleNodeAttribute attribute = new SingleNodeAttribute();
		attribute.setCode(code);
		attribute.setEntityType(getEntityType());
		attribute.setTargetType(createTypeProxy(targetType));
		addAttribute(attribute);
		attribute.setWriteable(true);
		attribute.setValidTargetTypes(validTypes);

		return attribute;
	}

	private <T> EntityType<T> createTypeProxy(EntityType<T> type) {
		return (EntityType<T>) Proxy.newProxyInstance(
				type.getClass().getClassLoader(),
				new Class[] { EntityType.class },
				new EntityTypeInvocationHandler(entityTypeRepository, type
						.getCode()));
	}

	private <T> ValueConverter<T> createConverter(PrimitiveType<T> type) {
		ValueConverter valueConverter = converterMap.get(type.getJavaType());
		return valueConverter;
	}
}
