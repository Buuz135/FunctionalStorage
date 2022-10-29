package com.buuz135.functionalstorage.client.loader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class IQuadTransformer {

    public static int STRIDE = DefaultVertexFormat.BLOCK.getIntegerSize();
    public static int POSITION = findOffset(DefaultVertexFormat.ELEMENT_POSITION);
    public static int COLOR = findOffset(DefaultVertexFormat.ELEMENT_COLOR);
    public static int UV0 = findOffset(DefaultVertexFormat.ELEMENT_UV0);
    public static int UV1 = findOffset(DefaultVertexFormat.ELEMENT_UV1);
    public static int UV2 = findOffset(DefaultVertexFormat.ELEMENT_UV2);
    public static int NORMAL = findOffset(DefaultVertexFormat.ELEMENT_NORMAL);

    private static int findOffset(VertexFormatElement element) {
        int index = DefaultVertexFormat.BLOCK.getElements().indexOf(element);
        return index < 0 ? -1 : DefaultVertexFormat.BLOCK.getOffset(index) / 4;
    }
}
