package com.sokiki.bindmod.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sokiki.bindmod.BindModConfig;
import com.sokiki.bindmod.Waypoint;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * 3D-метка в мире: столб света, кольцо на земле, рамка и подпись с расстоянием.
 */
public final class WaypointRenderer {
	private static final double MAX_DISTANCE = 512.0 * 512.0;

	private WaypointRenderer() {
	}

	public static void register(BindModConfig config) {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> render(context, config));
	}

	private static void render(WorldRenderContext context, BindModConfig config) {
		Minecraft mc = Minecraft.getInstance();
		if (!config.waypointsEnabled || mc.level == null || mc.player == null || config.waypoints.isEmpty()) {
			return;
		}
		PoseStack pose = context.matrixStack();
		MultiBufferSource consumers = context.consumers();
		Camera camera = context.camera();
		if (pose == null || consumers == null || camera == null) {
			return;
		}

		String dimension = mc.level.dimension().location().toString();
		Vec3 cam = camera.getPosition();
		float time = (System.currentTimeMillis() % 1_000_000L) / 1000.0f;

		pose.pushPose();
		pose.translate(-cam.x, -cam.y, -cam.z);
		VertexConsumer lines = consumers.getBuffer(RenderType.lines());

		for (Waypoint waypoint : config.waypoints) {
			if (waypoint.dimension != null && !waypoint.dimension.isEmpty() && !waypoint.dimension.equals(dimension)) {
				continue;
			}
			double distanceSq = cam.distanceToSqr(waypoint.x, waypoint.y, waypoint.z);
			if (distanceSq > MAX_DISTANCE) {
				continue;
			}
			drawMarker(lines, pose.last().pose(), waypoint, time);
		}

		if (consumers instanceof MultiBufferSource.BufferSource bufferSource) {
			bufferSource.endBatch(RenderType.lines());
		}

		for (Waypoint waypoint : config.waypoints) {
			if (waypoint.dimension != null && !waypoint.dimension.isEmpty() && !waypoint.dimension.equals(dimension)) {
				continue;
			}
			double distanceSq = cam.distanceToSqr(waypoint.x, waypoint.y, waypoint.z);
			if (distanceSq > MAX_DISTANCE) {
				continue;
			}
			drawLabel(mc, pose, consumers, camera, waypoint, Math.sqrt(distanceSq), time);
		}
		pose.popPose();
	}

	private static void drawMarker(VertexConsumer consumer, Matrix4f matrix, Waypoint waypoint, float time) {
		int rgb = waypoint.rgb();
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;

		double x = waypoint.x;
		double y = waypoint.y;
		double z = waypoint.z;

		// столб света вверх
		int beamColor = 120;
		line(consumer, matrix, x, y, z, x, y + 24.0, z, r, g, b, beamColor);

		// рамка вокруг точки
		double half = 0.5;
		double bottom = y;
		double top = y + 1.0;
		int alpha = (int) (170 + 60 * Math.sin(time * 2.2f));
		alpha = Mth.clamp(alpha, 120, 255);

		// вертикальные рёбра
		line(consumer, matrix, x - half, bottom, z - half, x - half, top, z - half, r, g, b, alpha);
		line(consumer, matrix, x + half, bottom, z - half, x + half, top, z - half, r, g, b, alpha);
		line(consumer, matrix, x - half, bottom, z + half, x - half, top, z + half, r, g, b, alpha);
		line(consumer, matrix, x + half, bottom, z + half, x + half, top, z + half, r, g, b, alpha);

		// низ
		line(consumer, matrix, x - half, bottom, z - half, x + half, bottom, z - half, r, g, b, alpha);
		line(consumer, matrix, x + half, bottom, z - half, x + half, bottom, z + half, r, g, b, alpha);
		line(consumer, matrix, x + half, bottom, z + half, x - half, bottom, z + half, r, g, b, alpha);
		line(consumer, matrix, x - half, bottom, z + half, x - half, bottom, z - half, r, g, b, alpha);

		// верх
		line(consumer, matrix, x - half, top, z - half, x + half, top, z - half, r, g, b, alpha);
		line(consumer, matrix, x + half, top, z - half, x + half, top, z + half, r, g, b, alpha);
		line(consumer, matrix, x + half, top, z + half, x - half, top, z + half, r, g, b, alpha);
		line(consumer, matrix, x - half, top, z + half, x - half, top, z - half, r, g, b, alpha);

		// кольцо на земле (два кольца, вращаются в разные стороны)
		float ringY = (float) (y + 0.03);
		double radius = 1.6;
		int segments = 48;
		for (int i = 0; i < segments; i++) {
			float a1 = (float) (i * Mth.TWO_PI / segments + time * 0.6f);
			float a2 = (float) ((i + 1) * Mth.TWO_PI / segments + time * 0.6f);
			line(consumer, matrix,
					x + Math.cos(a1) * radius, ringY, z + Math.sin(a1) * radius,
					x + Math.cos(a2) * radius, ringY, z + Math.sin(a2) * radius,
					r, g, b, 150);
		}
		for (int i = 0; i < segments; i++) {
			float a1 = (float) (i * Mth.TWO_PI / segments - time * 0.9f);
			float a2 = (float) ((i + 1) * Mth.TWO_PI / segments - time * 0.9f);
			line(consumer, matrix,
					x + Math.cos(a1) * 0.7, ringY, z + Math.sin(a1) * 0.7,
					x + Math.cos(a2) * 0.7, ringY, z + Math.sin(a2) * 0.7,
					r, g, b, 110);
		}
	}

	private static void drawLabel(Minecraft mc, PoseStack pose, MultiBufferSource consumers, Camera camera,
								  Waypoint waypoint, double distance, float time) {
		int rgb = waypoint.rgb();
		String title = waypoint.name == null || waypoint.name.isBlank() ? "Метка" : waypoint.name;
		String meters = Math.round(distance) + " м";

		float bob = (float) Math.sin(time * 2.0f) * 0.12f;

		pose.pushPose();
		pose.translate(waypoint.x, waypoint.y + 2.2f + bob, waypoint.z);
		pose.mulPose(camera.rotation());
		pose.scale(-0.026f, -0.026f, 0.026f);

		Matrix4f matrix = pose.last().pose();
		int background = 0x66000000;
		float titleWidth = mc.font.width(title);
		float metersWidth = mc.font.width(meters);

		mc.font.drawInBatch(title, -titleWidth / 2.0f, -11, rgb | 0xFF000000, true, matrix, consumers,
				Font.DisplayMode.SEE_THROUGH, background, 0xF000F0);
		mc.font.drawInBatch(meters, -metersWidth / 2.0f, 1, 0xFFBFD8E8, false, matrix, consumers,
				Font.DisplayMode.SEE_THROUGH, background, 0xF000F0);

		pose.popPose();
	}

	private static void line(VertexConsumer consumer, Matrix4f matrix,
							 double x1, double y1, double z1, double x2, double y2, double z2,
							 int r, int g, int b, int a) {
		float dx = (float) (x2 - x1);
		float dy = (float) (y2 - y1);
		float dz = (float) (z2 - z1);
		float lengthSq = dx * dx + dy * dy + dz * dz;
		if (lengthSq > 1.0e-6f) {
			float inv = (float) Mth.fastInvSqrt(lengthSq);
			dx *= inv;
			dy *= inv;
			dz *= inv;
		} else {
			dx = 0.0f;
			dy = 1.0f;
			dz = 0.0f;
		}
		consumer.addVertex(matrix, (float) x1, (float) y1, (float) z1)
				.setColor(r, g, b, a)
				.setNormal(dx, dy, dz);
		consumer.addVertex(matrix, (float) x2, (float) y2, (float) z2)
				.setColor(r, g, b, a)
				.setNormal(dx, dy, dz);
	}
}
