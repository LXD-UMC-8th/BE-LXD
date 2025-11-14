package org.lxdproject.lxd.global.common.entity.enums;

public enum ImageDir {
    PROFILE("profile"),
    DIARY("diary");

    private final String dirName;

    ImageDir(String dirName) {
        this.dirName = dirName;
    }

    public String getDirName() {
        return dirName;
    }
}

