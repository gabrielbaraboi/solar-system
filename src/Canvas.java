import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLPointerFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.ArrayList;

	public class Canvas extends GLCanvas implements GLEventListener, KeyListener {
	private static final float SUN_RADIUS = 12f;
	private TextRenderer renderer;

	private static final long serialVersionUID = 1L;
	private FPSAnimator animator;
	private GLU glu;
	private Texture earthTexture;
	private Texture cloudTexture;
	private Texture moonTexture;
	private Texture skyTexture;
	private ArrayList<Planet> planets;

	private float Angle = 0;
	private float earthAngle = 0;
	private float systemAngle = 0;
	private Sun sun;
	float cameraAzimuth = 0.0f, cameraSpeed = 0.0f, cameraElevation = 0.0f;

	float cameraCoordsPosX = 0.0f, cameraCoordsPosY = 0.0f, cameraCoordsPosZ = 0.0f;

	float cameraUpx = 0.0f, cameraUpy = 0.0f, cameraUpz = 0.0f;
	public Canvas(int width, int height, GLCapabilities capabilities) {
		super(capabilities);
		setSize(width, height);
		addGLEventListener(this);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		glu = new GLU();
		planets = new ArrayList<>();
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glDisable(GL.GL_DEPTH_TEST);

		renderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 16));

		gl.glShadeModel(GL2.GL_SMOOTH);

		gl.glClearColor(0f, 0f, 0f, 0f);

		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		this.addKeyListener(this);
		animator = new FPSAnimator(this, 60);
		animator.start();

		String textureFile = "assets\\earth.jpg";
		earthTexture = getObjectTexture(gl, textureFile);
		textureFile = "assets\\clouds.png";
		cloudTexture = getObjectTexture(gl, textureFile);
		textureFile = "assets\\stars.png";
		skyTexture = getObjectTexture(gl, textureFile);
		textureFile = "assets\\sun.jpg";
		this.sun = new Sun(gl, glu, getObjectTexture(gl, textureFile));
		textureFile = "assets\\mercury.jpg";
		Planet mercury = new Planet(gl, glu, getObjectTexture(gl, textureFile), 1.2f, SUN_RADIUS + 2f, 2.56f);
		textureFile = "assets\\venus.jpg";
		Planet venus = new Planet(gl, glu, getObjectTexture(gl, textureFile), 0.7f, SUN_RADIUS + 12f, 3.56f);
		textureFile = "assets\\jupiter.jpg";
		Planet Jupiter = new Planet(gl, glu, getObjectTexture(gl, textureFile), 0.25f, SUN_RADIUS + 65f, 8.56f);
		textureFile = "assets\\mars.jpg";
		Planet mars = new Planet(gl, glu, getObjectTexture(gl, textureFile), 0.3f, SUN_RADIUS + 50f, 3.56f);
		textureFile = "assets\\moon.png";
		moonTexture = getObjectTexture(gl, textureFile);
		textureFile = "assets\\saturn.jpg";
		Planet Saturn = new Planet(gl, glu, getObjectTexture(gl, textureFile), 0.3f, SUN_RADIUS + 90f, 7.56f);
		textureFile = "assets\\uranus.jpg";
		Planet Uranus = new Planet(gl, glu, getObjectTexture(gl, textureFile), 0.25f, SUN_RADIUS + 105f, 6.56f);
		textureFile = "assets\\neptune.jpg";
		Planet Neptune = new Planet(gl, glu, getObjectTexture(gl, textureFile), 0.275f, SUN_RADIUS + 120f, 5.56f);
		planets.add(mercury);
		planets.add(venus);
		planets.add(mars);
		planets.add(Jupiter);
		planets.add(Saturn);
		planets.add(Uranus);
		planets.add(Neptune);

	}

	@Override
	public void dispose(GLAutoDrawable glAutoDrawable) {

	}

	@Override
	public void display(GLAutoDrawable glAutoDrawable) {
		if (!animator.isAnimating()) {
			return;
		}
		final GL2 gl = glAutoDrawable.getGL().getGL2();
		setCamera(gl, 400);
		aimCamera(gl, glu);
		moveCamera();
		setLights(gl);

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		sun.display();
		drawEarthAndMoon(gl);
		for (Planet p : planets)
			p.display();

		skyTexture.bind(gl);
		skyTexture.enable(gl);
		drawSkybox(gl);

		Rectangle rectangle = getBounds();
		int width = rectangle.width;
		int height = rectangle.height;
		String text = "by Gabriel Baraboi, Bogdan Burileanu and Ion Covali";
		drawText(text, width, height);
	}

	private void drawEarthAndMoon(GL2 gl) {
		gl.glPushMatrix();
		systemAngle = (systemAngle + 0.4f) % 360f;

		final float distance = SUN_RADIUS + 30f;
		final float x = (float) Math.sin(Math.toRadians(systemAngle)) * distance;
		final float y = (float) Math.cos(Math.toRadians(systemAngle)) * distance;
		final float z = 0;
		gl.glTranslatef(x, y, z);

		drawEarth(gl);
		drawMoon(gl);
		gl.glPopMatrix();

	}

	private void drawText(String text, int width, int height) {
		renderer.beginRendering(width, height);
		renderer.setColor(1.0f, 0.2f, 0.2f, 0.8f);
		renderer.draw(text, 10, 10);
		renderer.endRendering();
	}

	private void drawEarth(GL2 gl) {

		float[] rgba = { 1f, 1f, 1f };
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.5f);

		earthAngle = (earthAngle + 0.1f) % 360f;
		cloudTexture.enable(gl);
		cloudTexture.bind(gl);

		gl.glPushMatrix();
		gl.glRotatef(earthAngle, 0.2f, 0.1f, 0);
		final float radius = 6.378f;
		final int slices = 16;
		final int stacks = 16;
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_COLOR, GL.GL_DST_ALPHA);

		GLUquadric clouds = glu.gluNewQuadric();
		glu.gluQuadricOrientation(clouds, GLU.GLU_OUTSIDE);
		glu.gluQuadricTexture(clouds, true);
		glu.gluSphere(clouds, 7, slices, stacks);
		earthTexture.enable(gl);
		earthTexture.bind(gl);
		gl.glDisable(GL.GL_BLEND);

		GLUquadric earth = glu.gluNewQuadric();
		glu.gluQuadricTexture(earth, true);
		glu.gluQuadricDrawStyle(earth, GLU.GLU_FILL);
		glu.gluQuadricNormals(earth, GLU.GLU_FLAT);
		glu.gluQuadricOrientation(earth, GLU.GLU_OUTSIDE);

		glu.gluSphere(earth, radius, slices, stacks);

		gl.glPopName();

		glu.gluDeleteQuadric(earth);
		glu.gluDeleteQuadric(clouds);

		gl.glPopMatrix();
	}

	// luna
	private void drawMoon(GL2 gl) {

		gl.glPushMatrix();
		moonTexture.enable(gl);
		moonTexture.bind(gl);
		Angle = (Angle + 1f) % 360f;
		final float distance = 12.000f;
		final float x = (float) Math.sin(Math.toRadians(Angle)) * distance;
		final int y = (int) ((float) Math.cos(Math.toRadians(Angle)) * distance);
		final float z = 0;
		gl.glTranslatef(x, y, z);
		gl.glRotatef(Angle, 0, 0, -1);
		gl.glRotatef(45f, 0, 1, 0);

		final float radius = 3.378f;
		final int slices = 16;
		final int stacks = 16;
		GLUquadric moon = glu.gluNewQuadric();
		glu.gluQuadricTexture(moon, true);
		glu.gluQuadricDrawStyle(moon, GLU.GLU_FILL);
		glu.gluQuadricNormals(moon, GLU.GLU_FLAT);
		glu.gluQuadricOrientation(moon, GLU.GLU_INSIDE);
		glu.gluSphere(moon, radius, slices, stacks);

		gl.glPopMatrix();
		gl.glPopName();
	}

	private Texture getObjectTexture(GL2 gl, String fileName) {
		InputStream stream = null;
		Texture tex = null;
		String extension = fileName.substring(fileName.lastIndexOf('.'));
		try {
			stream = new FileInputStream(new File(fileName));
			TextureData data = TextureIO.newTextureData(gl.getGLProfile(), stream, false, extension);
			tex = TextureIO.newTexture(data);
		} catch (FileNotFoundException e) {
			System.err.println("Error loading the file!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IO Exception!");
			e.printStackTrace();
		}

		return tex;
	}

	private void setLights(GL2 gl) {
		float SHINE_ALL_DIRECTIONS = 1;
		float[] lightPos = { 0, 0, 0, SHINE_ALL_DIRECTIONS };
		float[] lightColorAmbient = { 0.5f, 0.5f, 0.5f, 1f };
		float[] lightColorSpecular = { 0.8f, 0.8f, 0.8f, 1f };
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightColorAmbient, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightColorSpecular, 0);

		gl.glEnable(GL2.GL_LIGHT1);
		gl.glEnable(GL2.GL_LIGHTING);

	}

	private void setCamera(GL2 gl, float distance) {
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		float widthHeightRatio = (float) getWidth() / (float) getHeight();
		glu.gluPerspective(45, widthHeightRatio, 1, 1000);
		glu.gluLookAt(0, 0, distance, 0, 0, 0, 0, 1, 0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL gl = drawable.getGL();
		gl.glViewport(0, 0, width, height);
	}

	private void drawSkybox(GL gl) {

		skyTexture.enable(gl);
		skyTexture.bind(gl);

		((GLPointerFunc) gl).glDisableClientState(GL2.GL_VERTEX_ARRAY);
		final float radius = 200f;
		final int slices = 16;
		final int stacks = 16;
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_COLOR, GL.GL_DST_ALPHA);
		GLUquadric sky = glu.gluNewQuadric();
		glu.gluQuadricTexture(sky, true);
		glu.gluQuadricDrawStyle(sky, GLU.GLU_FILL);
		glu.gluQuadricNormals(sky, GLU.GLU_FLAT);
		glu.gluQuadricOrientation(sky, GLU.GLU_INSIDE);
		glu.gluSphere(sky, radius, slices, stacks);

		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_COLOR, GL.GL_DST_ALPHA);
	}

	public void moveCamera() {
		float[] tmp = polarToCartesian(cameraAzimuth, cameraSpeed, cameraElevation);

		cameraCoordsPosX += tmp[0];
		cameraCoordsPosY += tmp[1];
		cameraCoordsPosZ += tmp[2];
	}

	public void aimCamera(GL2 gl, GLU glu) {
		gl.glLoadIdentity();

		float[] tmp = polarToCartesian(cameraAzimuth, 100.0f, cameraElevation);

		float[] camUp = polarToCartesian(cameraAzimuth, 100.0f, cameraElevation + 90);

		cameraUpx = camUp[0];
		cameraUpy = camUp[1];
		cameraUpz = camUp[2];

		glu.gluLookAt(cameraCoordsPosX, cameraCoordsPosY, cameraCoordsPosZ, cameraCoordsPosX + tmp[0],
				cameraCoordsPosY + tmp[1], cameraCoordsPosZ + tmp[2], cameraUpx, cameraUpy, cameraUpz);
	}

	private float[] polarToCartesian(float azimuth, float length, float altitude) {
		float[] result = new float[3];
		float x, y, z;

		float theta = (float) Math.toRadians(90 - azimuth);
		float tantheta = (float) Math.tan(theta);
		float radian_alt = (float) Math.toRadians(altitude);
		float cospsi = (float) Math.cos(radian_alt);

		x = (float) Math.sqrt((length * length) / (tantheta * tantheta + 1));
		z = tantheta * x;

		x = -x;

		if ((azimuth >= 180.0 && azimuth <= 360.0) || azimuth == 0.0f) {
			x = -x;
			z = -z;
		}

		y = (float) (Math.sqrt(z * z + x * x) * Math.sin(radian_alt));

		if (length < 0) {
			x = -x;
			z = -z;
			y = -y;
		}

		x = x * cospsi;
		z = z * cospsi;

		result[0] = x;
		result[1] = y;
		result[2] = z;

		return result;
	}

	public void keyPressed(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.VK_UP) {
			cameraElevation -= 2;
		}

		if (event.getKeyCode() == KeyEvent.VK_DOWN) {
			cameraElevation += 2;
		}

		if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
			cameraAzimuth -= 2;
		}

		if (event.getKeyCode() == KeyEvent.VK_LEFT) {
			cameraAzimuth += 2;
		}

		if (event.getKeyCode() == KeyEvent.VK_I) {
			cameraSpeed += 0.05;
		}

		if (event.getKeyCode() == KeyEvent.VK_O) {
			cameraSpeed -= 0.05;
		}

		if (event.getKeyCode() == KeyEvent.VK_S) {
			cameraSpeed = 0;
		}
		if (event.getKeyCode() < 250)
			keys[event.getKeyCode()] = true;

		if (cameraAzimuth > 359)
			cameraAzimuth = 1;

		if (cameraAzimuth < 1)
			cameraAzimuth = 359;
	}

	private final boolean[] keys = new boolean[250];

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}
