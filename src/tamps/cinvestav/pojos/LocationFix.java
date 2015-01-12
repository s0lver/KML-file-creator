package tamps.cinvestav.pojos;

import java.util.Date;

public class LocationFix {
    private float latitud;
    private float longitud;
    private float precision;
    private float altitud;
    private Date timestamp;

    public LocationFix(float latitud, float longitud, float precision, float altitud, Date timestamp) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.precision = precision;
        this.altitud = altitud;
        this.timestamp = timestamp;
    }

    public float getLatitud() {
        return latitud;
    }

    public void setLatitud(float latitud) {
        this.latitud = latitud;
    }

    public float getLongitud() {
        return longitud;
    }

    public void setLongitud(float longitud) {
        this.longitud = longitud;
    }

    public float getPrecision() {
        return precision;
    }

    public void setPrecision(float precision) {
        this.precision = precision;
    }

    public float getAltitud() {
        return altitud;
    }

    public void setAltitud(float altitud) {
        this.altitud = altitud;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
