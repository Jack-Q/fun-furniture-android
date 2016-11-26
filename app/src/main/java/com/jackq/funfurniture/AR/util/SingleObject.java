package com.jackq.funfurniture.AR.util;

import android.content.Intent;
import android.content.res.AssetManager;
import android.util.Log;

import com.jackq.funfurniture.AR.util.MeshObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class SingleObject extends MeshObject {

    private ByteBuffer verts;
    private ByteBuffer textCoords;
    private ByteBuffer norms;
    int numVerts = 0;


    public void loadModel(AssetManager assetManager, String filename)
            throws IOException {
        InputStream is = null;
        try {
            is = assetManager.open(filename);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is));

            String line;
            boolean firstObject = true;
            ArrayList<Float> vertexList = new ArrayList<>();
            ArrayList<Float> normalList = new ArrayList<>();
            ArrayList<Float> textureList = new ArrayList<>();
            ArrayList<Integer> faceList = new ArrayList<>();
            outer: while ((line = reader.readLine()) != null) {
                StringTokenizer stringTokenizer = new StringTokenizer(line);
                if (stringTokenizer.hasMoreTokens()) {
                    String s = stringTokenizer.nextToken();
                    switch (s) {
                        case "v":
                             if (!firstObject) break outer;
                            // 3 coordination for a vertex in vector
                            vertexList.add(Float.parseFloat(stringTokenizer.nextToken()));
                            vertexList.add(Float.parseFloat(stringTokenizer.nextToken()));
                            vertexList.add(Float.parseFloat(stringTokenizer.nextToken()));
                            break;
                        case "vn":
                            if (!firstObject) break outer;
                            // 3 coordination for a normal in vector
                            normalList.add(Float.parseFloat(stringTokenizer.nextToken()));
                            normalList.add(Float.parseFloat(stringTokenizer.nextToken()));
                            normalList.add(Float.parseFloat(stringTokenizer.nextToken()));
                            break;
                        case "vt":
                            if (!firstObject) break outer;
                            // 3 coordination for a texture mapping in vector
                            textureList.add(Float.parseFloat(stringTokenizer.nextToken()));
                            textureList.add(Float.parseFloat(stringTokenizer.nextToken()));
                            textureList.add(Float.parseFloat(stringTokenizer.nextToken()));
                            break;
                        case "f":
                            // face defined as 9 elements
                            StringTokenizer subStringTokenizer;
                            subStringTokenizer = new StringTokenizer(stringTokenizer.nextToken(), "/");
                            faceList.add(Integer.parseInt(subStringTokenizer.nextToken()));
                            faceList.add(Integer.parseInt(subStringTokenizer.nextToken()));
                            faceList.add(Integer.parseInt(subStringTokenizer.nextToken()));
                            subStringTokenizer = new StringTokenizer(stringTokenizer.nextToken(), "/");
                            faceList.add(Integer.parseInt(subStringTokenizer.nextToken()));
                            faceList.add(Integer.parseInt(subStringTokenizer.nextToken()));
                            faceList.add(Integer.parseInt(subStringTokenizer.nextToken()));
                            subStringTokenizer = new StringTokenizer(stringTokenizer.nextToken(), "/");
                            faceList.add(Integer.parseInt(subStringTokenizer.nextToken()));
                            faceList.add(Integer.parseInt(subStringTokenizer.nextToken()));
                            faceList.add(Integer.parseInt(subStringTokenizer.nextToken()));
                        case "g":
                            firstObject = false;
                    }
                }
            }

            int faceCount = faceList.size() / 9;
            // each vector has 3 coordination, each face has three vector of each type, each is 4 byte as a float number
            numVerts = faceCount * 3;
            verts = ByteBuffer.allocateDirect(faceCount * 3 * 3 * 4).order(ByteOrder.nativeOrder());
            textCoords = ByteBuffer.allocateDirect(faceCount * 3 * 2 * 4).order(ByteOrder.nativeOrder());
            norms = ByteBuffer.allocateDirect(faceCount * 3 * 3 * 4).order(ByteOrder.nativeOrder());
            for (int i = 0; i < faceCount; i++) {
                for (int iv = 0; iv < 3; iv++) {
                    int vertex = faceList.get(9 * i + 3 * iv) - 1;
                    verts.putFloat(vertexList.get(vertex * 3));
                    verts.putFloat(vertexList.get(vertex * 3 + 1));
                    verts.putFloat(vertexList.get(vertex * 3 + 2));
                    int texture = faceList.get(9 * i + 3 * iv + 1) - 1;
                    textCoords.putFloat(textureList.get(texture * 3));
                    textCoords.putFloat(textureList.get(texture * 3 + 1));
                    // textCoords.putFloat(textureList.get(texture * 3 + 2));
                    int norm = faceList.get(9 * i + 3 * iv + 2) - 1;
                    norms.putFloat(normalList.get(norm * 3));
                    norms.putFloat(normalList.get(norm * 3 + 1));
                    norms.putFloat(normalList.get(norm * 3 + 2));
                }
            }
            verts.rewind();
            norms.rewind();
            textCoords.rewind();
            Log.d("OBJ FILE LOADER", "Face Count: " + faceCount);
            Log.d("OBJ FILE LOADER", "Vertex: " + verts.capacity());
            Log.d("OBJ FILE LOADER", "Texture: " + textCoords.capacity());
            Log.d("OBJ FILE LOADER", "Norm: " + norms.capacity());
        } catch (Exception e) {
            Log.e("OBJ FILE LOADER", "filed to load object");
            Log.e("OBJ FILE LOADER", e.getMessage(), e);
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (Exception ignored) {
                }
        }
    }

//
//    public void loadModel(AssetManager assetManager, String filename)
//            throws IOException
//    {
//        InputStream is = null;
//        try
//        {
//            is = assetManager.open(filename);
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(is));
//
//            String line = reader.readLine();
//
//            int floatsToRead = Integer.parseInt(line);
//            numVerts = floatsToRead / 3;
//
//            verts = ByteBuffer.allocateDirect(floatsToRead * 4);
//            verts.order(ByteOrder.nativeOrder());
//            for (int i = 0; i < floatsToRead; i++)
//            {
//                verts.putFloat(Float.parseFloat(reader.readLine()));
//            }
//            verts.rewind();
//
//            line = reader.readLine();
//            floatsToRead = Integer.parseInt(line);
//
//            norms = ByteBuffer.allocateDirect(floatsToRead * 4);
//            norms.order(ByteOrder.nativeOrder());
//            for (int i = 0; i < floatsToRead; i++)
//            {
//                norms.putFloat(Float.parseFloat(reader.readLine()));
//            }
//            norms.rewind();
//
//            line = reader.readLine();
//            floatsToRead = Integer.parseInt(line);
//
//            textCoords = ByteBuffer.allocateDirect(floatsToRead * 4);
//            textCoords.order(ByteOrder.nativeOrder());
//            for (int i = 0; i < floatsToRead; i++)
//            {
//                textCoords.putFloat(Float.parseFloat(reader.readLine()));
//            }
//            textCoords.rewind();
//
//        } finally
//        {
//            if (is != null)
//                is.close();
//        }
//    }
//

    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType) {
        Buffer result = null;
        switch (bufferType) {
            case BUFFER_TYPE_VERTEX:
                result = verts;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = textCoords;
                break;
            case BUFFER_TYPE_NORMALS:
                result = norms;
            default:
                break;
        }
        return result;
    }


    @Override
    public int getNumObjectVertex() {
        return numVerts;
    }


    @Override
    public int getNumObjectIndex() {
        return 0;
    }
}
