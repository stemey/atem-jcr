package org.atemsource.jcr.entitytype;

import java.io.Serializable;

import org.atemsource.atem.api.EntityTypeRepository;
import org.atemsource.atem.api.type.EntityType;
import org.atemsource.atem.api.type.primitive.RefType;
import org.atemsource.atem.impl.common.attribute.primitive.SimpleTextType;

/**
 * TODO move targetTypes to attribute and add the incoming relation. incoming relation should also have a value of type refType/String.
 * 
 * 
 * @author stefan
 *
 */
public class JcrRefType extends SimpleTextType implements RefType<String> {

	private EntityType<?>[] targetTypes;

	private EntityTypeRepository entityTypeRepository;

	public JcrRefType(EntityType<?>[] targetTypes,
			EntityTypeRepository entityTypeRepository) {
		super();
		this.targetTypes = targetTypes;
		this.entityTypeRepository = entityTypeRepository;
	}

	public JcrRefType(EntityType<?> targetType,
			EntityTypeRepository entityTypeRepository) {
		super();
		this.targetTypes = new EntityType<?>[]{targetType};
		this.entityTypeRepository = entityTypeRepository;
	}

	public JcrRefType(EntityType<?>[] targetTypes) {
		super();
		this.targetTypes = targetTypes;
	}

	@Override
	public String getId(String value) {
		return value;
	}

	@Override
	public <R> EntityType<R>[] getTargetTypes() {
		return (EntityType<R>[]) targetTypes;
	}

	@Override
	public <R> EntityType<R> getTargetType(String value) {
		String[] split = value.split("::");
		return entityTypeRepository.getEntityType(split[0]);
	}

	@Override
	public <R> String createValue(EntityType<R> entityType, Serializable id) {
		return entityType.getCode() + "::" + String.valueOf(id);
	}

}
