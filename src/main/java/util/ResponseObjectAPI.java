package util;

import org.json.JSONArray;
import org.json.JSONObject;

public class ResponseObjectAPI {

    private JSONObject object;
    private JSONArray objects;
    private String rowData;
    private byte [] bytes;

    public JSONObject getObject() {
        return object;
    }

    public void setObject(JSONObject object) {
        this.object = object;
    }

    public JSONArray getObjects() {
        return objects;
    }

    public void setObjects(JSONArray objects) {
        this.objects = objects;
    }

    public String getRowData() {
        return rowData;
    }

    public void setRowData(String rowData) {
        this.rowData = rowData;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
