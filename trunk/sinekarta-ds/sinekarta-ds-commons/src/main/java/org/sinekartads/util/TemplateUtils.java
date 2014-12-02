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

		public static < Source extends Serializable, Target extends Serializable> Target cast (
				Class<Target> targetClass, 
				Source item ) {
			
			return targetClass.cast(item);
		}
		
		public static <Source extends Serializable, Target extends Serializable> Target[] cast (
				Class<Target> targetClass,  
				Source ... items ) {
			
			EntityCaster<Source, Target> caster = new EntityCaster<Source, Target> ( targetClass ); 
			return caster.transform ( items );
		}
		
		public static < Source extends Serializable, Target extends Serializable, 
						 SourceCol extends Collection<Source>, 
						 TargetCol extends Collection<Target>> TargetCol cast (
				Class<Target> targetClass,   
				SourceCol sourceCol ) {
			
			EntityCaster<Source, Target> caster = new EntityCaster<Source, Target> ( targetClass );
			return caster.transform(sourceCol);
		}
		
		public static < Source extends Serializable, Target extends Serializable, 
						 SourceSet extends Set<Source>, 
						 TargetSet extends Set<Target>> TargetSet castSet (
			Class<Target> targetClass,   
			SourceSet sourceCol ) {
			
			EntityCaster<Source, Target> caster = new EntityCaster<Source, Target> ( targetClass );
			return caster.transform(sourceCol);
		}
		
		public static < Source extends Serializable, Target extends Serializable, 
						 SourceList extends List<Source>, 
						 TargetList extends List<Target>> TargetList castList (
			Class<Target> targetClass,   
			SourceList sourceCol ) {
			
			EntityCaster<Source, Target> caster = new EntityCaster<Source, Target> ( targetClass );
			return caster.transform(sourceCol);
		}
		
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
				clone = (T) cloneBySerialization ( Serialization.toSerializable(item) );
			} catch(RuntimeException e) {
				throw e;
			} catch(Exception e) {
				// TODO attempt with other clonation methods if the first fails 
				throw new UnsupportedOperationException(String.format("Unable to clone the item - %s", item), e);
			}
			return clone;
		}
		
		public static < T extends Serializable > T cloneBySerialization ( T item ) {
			if ( item == null )													return null;
			byte[] bytes = Serialization.serialize ( Serialization.toSerializable(item) );
			return (T) Serialization.deserialize ( item.getClass(), bytes );
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
					SCol emptyCol = (SCol) clone ( Serialization.toSerializable(sCol) );
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
	
	
	public static final class Serialization {
		
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
		
		public static String serializeToHex ( Serializable item ) {
			byte[] bytes = SerializationUtils.serialize ( item );
			return HexUtils.encodeHex ( bytes );
		}
		
		public static <T> T deserializeFromHex ( Class<T> tClass, String hex ) {
			byte[] bytes = HexUtils.decodeHex(hex);
			return deserialize(tClass, bytes);
		}
		
		public static String serializeToBase64 ( Serializable item ) {
			byte[] bytes = SerializationUtils.serialize ( item );
			return Base64.encodeBase64String ( bytes );
		}
		
		public static <T> T deserializeFromBase64 ( Class<T> tClass, String base64 ) {
			byte[] bytes = Base64.decodeBase64 ( base64 );
			return deserialize ( tClass, bytes );
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
			EntityCol matchingCol = (EntityCol) Instantiation.clone ( Serialization.toSerializable(entityCol) );
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
			EntityMap matchingMap = (EntityMap) Instantiation.clone ( Serialization.toSerializable(entityMap) );
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
			EntityMap matchingMap = (EntityMap) Instantiation.clone ( Serialization.toSerializable(entityMap) );
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
			EntityCol sourceCol =   (EntityCol) Instantiation.clone ( Serialization.toSerializable(entityCol) );
			EntityCol filteredCol = (EntityCol) Instantiation.clone ( Serialization.toSerializable(entityCol) );
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