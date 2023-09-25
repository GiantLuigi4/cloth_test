package tfc.cloth;

import com.google.common.xml.XmlEscapers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import tfc.cloth.phys.Cloth;
import tfc.cloth.phys.Point;
import tfc.cloth.phys.StickyCloth;
import tfc.cloth.phys.Vector3;
import tfc.cloth.shapes.ClothGen;
import tfc.cloth.shapes.Face;
import tfc.cloth.shapes.IcoSphere;
import tfc.cloth.shapes.MeatballSphere;
import tfc.cloth.util.ClothMesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("cloth_test")
public class ClothTest {
	private static int width, height, depth;
	private static Cloth dummyCloth;
	private static ClothMesh mesh;
	
	public ClothTest() {
		MinecraftForge.EVENT_BUS.addListener(ClothTest::postRenderSky);
		MinecraftForge.EVENT_BUS.addListener(ClothTest::onTick);
		MinecraftForge.EVENT_BUS.addListener(ClothTest::onEntityTick);
		
		MinecraftForge.EVENT_BUS.addListener(ClothTest::onAttack);
		MinecraftForge.EVENT_BUS.addListener(ClothTest::onBlockClick);
		MinecraftForge.EVENT_BUS.addListener(ClothTest::onEmptyClick);
	}
	
	// TODO: this is trash, but it works
	private static void doAttack(Player player) {
		if (dummyCloth == null) return;
		
		if (player.getMainHandItem().getItem() instanceof SwordItem) {
			Vec3 look = player.getLookAngle();
			
			if (new AABB(
					player.getEyePosition().x,
					player.getEyePosition().y,
					player.getEyePosition().z,
					player.getEyePosition().x + look.x,
					player.getEyePosition().y + look.y,
					player.getEyePosition().z + look.z
			).intersects(dummyCloth.getBounds())) {
				Vec3 pos = player.getEyePosition().add(look);
				
				for (Point orderedPoint : dummyCloth.getOrderedPoints()) {
					
					if (orderedPoint.pos.distance(pos.x, pos.y, pos.z) < 1) {
						List<Point> list = new ArrayList<>(Arrays.asList(dummyCloth.getOrderedPoints()));
						list.remove(orderedPoint);
						dummyCloth.setOrderedPoints(list.toArray(new Point[0]));
						
						for (Point refObj : orderedPoint.getRefObjs()) {
							List<Vector3> refs = new ArrayList<>(Arrays.asList(refObj.getRefs()));
							refs.remove(orderedPoint.pos);
							refObj.setRefs(refs.toArray(new Vector3[0]));
							
							List<Point> points = new ArrayList<>(Arrays.asList(refObj.getRefObjs()));
							points.remove(orderedPoint);
							refObj.setRefObjects(points.toArray(new Point[0]));
						}
						
						orderedPoint.setRefs(new Vector3[0]);
						orderedPoint.setRefObjects(new Point[0]);
					}
				}
			}
		}
	}
	
	private static void onAttack(AttackEntityEvent event) {
		doAttack(event.getPlayer());
	}
	
	private static void onBlockClick(PlayerInteractEvent.LeftClickBlock event) {
		doAttack(event.getPlayer());
	}
	
	private static void onEmptyClick(PlayerInteractEvent.LeftClickEmpty event) {
		doAttack(event.getPlayer());
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
		
		dummyCloth.collide(event.getEntity());
	}
	
	private static void onTick(TickEvent.ClientTickEvent event) {
//		if (dummyCloth == null) return;
//		tick();
	}
	
	public static void postRenderSky(RenderLevelStageEvent event) {
		if (!event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_SKY)) return;
		
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
				Pair<List<Vector3>, List<Face>> pair = MeatballSphere.icosphere(3);
//				Pair<List<Vector3>, List<Face>> pair = IcoSphere.icosphere(3);
				for (Vector3 vector3 : pair.getFirst()) {
					vector3.mul(6, 6, 6);
				}
				List<Point> points = ClothGen.generate(pair.getFirst(), pair.getSecond());

				width = 101;
				height = 101;
				boolean structured = true;

//				List<Point> points = ClothGen.genCube(width, 1d / 2, structured, false);
//				List<Point> points = ClothGen.genSquare(width, height, 1d / 2, structured);

				for (Point orderedPoint : points) {
					orderedPoint.setAware(true);
//					orderedPoint.setDamping(0.98);
//					orderedPoint.setDamping(0.995);
					orderedPoint.setDamping(0.95);

					orderedPoint.springStrength = 1;

//					orderedPoint.getPos().add(-24, 100, 58);
					orderedPoint.getPos().add(-24, 80, 58);
					orderedPoint.lastPos.set(orderedPoint.getPos());
				}

				dummyCloth = new StickyCloth(points)
						.setCollisionStrength(2)
						.setStrongCollisions(false)
						.setExtraStrongCollisions(true)
				;
				mesh = new ClothMesh(dummyCloth, width, height);

				for (Point orderedPoint : dummyCloth.getOrderedPoints()) {
//					orderedPoint.constraint = point -> {
//						double divisor = 200;
//
//						point.push(
//								new Vector3(
//
//										Math.abs(
//												Math.cos(
//														Minecraft.getInstance().level.getGameTime()
//												) * Math.sin(
//														Minecraft.getInstance().level.getGameTime() * 2
//												)) / divisor,
//										0f,
//										Math.abs(
//												Math.cos(
//														Minecraft.getInstance().level.getGameTime()
//												) * Math.sin(
//														Minecraft.getInstance().level.getGameTime() * 2
//												)) / (divisor * 2)
//								)
//						);
//					};
				}
			}
		}
		
		if (dummyCloth == null) return;
		
		int count = 0;
		for (Point orderedPoint : dummyCloth.getOrderedPoints()) {
			CoM.add(orderedPoint.getPos());
			count++;
		}
		CoM.scl(1d / count);

		tick();

//		if (true) return;
		
		PoseStack stack = event.getPoseStack();
		stack.pushPose();
		stack.translate(
				-event.getCamera().getPosition().x,
				-event.getCamera().getPosition().y,
				-event.getCamera().getPosition().z
		);
		VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);

		boolean debug = true;
		boolean debugVeloc = true;
		boolean debugConnection = true;

//		mesh.draw(consumer, stack);
		if (debug) {
			Vector4f vec4f = new Vector4f();
			Vector4f vec4f1 = new Vector4f();
			Vector3 norm = new Vector3(0f, 0f, 0f);
			for (Point orderedPoint : dummyCloth.getOrderedPoints()) {
				if (orderedPoint.core) continue;

				Vector3 pos = orderedPoint.getPos();

				Vector3[] refs = orderedPoint.getRefs();

				vec4f1.set((float) pos.x, (float) pos.y, (float) pos.z, 1f);
				vec4f1.transform(stack.last().pose());

				float[] col0 = new float[]{0.5f, 0.5f, 0.5f};

				if (debugConnection) {
					if (orderedPoint.getRefObjs() == null) {
						for (Vector3 ref : refs) {
							vec4f.set((float) ref.x, (float) ref.y, (float) ref.z, 1f);
							vec4f.transform(stack.last().pose());

							ref.calcNormal(pos, pos, norm);
							float[] col1 = new float[]{0.5f, 0.5f, 0.5f};

							consumer.vertex(vec4f.x(), vec4f.y(), vec4f.z()).color(col1[0], col1[1], col1[2], 1).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
							consumer.vertex(vec4f1.x(), vec4f1.y(), vec4f1.z()).color(col0[0], col0[1], col0[2], 1).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
						}
					} else {
						for (Point refObj : orderedPoint.getRefObjs()) {
							if (refObj.core) continue;

							Vector3 ref = refObj.getPos();
							vec4f.set((float) ref.x, (float) ref.y, (float) ref.z, 1f);
							vec4f.transform(stack.last().pose());

							ref.calcNormal(pos, pos, norm);
							float[] col1 = new float[]{0.5f, 0.5f, 0.5f};

							consumer.vertex(vec4f.x(), vec4f.y(), vec4f.z()).color(col1[0], col1[1], col1[2], 1).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
							consumer.vertex(vec4f1.x(), vec4f1.y(), vec4f1.z()).color(col0[0], col0[1], col0[2], 1).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
						}
					}
				}
				if (debugVeloc) {
					Vector3 veloc = orderedPoint.getVeloc().add(orderedPoint.pos);
					vec4f.set((float) veloc.x, (float) veloc.y, (float) veloc.z, 1f);
					vec4f.transform(stack.last().pose());

					veloc.calcNormal(pos, pos, norm);
					float[] col1 = new float[]{1, 0, 0};

					consumer.vertex(vec4f.x(), vec4f.y(), vec4f.z()).color(col1[0], col1[1], col1[2], 1).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
					consumer.vertex(vec4f1.x(), vec4f1.y(), vec4f1.z()).color(col1[0], col1[1], col1[2], 1).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
				}
			}
		}
		LevelRenderer.renderLineBox(stack, consumer, new AABB(CoM.x - 0.01, CoM.y - 0.01, CoM.z - 0.01, CoM.x + 0.01, CoM.y + 0.01, CoM.z + 0.01), 0, 0, 0, 1f);
		stack.popPose();
	}
}
