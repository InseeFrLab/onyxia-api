package fr.insee.onyxia.model.task;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceFile {

    private String gid, mode, path, uid;
    private int nlink,size;
    private float mtime;

    private Pattern pathRegex = Pattern.compile(".*/runs/[a-z0-9\\-]*/(.*)");

    @JsonProperty
    public String getRelativePath() {
        Matcher matcher = pathRegex.matcher(path);
        matcher.find();
        String match = matcher.group(1);
        if (!match.startsWith("/")) {
            match = "/"+match;
        }
        return match;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getNlink() {
        return nlink;
    }

    public void setNlink(int nlink) {
        this.nlink = nlink;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public float getMtime() {
        return mtime;
    }

    public void setMtime(float mtime) {
        this.mtime = mtime;
    }
}
