package tv.ismar.iqiyiplayer;

import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.qiyi.sdk.player.OpenGLUtils;

import java.nio.FloatBuffer;

public class TexturePainter {
    private static final String TAG = "TextureDrawer";

    private static final float[] sVerticesData = {
            // X, Y, Z,
            -0.5f, -0.5f, +0.0f,
            +0.0f, -0.5f, +0.0f,
            -0.5f, +0.5f, +0.0f,
            +0.0f, +0.5f, +0.0f,
    };
    private static final float[] sVerticesData1 = {
            // X, Y, Z,
            -0.0f, -0.5f, +0.0f,
            +0.5f, -0.5f, +0.0f,
            -0.0f, +0.5f, +0.0f,
            +0.5f, +0.5f, +0.0f,
    };
    private static final float[] sFragmentsData = {
            // S, T,
            +0.0f, +0.0f,
            +1.0f, +0.0f,
            +0.0f, +1.0f,
            +1.0f, +1.0f,
    };
    private final String mVertexShader =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    private final String mFragmentShader =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";
    private final Object LOCK = new Object();
    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];
    private FloatBuffer mVertices;
    private FloatBuffer mVertices1;
    private FloatBuffer mFragments;
    private int mProgram;
    private int mTextureID;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;
    private boolean mUpdateTexture;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    private OnFrameAvailableListener mFrameListener = new OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            Log.d(TAG, "onFrameAvailable(" + surfaceTexture + ") mUpdateTexture=" + mUpdateTexture);
            synchronized (LOCK) {
                mUpdateTexture = true;
            }
        }
    };

    public void drawTexture() {
        Log.d(TAG, "drawTexture() mUpdateTexture=" + mUpdateTexture);
        synchronized (LOCK) {
            if (mUpdateTexture) {
                mSurfaceTexture.updateTexImage();
                mSurfaceTexture.getTransformMatrix(mSTMatrix);
                mUpdateTexture = false;
            }
        }
        if (mProgram == 0) {
            Log.d(TAG, "drawTexture() mProgram=" + mProgram);
            return;
        }
        OpenGLUtils.clear(0.0f, 0.0f, 0.0f, 1.0f);
        OpenGLUtils.useProgram(mProgram);
        OpenGLUtils.bindTexture(mTextureID);
        OpenGLUtils.setAttribute(maPositionHandle, 3, mVertices);
        OpenGLUtils.setAttribute(maTextureHandle, 2, mFragments);
        OpenGLUtils.setUniform(muMVPMatrixHandle, mMVPMatrix);
        OpenGLUtils.setUniform(muSTMatrixHandle, mSTMatrix);
        OpenGLUtils.drawTriangleStrip();

        OpenGLUtils.setAttribute(maPositionHandle, 3, mVertices1);
        OpenGLUtils.setAttribute(maTextureHandle, 2, mFragments);
        OpenGLUtils.setUniform(muMVPMatrixHandle, mMVPMatrix);
        OpenGLUtils.setUniform(muSTMatrixHandle, mSTMatrix);
        OpenGLUtils.drawTriangleStrip();
    }

    public Surface prepareTexture() {
        Log.d(TAG, "preparTexture() mUpdateTexture=" + mUpdateTexture);
        releaseTexture();

        mVertices = OpenGLUtils.createFloatBuffer(sVerticesData);
        mVertices1 = OpenGLUtils.createFloatBuffer(sVerticesData1);
        mFragments = OpenGLUtils.createFloatBuffer(sFragmentsData);
        Matrix.setIdentityM(mSTMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);

        mProgram = OpenGLUtils.createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            Log.d(TAG, "preparTexture() mProgram=" + mProgram);
            return null;
        }

        maPositionHandle = OpenGLUtils.getAttributeLocation(mProgram, "aPosition");
        maTextureHandle = OpenGLUtils.getAttributeLocation(mProgram, "aTextureCoord");
        muMVPMatrixHandle = OpenGLUtils.getUniformLocation(mProgram, "uMVPMatrix");
        muSTMatrixHandle = OpenGLUtils.getUniformLocation(mProgram, "uSTMatrix");
        mTextureID = OpenGLUtils.genAndBindTexture();

        mSurfaceTexture = new SurfaceTexture(mTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(mFrameListener);
        synchronized (LOCK) {
            mUpdateTexture = false;
        }
        mSurface = new Surface(mSurfaceTexture);
        return mSurface;
    }

    public void releaseTexture() {
        Log.d(TAG, "releaseTexture()");
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        if (mVertices != null) {
            mVertices.clear();
            mVertices = null;
        }
        if (mFragments != null) {
            mFragments.clear();
            mFragments = null;
        }
        mProgram = 0;
        mTextureID = 0;
        muMVPMatrixHandle = 0;
        muSTMatrixHandle = 0;
        maPositionHandle = 0;
        maTextureHandle = 0;
    }
}
