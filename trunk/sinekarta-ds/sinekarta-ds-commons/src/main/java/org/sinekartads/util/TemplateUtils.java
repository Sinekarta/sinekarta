package org.sinekartads.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.sinekartads.util.EntityTransformer.BitsBytesTransformer;
import org.springframework.util.Assert;

@SuppressWarnings("unchecked")
public abstract class TemplateUtils {

	// -----
	// --- Utility suites - provide a set of correlated utility methods 
	// -

	public static final class Cast {
		
		public static < Source, Target > Target[] cast ( Class<Target> targetClass,
														 Source ... items 			) {
			
			EntityCaster<Source, Target> caster = new EntityCaster<Source, Target> ( targetClass ); 
			return caster.transform ( items );
		}
		
		public static < Source, Target, 
						SourceCol extends Collection<Source>, 
						TargetCol extends Collection<Target>> TargetCol cast (
				Class<Target> targetClass,   
				SourceCol sourceCol ) {
			
			EntityCaster<Source, Target> caster = new EntityCaster<Source, Target> ( targetClass );
			return caster.transform(sourceCol);
		}
		
//		public static < Source, Target, 
//						SourceSet extends Set<Source>, 
//						TargetSet extends Set<Target>> TargetSet cast ( Class<Target> targetClass,   
//																		SourceSet sourceCol 	   ) {
//			
//			EntityCaster<Source, Target> caster = new EntityCaster<Source, Target> ( targetClass );
//			return caster.transform(sourceCol);
//		}
//		
//		public static < Source, Target, 
//						SourceList extends List<Source>, 
//						TargetList extends List<Target>> TargetList cast ( Class<Target> targetClass,   
//								 										   SourceList sourceCol 		) {
//			
//			EntityCaster<Source, Target> caster = new EntityCaster<Source, Target> ( targetClass );
//			return caster.transform(sourceCol);
//		}
		
	}
	
	
	
	public static final class Conversion {

		public static <T> T firstNotNullElement ( T ... items ) {
			
			if ( ArrayUtils.isEmpty(items) )									return null;
			T first = null;
			T item;
			for ( int i=0; i<items.length && first==null; i++ ) {
				item = items [ i ];
				if ( item != null ) {
					first = item;
				}
			}
			return first;
		}
		
		public static <T, TCol extends Collection<T> > T firstNotNullElement ( TCol tCol ) {
			
			if ( CollectionUtils.isEmpty(tCol) )								return null;
			Iterator<T> tIt = tCol.iterator();
			T first = null;
			T item;
			while ( tIt.hasNext() && first == null ) {
				item = tIt.next();
				if ( item != null ) {
					first = item;
				}
			}
			return first;
		}
		
		public static <T> Enumeration<T> arrayToEnumeration(T ... items) {
			return Collections.enumeration ( arrayToList(items) );
		}
		
		public static <T> Set<T> arrayToSet(T ... items) {
			Set<T> target;
			try {
				target = new HashSet<T>();
				for(T item : items) {
					target.add(item);
				}
			} catch ( Exception e ) {
				// hide implementation-depending exceptions
				throw new RuntimeException( e.getMessage(), e );
			}
			return target;
		}
		
		public static <T> List<T> arrayToList(T ... items) {
			List<T> target;
			try {
				target = new ArrayList<T>();
				for(T item : items) {
					target.add(item);
				}
			} catch ( Exception e ) {
				// hide implementation-depending exceptions
				throw new RuntimeException( e.getMessage(), e );
			}
			return target;
		}
		
		public static <T> T[] collectionToArray(Collection<T> items) {
			
			T[] target;
			List<T> tList = new ArrayList<T>();
			try {
				Iterator<T> itemIt = items.iterator();
				T item;
				while( itemIt.hasNext() ) {
					item = itemIt.next();
					if ( item != null ) {
						tList.add(item); 
					}
				}
				if ( tList.isEmpty() ) 											return null;
				Class<T> tClass = (Class<T>)tList.iterator().next().getClass();
				target = (T[])Array.newInstance(tClass,tList.size());
				tList.toArray(target);
			} catch ( Exception e ) {
				throw new RuntimeException( e.getMessage(), e );
			}
			return target;
		}
		
		public static <T> T[] enumerationToArray(Enumeration<T> items) {
			return collectionToArray ( enumerationToList(items) );
		}
		
		public static <T> List<T> enumerationToList(Enumeration<T> items) {
			List<T> target = new ArrayList<T> ( );
			try {
				while( items.hasMoreElements() ) {
					target.add(items.nextElement());
				}
			} catch ( Exception e ) {
				// hide implementation-depending exceptions
				throw new RuntimeException( e.getMessage(), e );
			}
			return target;
		}
	}
	
	
	public static final class Instantiation {
		
		public static <T> T clone ( T item ) {
			T clone = null;
			try {
				clone = (T) cloneBySerialization ( Encoding.toSerializable(item) );
			} catch(RuntimeException e) {
				throw e;
			} catch(Exception e) {
				// TODO attempt with other clonation methods if the first one fails 
				throw new UnsupportedOperationException(String.format("Unable to clone the item - %s", item), e);
			}
			return clone;
		}
		
		public static < T extends Serializable > T cloneBySerialization ( T item ) {
			if ( item == null )													return null;
			byte[] bytes = Encoding.serialize ( Encoding.toSerializable(item) );
			return (T) Encoding.deserialize ( item.getClass(), bytes );
	    }
		
		public static <T> T[] nullFilledArray ( T templateObject, int length ) {
			return nullFilledArray ( (Class<T>)templateObject.getClass(), length );
		}
		
		public static <T> T[] nullFilledArray ( Class<T> tClass, int length ) {
			return (T[]) Array.newInstance(tClass, length);
		}
		
		public static <T, TCol extends Collection<T>> TCol emptyCollection ( TCol colTemplate ) {
			Assert.notNull ( colTemplate );
			return (TCol) emptyCollection ( colTemplate.getClass() );
		}
		
		public static <T, TCol extends Collection<T>> TCol emptyCollection ( Class<TCol> colClass ) {
			Assert.notNull ( colClass );
			TCol tCol;
			try {
				tCol = (TCol) colClass.newInstance();
			} catch(Exception e) {
				throw new IllegalArgumentException(String.format (
						"unable to generate a collection based on the template class - %s", colClass ));
			}
			return tCol;
		}
		
		public static < S, T, 
						SCol extends Collection<S>, 
						TCol extends Collection<T> > TCol emptyCollection ( SCol sCol, Class<TCol> tColClass ) {
			Assert.isTrue ( sCol != null || tColClass != null );
			TCol tCol = null;
			if ( sCol != null ) {
				try {
					SCol emptyCol = (SCol) clone ( Encoding.toSerializable(sCol) );
					emptyCol.clear();
					tCol = (TCol)emptyCol;
				} catch(Exception e) {
					tCol = null;
				}
			} 
			if ( tCol == null ) {
				
			}
			if ( tCol == null ) {
				throw new IllegalArgumentException(String.format ( "unable to clone the target collection - %s\n", tColClass) );
			}
			return tCol;
		}
	}
	
	
	public static final class Encoding {
		
		public static Serializable toSerializable ( Object item) {
			if( !(item instanceof Serializable) ) {
				throw new UnsupportedOperationException(String.format ( "not serializable object - %s", item ));
			}
			return (Serializable) item;
		}
		
		public static byte[] serialize ( Serializable item ) {
			return SerializationUtils.serialize ( item );
		}
		
		public static <T> T deserialize ( Class<T> tClass, byte[] bytes ) {
			return (T)SerializationUtils.deserialize(bytes);
		}
		
		public static String serializeHex ( Serializable item ) {
			byte[] bytes = SerializationUtils.serialize ( item );
			return HexUtils.encodeHex ( bytes );
		}
		
		public static <T> T deserializeHex ( Class<T> tClass, String hex ) {
			byte[] bytes = HexUtils.decodeHex(hex);
			return deserialize(tClass, bytes);
		}
		
		public static String serializeBase64 ( Serializable item ) {
			byte[] bytes = SerializationUtils.serialize ( item );
			return Base64.encodeBase64String ( bytes );
		}
		
		public static <T> T deserializeBase64 ( Class<T> tClass, String base64 ) {
			byte[] bytes = Base64.decodeBase64 ( base64 );
			return deserialize ( tClass, bytes );
		}
		
		public static <T> T deserializeJSON ( Class<T> tClass, String json ) {
			JSONObject jsonObject = JSONObject.fromObject ( json );
			return (T)JSONObject.toBean(jsonObject, standardJsonConfig(tClass));
		}

		public static String serializeJSON ( Object item ) {
			return serializeJSON ( item, false );
		}
		
		public static <T> String serializeJSON ( T[] tArray) {
			return serializeJSON ( tArray, false );
		}
		
		public static String serializeJSON ( Object item, boolean prettify ) {
			if ( item == null )													return "";
			String json;
			try {
				JSONObject jsonobj = JSONObject.fromObject(item, standardJsonConfig(item.getClass()));
				if ( prettify ) {
					json = jsonobj.toString(4);
				} else {
					json = jsonobj.toString();
				}
			} catch(RuntimeException e) {
				throw e;
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
			return json;
		}

		
		
		public static <T> String serializeJSON ( T[] tArray, boolean prettify ) {
			Class<T[]> tArrayClass = (Class<T[]>)tArray.getClass();
			String json;
			try {
				JSONArray jsonArray = JSONArray.fromObject ( tArray, standardJsonConfig(tArrayClass) );
				if ( prettify ) {
					json = jsonArray.toString(4);
				} else {
					json = jsonArray.toString();
				}
			} catch(RuntimeException e) {
				throw e;
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
			return json;
		}
		
		public static <T> T[] deserializeJSONArray ( Class<T[]> tArrayClass, String json ) {
			JSONArray jsonArray = JSONArray.fromObject ( json, standardJsonConfig(tArrayClass) );
			Class<T> tClass = (Class<T>) tArrayClass.getComponentType();
			T[] tArray = Instantiation.nullFilledArray ( tClass, jsonArray.size() );
			return (T[]) jsonArray.toArray ( tArray );
		}
		
		public static String prettifyJSON ( String json ) {
			String prettyJSON;
			try {
				JSONObject jsonobj = JSONObject.fromObject(json);
				prettyJSON = jsonobj.toString(4);
			} catch(RuntimeException e) {
				throw e;
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
			return prettyJSON;
		}
		
		public static JsonConfig standardJsonConfig ( Class<?> tClass) {
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.setRootClass(tClass);
			jsonConfig.setIgnoreTransientFields(true);
			return jsonConfig;
		}
	}
	
	
	
	public static class Filter {
		
		public static <Entity> Entity[] filter(
				EntityFilter<Entity> filter, 
				Entity ... items) {
			
			Collection<Entity> matchingItems = new ArrayList<Entity>();
			for ( Entity item : items) {
				if ( filter.match(item) ) {
					matchingItems.add(item);
				}
			}
			return Conversion.collectionToArray(matchingItems);
		}
		
		public static < Entity extends Serializable, 
					    EntityCol extends Collection<Entity> > EntityCol filter( EntityFilter<Entity> filter, 
					    														 EntityCol entityCol 			) {
			
			// Obtain a clean copy of the given collection
			EntityCol matchingCol = (EntityCol) Instantiation.clone ( Encoding.toSerializable(entityCol) );
			matchingCol.clear();
			
			// Return the collection of all the items for which filter succeeds 
			for ( Entity item : entityCol ) {
				if ( filter.match(item) ) {
					matchingCol.add(item);
				}
			}
			return matchingCol;
		}
		
		public static < Key extends Serializable, 
						Value extends Serializable,
						EntityMap extends Map<Key, Value> > EntityMap filterKeys ( EntityFilter<Key> filter, 
																				   EntityMap entityMap		) {
																			
			// Obtain a clean copy of the given collection
			EntityMap matchingMap = (EntityMap) Instantiation.clone ( Encoding.toSerializable(entityMap) );
			matchingMap.clear();
			
			// Return a map where filter succeeds for all entry keys
			for (Map.Entry<Key, Value> entry : entityMap.entrySet() ) {
				if ( filter.match(entry.getKey()) ) {
					matchingMap.put ( entry.getKey(), entry.getValue() );
				}
			}
			return matchingMap;
		}
		
		public static < Key extends Serializable, 
						Value extends Serializable,
						EntityMap extends Map<Key, Value> > EntityMap filterValues( EntityFilter<Value> filter, 
																					EntityMap entityMap			) {
			
			// Obtain a clean copy of the given collection
			EntityMap matchingMap = (EntityMap) Instantiation.clone ( Encoding.toSerializable(entityMap) );
			matchingMap.clear();
			
			// Return a map where filter succeeds for all entry keys
			for (Map.Entry<Key, Value> entry : entityMap.entrySet() ) {
				if ( filter.match(entry.getValue()) ) {
					matchingMap.put ( entry.getKey(), entry.getValue() );
				}
			}
			return matchingMap;
		}
		
		public static <Entity extends Serializable> Entity[] removeClones(
				final EntityComparator<Entity> comparator, 
				final Entity ... items) {
			
			// Return the filteredArray
			Collection<Entity> sourceCol = Conversion.arrayToList ( items );
			Collection<Entity> filteredCol = removeClones ( comparator, sourceCol );
			Entity[] filteredItems = Conversion.collectionToArray ( filteredCol );
			return filteredItems;
		}
		
		public static < Entity extends Serializable, 
					    EntityCol extends Collection<Entity> > EntityCol removeClones ( final EntityComparator<Entity> comparator, 
					    																final EntityCol entityCol 				   ) {
			
			// Obtain a clean copy of the given collection
			EntityCol clones;
			EntityCol sourceCol =   (EntityCol) Instantiation.clone ( Encoding.toSerializable(entityCol) );
			EntityCol filteredCol = (EntityCol) Instantiation.clone ( Encoding.toSerializable(entityCol) );
			filteredCol.clear();
			
			// Remove the first entity and all its clones until the sourceCol is empty,
			//			the first entity removed each step will compose the filteredCol 
			while ( sourceCol.size() > 0 ) {
				final Entity entity = sourceCol.iterator().next();
				clones = filter(new EntityFilter<Entity>() {
					@Override
					public boolean match(Entity target) {
						return comparator.compare(entity, target) == 0;
					}
				}, entityCol);
				sourceCol.removeAll(clones);
				filteredCol.add(entity);
			}
			
			// Return the filteredCol, it will contain no clones
			return filteredCol;
		}
	}
	
	
	

	public static final class Transformation {
		
		public static < Source extends Serializable, Target extends Serializable> Target transform (
				EntityTransformer<Source, Target> transformer, 
				Source item ) {
			
			return transformer.transform(item);
		}
		
		public static < Source extends Serializable, Target extends Serializable> Target[] transform (
				EntityTransformer<Source, Target> transformer, 
				Source ... items ) {
			
			return transformer.transform(items);
		}
		
		public static < Source extends Serializable, Target extends Serializable, 
						 SourceCol extends Collection<Source>, 
						 TargetCol extends Collection<Target>> TargetCol transform (
				EntityTransformer<Source, Target> transformer, 
				SourceCol sourceCol ) {
			
			return transformer.transform ( sourceCol );
		}
		
		public static byte[] bitsToBytes(	boolean[] bits ) {
			return BitsBytesTransformer.instance.transform(bits);
		}
		
		public static boolean[] bytesToBits(	byte[] bytes ) {
			return BitsBytesTransformer.reverted.transform(bytes);
		}
	}
}