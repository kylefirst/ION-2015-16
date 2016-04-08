package main;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import lejos.hardware.Bluetooth;
import lejos.remote.nxt.NXTCommConnector;
import lejos.remote.nxt.NXTConnection;

/**
 * This class receives the parking instructions over bluetooth.
 *
 * @author jacob
 *
 */
public class BluetoothReceiver {

	/**
	 * Connects to the command center using a serial connection and gets the data.
	 *
	 * @return the data. The first 3 values are the parking lots, and the fourth value is the starting base.
	 * @throws IOException
	 *             if there is a problem with bluetooth.
	 */
	public static int[] getData() throws IOException {
	
		NXTCommConnector connector = Bluetooth.getNXTCommConnector();
		NXTConnection con = connector.waitForConnection(0, NXTConnection.RAW);
		ION2016.report("Connected to command center");
		
		DataInputStream dis = con.openDataInputStream();
		int[] result = new int[4];
		
		for (int x = 0; x < result.length; x++) {
			result[x] = Integer.parseInt((char) dis.read() + "");
			if (result[x] == 0)
				result[x] = 10;
		}
		
		dis.close();
		con.close();
		return result;
	}
	
	/**
	 * Parses the raw data returned by {@link #getData()} to get the parking lot numbers.
	 *
	 * @param rawData
	 *            the raw data returned by {@link #getData()}.
	 * @return the parking lot numbers.
	 */
	public static Set<Integer> getParkingLots(int[] rawData) {
	
		Set<Integer> result = new TreeSet<>();
		for (int x = 0; x < 3; x++)
			result.add(rawData[x]);
		return result;
	}
	
	/**
	 * Parses the raw data returned by {@link #getData()} to get the starting lot.
	 *
	 * @param rawData
	 *            the raw data returned by {@link #getData()}.
	 * @return the starting base. 1 is the old base, 2 is the new base.
	 */
	public static int getStartingBase(int[] rawData) {
	
		return rawData[3];
	}

	/**
	 * Tests bluetooth!
	 *
	 * @param args
	 *            nothing.
	 */
	public static void main(String[] args) {
	
		try {
			int[] data = getData();
			ION2016.report("Got parking data");
			System.out.println(getParkingLots(data));
			System.out.println(getStartingBase(data));
		} catch (IOException e) {
			ION2016.report("Failed to use bluetooth");
			e.printStackTrace();
		}

	}
}