package org.sinekartads.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.lang3.ArrayUtils;

public class TemplateClassUtils {
	
	static ObjectFormatter<Class<?>> classFormatter  = new AbstractObjectFormatter<Class<?>>() {
		@Override
		public String format(Class<?> clazz) {
			return clazz.getName();
		}
	};
	
	/**
	 * Extract all the type arguments assigned to the template arguments during the instantiation
	 * of a parametric class.
	 * Example 1:	parametricClass 	= Map<ASN1ObjectIdentifier, DERTaggedObject>.class
	 * 				templateClass		= Map.class
	 * 							 	 ---> [ASN1ObjectIdentifier.class, DERTaggedObject.class]
	 * Example 2:	parametricClass 	= Map.class
	 * 				templateClass		= Map.class
	 * 							 	 ---> []
	 * Example 3:	@See 
	 * @param parametricClass 		- class of the instance being checked 
	 * @param templateClass 		- template cla00ss which the instance being checked belongs to 
	 * @return all the type arguments that have been assigned to the template arguments during 
	 *			the given paramatricClass instantiation
	 */
	public static Class<?>[] getTemplateArguments (
			Class<?> parametricClass, 
			Class<?> templateClass ) 
					throws IllegalArgumentException {
		parametricClass = new ArrayList<String>().getClass();
		Class<?>[] typeArgs = doResolveTypeArguments(parametricClass, parametricClass, templateClass);
//		Class<?>[] typeArgs = GenericTypeResolver.resolveTypeArguments(parametricClass, templateClass);
		if( ArrayUtils.isEmpty(typeArgs) ) {
			throw new IllegalArgumentException ( String.format( 
						"unable to detect any argument of parametric class %s", 
						parametricClass.getName() ));
		}
		
		// Load all the type arguments that the parametricClass assign to the baseTemplateClass arguments
		return typeArgs;
	}

	
	/**
	 * Extract the only type argument assigned to the template arguments during the instantiation 
	 * of a parametric class.
	 * Example 1:	parametricClass 	= List<ASN1ObjectIdentifier>.class
	 * 				templateClass		= List.class
	 * 								 ---> ASN1ObjectIdentifier.class
	 * Example 2:	parametricClass 	= List.class
	 * 				templateClass		= List.class
	 * 							 	 ---> IllegalArgumentException: no type arguments
	 * Example 3:	parametricClass 	= Map<ASN1ObjectIdentifier, DERTaggedObject>.class
	 * 				templateClass		= Map.class
	 * 							 	 ---> IllegalArgumentException: multiple type argument 
	 * Example 4:	@See 
	 * @param parametricClass 		- class of the instance being checked 
	 * @param templateClass 		- template class which the instance being checked belongs to 
	 * @return the only type arguments that have been assigned to the template arguments during 
	 *			the paramatricClass instantiation
	 * @throws IllegalArgumentException if there have been found no type arguments, 
	 * 			or more than one
	 */
	public static Class<?> getTemplateArgument (
			Class<?> parametricClass, 
			Class<?> templateClass ) 
					throws IllegalArgumentException {
		
		// Load all the type arguments that the parametricClass assign to the baseTemplateClass arguments
		Class<?>[] typeArgs = getTemplateArguments(parametricClass, templateClass);
		
		if ( typeArgs.length > 1) {
			throw new IllegalArgumentException ( String.format( 
					"the parametric class %s has more then one argument: \n\t%s \n" +
					"use getTemplateArguments(Class<?>, Class<?>) " +
					"or getTemplateArgument(Class<?>, Class<?>, Class<?>) instead)", 
					parametricClass.getName(), 
					TextUtils.fromArray(typeArgs)));
		} 
		
		// Return the unique type argument
		return typeArgs[0];
	}
	
	/**
	 * Extract all the types argument assigned to the template arguments during the instantiation 
	 * of a parametric class.
	 * Example 1:	parametricClass 	= Map<ASN1ObjectIdentifier, DERTaggedObject>.class
	 * 				templateClass		= Map.class
	 *				typeArgSuperclass	= DERTaggedObject.class
	 * 							 	 ---> DERTaggedObject.class
	 * Example 2:	parametricClass 	= Map<ASN1ObjectIdentifier, DERTaggedObject>.class
	 * 				templateClass		= Map.class
	 *				typeArgSuperclass	= ASN1ObjectIdentifier.class
	 * Example 3:	parametricClass 	= Map<ASN1ObjectIdentifier, DERTaggedObject>.class
	 * 				templateClass		= Map.class
	 *				typeArgSuperclass	= ASN1TaggedObject.class
	 * 							 	 ---> IllegalArgumenException: no matches
	 * Example 4:	parametricClass 	= Map<ASN1ObjectIdentifier, DERTaggedObject>.class
	 * 				templateClass		= Map.class
	 *				typeArgSuperclass	= DERObject.class
	 * 							 	 ---> IllegalArgumenException: multiple matches 
	 * Example 5:	parametricClass 	= Map<ASN1ObjectIdentifier, DERTaggedObject>.class
	 * 				templateClass		= Map.class
	 *				typeArgSuperclass	= ASN1ObjectIdentifier.class 
	 * Example 6:	@See BaseWS
	 * @param parametricClass 		- class of the instance being checked 
	 * @param templateClass	 		- template class which the instance being checked belongs to
	 * @param typeArgSuperclass 	- class or interface searched to detect the required type argument
	 * @return the only type arguments, which implements or extends the typeArgSuperclass that 
				have been assigned to the template arguments during the paramatricClass instantiation
	 * @throws IllegalArgumentException if there have been found no type arguments, 
	 * 			or more than one
	 */
	public static Class<?> getTemplateArgument (
			Class<?> parametricClass, 
			Class<?> templateClass, 
			Class<?> typeArgSuperclass ) 
					throws IllegalArgumentException {
		
		// Load all the type arguments that the parametricClass assign to the baseTemplateClass arguments
		Class<?>[] typeArgs = getTemplateArguments(parametricClass, templateClass);
		
		// Filter the type arguments matching with the given typeArgSuperclass 
		Set<Class<?>> matchingArguments = new HashSet<Class<?>>();
		for (Class<?> typeArg : typeArgs) {
			if (typeArgSuperclass.isAssignableFrom(typeArg)) {
				matchingArguments.add(typeArg);
			}
		}
		
		// Return the matching (and unique) type argument, error with more or less than one match
		if( matchingArguments.isEmpty() ) {
			throw new IllegalArgumentException ( String.format( 
					"unable to detect any argument of parametric class %s extending %s: \n\t%s \n" +
					"use getTemplateArguments(Class<?>, Class<?>) instead",
					typeArgSuperclass.getName(), parametricClass.getName(), 
					TextUtils.fromCollection(matchingArguments, classFormatter) ));			
		}
		if ( matchingArguments.size() > 1 ) {
			throw new IllegalArgumentException ( String.format( 
					"the parametric class %s has more then one argument extending %s: \n\t%s \n" +
					"use getTemplateArguments(Class<?>, Class<?>) instead",
					typeArgSuperclass.getName(), parametricClass.getName(), 
					TextUtils.fromCollection(matchingArguments, classFormatter)));
		}
		return matchingArguments.iterator().next();
	}
	
	
	
	// -----
	// --- Convenience methods - applied directly on a instance of the parametricClass 
	// -
	
	public static Class<?>[] getTemplateArguments (
			Object parametricInstance, 
			Class<?> templateClass ) 
					throws IllegalArgumentException {
		
		return getTemplateArguments(parametricInstance.getClass(), templateClass);
	}
	
	public static Class<?> getTemplateArgument (
			Object parametricInstance, 
			Class<?> templateClass ) 
					throws IllegalArgumentException {
		
		return getTemplateArgument(parametricInstance.getClass(), templateClass);
	}
	
	public static Class<?> getTemplateArgument (
			Object parametricInstance, 
			Class<?> templateClass, 
			Class<?> typeArgSuperclass ) 
					throws IllegalArgumentException {
		
		return getTemplateArgument(parametricInstance.getClass(), templateClass, typeArgSuperclass);
	}
	
	
	
	// -----
	// --- Implementation methods - ripped and adapted from org.springframework.core.GenericTypeResolver
	// -
	
	/** Cache from Class to TypeVariable Map */
	private static final Map<Class<?>, Reference<Map<TypeVariable<?>, Type>>> typeVariableCache =
			Collections.synchronizedMap(new WeakHashMap<Class<?>, Reference<Map<TypeVariable<?>, Type>>>());
	
	private static Class<?>[] doResolveTypeArguments(Class<?> ownerClass, Class<?> classToIntrospect, Class<?> genericIfc) {
		while (classToIntrospect != null) {
			if (genericIfc.isInterface()) {
				Type[] ifcs = classToIntrospect.getGenericInterfaces();
				for (Type ifc : ifcs) {
					Class<?>[] result = doResolveTypeArguments(ownerClass, ifc, genericIfc);
					if (result != null) {
						return result;
					}
				}
			}
			else {
				Class<?>[] result = doResolveTypeArguments(
						ownerClass, classToIntrospect.getGenericSuperclass(), genericIfc);
				if (result != null) {
					return result;
				}
			}
			classToIntrospect = classToIntrospect.getSuperclass();
		}
		return null;
	}
	
	private static Class<?>[] doResolveTypeArguments(Class<?> ownerClass, Type ifc, Class<?> genericIfc) {
		if (ifc instanceof ParameterizedType) {
			ParameterizedType paramIfc = (ParameterizedType) ifc;
			Type rawType = paramIfc.getRawType();
			if (genericIfc.equals(rawType)) {
				Type[] typeArgs = paramIfc.getActualTypeArguments();
				Class<?>[] result = new Class[typeArgs.length];
				for (int i = 0; i < typeArgs.length; i++) {
					Type arg = typeArgs[i];
					result[i] = extractClass(ownerClass, arg);
				}
				return result;
			}
			else if (genericIfc.isAssignableFrom((Class<?>) rawType)) {
				return doResolveTypeArguments(ownerClass, (Class<?>) rawType, genericIfc);
			}
		}
		else if (genericIfc.isAssignableFrom((Class<?>) ifc)) {
			return doResolveTypeArguments(ownerClass, (Class<?>) ifc, genericIfc);
		}
		return null;
	}
	
	/**
	 * Extract a class instance from given Type.
	 */
	private static Class<?> extractClass(Class<?> ownerClass, Type arg) {
		if (arg instanceof ParameterizedType) {
			return extractClass(ownerClass, ((ParameterizedType) arg).getRawType());
		}
		else if (arg instanceof GenericArrayType) {
			GenericArrayType gat = (GenericArrayType) arg;
			Type gt = gat.getGenericComponentType();
			Class<?> componentClass = extractClass(ownerClass, gt);
			return Array.newInstance(componentClass, 0).getClass();
		}
		else if (arg instanceof TypeVariable) {
			TypeVariable<?> tv = (TypeVariable<?>) arg;
			arg = getTypeVariableMap(ownerClass).get(tv);
			if (arg == null) {
				arg = extractBoundForTypeVariable(tv);
			}
			else {
				arg = extractClass(ownerClass, arg);
			}
		}
		return (arg instanceof Class ? (Class<?>) arg : Object.class);
	}
	
	/**
	 * Build a mapping of {@link TypeVariable#getName TypeVariable names} to concrete
	 * {@link Class} for the specified {@link Class}. Searches all super types,
	 * enclosing types and interfaces.
	 */
	static Map<TypeVariable<?>, Type> getTypeVariableMap(Class<?> clazz) {
		Reference<Map<TypeVariable<?>, Type>> ref = typeVariableCache.get(clazz);
		Map<TypeVariable<?>, Type> typeVariableMap = (ref != null ? ref.get() : null);

		if (typeVariableMap == null) {
			typeVariableMap = new HashMap<TypeVariable<?>, Type>();

			// interfaces
			extractTypeVariablesFromGenericInterfaces(clazz.getGenericInterfaces(), typeVariableMap);

			// super class
			Type genericType = clazz.getGenericSuperclass();
			Class<?> type = clazz.getSuperclass();
			while (type != null && !Object.class.equals(type)) {
				if (genericType instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) genericType;
					populateTypeMapFromParameterizedType(pt, typeVariableMap);
				}
				extractTypeVariablesFromGenericInterfaces(type.getGenericInterfaces(), typeVariableMap);
				genericType = type.getGenericSuperclass();
				type = type.getSuperclass();
			}

			// enclosing class
			type = clazz;
			while (type.isMemberClass()) {
				genericType = type.getGenericSuperclass();
				if (genericType instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) genericType;
					populateTypeMapFromParameterizedType(pt, typeVariableMap);
				}
				type = type.getEnclosingClass();
			}

			typeVariableCache.put(clazz, new WeakReference<Map<TypeVariable<?>, Type>>(typeVariableMap));
		}

		return typeVariableMap;
	}
	
	private static void extractTypeVariablesFromGenericInterfaces(Type[] genericInterfaces, Map<TypeVariable<?>, Type> typeVariableMap) {
		for (Type genericInterface : genericInterfaces) {
			if (genericInterface instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) genericInterface;
				populateTypeMapFromParameterizedType(pt, typeVariableMap);
				if (pt.getRawType() instanceof Class) {
					extractTypeVariablesFromGenericInterfaces(
							((Class<?>) pt.getRawType()).getGenericInterfaces(), typeVariableMap);
				}
			}
			else if (genericInterface instanceof Class) {
				extractTypeVariablesFromGenericInterfaces(
						((Class<?>) genericInterface).getGenericInterfaces(), typeVariableMap);
			}
		}
	}
	
	/**
	 * Extracts the bound <code>Type</code> for a given {@link TypeVariable}.
	 */
	static Type extractBoundForTypeVariable(TypeVariable<?> typeVariable) {
		Type[] bounds = typeVariable.getBounds();
		if (bounds.length == 0) {
			return Object.class;
		}
		Type bound = bounds[0];
		if (bound instanceof TypeVariable) {
			bound = extractBoundForTypeVariable((TypeVariable<?>) bound);
		}
		return bound;
	}
	
	/**
	 * Read the {@link TypeVariable TypeVariables} from the supplied {@link ParameterizedType}
	 * and add mappings corresponding to the {@link TypeVariable#getName TypeVariable name} ->
	 * concrete type to the supplied {@link Map}.
	 * <p>Consider this case:
	 * <pre class="code>
	 * public interface Foo<S, T> {
	 *  ..
	 * }
	 *
	 * public class FooImpl implements Foo<String, Integer> {
	 *  ..
	 * }</pre>
	 * For '<code>FooImpl</code>' the following mappings would be added to the {@link Map}:
	 * {S=java.lang.String, T=java.lang.Integer}.
	 */
	private static void populateTypeMapFromParameterizedType(ParameterizedType type, Map<TypeVariable<?>, Type> typeVariableMap) {
		if (type.getRawType() instanceof Class) {
			Type[] actualTypeArguments = type.getActualTypeArguments();
			TypeVariable<?>[] typeVariables = ((Class<?>) type.getRawType()).getTypeParameters();
			for (int i = 0; i < actualTypeArguments.length; i++) {
				Type actualTypeArgument = actualTypeArguments[i];
				TypeVariable<?> variable = typeVariables[i];
				if (actualTypeArgument instanceof Class) {
					typeVariableMap.put(variable, actualTypeArgument);
				}
				else if (actualTypeArgument instanceof GenericArrayType) {
					typeVariableMap.put(variable, actualTypeArgument);
				}
				else if (actualTypeArgument instanceof ParameterizedType) {
					typeVariableMap.put(variable, actualTypeArgument);
				}
				else if (actualTypeArgument instanceof TypeVariable) {
					// We have a type that is parameterized at instantiation time
					// the nearest match on the bridge method will be the bounded type.
					TypeVariable<?> typeVariableArgument = (TypeVariable<?>) actualTypeArgument;
					Type resolvedType = typeVariableMap.get(typeVariableArgument);
					if (resolvedType == null) {
						resolvedType = extractBoundForTypeVariable(typeVariableArgument);
					}
					typeVariableMap.put(variable, resolvedType);
				}
			}
		}
	}
}