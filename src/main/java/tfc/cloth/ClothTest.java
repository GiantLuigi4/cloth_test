package tfc.cloth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import tfc.cloth.phys.*;
import tfc.cloth.shapes.ClothGen;

import java.util.ArrayList;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("cloth_test")
public class ClothTest {
	private static int width, height;
	private static Cloth dummyCloth;
	
	public ClothTest() {
		MinecraftForge.EVENT_BUS.addListener(ClothTest::postRenderSky);
		MinecraftForge.EVENT_BUS.addListener(ClothTest::onTick);
		MinecraftForge.EVENT_BUS.addListener(ClothTest::onEntityTick);
	}
	
	private static final MCTracer tracer = new MCTracer();
	private static final Vector3 gravity = new Vector3(0, -0.01, 0);
	
	protected static Vector3[] onlyNonNull(Vector3... input) {
		ArrayList<Vector3> list = new ArrayList<>();
		for (Vector3 vector3 : input) {
			if (vector3 == null) continue;
			list.add(vector3);
		}
		return list.toArray(new Vector3[0]);
	}
	
	private static final Vector3 CoM = new Vector3(0, 0, 0);
	
	private static void tick() {
		tracer.setLevel(Minecraft.getInstance().level);
//		for (int i = 0; i < 128; i++) dummyCloth.getOrderedPoints()[i].getPos().set(0, 0, i);
//		for (AbstractPoint orderedPoint : dummyCloth.getOrderedPoints()) {
//			orderedPoint.getVeloc().add(new Vector3(0, new Random().nextFloat() * 10, 0));
//		}
		
		gravity.set(0, -0.001, 0);
		dummyCloth.tick(tracer, new Vector3(0, 0, 0), (pos) -> gravity);
	}
	
	private static void onEntityTick(LivingEvent.LivingUpdateEvent event) {
		if (dummyCloth == null) return;

//		if (true) return;
		
		double colliderSize = Math.max(
				2, Math.max(
						event.getEntity().getBbWidth(), event.getEntity().getBbHeight()
				)
		);
		
		if (!event.getEntity().level.isClientSide) {
			if (event.getEntity() instanceof Player)
				return;
		}
		
		Vector3 delta = new Vector3(0, 0, 0);
		Vector3 worker = new Vector3(0, 0, 0);
		
		Vector3 pos = new Vector3(
				event.getEntity().position().x,
				event.getEntity().position().y + colliderSize / 2,
				event.getEntity().position().z
		);
		
		double count = 0;
		
		for (AbstractPoint orderedPoint : dummyCloth.getOrderedPoints()) {
			if (pos.distance(
					orderedPoint.getPos().x,
					orderedPoint.getPos().y,
					orderedPoint.getPos().z
			) < colliderSize / 2) {
				worker.set(pos);
				
				worker.setDistance(orderedPoint.getPos(), colliderSize / 2);
				
				double ln = event.getEntity().getDeltaMovement().length();
				if (ln < 0.4) ln = 0;
				else
					ln *= 3;
				
				Vector3 impulse = new Vector3(
						worker.x - pos.x,
						worker.y - pos.y,
						worker.z - pos.z
				).scl(ln + 1);
				
				orderedPoint.push(impulse.scl(-0.6));
				
				delta.add(worker.sub(pos));
				
				ln = event.getEntity().getDeltaMovement().length();
				if (ln < 1) ln = 1;
				ln = Math.pow(ln + ln, 3);
				ln /= 8;
				
				count += (dummyCloth.isStrongCollisions() ? 1 : 2) / ln;
			}
		}
		
		if (count != 0) {
			delta.scl((1d / count));

//			double dot = event.getEntity().getDeltaMovement().dot(new Vec3(
//					delta.x, delta.y, delta.z
//			));
//			if (dot < 0) {
//				event.getEntity().setDeltaMovement(
//						event.getEntity().getDeltaMovement().x / 1.5,
//						event.getEntity().getDeltaMovement().y / 1.5,
//						event.getEntity().getDeltaMovement().z / 1.5
//				);
//			}
			
			if (dummyCloth.isExtraStrongCollisions()) {
				if (
						Math.signum(event.getEntity().getDeltaMovement().x) != Math.signum(delta.x)
				) event.getEntity().setDeltaMovement(
						event.getEntity().getDeltaMovement().x / 2, event.getEntity().getDeltaMovement().y, event.getEntity().getDeltaMovement().z
				);
				if (
						Math.signum(event.getEntity().getDeltaMovement().y) != Math.signum(delta.y)
				) event.getEntity().setDeltaMovement(
						event.getEntity().getDeltaMovement().x, event.getEntity().getDeltaMovement().y / 2, event.getEntity().getDeltaMovement().z
				);
				if (
						Math.signum(event.getEntity().getDeltaMovement().z) != Math.signum(delta.z)
				) event.getEntity().setDeltaMovement(
						event.getEntity().getDeltaMovement().x, event.getEntity().getDeltaMovement().y, event.getEntity().getDeltaMovement().z / 2
				);
			}
			
			delta.scl(dummyCloth.getCollisionStrength());
			event.getEntity().push(delta.x, delta.y, delta.z);
			
			if (delta.y > 0)
				event.getEntity().setOnGround(true);
		}
	}
	
	private static void onTick(TickEvent.ClientTickEvent event) {
//		if (dummyCloth == null) return;
//		tick();
	}
	
	public static void postRenderSky(RenderLevelStageEvent event) {
		if (!event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_SKY)) return;

//		if (true) return;
		
		CoM.set(0, 0, 0);
		
		if (true) {
			PoseStack stack = event.getPoseStack();
			stack.pushPose();
			stack.translate(
					-event.getCamera().getPosition().x,
					-event.getCamera().getPosition().y,
					-event.getCamera().getPosition().z
			);
			
			stack.popPose();
			
			if (dummyCloth == null) {
//				Pair<List<Vector3>, List<Face>> pair = MeatballSphere.icosphere(3);
//				Pair<List<Vector3>, List<Face>> pair = IcoSphere.icosphere(4);
//				dummyCloth = ClothGen.generate(pair.getFirst(), pair.getSecond());
				
				width = 101;
				height = 101;
				boolean structured = false;
				
				dummyCloth = ClothGen.genSquare(width, height, structured)
						.setCollisionStrength(2)
						.setStrongCollisions(false)
						.setExtraStrongCollisions(true)
				;
//				for (AbstractPoint orderedPoint : dummyCloth.getOrderedPoints())
//					((Point) orderedPoint).setDamping(0.98);
				
				for (AbstractPoint orderedPoint : dummyCloth.getOrderedPoints()) {
					orderedPoint.getPos().add(-24, 70, 58);
					((Point) orderedPoint).lastPos.add(-24, 70, 58);
				}
				
				for (AbstractPoint orderedPoint : dummyCloth.getOrderedPoints()) {
					orderedPoint.constraint = new Constraint() {
						@Override
						public void apply(AbstractPoint point) {
							if (point instanceof Point pt) {
//								double divisor = 200;
//
//								pt.push(
//										new Vector3(
//
//												Math.abs(
//														Math.cos(
//																Minecraft.getInstance().level.getGameTime()
//														) * Math.sin(
//																Minecraft.getInstance().level.getGameTime() * 2
//														)) / divisor,
//												0f,
//												Math.abs(
//														Math.cos(
//																Minecraft.getInstance().level.getGameTime()
//														) * Math.sin(
//																Minecraft.getInstance().level.getGameTime() * 2
//														)) / (divisor * 2)
//										)
//								);
							}
						}
					};
				}
			}
		}
		
		if (dummyCloth == null) return;
		
		int count = 0;
		for (AbstractPoint orderedPoint : dummyCloth.getOrderedPoints()) {
			CoM.add(orderedPoint.getPos());
			count++;
		}
		CoM.scl(1d / count);
		
		tick();
		
		PoseStack stack = event.getPoseStack();
		stack.pushPose();
		stack.translate(
				-event.getCamera().getPosition().x,
				-event.getCamera().getPosition().y,
				-event.getCamera().getPosition().z
		);
		
		Vector4f vec4f = new Vector4f();
		Vector4f vec4f1 = new Vector4f();
		
		Vector3 norm = new Vector3(0, 0, 0);

//		for (AbstractPoint orderedPoint : dummyCloth.getOrderedPoints()) {
//			Vector3 pos = orderedPoint.getPos();
//			Vector3[] refs;
//			if (orderedPoint instanceof Point) {
//				refs = ((Point) orderedPoint).getRefs();
//			} else {
//				refs = new Vector3[0];
//			}
//
//			vec4f1.set((float) pos.x, (float) pos.y, (float) pos.z, 1f);
//			vec4f1.transform(stack.last().pose());
//
////			float[] col0 = new float[]{i / ptCount, i / ptCount, i / ptCount};
//			float[] col0 = new float[]{0.5f, 0.5f, 0.5f};
//
//			for (Vector3 ref : refs) {
//				vec4f.set((float) ref.x, (float) ref.y, (float) ref.z, 1f);
//				vec4f.transform(stack.last().pose());
//
//				ref.calcNormal(pos, pos, norm);
////				float[] col1 = new float[]{i / ptCount, i / ptCount, i / ptCount};
//				float[] col1 = col0;
//
//				consumer.vertex(vec4f.x(), vec4f.y(), vec4f.z()).color(col1[0], col1[1], col1[2], 0.25f).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
//				consumer.vertex(vec4f1.x(), vec4f1.y(), vec4f1.z()).color(col0[0], col0[1], col0[2], 0.25f).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
//			}
//		}
		
		VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height - 1; y++) {
				AbstractPoint orderedPoint = dummyCloth.getOrderedPoints()[x + (y * width)];
				AbstractPoint ref = dummyCloth.getOrderedPoints()[x + ((y + 1) * width)];
				Vector3 pos = orderedPoint.getPos();
				
				vec4f.set((float) pos.x, (float) pos.y, (float) pos.z, 1f);
				vec4f.transform(stack.last().pose());
				
				pos = ref.getPos();
				
				vec4f1.set((float) pos.x, (float) pos.y, (float) pos.z, 1f);
				vec4f1.transform(stack.last().pose());
				
				double sqrt = Math.sqrt((x - width / 2d) * (x - width / 2d) + (y - width / 2d) * (y - width / 2d));
				sqrt /= Math.sqrt(width * width + height * height);
				sqrt *= 2;
				float[] col = new float[]{
						(float) sqrt,
						(float) sqrt,
						(float) sqrt
				};
				consumer.vertex(vec4f.x(), vec4f.y(), vec4f.z()).color(col[0], col[1], col[2], 0.25f).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
				
				sqrt = Math.sqrt((x - width / 2d) * (x - width / 2d) + (y - width / 2d + 1) * (y - width / 2d + 1));
				sqrt /= Math.sqrt(width * width + height * height);
				sqrt *= 2;
				col = new float[]{
						(float) sqrt,
						(float) sqrt,
						(float) sqrt
				};
				consumer.vertex(vec4f1.x(), vec4f1.y(), vec4f1.z()).color(col[0], col[1], col[2], 0.25f).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
			}
		}
		
		for (int x = 0; x < width - 1; x++) {
			for (int y = 0; y < height; y++) {
				AbstractPoint orderedPoint = dummyCloth.getOrderedPoints()[x + (y * width)];
				AbstractPoint ref = dummyCloth.getOrderedPoints()[(x + 1) + (y * width)];
				Vector3 pos = orderedPoint.getPos();
				
				vec4f.set((float) pos.x, (float) pos.y, (float) pos.z, 1f);
				vec4f.transform(stack.last().pose());
				
				pos = ref.getPos();
				
				vec4f1.set((float) pos.x, (float) pos.y, (float) pos.z, 1f);
				vec4f1.transform(stack.last().pose());
				
				double sqrt = Math.sqrt((x - width / 2d) * (x - width / 2d) + (y - width / 2d) * (y - width / 2d));
				sqrt /= Math.sqrt(width * width + height * height);
				sqrt *= 2;
				float[] col = new float[]{
						(float) sqrt,
						(float) sqrt,
						(float) sqrt
				};
				consumer.vertex(vec4f.x(), vec4f.y(), vec4f.z()).color(col[0], col[1], col[2], 0.25f).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
				
				sqrt = Math.sqrt((x - width / 2d + 1) * (x - width / 2d + 1) + (y - width / 2d) * (y - width / 2d));
				sqrt /= Math.sqrt(width * width + height * height);
				sqrt *= 2;
				col = new float[]{
						(float) sqrt,
						(float) sqrt,
						(float) sqrt
				};
				consumer.vertex(vec4f1.x(), vec4f1.y(), vec4f1.z()).color(col[0], col[1], col[2], 0.25f).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
			}
		}
		
		LevelRenderer.renderLineBox(stack, consumer, new AABB(CoM.x - 0.01, CoM.y - 0.01, CoM.z - 0.01, CoM.x + 0.01, CoM.y + 0.01, CoM.z + 0.01), 0, 0, 0, 1f);
		
		stack.popPose();
	}
}
