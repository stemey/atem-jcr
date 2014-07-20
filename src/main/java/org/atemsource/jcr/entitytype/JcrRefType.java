package org.atemsource.jcr.entitytype;

import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.primitive.RefType;
import org.atemsource.atem.impl.common.attribute.primitive.SimpleTextType;

public class JcrRefType extends SimpleTextType implements RefType<String> {

	private EntityType<?> targetType;

	@Override
	public EntityType<?> getTargetType() {
		return targetType;
	}


	public JcrRefType(EntityType<?> targetType) {
		super();
		this.targetType = targetType;
	}


	@Override
	public String getId(String value) {
		return value;
	}


	@Override
	public EntityType<?> getTargetType(String value) {
		return targetType;
	}

}
