package main;

import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import lejos.hardware.Button;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;
import sensing.EV3ColorSensor;
import sensing.HTColorSensor;
import utilities.Color;
import utilities.ColorPalette;

/**
 * A class used for testing.
 *
 *
 */
public class TestingClass {

	private static Thread stop = new Thread("Stopper") {

		@Override
		public void run() {
		
			while (true)
				if (Button.ESCAPE.isDown() && Button.ENTER.isDown())
					System.exit(0);
		}
	};

	/**
	 * Main.
	 *
	 * @param args
	 *            nothing.
	 * @throws InterruptedException
	 */
	public static void main(String... args) throws InterruptedException {
	
		stop.setDaemon(true);
		stop.start();
		
		System.out.println("Is playing file");

	}

	public static void sonarTest() {
	
		// lejos.hardware.sensor.EV3UltrasonicSensor
		
		lejos.hardware.sensor.EV3UltrasonicSensor sensor = new lejos.hardware.sensor.EV3UltrasonicSensor(SensorPort.S4);
		SampleProvider distanceMode = sensor.getDistanceMode();
		Scanner s = new Scanner(System.in);

		for (int x = 0; x < 100; x++) {
			// System.out.println("*****************Sample:");
			// s.nextLine();
			Delay.msDelay(1000);
			float[] sample = new float[distanceMode.sampleSize()];
			distanceMode.fetchSample(sample, 0);
			System.out.printf("%.5f m\n", sample[0]);
			// int bits = Float.floatToIntBits(sample[0] / 0.001f);
			// byte[] bytes = new byte[4];
			// bytes[0] = (byte) (bits & 0xff);
			// bytes[1] = (byte) (bits >> 8 & 0xff);
			// bytes[2] = (byte) (bits >> 16 & 0xff);
			// bytes[3] = (byte) (bits >> 24 & 0xff);
			// System.out.println("Bytes:");
			// System.out.println(Arrays.toString(bytes));
			// System.out.print("String: ");
			// System.out.println(new String(bytes));
			// System.out.println("*****************End");
		}
	}

	public static void pilotTest() throws InterruptedException {
	
		DifferentialPilot p = new DifferentialPilot(.056, .1225, Motor.A, Motor.D);
		// p.travel(.2);
		// for (double x = -.4; x <= .4; x += .001) {
		// p.arcForward(x);
		// System.out.println(x);
		// }

		while (Button.ESCAPE.isUp())
			p.steer(50);
		// Delay.msDelay(1000);
	}

	/**
	 * Tests the sensors to see how the EV3 sensors compare to the HT sensors.
	 */
	public static void colorSensorTest() {
	
		/*
		 * portMappings.leftSensor 2
		 * portMappings.rightSensor 3
		 * portMappings.frontSensor 1
		 */
		EV3ColorSensor left = new EV3ColorSensor(SensorPort.S2);
		EV3ColorSensor right = new EV3ColorSensor(SensorPort.S3);
		HTColorSensor front = new HTColorSensor(SensorPort.S1);

		System.out.println("Place sensors over black");
		Button.waitForAnyPress();
		left.calibrateBlack();
		right.calibrateBlack();
		front.calibrateBlack();

		System.out.println("Place sensors over white");
		Button.waitForAnyPress();
		left.calibrateWhite();
		right.calibrateWhite();
		front.calibrateWhite();

		while (Button.ESCAPE.isUp()) {
			Button.waitForAnyPress();
			Color lc = left.getColor();
			Color rc = right.getColor();
			Color fc = front.getColor();
			System.out.println("Distances:");
			System.out.printf("RL: %.2f RF: %.2f LF: %.2f", Color.getDistance(rc, lc), Color.getDistance(rc, fc), Color.getDistance(lc, fc));
			Button.waitForAnyPress();
			System.out.println("Colors:");
			System.out.println("Right: " + rc);
			System.out.println("Left: " + lc);
			System.out.println("Front: " + fc);
		}
	}

	/**
	 * Tests waiting for button presses
	 */
	public static void buttonTest() {
	
		// new TestingClass();
		System.out.println("Waiting for Button!");
		int bob;
		if (0 != (bob = Button.waitForAnyPress(3000)))
			System.out.println("Button Pushed!!!");
		else
			System.out.println("No Button Pushed!");
		System.out.println(bob);
		Button.waitForAnyPress();
	}

	/**
	 * Runs a program for testing color matching.
	 */
	public static void colorMatchingTest() {
	
		EV3ColorSensor leftSensor;
		ColorPalette colorPalette;
		leftSensor = new EV3ColorSensor(SensorPort.S2);
		// rightSensor = new EV3ColorSensor(SensorPort.S3);

		colorPalette = new ColorPalette();
		System.out.println("Place Sensor over Black: ");
		Button.waitForAnyPress();
		leftSensor.calibrateBlack();
		System.out.println("Place Sensor over White: ");
		Button.waitForAnyPress();
		leftSensor.calibrateWhite();

		System.out.println("Enter red: ");
		Button.waitForAnyPress();
		colorPalette.add(leftSensor.getColor(), "red");
		//
		System.out.println("Enter green: ");
		Button.waitForAnyPress();
		colorPalette.add(leftSensor.getColor(), "green");

		System.out.println("Enter blue: ");
		Button.waitForAnyPress();
		colorPalette.add(leftSensor.getColor(), "blue");

		System.out.println("Enter white: ");
		Button.waitForAnyPress();
		colorPalette.add(leftSensor.getColor(), "white");

		System.out.println("Enter brack: ");
		Button.waitForAnyPress();
		colorPalette.add(leftSensor.getColor(), "black");

		System.out.println("Enter yerro: ");
		Button.waitForAnyPress();
		colorPalette.add(leftSensor.getColor(), "yellow");

		// System.out.println("Enter yellow-black: ");
		// Button.waitForAnyPress();
		// Color c = leftSensor.getColor();
		// System.out.printf("Real:\n%.2f, %.2f, %.2f\n", c.getRed(), c.getGreen(), c.getBlue());
		// Color combined = colorPalette.getColors().get("black").blend(colorPalette.getColors().get("yellow"));
		// System.out.printf("Calculated:\n%.2f, %.2f, %.2f\n", combined.getRed(), combined.getGreen(), combined.getBlue());
		//
		// System.out.println("Enter red-black: ");
		// Button.waitForAnyPress();
		// c = leftSensor.getColor();
		// System.out.printf("Real:\n%.2f, %.2f, %.2f\n", c.getRed(), c.getGreen(), c.getBlue());
		// combined = colorPalette.getColors().get("black").blend(colorPalette.getColors().get("red"));
		// System.out.printf("Calculated:\n%.2f, %.2f, %.2f\n", combined.getRed(), combined.getGreen(), combined.getBlue());
		//
		// System.out.println("Enter blue-black: ");
		// Button.waitForAnyPress();
		// c = leftSensor.getColor();
		// System.out.printf("Real:\n%.2f, %.2f, %.2f\n", c.getRed(), c.getGreen(), c.getBlue());
		// combined = colorPalette.getColors().get("black").blend(colorPalette.getColors().get("blue"));
		// System.out.printf("Calculated:\n%.2f, %.2f, %.2f\n", combined.getRed(), combined.getGreen(), combined.getBlue());
		//
		// System.out.println("Enter white-black: ");
		// Button.waitForAnyPress();
		// c = leftSensor.getColor();
		// System.out.printf("Real:\n%.2f, %.2f, %.2f\n", c.getRed(), c.getGreen(), c.getBlue());
		// combined = colorPalette.getColors().get("black").blend(colorPalette.getColors().get("white"));
		// System.out.printf("Calculated:\n%.2f, %.2f, %.2f\n", combined.getRed(), combined.getGreen(), combined.getBlue());

		// Button.waitForAnyPress();
		Set<String> colors = new TreeSet<>();
		colors.add("red");
		colors.add("blue");
		colors.add("yellow");
		colors.add("white");

		while (Button.ESCAPE.isUp()) {

			Button.waitForAnyPress();
			Color c = leftSensor.getColor();
			System.out.printf("%.2f, %.2f, %.2f\n", c.getRed(), c.getGreen(), c.getBlue());
			System.out.println(colorPalette.getBestMatch(c));
			String blend = colorPalette.getBestBlend(colors, "black", c);
			System.out.printf("Blend: " + blend + ", %.2f\n", c.determineComposition(colorPalette.getColor(blend), colorPalette.getColor("black")));
		}
	}
}