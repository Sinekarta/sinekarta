package org.sinekartads.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;


public abstract class EntityTransformer<Source, Target> {

	// -----
	// --- Generic transformer implementation
	// -
	
	public Target transform(Source item) {
		if ( item == null )		return null;
		
		return doTransform(item);
	}
	
	public Target[] transform(Source[] sArray) {
		if ( sArray == null ) 													return null;
		Collection<Source> sCol = TemplateUtils.Conversion.arrayToList ( sArray );
		Collection<Target> tCol = new ArrayList<Target>();
		return TemplateUtils.Conversion.collectionToArray ( transform(sCol, tCol) );
	}
	
	public < SourceCol extends Collection<Source>, 
			 TargetCol extends Collection<Target> > TargetCol transform ( SourceCol sourceCol,
					 													  TargetCol targetCol ) {
		for ( Source source : sourceCol) {
			targetCol.add ( doTransform(source) ); 
		}
		return targetCol;
	}
	
	
	public < SourceCol extends Collection<Source>, 
			 TargetCol extends Collection<Target> > TargetCol transform ( SourceCol sourceCol ) {
		if ( sourceCol == null) 													return null;
		@SuppressWarnings("unchecked")
		TargetCol targetCol = (TargetCol)TemplateUtils.Instantiation.emptyCollection(sourceCol);
		return transform ( sourceCol, targetCol );
	}
	
	protected abstract Target doTransform(Source item);
	
	
	
	// -----
	// --- Reversible transformer implementation 
	// -
	
	public abstract static class ReversibleTransformer<Source extends Serializable, Target extends Serializable> 
			extends EntityTransformer<Source, Target> {
		
		public Source reverseTransform(Target item) {
			if ( item == null )		return null;
			
			return doReverseTransform(item);
		}
		
		public ReversibleTransformer<Target, Source> reverse() {
			return new ReversibleTransformer<Target, Source>() {
					@Override
					public Source doTransform(Target item) {
						return ReversibleTransformer.this.reverseTransform(item);
					}
					@Override
					public Target doReverseTransform(Source item) {
						return ReversibleTransformer.this.transform(item);
					}
				};
		}
		
		protected abstract Source doReverseTransform(Target item);
	}
	
	
	
	
	
	// -----
	// --- Default specialized implementations
	// -
	
	public static class BitsBytesTransformer extends ReversibleTransformer<boolean[], byte[]> {

		static final EntityTransformer<boolean[], byte[]> instance = new BitsBytesTransformer();
		static final EntityTransformer<byte[], boolean[]> reverted = new BitsBytesTransformer().reverse();
		
		public static EntityTransformer<boolean[], byte[]> getInstance() {
			return instance;
		}
		
		public static EntityTransformer<byte[], boolean[]> getRevertedInstance() {
			return reverted;
		}
		
		@Override
		public boolean[] doReverseTransform ( byte[] bytes ) {
//			final int blockNumber = bytes.length;
//			boolean[] bits = new boolean[blockNumber*8];
//			byte tmpByte;
//			for(int block=blockNumber-1; block>=0; block--) {
//				tmpByte = bytes[block];
//				Byte b = bytes[block];b.
//				for(int bit=8-1; bit>=0; bit--) {
//					tmpByte += bytes[block*8+bit-offset] ? 1 : 0;
//					tmpByte = Integer.valueOf(tmpByte<<8).byteValue();
//				}
//				bits[block] = tmpByte;
//			}
//			return bits;
			// FIXME complete the method body
			throw new UnsupportedOperationException( "method body not implemented yet" );
		}

		@Override
		public byte[] doTransform ( boolean[] bits ) {
			final int bytesNumber = bits.length/8;
			final int offset = bits.length%8;
			byte[] bytes = new byte[bytesNumber];
			byte tmpByte;
			for(int block=bytesNumber-1; block>=0; block--) {
				tmpByte = 0;
				for(int bit=8-1; bit>=0; bit--) {
					tmpByte += bits[block*8+bit-offset] ? 1 : 0;
					tmpByte = Integer.valueOf(tmpByte<<8).byteValue();
				}
				bytes[block] = tmpByte;
			}
			return bytes;
		}
	}
}
