package org.rajawali3d.loader;

/**
 * Created by jackq on 11/29/16.
 */

public class SceneModel {
    public enum Anchor {
        floor, ceiling, underground, wall
    }

    private int id;
    private int model;
    private Anchor anchor;
    private int version;
    private String mtl;
    private String obj;
    private String shadow;
    private String texture;
    private String thumb;


    public SceneModel(){}

    public SceneModel(int id) {
        this.id = id;
    }

    // region getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getModel() {
        return model;
    }

    public void setModel(int model) {
        this.model = model;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getMtl() {
        return mtl;
    }

    public void setMtl(String mtl) {
        this.mtl = mtl;
    }

    public String getObj() {
        return obj;
    }

    public void setObj(String obj) {
        this.obj = obj;
    }

    public String getShadow() {
        return shadow;
    }

    public void setShadow(String shadow) {
        this.shadow = shadow;
    }

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    // endregion
}
