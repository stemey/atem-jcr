package org.atemsource.jcr.entitytype;

import javax.jcr.Node;

import org.atemsource.atem.api.attribute.annotation.Cardinality;
import org.atemsource.atem.api.attribute.relation.SingleAttribute;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.SingleAttributeBuilder;
import org.atemsource.atem.api.type.Type;
import org.atemsource.atem.impl.common.AbstractEntityType;
import org.atemsource.jcr.entitytype.converter.StringConverter;

public class JcrSingleAssociationAttributeBuilder<T> implements
		SingleAttributeBuilder<T> {

	private String code;
	private AbstractEntityType<?> entityType;
	public JcrSingleAssociationAttributeBuilder(String code,AbstractEntityType<?> entityType) {
		super();
		this.code = code;
		this.entityType=entityType;
	}

	private EntityType<T> targetType;
	private boolean composition;
	private Cardinality cardinality;
	
	@Override
	public SingleAttributeBuilder<T> type(EntityType<T> targetType) {
		this.targetType=targetType;
		return this;
	}

	@Override
	public SingleAttributeBuilder<T> cardinality(Cardinality cardinality) {
		this.cardinality=cardinality;
		return this;
	}

	@Override
	public SingleAttributeBuilder<T> composition(boolean composition) {
		this.composition=composition;
		return this;
	}

	@Override
	public SingleAttribute<T> create() {
		if (composition) {
			SingleNodeAttribute singleNodeAttribute = new SingleNodeAttribute();
			singleNodeAttribute.setCode(code);
			singleNodeAttribute.setComposition(true);
			if (cardinality!=null) {
				singleNodeAttribute.setTargetCardinality(cardinality);
			}else{
				singleNodeAttribute.setTargetCardinality(Cardinality.ONE);
			}
			singleNodeAttribute.setTargetType((Type<Node>) targetType);
			singleNodeAttribute.setEntityType(entityType);
			entityType.addAttribute(singleNodeAttribute);
			return (SingleAttribute<T>) singleNodeAttribute;
		}else {
			JcrPrimitiveAttribute<String> attribute = new JcrPrimitiveAttribute<String>();
			attribute.setCode(code);
			if (cardinality!=null) {
				attribute.setTargetCardinality(cardinality);
			}else{
				attribute.setTargetCardinality(Cardinality.ONE);
			}
			attribute.setValueConverter(new StringConverter());
			JcrRefType refType = new JcrRefType(targetType);
			attribute.setTargetType(refType);
			attribute.setEntityType(entityType);
			entityType.addAttribute(attribute);
			return (SingleAttribute<T>) attribute;
			
		}
	}

}
