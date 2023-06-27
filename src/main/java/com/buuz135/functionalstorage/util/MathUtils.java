package com.buuz135.functionalstorage.util;


import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MathUtils
{
	public static Matrix4f createTransformMatrix(Vector3f translation, Vector3f eulerDegrees, Vector3f scale) {
		var q = new Quaternionf();
		q = q.fromAxisAngleDeg(eulerDegrees, 0);
		return createTransformMatrix(translation, q, scale);
	}
	
	public static Matrix4f createTransformMatrix(Vector3f translation, Vector3f eulerDegrees, float scale) {
		return createTransformMatrix(translation, eulerDegrees, new Vector3f(scale, scale, scale));
	}
	
	public static Matrix4f createTransformMatrix(Vector3f translation, Quaternionf rotation, Vector3f scale) {
		new Matrix4f();
		/*
		Matrix4f transform = Matrix4f.createTranslateMatrix(translation.x(), translation.y(), translation.z());
		transform.multiply(rotation);
		Matrix4f scaleMat = Matrix4f.createScaleMatrix(scale.x(), scale.y(), scale.z());
		transform.multiply(scaleMat);
		*/

		return new Matrix4f();
	}
	
	public static Matrix4f createTransformMatrix(Vector3f translation, Quaternionf rotation, float scale) {
		return createTransformMatrix(translation, rotation, new Vector3f(scale, scale, scale));
	}
}
