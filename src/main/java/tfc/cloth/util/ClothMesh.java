package tfc.cloth.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector4f;
import tfc.cloth.phys.Cloth;
import tfc.cloth.phys.Point;
import tfc.cloth.phys.Vector3;

public class ClothMesh {
	Cloth cloth;
	Point[] points;
	int width, height;
	
	public ClothMesh(Cloth cloth, int width, int height) {
		this.cloth = cloth;
		this.points = cloth.getOrderedPoints();
		this.width = width;
		this.height = height;
	}
	
	public void draw(VertexConsumer consumer, PoseStack stack) {
		Vector4f vec4f = new Vector4f();
		Vector4f vec4f1 = new Vector4f();
		
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
		
		double divis = Math.sqrt(width * width + height * height) / 2;
		double sqrt = 1;
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height - 1; y++) {
				Point orderedPoint = points[x + (y * width)];
				if (orderedPoint.getRefs().length == 0) continue;
				Point ref = points[x + ((y + 1) * width)];
				if (ref.getRefs().length == 0) continue;
				Vector3 pos = orderedPoint.getPos();
				
				vec4f.set((float) pos.x, (float) pos.y, (float) pos.z, 1f);
				vec4f.transform(stack.last().pose());
				
				pos = ref.getPos();
				
				vec4f1.set((float) pos.x, (float) pos.y, (float) pos.z, 1f);
				vec4f1.transform(stack.last().pose());
				
				Vector3 norm =
						new Vector3(
								vec4f.x(),
								vec4f.y(),
								vec4f.z()
						).sub(
								vec4f1.x(),
								vec4f1.y(),
								vec4f1.z()
						).normalize();
				
//				double sqrt = Math.sqrt((x - width / 2d) * (x - width / 2d) + (y - width / 2d) * (y - width / 2d));
//				sqrt /= divis;
				consumer.vertex(vec4f.x(), vec4f.y(), vec4f.z()).color((float) sqrt, (float) sqrt, (float) sqrt, 0.25f).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
				
//				sqrt = Math.sqrt((x - width / 2d) * (x - width / 2d) + (y - width / 2d) * (y - width / 2d));
//				sqrt /= divis;
				consumer.vertex(vec4f1.x(), vec4f1.y(), vec4f1.z()).color((float) sqrt, (float) sqrt, (float) sqrt, 0.25f).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
			}
		}
		
		for (int x = 0; x < width - 1; x++) {
			for (int y = 0; y < height; y++) {
				Point orderedPoint = points[x + (y * width)];
				if (orderedPoint.getRefs().length == 0) continue;
				Point ref = points[(x + 1) + (y * width)];
				if (ref.getRefs().length == 0) continue;
				Vector3 pos = orderedPoint.getPos();
				
				vec4f.set((float) pos.x, (float) pos.y, (float) pos.z, 1f);
				vec4f.transform(stack.last().pose());
				
				pos = ref.getPos();
				
				vec4f1.set((float) pos.x, (float) pos.y, (float) pos.z, 1f);
				vec4f1.transform(stack.last().pose());
				
				Vector3 norm =
						new Vector3(
								vec4f.x(),
								vec4f.y(),
								vec4f.z()
						).sub(
								vec4f1.x(),
								vec4f1.y(),
								vec4f1.z()
						).normalize();
				
//				double sqrt = Math.sqrt((x - width / 2d) * (x - width / 2d) + (y - width / 2d) * (y - width / 2d));
//				sqrt /= divis;
				consumer.vertex(vec4f.x(), vec4f.y(), vec4f.z()).color((float) sqrt, (float) sqrt, (float) sqrt, 0.25f).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
				
//				sqrt = Math.sqrt((x - width / 2d) * (x - width / 2d) + (y - width / 2d) * (y - width / 2d));
//				sqrt /= divis;
				consumer.vertex(vec4f1.x(), vec4f1.y(), vec4f1.z()).color((float) sqrt, (float) sqrt, (float) sqrt, 0.25f).normal(stack.last().normal(), (float) norm.x, (float) norm.y, (float) norm.z).endVertex();
			}
		}
	}
}
