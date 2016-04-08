package utilities.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * This class contains static methods to facilitate input and output.
 *
 * @author Jacob Glueck
 */
public class IO {
	
	/**
	 * A private constructor which prevents initialization.
	 */
	private IO() {
	
	}
	
	/**
	 * Saves data to a file.
	 *
	 * @param fname
	 *            The file name.
	 *
	 * @param data
	 *            The data.
	 * @throws IOException
	 *             In the event of any file errors, the exception will be propagated up the call stack.
	 */
	public static void save(String fname, byte... data) throws IOException {
	
		File file = new File(fname);
		FileOutputStream out = new FileOutputStream(file);
		out.write(data);
		out.close();
	}
	
	/**
	 * Reads data from a file.
	 *
	 * @param fname
	 *            The file name.
	 * @return The data.
	 * @throws IOException
	 *             In the event of any file errors, the exception will be propagated up the call stack.
	 */
	public static byte[] read(String fname) throws IOException {
	
		File file = new File(fname);
		FileInputStream in = new FileInputStream(file);
		byte[] datab = new byte[in.available()];
		in.read(datab);
		in.close();
		return datab;
	}
	
	/**
	 * Saves data to a file.
	 *
	 * @param fname
	 *            The file name.
	 * @param serializer
	 *            The serializer used to serialize the data.
	 *
	 * @param data
	 *            The data.
	 * @throws IOException
	 *             In the event of any file errors, the exception will be propagated up the call stack.
	 */
	public static <T> void save(String fname, Serializer<T> serializer, @SuppressWarnings("unchecked") T... data) throws IOException {
	
		save(fname, serialize(serializer, data));
	}
	
	/**
	 * Reads data from a file.
	 *
	 * @param fname
	 *            The file name.
	 * @param serializer
	 *            The serializer used to deserialize the data.
	 * @return The data.
	 * @throws IOException
	 *             In the event of any file errors, the exception will be propagated up the call stack.
	 */
	public static <T> T[] read(String fname, Serializer<T> serializer) throws IOException {
	
		return deserialize(serializer, read(fname));
	}
	
	/**
	 * Converts the data to a byte array.
	 *
	 * @param data
	 *            The data.
	 * @return The byte array.
	 */
	public static byte[] serialize(String... data) {
	
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(bytes);
		
		try {
			dataOut.writeInt(data.length);
			for (String d : data)
				dataOut.writeInt(d.length());
			for (String d : data)
				for (int i = 0; i < d.length(); i++)
					dataOut.writeChar(d.charAt(i));
			dataOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bytes.toByteArray();
	}
	
	/**
	 * Converts a byte array of data back into a string array.
	 *
	 * @param input
	 *            The data.
	 * @return The string array.
	 */
	public static String[] deserializeString(byte[] input) {
	
		ByteArrayInputStream bytes = new ByteArrayInputStream(input);
		DataInputStream dataIn = new DataInputStream(bytes);
		
		String[] data = null;
		try {
			data = new String[dataIn.readInt()];
			int[] lengths = new int[data.length];
			for (int i = 0; i < lengths.length; i++)
				lengths[i] = dataIn.readInt();
			for (int i = 0; i < data.length; i++) {
				data[i] = "";
				for (int p = 0; p < lengths[i]; p++)
					data[i] += dataIn.readChar();
			}
			dataIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}
	
	/**
	 * Converts the data to a byte array.
	 *
	 * @param data
	 *            The data.
	 * @return The byte array.
	 */
	public static byte[] serialize(int... data) {
	
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(bytes);
		
		try {
			dataOut.writeInt(data.length);
			for (int d : data)
				dataOut.writeInt(d);
			dataOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bytes.toByteArray();
	}
	
	/**
	 * Converts a byte array of data back into a string array.
	 *
	 * @param input
	 *            The data.
	 * @return The int array.
	 */
	public static int[] deserializeInt(byte[] input) {
	
		ByteArrayInputStream bytes = new ByteArrayInputStream(input);
		DataInputStream dataIn = new DataInputStream(bytes);
		
		int[] data = null;
		try {
			data = new int[dataIn.readInt()];
			for (int i = 0; i < data.length; i++)
				data[i] = dataIn.readInt();
			dataIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}
	
	/**
	 * Converts the data to a byte array.
	 *
	 * @param data
	 *            The data.
	 * @return The byte array.
	 */
	public static byte[] serialize(double... data) {
	
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(bytes);
		
		try {
			dataOut.writeInt(data.length);
			for (double d : data)
				dataOut.writeDouble(d);
			dataOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bytes.toByteArray();
	}
	
	/**
	 * Converts a byte array of data back into a string array.
	 *
	 * @param input
	 *            The data.
	 * @return The double array.
	 */
	public static double[] deserializeDouble(byte[] input) {
	
		ByteArrayInputStream bytes = new ByteArrayInputStream(input);
		DataInputStream dataIn = new DataInputStream(bytes);
		
		double[] data = null;
		try {
			data = new double[dataIn.readInt()];
			for (int i = 0; i < data.length; i++)
				data[i] = dataIn.readDouble();
			dataIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}
	
	/**
	 * Converts the data to a byte array.
	 *
	 * @param serializer
	 *            The serializer.
	 * @param data
	 *            The data.
	 * @return The byte array.
	 */
	public static <T> byte[] serialize(Serializer<T> serializer, @SuppressWarnings("unchecked") T... data) {
	
		byte[][] datab = new byte[data.length][1];
		for (int x = 0; x < datab.length; x++)
			datab[x] = serializer.serialize(data[x]);
		return serialize(datab);
	}
	
	/**
	 * Converts a byte array of data back into a string array.
	 *
	 * @param serializer
	 *            The serializer.
	 * @param input
	 *            The data.
	 * @return The byte matrix.
	 */
	public static <T> T[] deserialize(Serializer<T> serializer, byte[] input) {
	
		byte[][] datab = deserializeBytes(input);
		T[] result = serializer.createResultArray(datab.length);
		for (int x = 0; x < result.length; x++)
			result[x] = serializer.deserialize(datab[x]);
		return result;
	}
	
	/**
	 * Converts the data to a byte array.
	 *
	 * @param data
	 *            The data.
	 * @return The byte array.
	 */
	public static byte[] serialize(byte[]... data) {
	
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(bytes);
		
		try {
			dataOut.writeInt(data.length);
			for (byte[] d : data)
				dataOut.writeInt(d.length);
			for (byte[] d : data)
				dataOut.write(d);
			dataOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bytes.toByteArray();
	}
	
	/**
	 * Converts a byte array of data back into a string array.
	 *
	 * @param input
	 *            The data.
	 * @return The byte matrix.
	 */
	public static byte[][] deserializeBytes(byte[] input) {
	
		ByteArrayInputStream bytes = new ByteArrayInputStream(input);
		DataInputStream dataIn = new DataInputStream(bytes);
		
		byte[][] data = null;
		try {
			data = new byte[dataIn.readInt()][1];
			for (int x = 0; x < data.length; x++)
				data[x] = new byte[dataIn.readInt()];
			for (byte[] d : data)
				dataIn.read(d);
			dataIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return data;
	}
	
	/**
	 * Saves the map to a file. 1 entry per line, with the key, then a tab, then value.toString().
	 *
	 * @param data
	 *            The data.
	 * @param fname
	 *            The name of the file.
	 * @throws FileNotFoundException
	 *             if the file is not found.
	 */
	public static void saveMap(Map<String, ?> data, String fname) throws FileNotFoundException {
	
		File f = new File(fname);
		PrintStream ps = new PrintStream(new FileOutputStream(f));
		for (String key : data.keySet())
			ps.println(key + "\t" + data.get(key).toString());
		ps.close();
	}
	
	/**
	 * Reads the map from a file. 1 entry per line, with the key, then a tab, then value.toString().
	 *
	 * @param fname
	 *            The name of the file.
	 * @return returns a map representing the data found in the file.
	 * @throws FileNotFoundException
	 *             if the file is not found.
	 */
	public static Map<String, String> readMap(String fname) throws FileNotFoundException {
	
		File f = new File(fname);
		Scanner s = new Scanner(f);
		Map<String, String> result = new TreeMap<String, String>();
		while (s.hasNextLine()) {
			String str = s.nextLine();
			String[] split = str.split("\t", 2);
			result.put(split[0], split[1]);
		}
		s.close();
		return result;
	}
}