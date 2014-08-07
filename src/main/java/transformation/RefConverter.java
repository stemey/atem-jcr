package transformation;

import org.atemsource.atem.api.type.primitive.RefType;
import org.atemsource.atem.service.entity.TypeAndId;
import org.atemsource.atem.service.refresolver.RefResolver;
import org.atemsource.atem.utility.transform.api.JavaConverter;
import org.atemsource.atem.utility.transform.api.TransformationContext;

public class RefConverter implements JavaConverter<String, String> {

	private RefType<String> refType;
	private RefResolver refResolver;

	@Override
	public String convertAB(String a, TransformationContext ctx) {
		return refResolver.getSingleUri(refType.getTargetType(a),
				refType.getId(a));

	}

	@Override
	public String convertBA(String b, TransformationContext ctx) {
		TypeAndId<Object, Object> parseSingleUri = refResolver.parseSingleUri(b);
		return refType.createValue(parseSingleUri.getEntityType(),parseSingleUri.getId());
	}

}
