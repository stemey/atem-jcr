package org.atemsource.jcr.entitytype;

import java.util.HashMap;
import java.util.Map;

import org.atemsource.atem.api.attribute.CollectionAttribute;
import org.atemsource.atem.api.attribute.CollectionSortType;
import org.atemsource.atem.api.attribute.relation.SingleAttribute;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.PrimitiveType;
import org.atemsource.atem.api.type.SingleAttributeBuilder;
import org.atemsource.atem.api.type.Type;
import org.atemsource.atem.impl.common.AbstractEntityType;
import org.atemsource.atem.impl.common.AbstractEntityTypeBuilder;
import org.atemsource.atem.impl.common.builder.SingleAssociationAttributeBuilder;

public class JcrEntityTypeBuilder extends AbstractEntityTypeBuilder {

	@Override
	public <J> SingleAttributeBuilder<J> addSingleAssociationAttribute(
			String code) {
		return new JcrSingleAssociationAttributeBuilder(code,(AbstractEntityType) getReference());
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
	public CollectionAttribute addMultiAssociationAttribute(String code, Type targetType, Type[] validTypes,
		CollectionSortType collectionSortType)
	{
		CollectionNodeAttribute attribute =new CollectionNodeAttribute();
		attribute.setCode(code);
		attribute.setMetaType(entityTypeRepository.getEntityType(attribute));
		attribute.setEntityType(getEntityType());
		attribute.setTargetType(targetType);
		addAttribute(attribute);
		attribute.setWriteable(true);
		attribute.setValidTargetTypes(validTypes);

		return attribute;
	}
	@Override
	public SingleAttribute addSingleAssociationAttribute(String code,
			EntityType targetType, Type[] validTypes) {
		SingleNodeAttribute attribute = new SingleNodeAttribute();
		attribute.setCode(code);
		attribute.setEntityType(getEntityType());
		attribute.setTargetType(targetType);
		addAttribute(attribute);
		attribute.setWriteable(true);
		attribute.setValidTargetTypes(validTypes);

		return attribute;
	}

	private <T> ValueConverter<T> createConverter(PrimitiveType<T> type) {
		ValueConverter valueConverter = converterMap.get(type.getJavaType());
		return valueConverter;
	}
}