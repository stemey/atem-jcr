package org.atemsource.jcr.entitytype;

import javax.jcr.Node;

import org.atemsource.atem.api.EntityTypeRepository;
import org.atemsource.atem.api.attribute.Attribute;
import org.atemsource.atem.api.attribute.annotation.Cardinality;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.MultiAssociationAttributeBuilder;
import org.atemsource.atem.api.type.Type;
import org.atemsource.atem.impl.common.AbstractEntityType;
import org.atemsource.jcr.entitytype.converter.StringConverter;

public class JcrMultiAssociationAttributeBuilder<R,T> extends JcrSingleAssociationAttributeBuilder<T>
		implements MultiAssociationAttributeBuilder<R, T>{

	
	@Override
	public Attribute create() {
		if (isComposition()) {
			CollectionNodeAttribute attribute = new CollectionNodeAttribute();
			attribute.setRequired(isRequired());
			attribute.setComposition(true);
			attribute.setCode(getCode());
			if (getCardinality() != null) {
				attribute.setTargetCardinality(getCardinality());
			} else {
				attribute.setTargetCardinality(Cardinality.ZERO_TO_MANY);
			}
			EntityType<T> targetType = (EntityType<T>)getTargetType();
			attribute.setTargetType((Type<Node>) targetType);
			attribute.setEntityType(getEntityType());
			getEntityType().addAttribute(attribute);
			return attribute;
		} else {
			PrimitiveListAttribute<String,Object> attribute = new PrimitiveListAttribute<String,Object>();
			attribute.setRequired(isRequired());
			attribute.setCode(getCode());
			if (getCardinality() != null) {
				attribute.setTargetCardinality(getCardinality());
			} else {
				attribute.setTargetCardinality(Cardinality.ZERO_TO_MANY);
			}
			attribute.setValueConverter(new StringConverter());
			EntityType<T> targetType = (EntityType<T>)getTargetType();
			JcrRefType refType = new JcrRefType(targetType,
					getEntityTypeRepository());
			attribute.setTargetType(refType);
			attribute.setEntityType(getEntityType());
			getEntityType().addAttribute(attribute);
			return attribute;

		}
	}

	public JcrMultiAssociationAttributeBuilder(String code,
			AbstractEntityType<?> entityType,
			EntityTypeRepository entityTypeRepository) {
		super(code, entityType, entityTypeRepository);
	}

	

}
