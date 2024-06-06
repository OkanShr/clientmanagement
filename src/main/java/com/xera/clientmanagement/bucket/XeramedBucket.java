package com.xera.clientmanagement.bucket;

public enum XeramedBucket {

    BUCKET("xeramedimages");
    private final String xeramedBucket;

    XeramedBucket(String xeramedBucket){
        this.xeramedBucket = xeramedBucket;
    }

    public String getXeramedBucket() {
        return xeramedBucket;
    }


}
