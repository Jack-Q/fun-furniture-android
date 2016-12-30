package com.jackq.funfurniture.model;



public class FurnitureModel {
    enum ModelAnchor{
        floor, ceiling, free, underground, wall
    }
    private String id;
    private ModelAnchor anchor;
    private String mtl;
    private String obj;
    private String texture;
    private String shadow;
    private String thumb;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ModelAnchor getAnchor() {
        return anchor;
    }

    public void setAnchor(ModelAnchor anchor) {
        this.anchor = anchor;
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

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public String getShadow() {
        return shadow;
    }

    public void setShadow(String shadow) {
        this.shadow = shadow;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getFileNameMtl(){
        return this.getId() + "-model.mtl";
    }
    public String getFileNameObj(){
        return this.getId() + "-model.obj";
    }
    public String getFileNameShadow(){
        return this.getId() + "-shadow.png";
    }
    public String getFileNameTexture(){
        return this.getId() + "-texture.jpg";
    }
    public String getFileNameThumb(){
        return this.getId() + "-thumb.jpg";
    }
}
