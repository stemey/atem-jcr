package org.atemsource.jcr.entitytype;

import javax.jcr.Node;

import org.atemsource.atem.api.EntityTypeRepository;
import org.atemsource.atem.api.attribute.Attribute;
import org.atemsource.atem.api.attribute.annotation.Cardinality;
import org.atemsource.atem.api.attribute.relation.SingleAttribute;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.SingleAssociationAttributeBuilder;
import org.atemsource.atem.api.type.Type;
import org.atemsource.atem.impl.common.AbstractEntityType;
import org.atemsource.jcr.entitytype.converter.StringConverter;

public class JcrSingleAssociationAttributeBuilder<T> implements
		SingleAssociationAttributeBuilder<T> {

	public String getCode() {
		return code;
	}

	public AbstractEntityType<?> getEntityType() {
		return entityType;
	}

	public Type<T> getTargetType() {
		return targetType;
	}

	public boolean isComposition() {
		return composition;
	}

	public Cardinality getCardinality() {
		return cardinality;
	}

	public EntityTypeRepository getEntityTypeRepository() {
		return entityTypeRepository;
	}

	public boolean isRequired() {
		return required;
	}

	private String code;
	private AbstractEntityType<?> entityType;

	public JcrSingleAssociationAttributeBuilder(String code,
			AbstractEntityType<?> entityType, EntityTypeRepository entityTypeRepository) {
		super();
		this.code = code;
		this.entityType = entityType;
		this.entityTypeRepository=entityTypeRepository;
	}

	private Type<T> targetType;
	private boolean composition;
	private Cardinality cardinality;
	private EntityTypeRepository entityTypeRepository;
	private boolean required;

	@Override
	public SingleAssociationAttributeBuilder<T> type(Type<T> targetType) {
		this.targetType = targetType;
		return this;
	}

	@Override
	public SingleAssociationAttributeBuilder<T> cardinality(Cardinality cardinality) {
		this.cardinality = cardinality;
		return this;
	}

	@Override
	public SingleAssociationAttributeBuilder<T> composition(boolean composition) {
		this.composition = composition;
		return this;
	}

	@Override
	public Attribute create() {
		if (composition) {
			SingleNodeAttribute singleNodeAttribute = new SingleNodeAttribute();
			singleNodeAttribute.setRequired(required);
			singleNodeAttribute.setCode(code);
			singleNodeAttribute.setComposition(true);
			if (cardinality != null) {
				singleNodeAttribute.setTargetCardinality(cardinality);
			} else {
				singleNodeAttribute.setTargetCardinality(Cardinality.ONE);
			}
			singleNodeAttribute.setTargetType((Type<Node>) targetType);
			singleNodeAttribute.setEntityType(entityType);
			entityType.addAttribute(singleNodeAttribute);
			return (SingleAttribute<T>) singleNodeAttribute;
		} else {
			JcrPrimitiveAttribute<String> attribute = new JcrPrimitiveAttribute<String>();
			attribute.setRequired(required);
			attribute.setCode(code);
			if (cardinality != null) {
				attribute.setTargetCardinality(cardinality);
			} else {
				attribute.setTargetCardinality(Cardinality.ONE);
			}
			attribute.setValueConverter(new StringConverter());
			JcrRefType refType = new JcrRefType((EntityType<T>)targetType,
					entityTypeRepository);
			attribute.setTargetType(refType);
			attribute.setEntityType(entityType);
			entityType.addAttribute(attribute);
			return (SingleAttribute<T>) attribute;

		}
	}

	@Override
	public JcrSingleAssociationAttributeBuilder<T> required(boolean required) {
		this.required=required;
		return this;
	}


	@Override
	public JcrSingleAssociationAttributeBuilder<T> type(Class<T> javaType) {
		this.targetType=entityTypeRepository.getType(javaType);
		return this;
	}

}
