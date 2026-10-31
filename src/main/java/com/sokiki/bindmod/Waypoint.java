package com.sokiki.bindmod;

/**
 * 3D-метка на координаты.
 */
public final class Waypoint {
	private static final int[] COLORS = {
			0x64E1FF, 0x6BE39B, 0xFFD166, 0xFF8AC1, 0xB48CFF, 0xFF6B6B
	};

	public String name;
	public double x;
	public double y;
	public double z;
	public int color;
	public String dimension;

	public Waypoint(String name, double x, double y, double z, int color, String dimension) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.color = color;
		this.dimension = dimension;
	}

	public int rgb() {
		return COLORS[Math.floorMod(this.color, COLORS.length)];
	}

	public int nextColor() {
		return Math.floorMod(this.color + 1, COLORS.length);
	}

	public static int colorCount() {
		return COLORS.length;
	}

	public static int rgbOf(int index) {
		return COLORS[Math.floorMod(index, COLORS.length)];
	}

	public String coords() {
		return Math.round(this.x) + " " + Math.round(this.y) + " " + Math.round(this.z);
	}
}
