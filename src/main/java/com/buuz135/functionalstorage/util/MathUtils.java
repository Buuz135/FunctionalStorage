package com.buuz135.functionalstorage.util;

import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

public class MathUtils
{
	public static Matrix4f createTransformMatrix(Vector3f translation, Vector3f eulerDegrees, Vector3f scale) {
		Quaternion q = Quaternion.fromXYZDegrees(eulerDegrees);
		return createTransformMatrix(translation, q, scale);
	}
	
	public static Matrix4f createTransformMatrix(Vector3f translation, Vector3f eulerDegrees, float scale) {
		return createTransformMatrix(translation, eulerDegrees, new Vector3f(scale, scale, scale));
	}
	
	public static Matrix4f createTransformMatrix(Vector3f translation, Quaternion rotation, Vector3f scale) {
		Matrix4f transform = Matrix4f.createTranslateMatrix(translation.x(), translation.y(), translation.z());
		transform.multiply(rotation);
		Matrix4f scaleMat = Matrix4f.createScaleMatrix(scale.x(), scale.y(), scale.z());
		transform.multiply(scaleMat);
		return transform;
	}
	
	public static Matrix4f createTransformMatrix(Vector3f translation, Quaternion rotation, float scale) {
		return createTransformMatrix(translation, rotation, new Vector3f(scale, scale, scale));
	}
}
