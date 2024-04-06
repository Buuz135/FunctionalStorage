package com.buuz135.functionalstorage.util;


import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MathUtils
{
	public static Matrix4f createTransformMatrix(Vector3f translation, Vector3f eulerDegrees, Vector3f scale) {
		var q = new Quaternionf().rotationXYZ((float) (eulerDegrees.x * (Math.PI / 180f)), (float) (eulerDegrees.y * (Math.PI / 180f)), (float) (eulerDegrees.z * (Math.PI / 180f)));
		return createTransformMatrix(translation, q, scale);
	}
	
	public static Matrix4f createTransformMatrix(Vector3f translation, Vector3f eulerDegrees, float scale) {
		return createTransformMatrix(translation, eulerDegrees, new Vector3f(scale, scale, scale));
	}
	
	public static Matrix4f createTransformMatrix(Vector3f translation, Quaternionf rotation, Vector3f scale) {
		Matrix4f transform = new Matrix4f();
		transform.translation(translation).scale(scale).rotate(rotation);
		return transform;
	}
	
	public static Matrix4f createTransformMatrix(Vector3f translation, Quaternionf rotation, float scale) {
		return createTransformMatrix(translation, rotation, new Vector3f(scale, scale, scale));
	}
}
