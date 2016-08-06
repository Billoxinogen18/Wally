package com.projecttango.rajawali;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.vector.Vector3.Axis;

/**
 * Created by ioane5 on 4/25/16.
 */
public class ContentPlane extends Object3D {

    protected float mWidth;
    protected float mHeight;
    protected int mSegmentsW;
    protected int mSegmentsH;
    protected int mNumTextureTiles;
    private boolean mCreateTextureCoords;
    private boolean mCreateVertexColorBuffer;
    private Vector3.Axis mUpAxis;

    public ContentPlane() {
        this(1f, 1f, 1, 1, Vector3.Axis.Z, true, false, 1);
    }

    /**
     * Create a plane primitive. Calling this constructor will create a plane facing the specified axis.
     *
     * @param upAxis
     */
    public ContentPlane(Vector3.Axis upAxis) {
        this(1f, 1f, 1, 1, upAxis, true, false, 1);
    }

    /**
     * Create a plane primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
     *
     * @param width     The plane width
     * @param height    The plane height
     * @param segmentsW The number of vertical segments
     * @param segmentsH The number of horizontal segments
     */
    public ContentPlane(float width, float height, int segmentsW, int segmentsH) {
        this(width, height, segmentsW, segmentsH, Vector3.Axis.Z, true, false, 1);
    }

    /**
     * Create a plane primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
     *
     * @param width           The plane width
     * @param height          The plane height
     * @param segmentsW       The number of vertical segments
     * @param segmentsH       The number of horizontal segments
     * @param numTextureTiles The number of texture tiles. If more than 1 the texture will be repeat by n times.
     */
    public ContentPlane(float width, float height, int segmentsW, int segmentsH, int numTextureTiles) {
        this(width, height, segmentsW, segmentsH, Vector3.Axis.Z, true, false, numTextureTiles);
    }

    /**
     * Create a plane primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
     *
     * @param width     The plane width
     * @param height    The plane height
     * @param segmentsW The number of vertical segments
     * @param segmentsH The number of horizontal segments
     * @param upAxis    The up axis. Choose Axis.Y for a ground plane and Axis.Z for a camera facing plane.
     */
    public ContentPlane(float width, float height, int segmentsW, int segmentsH, Vector3.Axis upAxis) {
        this(width, height, segmentsW, segmentsH, upAxis, true, false, 1);
    }

    /**
     * Creates a plane primitive.
     *
     * @param width                    The plane width
     * @param height                   The plane height
     * @param segmentsW                The number of vertical segments
     * @param segmentsH                The number of horizontal segments
     * @param upAxis                   The up axis. Choose Axis.Y for a ground plane and Axis.Z for a camera facing plane.
     * @param createTextureCoordinates A boolean that indicates whether the texture coordinates should be calculated or not.
     * @param createVertexColorBuffer  A boolean that indicates whether a vertex color buffer should be created or not.
     */
    public ContentPlane(float width, float height, int segmentsW, int segmentsH, Vector3.Axis upAxis, boolean createTextureCoordinates,
                        boolean createVertexColorBuffer) {
        this(width, height, segmentsW, segmentsH, upAxis, createTextureCoordinates, createVertexColorBuffer, 1);
    }

    /**
     * Creates a plane primitive.
     *
     * @param width                    The plane width
     * @param height                   The plane height
     * @param segmentsW                The number of vertical segments
     * @param segmentsH                The number of horizontal segments
     * @param upAxis                   The up axis. Choose Axis.Y for a ground plane and Axis.Z for a camera facing plane.
     * @param createTextureCoordinates A boolean that indicates whether the texture coordinates should be calculated or not.
     * @param createVertexColorBuffer  A boolean that indicates whether a vertex color buffer should be created or not.
     * @param numTextureTiles          The number of texture tiles. If more than 1 the texture will be repeat by n times.
     */
    public ContentPlane(float width, float height, int segmentsW, int segmentsH, Vector3.Axis upAxis, boolean createTextureCoordinates,
                        boolean createVertexColorBuffer, int numTextureTiles) {
        this(width, height, segmentsW, segmentsH, upAxis, createTextureCoordinates, createVertexColorBuffer, numTextureTiles, true);
    }

    /**
     * Creates a plane primitive.
     *
     * @param width                    The plane width
     * @param height                   The plane height
     * @param segmentsW                The number of vertical segments
     * @param segmentsH                The number of horizontal segments
     * @param upAxis                   The up axis. Choose Axis.Y for a ground plane and Axis.Z for a camera facing plane.
     * @param createTextureCoordinates A boolean that indicates whether the texture coordinates should be calculated or not.
     * @param createVertexColorBuffer  A boolean that indicates whether a vertex color buffer should be created or not.
     * @param numTextureTiles          The number of texture tiles. If more than 1 the texture will be repeat by n times.
     * @param createVBOs               A boolean that indicates whether the VBOs should be created immediately.
     */
    public ContentPlane(float width, float height, int segmentsW, int segmentsH, Vector3.Axis upAxis, boolean createTextureCoordinates,
                        boolean createVertexColorBuffer, int numTextureTiles, boolean createVBOs) {
        super();
        mWidth = width;
        mHeight = height;
        mSegmentsW = segmentsW;
        mSegmentsH = segmentsH;
        mUpAxis = upAxis;
        mCreateTextureCoords = createTextureCoordinates;
        mCreateVertexColorBuffer = createVertexColorBuffer;
        mNumTextureTiles = numTextureTiles;
        init(createVBOs);
    }

    private void init(boolean createVBOs) {
        int i, j;
        int numVertices = (mSegmentsW + 1) * (mSegmentsH + 1);
        float[] vertices = new float[numVertices * 3];
        float[] textureCoords = null;
        if (mCreateTextureCoords)
            textureCoords = new float[numVertices * 2];
        float[] normals = new float[numVertices * 3];
        float[] colors = null;
        if (mCreateVertexColorBuffer)
            colors = new float[numVertices * 4];
        int[] indices = new int[mSegmentsW * mSegmentsH * 6];
        int vertexCount = 0;
        int texCoordCount = 0;

        for (i = 0; i <= mSegmentsW; i++) {
            for (j = 0; j <= mSegmentsH; j++) {
                float v1 = ((float) i / (float) mSegmentsW - 0.5f) * mWidth;
                float v2 = ((float) j / (float) mSegmentsH - 0.5f) * mHeight;
                if (mUpAxis == Vector3.Axis.X) {
                    vertices[vertexCount] = 0;
                    vertices[vertexCount + 1] = v1;
                    vertices[vertexCount + 2] = v2;
                } else if (mUpAxis == Vector3.Axis.Y) {
                    vertices[vertexCount] = v1;
                    vertices[vertexCount + 1] = 0;
                    vertices[vertexCount + 2] = v2;
                } else if (mUpAxis == Vector3.Axis.Z) {
                    vertices[vertexCount] = v1;
                    vertices[vertexCount + 1] = v2;
                    vertices[vertexCount + 2] = 0;
                }

                if (mCreateTextureCoords) {
                    float u = (float) i / (float) mSegmentsW;
                    textureCoords[texCoordCount++] = (1f - u) * mNumTextureTiles;
                    float v = (float) j / (float) mSegmentsH;
                    textureCoords[texCoordCount++] = v * mNumTextureTiles;
                }

                normals[vertexCount] = mUpAxis == Vector3.Axis.X ? 1 : 0;
                normals[vertexCount + 1] = mUpAxis == Vector3.Axis.Y ? 1 : 0;
                normals[vertexCount + 2] = mUpAxis == Vector3.Axis.Z ? 1 : 0;

                vertexCount += 3;
            }
        }

        int colspan = mSegmentsH + 1;
        int indexCount = 0;

        for (int col = 0; col < mSegmentsW; col++) {
            for (int row = 0; row < mSegmentsH; row++) {
                int ul = col * colspan + row;
                int ll = ul + 1;
                int ur = (col + 1) * colspan + row;
                int lr = ur + 1;

                if (mUpAxis == Vector3.Axis.X || mUpAxis == Vector3.Axis.Z) {
                    indices[indexCount++] = ur;
                    indices[indexCount++] = lr;
                    indices[indexCount++] = ul;

                    indices[indexCount++] = lr;
                    indices[indexCount++] = ll;
                    indices[indexCount++] = ul;
                } else {
                    indices[indexCount++] = ur;
                    indices[indexCount++] = ul;
                    indices[indexCount++] = lr;

                    indices[indexCount++] = lr;
                    indices[indexCount++] = ul;
                    indices[indexCount++] = ll;
                }
            }
        }

        if (mCreateVertexColorBuffer) {
            int numColors = numVertices * 4;
            for (j = 0; j < numColors; j += 4) {
                colors[j] = 1.0f;
                colors[j + 1] = 1.0f;
                colors[j + 2] = 1.0f;
                colors[j + 3] = 1.0f;
            }
        }

        setData(vertices, normals, textureCoords, colors, indices, createVBOs);

        vertices = null;
        normals = null;
        textureCoords = null;
        colors = null;
        indices = null;
    }

    public float getHeight() {
        return mHeight;
    }

    public float getWidth() {
        return mWidth;
    }

    public void setHeight(float mHeight) {
        this.mHeight = mHeight;
        init(true);
    }

    public void setWidth(float mWidth) {
        this.mWidth = mWidth;
        init(true);
    }
}