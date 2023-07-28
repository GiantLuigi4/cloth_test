package tfc.cloth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import tfc.cloth.phys.*;
import tfc.cloth.shapes.ClothGen;
import tfc.cloth.shapes.Face;
import tfc.cloth.shapes.MeatballSphere;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("cloth_test")
public class ClothTest {
	private static Cloth dummyCloth;
	
	public ClothTest() {
		MinecraftForge.EVENT_BUS.addListener(ClothTest::postRenderSky);
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
			Vector4f vec4f = new Vector4f();
			
			VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);
			Pair<List<Vector3>, List<Face>> pair = MeatballSphere.icosphere(4);
//			for (Face face : pair.getSecond()) {
//				Vector3 vec0 = pair.getFirst().get(face.v0());
//				Vector3 vec1 = pair.getFirst().get(face.v1());
//				Vector3 vec2 = pair.getFirst().get(face.v2());
//
//				vec4f.set((float) vec0.x, (float) vec0.y, (float) vec0.z, 1f);
//				vec4f.transform(stack.last().pose());
//				consumer.vertex(vec4f.x(), vec4f.y(), vec4f.z()).color(1f, 1, 1, 1).normal(stack.last().normal(), 1, 0, 0).endVertex();
//
//				vec4f.set((float) vec1.x, (float) vec1.y, (float) vec1.z, 1f);
//				vec4f.transform(stack.last().pose());
//				consumer.vertex(vec4f.x(), vec4f.y(), vec4f.z()).color(1f, 1, 1, 1).normal(stack.last().normal(), 1, 0, 0).endVertex();
//
//				vec4f.set((float) vec2.x, (float) vec2.y, (float) vec2.z, 1f);
//				vec4f.transform(stack.last().pose());
//				consumer.vertex(vec4f.x(), vec4f.y(), vec4f.z()).color(1f, 1, 1, 1).normal(stack.last().normal(), 1, 0, 0).endVertex();
//
//				vec4f.set((float) vec0.x, (float) vec0.y, (float) vec0.z, 1f);
//				vec4f.transform(stack.last().pose());
//				consumer.vertex(vec4f.x(), vec4f.y(), vec4f.z()).color(1f, 1, 1, 1).normal(stack.last().normal(), 1, 0, 0).endVertex();
//			}
			for (Vector3 vector3 : pair.getFirst()) {
//				vector3.scl(23);
			}
			
			stack.popPose();

//			dummyCloth = ClothGen.generate(pair.getFirst(), pair.getSecond(), true);
			
			for (AbstractPoint orderedPoint : dummyCloth.getOrderedPoints()) {
				orderedPoint.constraint = new Constraint() {
					@Override
					public void apply(AbstractPoint point) {
						if (point instanceof SoftBodyPoint pt) {
							Vector3 home = new Vector3(pt.origin).add(CoM);
							
							home.sub(point.getPos());
							home.scl(1);
							point.getVeloc().add(home);
						}
					}
				};
			}
		}
		
		if (dummyCloth == null) return;
		
		int count = 0;
		for (AbstractPoint orderedPoint : dummyCloth.getOrderedPoints()) {
			CoM.add(orderedPoint.getPos());
			count++;
		}
		CoM.scl(1d / count);
		
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
		
		tracer.setLevel(Minecraft.getInstance().level);
//		for (int i = 0; i < 128; i++) dummyCloth.getOrderedPoints()[i].getPos().set(0, 0, i);
//		for (AbstractPoint orderedPoint : dummyCloth.getOrderedPoints()) {
//			orderedPoint.getVeloc().add(new Vector3(0, new Random().nextFloat() * 10, 0));
//		}
		
		dummyCloth.tick(tracer, new Vector3(0, 0, 0), (pos) -> gravity);
		
		VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);
		for (AbstractPoint orderedPoint : dummyCloth.getOrderedPoints()) {
			Vector3 pos = orderedPoint.getPos();
			Vector3[] refs;
			Vector3[] hypotRefs;
			if (orderedPoint instanceof Point) {
				refs = ((Point) orderedPoint).getRefs();
				hypotRefs = ((Point) orderedPoint).getHypotRefs();
			} else if (orderedPoint instanceof SoftBodyPoint) {
				refs = ((SoftBodyPoint) orderedPoint).getRefs();
				hypotRefs = new Vector3[0];
			} else {
				refs = new Vector3[0];
				hypotRefs = new Vector3[0];
			}
			
			vec4f1.set((float) pos.x, (float) pos.y, (float) pos.z, 1f);
			vec4f1.transform(stack.last().pose());
			
			float[] col0 = new float[]{0, 0, 0};
			
			for (Vector3 ref : refs) {
				vec4f.set((float) ref.x, (float) ref.y, (float) ref.z, 1f);
				vec4f.transform(stack.last().pose());
				
				ref.calcNormal(pos, pos, norm);
				float[] col1 = new float[]{0, 0, 0};
				
				consumer.vertex(vec4f.x(), vec4f.y(), vec4f.z()).color(col1[0], col1[1], col1[2], 1).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
				consumer.vertex(vec4f1.x(), vec4f1.y(), vec4f1.z()).color(col0[0], col0[1], col0[2], 1).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
			}
			
			for (Vector3 ref : hypotRefs) {
				vec4f.set((float) ref.x, (float) ref.y, (float) ref.z, 1f);
				vec4f.transform(stack.last().pose());
				
				ref.calcNormal(pos, pos, norm);
				float[] col1 = new float[]{0, 0, 0};
				
				consumer.vertex(vec4f.x(), vec4f.y(), vec4f.z()).color(col1[0], col1[1], col1[2], 1).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
				consumer.vertex(vec4f1.x(), vec4f1.y(), vec4f1.z()).color(col0[0], col0[1], col0[2], 1).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
			}
		}
		
		LevelRenderer.renderLineBox(stack, consumer, new AABB(CoM.x - 0.01, CoM.y - 0.01, CoM.z - 0.01, CoM.x + 0.01, CoM.y + 0.01, CoM.z + 0.01), 0, 0, 0, 1f);
		
		stack.popPose();
	}
}
