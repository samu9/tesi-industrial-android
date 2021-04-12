package com.example.industrial;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.RajawaliRenderer;

public class Renderer extends RajawaliRenderer {

    public Context context;
    private DirectionalLight directionalLight;
    private Object3D machine3DModel;

    private float rotationPos = 0;


    public Renderer(Context context) {
        super(context);
        this.context = context;
        setFrameRate(60);
    }

    @Override
    protected void initScene() {
        directionalLight = new DirectionalLight(1f, .2f, -1.0f);
        directionalLight.setColor(1.0f, 1.0f, 1.0f);
        directionalLight.setPower(2);
        getCurrentScene().addLight(directionalLight);


        Material lambert = new Material();
        lambert.setColor(Color.BLACK);
        lambert.setDiffuseMethod(new DiffuseMethod.Lambert());
        lambert.enableLighting(true);


        LoaderOBJ objParser = new LoaderOBJ(context.getResources(), TextureManager.getInstance(), R.raw.robotic_arm_obj);
        try {
            objParser.parse();
        } catch (ParsingException e) {
            e.printStackTrace();
        }
        machine3DModel = objParser.getParsedObject();
        machine3DModel.setScale(0.004f);
        machine3DModel.setPosition(new Vector3(0,-1f,1));
        machine3DModel.setMaterial(lambert);

        getCurrentScene().addChild(machine3DModel);
        getCurrentCamera().setZ(4.2f);

    }

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
//        machine3DModel.rotate(Vector3.Axis.Y, 0.2);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        Log.i("Renderer", event.toString());
        float delta = 0;
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                rotationPos = event.getX();
                break;

            case MotionEvent.ACTION_MOVE:
                delta = (rotationPos - event.getX()) * 0.15f;
                break;


        }
        rotationPos = event.getX();
        machine3DModel.setRotation(Vector3.Axis.Y, delta);
    }
}
