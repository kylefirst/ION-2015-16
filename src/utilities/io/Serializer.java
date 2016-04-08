package utilities.io;

/**
 * Provides for serialization.
 * 
 * @author Jacob Glueck
 * @param <T>
 *            The type of object that this serializer can serialize.
 * 
 */
public interface Serializer<T> {
	
	/**
	 * Converts the data into a byte array.
	 * 
	 * @param data
	 *            The data.
	 * @return The byte array.
	 */
	public byte[] serialize(T data);
	
	/**
	 * Converts a byte array back into data.
	 * 
	 * @param bytes
	 *            The byte array.
	 * @return The data.
	 */
	public T deserialize(byte[] bytes);
	
	/**
	 * Creates an array which can store deserialized objects. This is necessary to ensure generic array type safety. In general, this method should
	 * return a new array of type T of the specified length. Failure to do so may cause serialization errors.
	 * 
	 * @param length
	 *            The length of the array.
	 * @return The new array.
	 */
	public T[] createResultArray(int length);
	
}
